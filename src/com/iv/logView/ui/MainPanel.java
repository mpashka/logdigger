package com.iv.logView.ui;

import com.iv.logView.io.FilteredLogReader;
import com.iv.logView.io.RowId;
import com.iv.logView.model.FilterModel;
import com.iv.logView.model.FindModel;
import com.iv.logView.model.LogColumnModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.prefs.Preferences;

public class MainPanel extends JFrame {

    private static final String SYNC_VALUE_KEY = "syncValue";
    private static final String SYNC_TIME_KEY = "syncTime";
    private static final String P_STATE = "state";
    private static final String P_WIDTH = "width";
    private static final String P_HEIGHT = "height";
    private static final String P_X = "x";
    private static final String P_Y = "y";

    private final FilteredLogReader logReader;
    private final java.util.List<RowId> bookmarks = new ArrayList<RowId>();
    private JTextPane text;
    private FindContext findContext = null;
    private JTable table;
    private FilterModel filterModel;
    private Style selected;
    private NextBookmarkAction nextBookmark;
    private PreviousBookmarkAction prevBookmark;
    private JStatusBar status;
    private final SyncRunner syncRunner;

    public MainPanel(FilteredLogReader logReader) {
        this.logReader = logReader;
        init();

        syncRunner = new SyncRunner();
        new Thread(syncRunner).start();

    }

    private void syncForTime(String time) {
        LogColumnModel timeCol = ((LogColumnModel) table.getColumn("Time"));
        if (timeCol != null) {
            int row = logReader.findNearestRow(timeCol, time);
            scrollToRow(row);
        }
    }

    private void initMenu() {
        final JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        menuItem = new JMenuItem(new FileReloadAction());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem(new ExitAction());
        menu.add(menuItem);

        menu = new JMenu("Search");
        menu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(menu);

        menuItem = new JMenuItem(new FindAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new FindNextAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new FindPreviousAction());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem(new SetFilterAction());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem(new ToggleBookmarkAction());
        menu.add(menuItem);

        nextBookmark = new NextBookmarkAction();
        menuItem = new JMenuItem(nextBookmark);
        menu.add(menuItem);

        prevBookmark = new PreviousBookmarkAction();
        menuItem = new JMenuItem(prevBookmark);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem(new SyncAction());
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }

    private void initFrame() {
        final Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                prefs.putInt(P_STATE, getExtendedState());
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (isVisible() && getExtendedState() != MAXIMIZED_BOTH) {
                    prefs.putInt(P_WIDTH, getWidth());
                    prefs.putInt(P_HEIGHT, getHeight());
                }
            }

