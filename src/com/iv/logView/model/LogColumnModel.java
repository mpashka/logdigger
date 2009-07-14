package com.iv.logView.model;

import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LogColumnModel extends TableColumn {
    private final int group;
    private final boolean indexable;
    private final Map<String, Color> hlMap = new HashMap<String, Color>();

    public LogColumnModel(int index, String name, int group, boolean indexable) {
        super(index);
        setHeaderValue(name);
        this.group = group;
        this.indexable = indexable;
    }

    public void addHighlighting(String value, Color color) {
        hlMap.put(value, color);
    }

    public Color getColor(String value) {
        return hlMap.get(value);
    }

    public String getName() {
        return (String) getHeaderValue();
    }

    public int getGroup() {
        return group;
    }

    public boolean isIndexable() {
        return indexable;
    }

    @Override
    public int hashCode() {
        return getHeaderValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getHeaderValue().equals(obj);
    }
}
