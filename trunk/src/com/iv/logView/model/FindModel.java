package com.iv.logView.model;

import javax.swing.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

public class FindModel {

    private static final int MAX_HISTORY_LENGTH = 20;
    private static final String HISTORY_KEY = "history";

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
        restoreHistory();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        LinkedList<String> history = restoreHistory();
        this.text = text;
        history.remove(text);
        history.addFirst(text);
        while (history.size() > MAX_HISTORY_LENGTH) {
            history.removeLast();
        }
        saveHistory(history);
    }

    private void saveHistory(LinkedList<String> history) {
        Preferences prefs = Preferences.userNodeForPackage(FindModel.class);
        StringBuilder sb = new StringBuilder();
        for (String str : history) {
            if (sb.length() > 0) sb.append((char) 0);
            sb.append(str);
        }
        prefs.put(HISTORY_KEY, sb.toString());
    }

    private LinkedList<String> restoreHistory() {
        LinkedList<String> history = new LinkedList<String>();
        Preferences prefs = Preferences.userNodeForPackage(FindModel.class);
        String[] strs = prefs.get(HISTORY_KEY, "").split("\\x00");
        for (String str : strs) {
            if (str.length() > 0) history.add(str);
        }
        return history;
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
        final List<String> history = restoreHistory();
        return history.toArray(new String[history.size()]);
    }

    public ComboBoxModel getColumns() {
        return colNames;
    }

}
