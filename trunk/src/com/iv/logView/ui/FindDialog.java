package com.iv.logView.ui;

import com.iv.logView.model.FindModel;
import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class FindDialog<T extends FindModel> extends OkCancalDialog<T> {
    private JComboBox searchText;
    private JCheckBox regexpChkBox;
    private JCheckBox caseSensChkBox;
    private JCheckBox fromCursorChkBox;

    public FindDialog(Frame owner, T model) {
        super(owner, model);
    }

    protected boolean validate() {
        String txt = (String) searchText.getSelectedItem();
        if (txt == null || txt.trim().length() == 0) {
            setError(searchText, "Text is reguired!");
            return false;
        }
        return true;
    }

    protected void submit(T model) {
        model.setText((String) searchText.getSelectedItem());
        model.setRegexp(regexpChkBox.isSelected());
        model.setCaseSensitive(caseSensChkBox.isSelected());
        model.setFromCursor(fromCursorChkBox.isSelected());
    }

    protected void cancel() {
        //do nothing
    }

    protected void init() {
        final T model = getModel();
        setTitle("Find Text");

        double f = TableLayout.FILL;
        double p = TableLayout.PREFERRED;
        double[] cols = new double[]{p, f};
        double[] rows = new double[]{p, p, p, p, p};
        TableLayout layout = new TableLayout(cols, rows);
        layout.setHGap(5);
        layout.setVGap(5);
        setLayout(layout);

        JLabel searchTextLbl = new JLabel("Text");
        searchTextLbl.setDisplayedMnemonic(KeyEvent.VK_T);
        add(searchTextLbl, "0, 0");

        searchText = new JComboBox(model.getHistory());
        searchText.setEditable(true);
        searchText.getEditor().selectAll();
        searchText.setPreferredSize(new Dimension(300, searchText.getPreferredSize().height));
        add(searchText, "1, 0");
        searchTextLbl.setLabelFor(searchText);

        JLabel searchInLbl = new JLabel("In column");
        searchInLbl.setDisplayedMnemonic(KeyEvent.VK_N);
        add(searchInLbl, "0, 1");

        JComboBox searchIn = new JComboBox(model.getColumns());
        searchIn.setEditable(false);
        searchIn.setPreferredSize(new Dimension(300, searchIn.getPreferredSize().height));
        add(searchIn, "1, 1");
        searchInLbl.setLabelFor(searchIn);

        regexpChkBox = new JCheckBox("Regular expression", model.isRegexp());
        regexpChkBox.setMnemonic(KeyEvent.VK_R);
        add(regexpChkBox, "0, 2, 1, 1");

        caseSensChkBox = new JCheckBox("Case sensitive", model.isCaseSensitive());
        caseSensChkBox.setMnemonic(KeyEvent.VK_S);
        add(caseSensChkBox, "0, 3, 1, 2");

        fromCursorChkBox = new JCheckBox("Find from cursor", model.isFromCursor());
        fromCursorChkBox.setMnemonic(KeyEvent.VK_C);
        add(fromCursorChkBox, "0, 4, 1, 3");

    }
}
