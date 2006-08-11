package com.iv.logView.io;

import com.iv.logView.model.LogColumnModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilteredLogReader extends LogReader {

    private final List<IndexRecord> filterIndex = new ArrayList<IndexRecord>();

    public FilteredLogReader(File file) throws IOException {
        super(file);
        getIndex().addAll(super.getIndex());
    }

    public void applyFilter(Filter filter) {
        getIndex().clear();
        if (filter == null) {
            getIndex().addAll(super.getIndex());
            return;
        }
        final Matcher matcher;
        if (filter.getMessagePattern() != null && filter.getMessagePattern().trim().length() > 0) {
            matcher = Pattern.compile(filter.getMessagePattern(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher("");
        } else {
            matcher = null;
        }
        for (IndexRecord rec : super.getIndex()) {
            boolean res = true;
            for (LogColumnModel cm : filter.getColumns()) {
                if (filter.getAll(cm).size() == filter.getSelected(cm).size()) continue;
                if (filter.getSelected(cm).contains(rec.getValue(cm))) continue;
                res &= false;
            }
            if (res && isMatch(matcher, rec)) {
                getIndex().add(rec);
            }
        }
    }

    protected List<IndexRecord> getIndex() {
        return filterIndex;
    }

    private boolean isMatch(Matcher matcher, IndexRecord rec) {
        if (matcher == null) return true;
        try {
            final String msg = get(rec);
            matcher.reset(msg);
            return matcher.find();
        } catch (Exception e) {
            return false;
        }
    }

}
