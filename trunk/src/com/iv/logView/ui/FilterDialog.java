package com.iv.logView.ui;

import com.iv.logView.model.FilterModel;
import com.iv.logView.model.LogColumnModel;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FilterDialog<T extends FilterModel> extends OkCancalDialog<T> {

    private JTextField patternEdit;
    private Map<LogColumnModel, JSelector> selectors;

    public FilterDialog(Frame owner, T model) {
        super(owner, model);
    }

    protected boolean validate() {
        if (patternEdit.getText() != null && patternEdit.getText().length() > 0) {
            try {
                Pattern.compile(patternEdit.getText());
            } catch (PatternSyntaxException e) {
                String msg;
                if (e.getIndex() != -1) {
                    patternEdit.setCaretPosition(e.getIndex());
                    msg = e.getDescription() + " near index " + e.getIndex();
                } else {
                    msg = e.getDescription();
                }
                setError(patternEdit, msg);
                return false;
            }
        }

        for (LogColumnModel cm : selectors.keySet()) {
            JSelector s = selectors.get(cm);
            if (s.getSelected().isEmpty()) {
                setError(s, cm.getHeaderValue() + " is not selected");
                return false;
            }
        }
        return true;
    }

    protected void submit(T model) {
        getModel().setMessagePattern(patternEdit.getText());
        for (LogColumnModel cm : selectors.keySet()) {
            Collection<String> selected = getModel().getSelected(cm);
            selected.clear();
            selected.addAll(selectors.get(cm).getSelected());
        }
    }

    protected void cancel() {
        // do nothing
    }

    protected void init() {
        setTitle("Filter");
        double[] cols = new double[]{TableLayout.FILL};
        double[] rows = new double[]{TableLayout.PREFERRED};
        TableLayout layout = new TableLayout(cols, rows);
        setLayout(layout);

        cols = new double[]{5, TableLayout.FILL, 5};
        rows = new double[]{TableLayout.PREFERRED, 5};
        JPanel pan = new JPanel(new TableLayout(cols, rows));
        pan.setBorder(BorderFactory.createTitledBorder("Regular expression"));
        patternEdit = new JTextField(getModel().getMessagePattern());
        pan.add(patternEdit, "1, 0");
        add(pan, "0, 0");

        int rn = 0;

        selectors = new HashMap<LogColumnModel, JSelector>();
        for (LogColumnModel cm : getModel().getColumns()) {
            if (!cm.isIndexable()) continue;
            pan = new JPanel(new BorderLayout());
            pan.setBorder(BorderFactory.createTitledBorder(cm.getName()));
            Collection<String> selected = getModel().getSelected(cm);
            Collection<String> all = getModel().getAll(cm);
            JSelector s = new JSelector(all, selected);
            selectors.put(cm, s);
            pan.add(s, BorderLayout.CENTER);

            layout.insertRow(++rn, TableLayout.FILL);
            add(pan, "0, " + rn);
        }
    }

}
