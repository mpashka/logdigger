package com.iv.logView.ui;

import info.clearthought.layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class MessageDialog extends AbstractDialog {

    private String text;

    public MessageDialog(Frame owner, String title, String text) {
        super(owner);
        this.text = text;
        setTitle(title);
        internalInit();
        moveToCenter();
    }

    private void internalInit() {
        double b = 10;
        double f = TableLayout.FILL;
        double p = TableLayout.PREFERRED;
        double[] cols = new double[]{b, f, p, f, b};
        double[] rows = new double[]{b, f, b, p, b};
        TableLayout layout = new TableLayout(cols, rows);
        dialog.setLayout(layout);
        dialog.add(new JLabel(text), "1, 1, 3, 1, CENTER, FULL");

        JButton okBtn = new JButton(new OkAction());
        dialog.add(okBtn, "2, 3");
        dialog.getRootPane().setDefaultButton(okBtn);
    }

    protected void onEscape() {
        dialog.setVisible(false);
    }

    private class OkAction extends AbstractAction {
        public OkAction() {
            putValue(NAME, "OK");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
            dialog.dispose();
        }
    }

}
