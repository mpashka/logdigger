package com.iv.logView.ui;

import javax.swing.*;
import java.awt.*;

/**
 * todo: need improvements
 */
public class JStatusBar extends JPanel {

    private final JLabel pos;

    public JStatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 20));
        setBorder(BorderFactory.createLoweredBevelBorder());
        pos = new JLabel();
        add(pos, BorderLayout.WEST);
    }

    public void setPosition(int rowNum, int rowCount) {
        pos.setText("" + rowNum + ":" + rowCount);
    }
}
