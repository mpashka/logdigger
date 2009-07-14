package com.iv.logView.model;

import java.util.regex.Pattern;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class LogTableColumnModel extends DefaultTableColumnModel {

    private final Pattern pattern;
    private LogColumnModel messageColumn;

    public LogTableColumnModel(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern getPattern() {
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
