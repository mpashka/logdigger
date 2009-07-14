package com.iv.logView.ui;

import com.iv.logView.io.FilteredLogReader;
import com.iv.logView.io.RowId;
import com.iv.logView.logging.Log;
import com.iv.logView.logging.LogFactory;
import com.iv.logView.model.LogColumnModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class JLogTable extends JTable {

    private static final Log LOG = LogFactory.getLogger(MainPanel.class);
    private static final long serialVersionUID = 7032230433676934146L;
    private final CellRenderer defaultCellRenderer = new CellRenderer();
    private final FilteredLogReader logReader;
    private final List<RowId> bookmarks;

    public JLogTable(FilteredLogReader logReader, List<RowId> bookmarks) {
        this.logReader = logReader;
        this.bookmarks = bookmarks;
        setModel(new LogTableModel());
        setColumnModel(logReader.getTableColumnModel());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        setTransferHandler(new TableTransferHandler());
        getTableHeader().addMouseListener(new ColumnFitAdapter());
    }

    public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
        return defaultCellRenderer;
    }

    public void scrollToRow(int rowIndex) {
        if (rowIndex < 0) return;
        requestFocus();
        getParent().validate();
        if (getRowCount() > 0) {
            rowIndex = Math.min(rowIndex, getRowCount() - 1);
            setRowSelectionInterval(rowIndex, rowIndex);
            Rectangle rect = getCellRect(rowIndex, -1, true);
            Insets i = getInsets();
            rect.x = i.left;
            rect.width = getWidth() - i.left - i.right;
            Scrolling.centerVertically(this, rect, true);
        }
    }

    private class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 8238286301366866280L;

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

    private class LogTableModel extends AbstractTableModel {
        private static final long serialVersionUID = -887734711302854039L;

        public int getRowCount() {
            try {
                return logReader.getRowCount();
            } catch (Exception e) {
                LOG.debug(e);
                return 0;
            }
        }

        public int getColumnCount() {
            return getColumnModel().getColumnCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                columnIndex = convertColumnIndexToModel(columnIndex);
                int gr = ((LogColumnModel) getColumnModel().getColumn(columnIndex)).getGroup();
                String str = logReader.get(rowIndex, gr);
                return str.length() > 200 ? str.substring(0, 199) : str;
            } catch (Exception e) {
//                LOG.error(e);
                return "???";
            }
        }
    }

    private class TableTransferHandler extends TransferHandler {
        private static final long serialVersionUID = -294249192259616760L;

        protected Transferable createTransferable(JComponent c) {
            if (c == JLogTable.this) {
                if (getSelectedRow() >= 0) {
                    try {
                        String str = logReader.get(getSelectedRow());
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

}