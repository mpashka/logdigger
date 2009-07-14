package com.iv.logView.ui;

import com.iv.logView.Main;
import com.iv.logView.Prefs;
import com.iv.logView.io.FilteredLogReader;
import com.iv.logView.io.RowId;
import com.iv.logView.logging.Log;
import com.iv.logView.logging.LogFactory;
import com.iv.logView.model.FilterModel;
import com.iv.logView.model.FindModel;
import com.iv.logView.model.LogColumnModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class MainPanel extends JFrame {

    private static final Log log = LogFactory.getLogger(MainPanel.class);
    private static final Highlighter.HighlightPainter HL_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
    private static final long serialVersionUID = -9071462322047687520L;

    private FilteredLogReader logReader;
    private final List<RowId> bookmarks = new ArrayList<RowId>();
    private JTextArea text;
    private FindContext findContext;
    private JSplitPane panel;
    private JLogTable table;
    private FilterModel filterModel;
    private ResetBookmarksAction resetBookmarks;
    private NextBookmarkAction nextBookmark;
    private PreviousBookmarkAction prevBookmark;
    private JStatusBar statusBar;
    private final SyncRunner syncRunner;
    private JMenu fileHistoryMenu;

    public MainPanel(FilteredLogReader logReader) {
        this.logReader = logReader;
        initFrame();
        initMenu();
        initPanel();
        addRecentFile(logReader.getFile().getAbsolutePath());
        syncRunner = new SyncRunner();
        new Thread(syncRunner).start();
    }

    private void syncForTime(String time) {
        LogColumnModel timeCol = ((LogColumnModel) table.getColumn("Time"));
        if (timeCol != null) {
            int row = logReader.findNearestRow(timeCol, time);
            table.scrollToRow(row);
        }
    }

    private void initMenu() {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        fileMenu.add(new JMenuItem(new FileOpenAction()));
        fileMenu.add(new JMenuItem(new FileReloadAction()));

        fileHistoryMenu = new JMenu("Open Recent File");
        fileHistoryMenu.setMnemonic('F');
        fileMenu.add(fileHistoryMenu);
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(new ExitAction()));

        final JMenu searchMenu = new JMenu("Search");
        searchMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(searchMenu);

        searchMenu.add(new JMenuItem(new FindAction()));
        searchMenu.add(new JMenuItem(new FindNextAction()));
        searchMenu.add(new JMenuItem(new FindPreviousAction()));
        searchMenu.addSeparator();
        searchMenu.add(new JMenuItem(new SetFilterAction()));
        searchMenu.addSeparator();
        searchMenu.add(new JMenuItem(new ToggleBookmarkAction()));

        resetBookmarks = new ResetBookmarksAction();
        resetBookmarks.setEnabled(false);
        searchMenu.add(new JMenuItem(resetBookmarks));

        nextBookmark = new NextBookmarkAction();
        searchMenu.add(new JMenuItem(nextBookmark));

        prevBookmark = new PreviousBookmarkAction();
        searchMenu.add(new JMenuItem(prevBookmark));

        searchMenu.addSeparator();

        searchMenu.add(new JMenuItem(new SyncAction()));

        final JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        helpMenu.add(new JMenuItem(new AboutAction()));

        setJMenuBar(menuBar);
    }

    private void initFrame() {
        setIconImage(ApplicationIcon.getImage());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                Prefs.getInstance().setWindowState(getExtendedState());
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (isVisible() && getExtendedState() != MAXIMIZED_BOTH) {
                    Prefs.getInstance().setWindowSize(getSize());
                }
            }

            public void componentMoved(ComponentEvent e) {
                if (isVisible() && getExtendedState() != MAXIMIZED_BOTH) {
                    Prefs.getInstance().setWindowLocation(getLocationOnScreen());
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                table.requestFocus();
            }
        });

        setExtendedState(Prefs.getInstance().getWindowState());
        Dimension frameSize = Prefs.getInstance().getWindowSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        Dimension screenSize = getToolkit().getScreenSize();
        Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
        int w = screenSize.width - screenInsets.left - screenInsets.right;
        int h = screenSize.height - screenInsets.top - screenInsets.bottom;
        if (frameSize.height > h) {
            frameSize.height = h;
        }
        if (frameSize.width > w) {
            frameSize.width = w;
        }
        setSize(frameSize);

        Point location = Prefs.getInstance().getWindowLocation();
        if (location.x == Integer.MIN_VALUE || location.y == Integer.MIN_VALUE) {
            Point center = ge.getCenterPoint();
            Dimension winSize = getSize();
            location.x = center.x - (winSize.width / 2);
            location.y = center.y - (winSize.height / 2);
        }
        setLocation(location);

        statusBar = new JStatusBar(logReader.getFile());
        add(statusBar, BorderLayout.SOUTH);
    }

    private void initPanel() {
        if (panel == null) {
            panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            panel.setResizeWeight(0.5);
            add(panel, BorderLayout.CENTER);
        }
        setTitle("LogViewer - " + logReader.getFile().getAbsolutePath());
        statusBar.setFile(logReader.getFile());

        table = new JLogTable(logReader, bookmarks);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                String str = null;
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();
                    statusBar.setPosition(selectedRow + 1, logReader.getRowCount());
                    try {
                        int gr = logReader.getTableColumnModel().getMessageColumn().getGroup();
                        str = logReader.get(selectedRow, gr);
                    } catch (IOException e1) {
                        // ignore
                        log.debug(e1);
                    }
                }
                text.setText(str);
                text.getHighlighter().removeAllHighlights();
                text.setCaretPosition(0);
            }
        });
        table.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                for (int i = 0; i < table.getColumnCount() - 1; i++) {
                    ColumnFitAdapter.fit(table, i);
                }
                table.scrollToRow(0);
                table.removeComponentListener(this);
            }
        });

        text = new JTextArea();
        text.setFont(new Font("Monospaced", Font.PLAIN, text.getFont().getSize()));
        text.setEditable(false);

        panel.setTopComponent(new JScrollPane(table));
        panel.setBottomComponent(new JScrollPane(text));
    }

    private void highlightText() {
        if (findContext != null) {
            final Highlighter hl = text.getHighlighter();
            hl.removeAllHighlights();
            for (FindResult fr : findContext.getFindResults()) {
                try {
                    hl.addHighlight(fr.getPos(), fr.getPos() + fr.getLength(), HL_PAINTER);
                } catch (BadLocationException e) {
                    log.warning("", e);
                }
            }
        }
    }

    private void addRecentFile(String fileName) {
        List<String> list = Prefs.getInstance().addFileHistory(fileName);
        if (list.size() > 1) {
            fileHistoryMenu.removeAll();
            for (int i = 1; i < list.size(); i++) {
                fileHistoryMenu.add(new FileReopenAction(list.get(i)));
            }
            fileHistoryMenu.setEnabled(true);
        } else {
            fileHistoryMenu.setEnabled(false);
        }
    }

    private void removeRecentFile(String fileName) {
        List<String> list = Prefs.getInstance().removeFileHistory(fileName);
        if (list.size() > 1) {
            fileHistoryMenu.removeAll();
            for (int i = 1; i < list.size(); i++) {
                fileHistoryMenu.add(new FileReopenAction(list.get(i)));
            }
            fileHistoryMenu.setEnabled(true);
        } else {
            fileHistoryMenu.setEnabled(false);
        }
    }

    private abstract class BaseAction extends AbstractAction {
        private static final long serialVersionUID = 7808489076570903803L;

        protected BaseAction(String name) {
            putValue(NAME, name);
        }

        protected BaseAction(String name, int mnemonic, KeyStroke accelerator) {
            putValue(NAME, name);
            if (mnemonic != 0) putValue(MNEMONIC_KEY, mnemonic);
            if (accelerator != null) putValue(ACCELERATOR_KEY, accelerator);
        }
    }

    private class AboutAction extends BaseAction {
        private static final long serialVersionUID = 1153101086949870538L;

        public AboutAction() {
            super("About", KeyEvent.VK_A, null);
        }

        public void actionPerformed(ActionEvent e) {
            SplashScreen splashScreen = new SplashScreen(MainPanel.this);
            splashScreen.setVisible(true);
        }
    }

    private class ExitAction extends BaseAction {
        private static final long serialVersionUID = -1093284110665167816L;

        public ExitAction() {
            super("Exit", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    private class FindAction extends BaseAction {
        private static final long serialVersionUID = -1955439185530709222L;

        public FindAction() {
            super("Find...", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent event) {
            final FindModel model = findContext == null ? new FindModel(logReader.getTableColumnModel()) : findContext.getModel();
            FindDialog<FindModel> dlg = new FindDialog<FindModel>(MainPanel.this, model);
            if (dlg.execute()) {
                findContext = new FindContext(model);
                final LogColumnModel lcm = (LogColumnModel) table.getColumn(model.getColumns().getSelectedItem());
                final int startRow = model.isFromCursor() && table.getSelectedRow() >= 0 ? table.getSelectedRow() : 0;
                new FindWorker(lcm) {
                    protected boolean doRun() {
                        for (int i = startRow; i < logReader.getRowCount(); i++) {
                            if (accept(i)) {
                                return true;
                            }
                        }
                        return false;
                    }
                }.start();
            }
        }

    }

    private class FindNextAction extends BaseAction {
        private static final long serialVersionUID = 282209051694714602L;

        public FindNextAction() {
            super("Find next", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        }

        public void actionPerformed(ActionEvent event) {
            final int startRow;
            if (findContext == null) {
                FindModel model = new FindModel(logReader.getTableColumnModel());
                FindDialog<FindModel> dlg = new FindDialog<FindModel>(MainPanel.this, model);
                if (!dlg.execute()) return;
                startRow = 0;
                findContext = new FindContext(model);
            } else {
                startRow = table.getSelectedRow() + 1;
            }
            final LogColumnModel lcm = (LogColumnModel) table.getColumn(findContext.getModel().getColumns().getSelectedItem());
            new FindWorker(lcm) {
                protected boolean doRun() {
                    for (int i = startRow; i < logReader.getRowCount(); i++) {
                        if (accept(i)) {
                            return true;
                        }
                    }
                    return false;
                }
            }.start();
        }
    }

    private class FindPreviousAction extends BaseAction {
        private static final long serialVersionUID = 138867181219627494L;

        public FindPreviousAction() {
            super("Find previous", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK));
        }

        public void actionPerformed(ActionEvent event) {
            final int startRow;
            if (findContext == null) {
                FindModel model = new FindModel(logReader.getTableColumnModel());
                FindDialog<FindModel> dlg = new FindDialog<FindModel>(MainPanel.this, model);
                if (!dlg.execute()) return;
                startRow = logReader.getRowCount() - 1;
                findContext = new FindContext(model);
            } else {
                startRow = table.getSelectedRow() - 1;
            }
            LogColumnModel lcm = (LogColumnModel) table.getColumn(findContext.getModel().getColumns().getSelectedItem());
            new FindWorker(lcm) {
                protected boolean doRun() {
                    for (int i = startRow; i >= 0; i--) {
                        if (accept(i)) {
                            return true;
                        }
                    }
                    return false;
                }
            }.start();
        }
    }

    private abstract class FindWorker extends Worker {

        private int foundRow;
        protected final LogColumnModel columnModel;

        public FindWorker(LogColumnModel lcm) {
            this.columnModel = lcm;
        }

        protected boolean accept(int row) {
            try {
                String txt = logReader.get(row, columnModel.getGroup());
                if (findContext.accept(txt)) {
                    foundRow = row;
                    return true;
                }
            } catch (IOException e) {
                log.debug(e);
            }
            return false;
        }

        protected void doFinish() {
            if (getValue()) {
                table.scrollToRow(foundRow);
                if (columnModel == logReader.getTableColumnModel().getMessageColumn()) {
                    highlightText();
                }
            } else {
                new MessageDialog(MainPanel.this, "Find", "\"" + findContext.getModel().getText() + "\" not found").execute();
            }
        }
    }

    private class SetFilterAction extends BaseAction {
        private static final long serialVersionUID = -4707623048237118270L;

        public SetFilterAction() {
            super("Set filter", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        }

        public void actionPerformed(ActionEvent e) {
            if (filterModel == null) {
                filterModel = new FilterModel(logReader.getIdx());
            }
            FilterDialog<FilterModel> dlg = new FilterDialog<FilterModel>(MainPanel.this, filterModel);
            if (dlg.execute()) {
                int oldRowNum = table.getSelectedRow();
                final RowId rowId = oldRowNum < 0 ? null : logReader.getId(oldRowNum);
                new Worker() {
                    int newRowNum;

                    protected boolean doRun() {
                        logReader.applyFilter(filterModel);
                        newRowNum = rowId == null ? 0 : logReader.findNearestRow(rowId);
                        return true;
                    }

                    protected void doFinish() {
                        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
                        table.scrollToRow(newRowNum);
                    }
                }.start();
            }
        }
    }

    private class FileOpenAction extends BaseAction {

        public FileOpenAction() {
            super("Open...", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent event) {
            final String fileName = Main.chooseFileName(MainPanel.this);
            if (fileName != null) {
                new FileWorker(fileName).start();
            }
        }

    }

    private class FileReopenAction extends BaseAction {

        private final int MAX_LENGTH = 50;
        private final String fileName;

        public FileReopenAction(String fileName) {
            super(null);
            putValue(NAME, shortName(fileName));
            putValue(SHORT_DESCRIPTION, fileName);
            this.fileName = fileName;
        }

        public void actionPerformed(ActionEvent event) {
            new FileWorker(fileName).start();
        }

        private String shortName(String name) {
            if (name.length() > MAX_LENGTH) {
                int idx = name.lastIndexOf(File.separatorChar);
                return name.substring(0, MAX_LENGTH - (name.length() - idx)) + "..." + name.substring(idx);
            }
            return name;
        }
    }

    private class FileWorker extends Worker {
        private final String fileName;

        public FileWorker(String fileName) {
            this.fileName = fileName;
        }

        public boolean doRun() {
            try {
                final FilteredLogReader old = logReader;
                logReader = new FilteredLogReader(new File(fileName), null);
                old.close();
                return true;
            } catch (IOException e) {
                new MessageDialog(MainPanel.this, "Error", e.getMessage()).execute();
                return false;
            }
        }

        protected void doFinish() {
            if (getValue()) {
                addRecentFile(fileName);
                resetBookmarks.actionPerformed(null);
                findContext = null;
                filterModel = null;
                initPanel();
            } else {
                removeRecentFile(fileName);
            }
        }
    }

    private class FileReloadAction extends BaseAction {
        private static final long serialVersionUID = 3247194136219471046L;

        public FileReloadAction() {
            super("Reload", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            new Worker() {
                private int row;

                protected boolean doRun() {
                    row = table.getSelectedRow();
                    try {
                        logReader.reload();
                    } catch (IOException ex) {
                        log.error(ex);
                    }
                    if (filterModel != null) {
                        filterModel.update(logReader.getIdx());
                    }
                    logReader.applyFilter(filterModel);
                    return true;
                }

                protected void doFinish() {
                    ((AbstractTableModel) table.getModel()).fireTableDataChanged();
                    table.scrollToRow(row);
                    statusBar.updateFileLength();
                }
            }.start();
        }
    }

    private class ToggleBookmarkAction extends BaseAction {
        private static final long serialVersionUID = -4750224291057056959L;

        public ToggleBookmarkAction() {
            super("Toggle Bookmark", KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            int rowNum = table.getSelectedRow();
            if (rowNum >= 0) {
                RowId rowId = logReader.getId(rowNum);
                if (!bookmarks.remove(rowId)) {
                    bookmarks.add(rowId);
                }
            }

            final boolean hasBookmarks = bookmarks.size() > 0;
            nextBookmark.setEnabled(hasBookmarks);
            prevBookmark.setEnabled(hasBookmarks);
            resetBookmarks.setEnabled(hasBookmarks);
        }
    }

    private abstract class GotoBookmarkAction extends BaseAction {
        private static final long serialVersionUID = -3931436965953560933L;

        protected GotoBookmarkAction(String name, int mnemonic, KeyStroke accelerator) {
            super(name, mnemonic, accelerator);
            setEnabled(false);
        }

        public abstract void actionPerformed(ActionEvent e);

        protected void doAction(final int direction) {
            int rowNum = table.getSelectedRow();
            if (bookmarks.size() > 0 && rowNum >= 0) {
                Collections.sort(bookmarks, new Comparator<RowId>() {
                    public int compare(RowId o1, RowId o2) {
                        return o1.compareTo(o2) * direction;
                    }
                });
                final RowId curRow = logReader.getId(rowNum);
                for (RowId rowId : bookmarks) {
                    if ((rowId.compareTo(curRow) * direction) > 0) {
                        int idx = logReader.findNearestRow(rowId);
                        table.scrollToRow(idx);
                        return;
                    }
                }
            }
        }
    }

    private class NextBookmarkAction extends GotoBookmarkAction {
        private static final long serialVersionUID = -8529937277725248238L;

        public NextBookmarkAction() {
            super("Next Bookmark", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            doAction(+1);
        }
    }

    private class PreviousBookmarkAction extends GotoBookmarkAction {
        private static final long serialVersionUID = 37004974802703769L;

        public PreviousBookmarkAction() {
            super("Previous Bookmark", KeyEvent.VK_V, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            doAction(-1);
        }
    }

    private class ResetBookmarksAction extends BaseAction {
        private static final long serialVersionUID = 5041673190688046365L;

        public ResetBookmarksAction() {
            super("Reset all bookmarks", 'a', KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            bookmarks.clear();
            table.repaint();
            nextBookmark.setEnabled(false);
            prevBookmark.setEnabled(false);
            setEnabled(false);
        }
    }

    private class SyncAction extends BaseAction {
        private static final long serialVersionUID = 8795943210191680542L;

        public SyncAction() {
            super("Sync by time", KeyEvent.VK_Y, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            int colIdx = logReader.getTableColumnModel().getColumnIndex("Time");
            int rowIdx = table.getSelectedRow();
            if (colIdx != -1 && rowIdx != -1) {
                String time = table.getValueAt(rowIdx, colIdx).toString();
                Prefs prefs = Prefs.getInstance();
                synchronized (syncRunner) {
                    long t = System.currentTimeMillis();
                    syncRunner.setTimestamp(t);
                    prefs.setSyncTime(t);
                    prefs.setSyncValue(time);
                }
            }
        }
    }

    private class SyncRunner implements Runnable {
        private long timestamp;

        public void run() {
            Prefs prefs = Prefs.getInstance();
            timestamp = prefs.getSyncTime();
            for (; ;) {
                synchronized (this) {
                    if (timestamp < prefs.getSyncTime()) {
                        String str = prefs.getSyncValue();
                        log.info("sync for time: " + str);
                        syncForTime(str);
                        timestamp = prefs.getSyncTime();
                    }
                }
                try {
                    Thread.sleep(333);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void setTimestamp(long time) {
            timestamp = time;
        }
    }

    private abstract class Worker extends SwingWorker<Boolean> {

        private void setEnabled(boolean enabled) {
            JMenuBar menu = getJMenuBar();
            for (int i = 0; i < menu.getMenuCount(); i++) {
                menu.getMenu(i).setEnabled(enabled);
            }
            panel.setEnabled(enabled);
            table.setEnabled(enabled);
            text.setEnabled(enabled);
        }

        public final Boolean construct() {
            getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setEnabled(false);
            return doRun();
        }

        protected abstract boolean doRun();

        protected abstract void doFinish();

        public final void finished() {
            doFinish();
            setEnabled(true);
            getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

    }
}