            public void componentMoved(ComponentEvent e) {
                if (isVisible() && getExtendedState() != MAXIMIZED_BOTH) {
                    prefs.putInt(P_X, getLocationOnScreen().x);
                    prefs.putInt(P_Y, getLocationOnScreen().y);
                }
            }

        });

        setExtendedState(prefs.getInt(P_STATE, 0));
        int w = prefs.getInt(P_WIDTH, 1200);
        int h = prefs.getInt(P_HEIGHT, 800);

        Dimension frameSize = new Dimension(w, h);
        Dimension screenSize = getToolkit().getScreenSize();
        Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
        w = screenSize.width - screenInsets.left - screenInsets.right;
        h = screenSize.height - screenInsets.top - screenInsets.bottom;
        if (frameSize.height > h) {
            frameSize.height = h;
        }
        if (frameSize.width > w) {
            frameSize.width = w;
        }
        setSize(frameSize);

        int x = prefs.getInt(P_X, Integer.MIN_VALUE);
        int y = prefs.getInt(P_Y, Integer.MIN_VALUE);
        if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
            x = (w - frameSize.width) / 2 + screenInsets.left;
            y = (h - frameSize.height) / 2 + screenInsets.top;
        }
        setLocation(x, y);
    }

    private void init() {
        initFrame();
        initMenu();

        status = new JStatusBar();
        getContentPane().add(status, BorderLayout.SOUTH);

        table = new JLogTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JScrollPane tableScroll = new JScrollPane(table);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                String str = null;
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();
                    status.setPosition(selectedRow + 1, logReader.getRowCount());
                    try {
                        int gr = logReader.getTableColumnModel().getMessageColumn().getGroup();
                        str = logReader.get(selectedRow, gr);
                    } catch (IOException e1) {
                        // ignore
                        e1.printStackTrace();
                    }
                }
                text.setText(str);
                text.setCaretPosition(0);
            }
        });
        table.setTransferHandler(new TableTransferHandler());
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                for (int i = 0; i < table.getColumnCount() - 1; i++) {
                    ColumnFitAdapter.fit(table, i);
                }
                scrollToRow(0);
                removeComponentListener(this);
            }
        });

        text = new JTextPane();
        text.setEditorKit(new NoWrapEditorKit());
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));
        text.setEditable(false);
        Style def = text.getLogicalStyle();
        StyleConstants.setFontFamily(def, "Monospaced");
        StyleConstants.setFontSize(def, 12);

        selected = text.addStyle("special", def);
        StyleConstants.setBackground(selected, Color.GREEN);

        JScrollPane textScroll = new JScrollPane(text);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, textScroll);
        split.setResizeWeight(0.5);

        getContentPane().add(split, BorderLayout.CENTER);
    }

    private class LogTableModel extends AbstractTableModel {

        public int getRowCount() {
            try {
                return logReader.getRowCount();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        public int getColumnCount() {
            return table.getColumnModel().getColumnCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                columnIndex = table.convertColumnIndexToModel(columnIndex);
                int gr = ((LogColumnModel) table.getColumnModel().getColumn(columnIndex)).getGroup();
                return logReader.get(rowIndex, gr);
            } catch (Exception e) {
                e.printStackTrace();
                return "???";
            }
        }
    }

    private void scrollToRow(int rowIndex) {
        if (rowIndex < 0) return;
        table.requestFocus();
        table.getParent().validate();
        if (table.getRowCount() > 0) {
            rowIndex = Math.min(rowIndex, table.getRowCount() - 1);
            table.setRowSelectionInterval(rowIndex, rowIndex);
            Rectangle rect = table.getCellRect(rowIndex, -1, true);
            Insets i = table.getInsets();
            rect.x = i.left;
            rect.width = table.getWidth() - i.left - i.right;
            Scrolling.centerVertically(table, rect, true);
        }
    }

    private void highlightText(FindContext ctx) {
        for (FindResult fr : ctx.getFindResults()) {
            text.getStyledDocument().setCharacterAttributes(fr.getPos(), fr.getLength(), selected, true);
        }
        ctx.getFindResults().clear();
    }

    private abstract class BaseAction extends AbstractAction {
        protected BaseAction(String name, int mnemonic, KeyStroke accelerator) {
            putValue(NAME, name);
            putValue(MNEMONIC_KEY, mnemonic);
            putValue(ACCELERATOR_KEY, accelerator);
        }
    }

    private class ExitAction extends BaseAction {
        public ExitAction() {
            super("Exit", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    private class FindAction extends BaseAction {
        public FindAction() {
            super("Find...", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            FindModel model = findContext == null ? new FindModel(logReader.getTableColumnModel()) : findContext.getModel();
            FindDialog<FindModel> dlg = new FindDialog<FindModel>(MainPanel.this, model);
            if (dlg.execute()) {
                findContext = new FindContext(model);
                LogColumnModel lcm = (LogColumnModel) table.getColumn(model.getColumns().getSelectedItem());
                final int rowIdx = model.isFromCursor() && table.getSelectedRow() >= 0 ? table.getSelectedRow() : 0;
                for (int i = rowIdx; i < logReader.getRowCount(); i++) {
                    try {
                        String txt = logReader.get(i, lcm.getGroup());
                        if (findContext.accept(txt)) {
                            scrollToRow(i);
                            if (lcm == logReader.getTableColumnModel().getMessageColumn()) {
                                highlightText(findContext);
                            }
                            return;
                        }
                    } catch (IOException e1) {
                        // skip row
                        e1.printStackTrace();
                    }
                }
                new MessageDialog(MainPanel.this, "Find", "\"" + model.getText() + "\" not found").execute();
            }
        }

    }

    private class FindNextAction extends BaseAction {
        public FindNextAction() {
            super("Find next", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        }

        public void actionPerformed(ActionEvent e) {
            int curRow;
            if (findContext == null) {
                FindModel model = new FindModel(logReader.getTableColumnModel());
                FindDialog<FindModel> dlg = new FindDialog<FindModel>(MainPanel.this, model);
                if (!dlg.execute()) return;
                curRow = 0;
                findContext = new FindContext(model);
            } else {
                curRow = table.getSelectedRow() + 1;
                findContext.getFindResults().clear();
            }
            LogColumnModel lcm = (LogColumnModel) table.getColumn(findContext.getModel().getColumns().getSelectedItem());
            for (int i = curRow; i < logReader.getRowCount(); i++) {
                try {
                    String txt = logReader.get(i, lcm.getGroup());
                    if (findContext.accept(txt)) {
                        scrollToRow(i);
                        if (lcm == logReader.getTableColumnModel().getMessageColumn()) {
                            highlightText(findContext);
                        }
                        return;
                    }
                } catch (IOException e1) {
                    // skip row
                    e1.printStackTrace();
                }
            }
            new MessageDialog(MainPanel.this, "Find", "\"" + findContext.getModel().getText() + "\" not found").execute();
        }
    }

    private class FindPreviousAction extends BaseAction {
        public FindPreviousAction() {
            super("Find previous", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            int curRow;
            if (findContext == null) {
                FindModel model = new FindModel(logReader.getTableColumnModel());
                FindDialog<FindModel> dlg = new FindDialog<FindModel>(MainPanel.this, model);
                if (!dlg.execute()) return;
                curRow = logReader.getRowCount() - 1;
                findContext = new FindContext(model);
            } else {
                curRow = table.getSelectedRow() - 1;
                findContext.getFindResults().clear();
            }
            LogColumnModel lcm = (LogColumnModel) table.getColumn(findContext.getModel().getColumns().getSelectedItem());
            for (int i = curRow; i >= 0; i--) {
                try {
                    String txt = logReader.get(i, lcm.getGroup());
                    if (findContext.accept(txt)) {
                        scrollToRow(i);
                        if (lcm == logReader.getTableColumnModel().getMessageColumn()) {
                            highlightText(findContext);
                        }
                        return;
                    }
                } catch (IOException e1) {
                    // skip row
                    e1.printStackTrace();
                }
            }
            new MessageDialog(MainPanel.this, "Find", "\"" + findContext.getModel().getText() + "\" not found").execute();
        }
    }

    private class SetFilterAction extends BaseAction {

        public SetFilterAction() {
            super("Set filter", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        }

        public void actionPerformed(ActionEvent e) {
            getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                if (filterModel == null) {
                    filterModel = new FilterModel(logReader.getIdx());
                }
                FilterDialog<FilterModel> dlg = new FilterDialog<FilterModel>(MainPanel.this, filterModel);
                if (dlg.execute()) {
                    int newRowNum;
                    if (logReader.getRowCount() > 0) {
                        final int oldRowNum = table.getSelectedRow() >= 0 ? table.getSelectedRow() : 0;
                        final RowId rowId = logReader.getId(oldRowNum);
                        logReader.applyFilter(filterModel);
                        newRowNum = logReader.findNearestRow(rowId);
                    } else {
                        logReader.applyFilter(filterModel);
                        newRowNum = 0;
                    }
                    ((AbstractTableModel) table.getModel()).fireTableDataChanged();
                    scrollToRow(newRowNum);
                }
            } finally {
                getRootPane().setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private class FileReloadAction extends BaseAction {
        public FileReloadAction() {
            super("Reload", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                int row = table.getSelectedRow();
                try {
                    logReader.reload();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                logReader.applyFilter(filterModel);
                ((AbstractTableModel) table.getModel()).fireTableDataChanged();
                scrollToRow(row);
            } finally {
                getRootPane().setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private class ToggleBookmarkAction extends BaseAction {
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
            nextBookmark.setEnabled(bookmarks.size() > 0);
            prevBookmark.setEnabled(bookmarks.size() > 0);
        }
    }

    private abstract class GotoBookmarkAction extends BaseAction {

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
                        scrollToRow(idx);
                        return;
                    }
                }
            }
        }
    }

    private class NextBookmarkAction extends GotoBookmarkAction {
        public NextBookmarkAction() {
            super("Next Bookmark", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            doAction(+1);
        }
    }

    private class PreviousBookmarkAction extends GotoBookmarkAction {
        public PreviousBookmarkAction() {
            super("Previous Bookmark", KeyEvent.VK_V, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            doAction(-1);
        }
    }

    private class SyncAction extends BaseAction {

        public SyncAction() {
            super("Sync by time", KeyEvent.VK_Y, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            int colIdx = logReader.getTableColumnModel().getColumnIndex("Time");
            int rowIdx = table.getSelectedRow();
            if (colIdx != -1 && rowIdx != -1) {
                String time = table.getValueAt(rowIdx, colIdx).toString();
                Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
                synchronized (syncRunner) {
                    long t = System.currentTimeMillis();
                    syncRunner.updateTimestamp(t);
                    prefs.putLong(SYNC_TIME_KEY, t);
                    prefs.put(SYNC_VALUE_KEY, time);
                }
            }
        }
    }

    private class JLogTable extends JTable {

        private final CellRenderer defaultCellRenderer = new CellRenderer();

        public JLogTable() {
            super(new LogTableModel(), logReader.getTableColumnModel());
            getTableHeader().addMouseListener(new ColumnFitAdapter());
        }

        public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
            return defaultCellRenderer;
        }
    }

    private class CellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            LogColumnModel colModel = (LogColumnModel) logReader.getTableColumnModel().getColumn(column);
            Color color = colModel.getColor(value.toString());
            if (color != null) {
                c.setForeground(color);
            } else {
                c.setForeground(table.getForeground());
            }

            RowId rowId = logReader.getId(row);
            if (bookmarks.contains(rowId) && !isSelected && !hasFocus) {
                c.setBackground(Color.CYAN);
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
            } else {
                c.setBackground(table.getBackground());
            }
            return c;
        }
    }

    private class TableTransferHandler extends TransferHandler {
        protected Transferable createTransferable(JComponent c) {
            if (c == table) {
                if (table.getSelectedRow() > 0) {
                    try {
                        String str = logReader.get(table.getSelectedRow());
                        return new StringSelection(str);
                    } catch (IOException e1) {
                        // do nothong
                    }
                }

            }
            return null;
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }

    private class SyncRunner implements Runnable {
        private long timestamp;

        public void run() {
            Preferences prefs = Preferences.userNodeForPackage(MainPanel.class);
            timestamp = prefs.getLong(SYNC_TIME_KEY, 0);
            for (; ;) {
                synchronized (this) {
                    if (timestamp < prefs.getLong(SYNC_TIME_KEY, 0)) {
                        String str = prefs.get(SYNC_VALUE_KEY, "");
                        System.out.println("sync for time: " + str);
                        syncForTime(str);
                        timestamp = prefs.getLong(SYNC_TIME_KEY, 0);
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void updateTimestamp(long time) {
            timestamp = time;
        }
    }
}
