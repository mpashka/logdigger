package com.iv.logView.model;

import com.iv.logView.io.Filter;

import java.util.*;

public class FilterModel implements Filter {

    private String messagePattern = "";
    private final Map<LogColumnModel, SelectionPair> map = new HashMap<LogColumnModel, SelectionPair>();

    public FilterModel(Map<LogColumnModel, Set<String>> idxMap) {
        for (LogColumnModel cm : idxMap.keySet()) {
            map.put(cm, new SelectionPair(idxMap.get(cm)));
        }
    }

    public String getMessagePattern() {
        return messagePattern;
    }

    public void setMessagePattern(String messagePattern) {
        this.messagePattern = messagePattern;
    }

    public Collection<LogColumnModel> getColumns() {
        return map.keySet();
    }

    public Collection<String> getAll(LogColumnModel column) {
        return Collections.unmodifiableCollection(map.get(column).allValues);
    }

    public Collection<String> getSelected(LogColumnModel column) {
        return map.get(column).selectedValues;
    }

    private static class SelectionPair {
        final Collection<String> allValues = new TreeSet<String>();
        final Collection<String> selectedValues = new TreeSet<String>();

        public SelectionPair(Collection<String> all) {
            allValues.addAll(all);
            selectedValues.addAll(all);
        }

    }
}
