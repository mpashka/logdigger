package com.iv.logView.model;

import com.iv.logView.Prefs;
import com.iv.logView.io.Filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterModel implements Filter {

    private String messagePattern = "";
    private final Map<LogColumnModel, SelectionModel> map = new HashMap<LogColumnModel, SelectionModel>();

    public FilterModel(Map<LogColumnModel, Set<String>> idxMap) {
        for (LogColumnModel cm : idxMap.keySet()) {
            map.put(cm, new SelectionModel(idxMap.get(cm)));
        }
    }

    @Override
    public String getMessagePattern() {
        return messagePattern;
    }

    public String[] getPatternHistory() {
        final List<String> history = Prefs.getInstance().getFilterPatternHistory();
        return history.toArray(new String[history.size()]);
    }

    public void setMessagePattern(String messagePattern) {
        final Prefs prefs = Prefs.getInstance();
        this.messagePattern = messagePattern;
        List<String> history = prefs.getFilterPatternHistory();
        history.remove(messagePattern);
        history.add(0, messagePattern);
        prefs.setFilterPatternHistory(history);
    }

    @Override
    public Collection<LogColumnModel> getColumns() {
        return map.keySet();
    }

    @Override
    public Collection<String> getAll(LogColumnModel column) {
        return Collections.unmodifiableCollection(map.get(column).getAll());
    }

    @Override
    public Collection<String> getSelected(LogColumnModel column) {
        return map.get(column).getSelected();
    }

    public void update(Map<LogColumnModel, Set<String>> idxMap) {
        for (LogColumnModel cm : idxMap.keySet()) {
            SelectionModel selection = map.get(cm);
            if (selection == null) {
                map.put(cm, new SelectionModel(idxMap.get(cm)));
            } else {
                selection.update(idxMap.get(cm));
            }
        }
    }

}
