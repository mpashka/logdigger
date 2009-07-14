package com.iv.logView;

import com.iv.logView.io.FilteredLogReader;
import com.iv.logView.logging.Log;
import com.iv.logView.logging.LogFactory;
import com.iv.logView.ui.MainPanel;
import com.iv.logView.ui.MessageDialog;
import com.iv.logView.ui.SplashScreen;

import java.io.File;
import java.util.regex.Pattern;
import java.awt.*;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class Main {

    private static final Log LOG = LogFactory.getLogger(Main.class);
    private static final String LOOK_AND_FEEL = "com.jgoodies.plaf.plastic.PlasticLookAndFeel";

    public static void main(String[] args) throws Exception {

        try {
            UIManager.setLookAndFeel(LOOK_AND_FEEL);
        } catch (Exception e) {
            LOG.warning("Can't initialize L&F " + LOOK_AND_FEEL, e);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        LOG.info("========================================");

        String fileName;
        if (args.length > 0) {
            fileName = args[0];
        } else {
            fileName = chooseFileName(null);
            if (fileName == null) {
                System.exit(0);
            }
        }
        
        SplashScreen splashScreen = new SplashScreen(null);
        splashScreen.setVisible(true);
        try {
            final String version = Main.class.getPackage().getImplementationVersion();
            if (version != null) {
                LOG.info("Version: " + version);
            }

            final File file = new File(fileName).getAbsoluteFile();
            if (!file.exists() || file.isDirectory() || !file.canRead()) {
                error("Can't read file " + file.getName());
            }
            Prefs.getInstance().setLastDir(file.getParentFile() == null ? "/" : file.getParentFile().getAbsolutePath());

            FilteredLogReader logReader = new FilteredLogReader(file, splashScreen);
            final MainPanel panel = new MainPanel(logReader);
            logReader.setProgressListener(null);
            panel.setVisible(true);
        } catch (Exception ex) {
            splashScreen.setVisible(false);
            LOG.error(ex);
            error(ex.getMessage());
        } finally {
            splashScreen.setVisible(false);
            splashScreen.dispose();
        }
    }

    public static String chooseFileName(Component parent) {
        final Pattern namePattern = Pattern.compile("\\.log(\\.\\d+)*$", Pattern.CASE_INSENSITIVE);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open log file");
        chooser.setCurrentDirectory(new File(Prefs.getInstance().getLastDir()));
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || namePattern.matcher(f.getName()).find();
            }
            @Override
            public String getDescription() {
                return "Log Files";
            }
        });
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private static void error(String text) {
        new MessageDialog(null, "Error", text).execute();
        System.exit(0);
    }

}


