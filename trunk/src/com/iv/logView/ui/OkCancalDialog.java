package com.iv.logView.ui;

import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class OkCancalDialog<T> extends AbstractDialog<T> {

    private JPanel contentPane;
    private JLabel errorLbl;
    private ErrorContect errCtx;

    protected OkCancalDialog(Frame owner, T model) {
        super(owner, model);
        internalInit();
        init();
        moveToCenter();
    }

    protected abstract boolean validate();

    protected abstract void submit(T model);

    protected abstract void cancel();

    protected abstract void init();

    protected void setLayout(LayoutManager manager) {
        contentPane.setLayout(manager);
    }

    protected <T extends Component> T add(T comp) {
        contentPane.add(comp);
        return comp;
    }

    protected <T extends Component> T add(String name, T comp) {
        contentPane.add(name, comp);
        return comp;
    }

    protected <T extends Component> T add(T comp, Object constraints) {
        contentPane.add(comp, constraints);
        return comp;
    }

    protected void setError(JComponent comp, String message) {
        if (comp != null) {
            if (comp.getParent() instanceof JScrollPane) {
                comp = (JComponent) comp.getParent();
            }
            errCtx = new ErrorContect(comp);
            comp.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                scrollPane.getViewport().getView().requestFocus();
            } else {
                comp.requestFocus();
            }
        }
        errorLbl.setText(message);
    }

    protected boolean hasError() {
        return errCtx != null;
    }

    protected void clearError() {
        if (errCtx != null) {
            errCtx.reset();
            errorLbl.setText(null);
            errCtx = null;
        }
    }

    protected void onEscape() {
        internalCancel();
    }

    private void internalInit() {
        clearError();
        execResult = false;
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                internalCancel();
            }
        });
        double f = TableLayout.FILL;
        double p = TableLayout.PREFERRED;
        double b = 5;
        double[] cols = new double[]{b, f, p, p, b};
        double[] rows = new double[]{b, f, p, b};
        TableLayout layout = new TableLayout(cols, rows);
        layout.setVGap((int) b);
        layout.setHGap((int) b);
        dialog.setLayout(layout);
        contentPane = new JPanel();
        dialog.add(contentPane, "1, 1, 3, 1");

        errorLbl = new JLabel();
        errorLbl.setForeground(Color.RED);
        dialog.add(errorLbl, "1, 2, CENTER, CENTER");

        JButton okBtn = new JButton(new OkAction());
        okBtn.setDefaultCapable(true);
        dialog.add(okBtn, "2, 2");
        JButton cancelBtn = new JButton(new CancelAction());
        dialog.add(cancelBtn, "3, 2");
        dialog.getRootPane().setDefaultButton(okBtn);
    }

    private void internalSubmit() {
        clearError();
        if (validate()) {
            submit(getModel());
            execResult = true;
            dialog.setVisible(false);
        }
    }

    private void internalCancel() {
        cancel();
        execResult = false;
        dialog.setVisible(false);
    }

    private class OkAction extends AbstractAction {
        public OkAction() {
            putValue(NAME, "OK");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            internalSubmit();
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            putValue(NAME, "Cancel");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        }

        public void actionPerformed(ActionEvent e) {
            internalCancel();
        }
    }

    private static class ErrorContect {
        private JComponent component;
        private Border normalBorter;

        public ErrorContect(JComponent component) {
            this.component = component;
            this.normalBorter = component.getBorder();
        }

        public JComponent getComponent() {
            return component;
        }

        public Border getNormalBorter() {
            return normalBorter;
        }

        public void reset() {
            component.setBorder(normalBorter);
        }
    }

}
