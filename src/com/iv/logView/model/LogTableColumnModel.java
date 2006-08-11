package com.iv.logView.model;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class LogTableColumnModel extends DefaultTableColumnModel {

    private final String pattern;
    private LogColumnModel messageColumn;

    public LogTableColumnModel(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void addColumn(TableColumn column, boolean isMsgCollumn) {
        addColumn(column);
        if (isMsgCollumn) {
            messageColumn = (LogColumnModel) column;
        }
    }

    public LogColumnModel getMessageColumn() {
        return messageColumn;
    }


}
