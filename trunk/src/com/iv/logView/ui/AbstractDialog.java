package com.iv.logView.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public abstract class AbstractDialog<T> {

    private T model;
    protected final JDialog dialog;
    protected boolean execResult;

    protected AbstractDialog(Frame owner) {
        this(owner, null);
    }

    protected AbstractDialog(Frame owner, T model) {
        this.model = model;
        dialog = new JDialog(owner, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        activateEscapeKey();
    }

    public boolean execute() {
        execResult = false;
        dialog.setVisible(true);
        dialog.setVisible(false);
        dialog.dispose();
        dispose();
        return execResult;
    }

    protected void setTitle(String title) {
        dialog.setTitle(title);
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    protected void moveToCenter() {
        dialog.pack();
        if (dialog.getOwner().isVisible()) {
            Point p = dialog.getOwner().getLocation();
            int x = (dialog.getOwner().getSize().width - dialog.getWidth()) / 2;
            int y = (dialog.getOwner().getSize().height - dialog.getHeight()) / 2;
            dialog.setLocation(p.x + x, p.y + y);
        } else {
            Dimension frameSize = dialog.getSize();
            Point cp = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
            Dimension screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }
            dialog.setLocation(cp.x - frameSize.width / 2, cp.y - frameSize.height / 2);
        }
    }

    protected void dispose() {
    }

    private void activateEscapeKey() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onEscape();
            }
        };
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    protected void onEscape() {

    }


}
