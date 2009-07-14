package com.iv.logView.ui;

import com.iv.logView.model.FilterModel;
import com.iv.logView.model.LogColumnModel;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FilterDialog<T extends FilterModel> extends OkCancalDialog<T> {

    private JComboBox patternEdit;
    private Map<LogColumnModel, JSelector> selectors;

    public FilterDialog(Frame owner, T model) {
        super(owner, model);
    }

    @Override
    protected boolean validate() {
        String pattern = (String) patternEdit.getSelectedItem();
        if (pattern != null && pattern.length() > 0) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                String msg;
                if (e.getIndex() != -1) {
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

    @Override
    protected void submit(T model) {
        getModel().setMessagePattern((String) patternEdit.getSelectedItem());
        for (LogColumnModel cm : selectors.keySet()) {
            Collection<String> selected = getModel().getSelected(cm);
            selected.clear();
            selected.addAll(selectors.get(cm).getSelected());
        }
    }

    @Override
    protected void cancel() {
        // do nothing
    }

    @Override
    protected void init() {
        setTitle("Filter");
        double[] cols = new double[]{TableLayout.FILL};
        double[] rows = new double[]{TableLayout.PREFERRED, TableLayout.FILL};
        TableLayout layout = new TableLayout(cols, rows);
        setLayout(layout);

        cols = new double[]{5, TableLayout.FILL, 5};
        rows = new double[]{TableLayout.PREFERRED, 5};
        final JPanel pan = new JPanel(new TableLayout(cols, rows));
        pan.setBorder(BorderFactory.createTitledBorder("Regular expression"));
        patternEdit = new JComboBox(getModel().getPatternHistory());
        patternEdit.setEditable(true);
        patternEdit.getEditor().setItem(getModel().getMessagePattern());
        patternEdit.getEditor().selectAll();
        pan.add(patternEdit, "1, 0");
        add(pan, "0, 0");

        JTabbedPane tabPan = new JTabbedPane();
        int tabIdx = 0;
        selectors = new HashMap<LogColumnModel, JSelector>();
        for (LogColumnModel model : getModel().getColumns()) {
            if (!model.isIndexable()) continue;
            final JPanel selectorPan = new JPanel(new BorderLayout());
//            selectorPan.setBorder(BorderFactory.createTitledBorder(model.getName()));
            Collection<String> selected = getModel().getSelected(model);
            Collection<String> all = getModel().getAll(model);
            JSelector selector = new JSelector(all, selected);
            selectors.put(model, selector);
            selectorPan.add(selector, BorderLayout.CENTER);
            tabPan.addTab("" + (tabIdx + 1) + " " + model.getName(), selector);
            tabPan.setMnemonicAt(tabIdx, KeyEvent.VK_1 + tabIdx);
            tabIdx++;
        }
        add(tabPan, "0, 1");
    }

}
