package com.iv.logView.ui;

import java.awt.Image;
import javax.swing.ImageIcon;

public class ApplicationIcon {
    private final Image icon;

    private static class ApplicationIconHolder {
        private static ApplicationIcon instance = new ApplicationIcon();
    }

    private ApplicationIcon() {
        icon = new ImageIcon(getClass().getClassLoader().getResource("icon.png")).getImage();
    }

    public static Image getImage() {
        return ApplicationIconHolder.instance.icon;
    }

}
