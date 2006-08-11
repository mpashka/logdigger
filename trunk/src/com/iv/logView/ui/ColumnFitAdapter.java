package com.iv.logView.ui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class ColumnFitAdapter extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            final JTableHeader header = (JTableHeader) e.getSource();
            final JTable table = header.getTable();
            final TableColumn tableColumn = getResizingColumn(header, e.getPoint());
            if (tableColumn == null)
                return;
            int col = header.getColumnModel().getColumnIndex(tableColumn.getIdentifier());
            fit(table, col);
        }
    }

    public static void fit(JTable table, int col) {
        if (!(table.getParent() instanceof JViewport))
            return;
        final JViewport viewport = (JViewport) table.getParent();
        final JTableHeader header = table.getTableHeader();
        final TableColumn tableColumn = table.getColumnModel().getColumn(col);
        int width = (int) header.getDefaultRenderer().getTableCellRendererComponent(
                table, tableColumn.getIdentifier(),
                false, false, -1, col).getPreferredSize().getWidth();

        boolean wasVisible = false;
        for (int row = 0; row < table.getRowCount(); row++) {
            if (!isRowVisible(viewport, table, row)) {
                if (wasVisible)
                    break;
                else
                    continue;
            }
            wasVisible = true;
            int preferedWidth = (int) table.getCellRenderer(row, col).getTableCellRendererComponent(table,
                    table.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
            width = Math.max(width, preferedWidth);
        }
        header.setResizingColumn(tableColumn);
        tableColumn.setWidth(width + table.getIntercellSpacing().width + 10);
    }

    private static TableColumn getResizingColumn(JTableHeader header, Point p) {
        int column = header.columnAtPoint(p);
        if (column == -1) {
            return null;
        }
        Rectangle r = header.getHeaderRect(column);
        r.grow(-3, 0);
        if (r.contains(p))
            return null;
        int midPoint = r.x + r.width / 2;
        int columnIndex;
        if (header.getComponentOrientation().isLeftToRight())
            columnIndex = (p.x < midPoint) ? column - 1 : column;
        else
            columnIndex = (p.x < midPoint) ? column : column - 1;
        if (columnIndex == -1)
            return null;
        return header.getColumnModel().getColumn(columnIndex);
    }

    public static boolean isRowVisible(JViewport viewport, JTable table, int row) {
        Rectangle rect = table.getCellRect(row, 1, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        return new Rectangle(viewport.getExtentSize()).contains(rect);
    }

}

