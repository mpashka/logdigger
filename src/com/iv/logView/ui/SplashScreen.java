package com.iv.logView.ui;

import com.iv.logView.io.ProgressListener;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class SplashScreen extends JWindow implements ProgressListener {
    private final SplashProgress splashProgress;

    public SplashScreen(Frame owner) {
        setFocusable(true);
        if (owner != null) requestFocus();
        boolean closeOnClick = owner != null;
        final JPanel content = (JPanel) getContentPane();
        double f = TableLayout.FILL;
        double p = TableLayout.PREFERRED;
        double b = 5;
        double[] cols = new double[]{b, f, b};
        double[] rows = new double[]{b, p, p, p, p, b};
        TableLayout layout = new TableLayout(cols, rows);
        setLayout(layout);

        content.setBorder(BorderFactory.createRaisedBevelBorder());

        JLabel imgLbl = new JLabel(
                new ImageIcon(getClass().getClassLoader().getResource("splash.png")),
                JLabel.CENTER
        );
        imgLbl.setBorder(BorderFactory.createLoweredBevelBorder());
        content.add(imgLbl, "1,1");
        splashProgress = new SplashProgress();
        splashProgress.setPreferredSize(new Dimension(getWidth(), 5));
        content.add(splashProgress, "1,2");
        String version = getClass().getPackage().getImplementationVersion();
        content.add(new JLabel("LogViewer version " + version, JLabel.CENTER), "1,3");
        content.add(new JLabel("Copyright (c) 2008 by P.A.S", JLabel.CENTER), "1,4");
        centerOnScreen(owner);

        if (closeOnClick) {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    close();
                }
            });
            addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    close();
                }
            });
            addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    close();
                }
            });
        }
    }

    public void close() {
        setVisible(false);
        dispose();
        if (getOwner() != null) getOwner().requestFocus();
    }

    public void onBegin() {
        splashProgress.reset();
    }

    public void onEnd() {
        splashProgress.done();
    }

    public void onProgress(int percent) {
        splashProgress.setPercent(percent);
    }

    private void centerOnScreen(Frame owner) {
        pack();
        final Point center;
        if (owner == null) {
            center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        } else {
            Rectangle r = owner.getBounds();
            center = new Point(r.x + r.width / 2, r.y + r.height / 2);
        }
        Dimension winSize = getSize();
        int x = center.x - (winSize.width / 2);
        int y = center.y - (winSize.height / 2);
        setLocation(x, y);
    }

    private static class SplashProgress extends JComponent {
        public static final int DEFAULT_STEP = 5;
        private final int step;
        private int percent = 0;

        public SplashProgress() {
            this(DEFAULT_STEP);
        }

        public SplashProgress(int step) {
            this.step = step;
        }

        public void reset() {
            this.percent = 0;
            repaint();
        }

        public void done() {
            this.percent = 100;
            repaint();
        }

        public void setPercent(int percent) {
            if (percent - this.percent > step) {
                this.percent = percent - (percent % step);
                repaint();
            }
        }

        public void paint(Graphics g) {
            int w = Math.round(getWidth() / 100f * percent);
            g.setColor(Color.RED);
            g.fillRect(0, 1, w - 2, getHeight());
        }
    }
}
