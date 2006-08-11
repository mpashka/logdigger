package com.iv.logView.io;

import com.iv.logView.model.LogColumnModel;

import java.util.Collection;

public interface Filter {

    String getMessagePattern();

    Collection<LogColumnModel> getColumns();

    Collection<String> getAll(LogColumnModel column);

    Collection<String> getSelected(LogColumnModel column);

}
