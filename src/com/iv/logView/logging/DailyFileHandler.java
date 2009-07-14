package com.iv.logView.logging;

import java.io.*;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class DailyFileHandler extends StreamHandler {

    public DailyFileHandler() throws FileNotFoundException {
        String logDir = System.getProperty("log.dir");
        if (logDir == null) {
            logDir = System.getProperty("user.home") + "/" + ".logView";
        }
        new File(logDir).mkdirs();
        String fileName = String.format("%s/%2$tY-%2$tm-%2$td.log", logDir, new Date());
        File file = new File(fileName);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file, true));
        setOutputStream(out);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        super.publish(record);
        flush();
    }
}
