package com.iv.logView.model;

import com.iv.logView.Prefs;

import java.util.Enumeration;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

public class FindModel {

    private String text = "";
    private final ComboBoxModel colNames;
    private boolean regexp = false;
    private boolean caseSensitive = false;
    private boolean fromCursor = false;

    public FindModel(LogTableColumnModel tblModel) {
        String cols[] = new String[tblModel.getColumnCount()];
        int i = 0;
        for (Enumeration en = tblModel.getColumns(); en.hasMoreElements();) {
            LogColumnModel colModel = (LogColumnModel) en.nextElement();
            cols[i++] = colModel.getName();
        }
        colNames = new DefaultComboBoxModel(cols);
        colNames.setSelectedItem(cols);
        colNames.setSelectedItem(tblModel.getMessageColumn().getName());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        List<String> history = Prefs.getInstance().getSearchTextHistory();
        this.text = text;
        history.remove(text);
        history.add(0, text);
        Prefs.getInstance().setSearchTextHistory(history);
    }

    public boolean isRegexp() {
        return regexp;
    }

    public void setRegexp(boolean regexp) {
        this.regexp = regexp;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isFromCursor() {
        return fromCursor;
    }

    public void setFromCursor(boolean fromCursor) {
        this.fromCursor = fromCursor;
    }

    public String[] getHistory() {
        final List<String> history = Prefs.getInstance().getSearchTextHistory();
        return history.toArray(new String[history.size()]);
    }

    public ComboBoxModel getColumns() {
        return colNames;
    }

}
