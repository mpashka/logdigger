package com.iv.logView;

import com.iv.logView.io.FilteredLogReader;
import com.iv.logView.ui.MainPanel;
import com.iv.logView.ui.MessageDialog;

import javax.swing.*;
import java.io.File;
import java.util.prefs.Preferences;

public class Main {

    private static final String LAS_DIR_KEY = "last.dir";

    public static void main(String... args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticLookAndFeel");
        } catch (Exception e) {
            // ignore. use default l&f
        }

        final String version = Main.class.getPackage().getImplementationVersion();
        if (version != null) {
            System.out.println("Version: " + version);
        }

        String fileName;
        if (args.length > 0) {
            fileName = args[0];
        } else {
            fileName = chooseFileName();
            if (fileName == null) {
                System.exit(0);
            }
        }
        final File file = new File(fileName).getAbsoluteFile();
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            error("can't read file " + file.getName());
        }
        final Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(LAS_DIR_KEY, file.getParentFile() == null ? "/" : file.getParentFile().getAbsolutePath());

        final MainPanel panel = new MainPanel(new FilteredLogReader(file));
        panel.setTitle("LogView - " + file.getAbsolutePath());
        panel.setVisible(true);
    }

    private static String chooseFileName() {
        JFileChooser chooser = new JFileChooser();
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        chooser.setCurrentDirectory(new File(prefs.get(LAS_DIR_KEY, "/")));
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private static void error(String text) {
        new MessageDialog(null, "ERROR", text).execute();
        System.exit(0);
    }

}


