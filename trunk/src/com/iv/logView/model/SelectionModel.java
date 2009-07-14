package com.iv.logView.model;

import java.util.*;

class SelectionModel {

    private final Collection<String> all = new TreeSet<String>();
    private final Collection<String> selected = new TreeSet<String>();

    public SelectionModel(Collection<String> all) {
        this.all.addAll(all);
        selected.addAll(all);
    }

    public void update(Collection<String> all) {
        Collection<String> added = new ArrayList<String>(all);
        added.removeAll(this.all);
        this.all.clear();
        this.all.addAll(all);
        selected.addAll(added);
        selected.retainAll(all);
    }


    public Collection<String> getAll() {
        return all;
    }

    public Collection<String> getSelected() {
        return selected;
    }
}
