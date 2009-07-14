package com.iv.logView.ui;

import com.iv.logView.io.ProgressListener;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class JStatusBar extends JPanel implements ProgressListener {

    private final JLabel positionLbl;
    private final JLabel reloadLbl;
    private final ImageIcon reloadIcon;
    private final FileMonitor fileMonitor;
    private JProgressBar progressBar;

    public JStatusBar(File file) {
        double f = TableLayout.FILL;
        double p = TableLayout.PREFERRED;
        double d = 3;
        double[] cols = new double[]{p, d, 100, d, f};
        double[] rows = new double[]{p};
        TableLayout layout = new TableLayout(cols, rows);
        layout.setHGap(5);
        setLayout(layout);

        setBorder(BorderFactory.createLoweredBevelBorder());

        reloadIcon = new ImageIcon(getClass().getClassLoader().getResource("reload.png"));

        reloadLbl = new JLabel();
        reloadLbl.setMinimumSize(new Dimension(16, 16));
        reloadLbl.setPreferredSize(new Dimension(16, 16));
        add(reloadLbl, "0,0");

        add(div(), "1,0");

        positionLbl = new JLabel("", JLabel.CENTER);
        add(positionLbl, "2,0");

        add(div(), "3,0");

        JPanel comp = new JPanel();
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setPreferredSize(new Dimension(100, 8));
        progressBar.setMaximum(100);
        progressBar.setVisible(false);
        comp.add(progressBar);
        add(comp, "4,0");

        fileMonitor = new FileMonitor(file);
        new Thread(fileMonitor).start();
    }

    public void setFile(File file) {
        fileMonitor.setFile(file);
        fileMonitor.updateFileLength();
    }

    public void updateFileLength() {
        fileMonitor.updateFileLength();
    }

    private JPanel div() {
        JPanel div = new JPanel();
        div.setBorder(BorderFactory.createLoweredBevelBorder());
        return div;
    }

    public void setPosition(int rowNum, int rowCount) {
        positionLbl.setText("" + rowNum + ":" + rowCount);
    }


    public void onBegin() {
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setVisible(true);
    }

    public void onEnd() {
        progressBar.setVisible(false);
    }

    public void onProgress(int percent) {
        progressBar.setValue(percent);
        progressBar.repaint();
    }

    class FileMonitor implements Runnable {
        private File file;
        private long fileLength = 0;

        public FileMonitor(File file) {
            this.file = file;
        }

        public void run() {
            updateFileLength();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (file != null && file.length() != fileLength) {
                        reloadLbl.setIcon(reloadIcon);
                        reloadLbl.setToolTipText("The viewed file has been changed. Press CTRL-R for reload.");
                        reloadLbl.repaint();
                        synchronized (this) {
                            wait();
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void updateFileLength() {
            reloadLbl.setIcon(null);
            reloadLbl.setToolTipText(null);
            fileLength = file == null ? 0 : file.length();
            synchronized (this) {
                fileMonitor.notify();
            }
        }

        public void setFile(File file) {
            this.file = file;
            fileLength = 0;
        }
    }
}
