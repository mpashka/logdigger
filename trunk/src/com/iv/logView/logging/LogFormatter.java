package com.iv.logView.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(LogRecord rec) {
        final StringBuilder buffer = new StringBuilder(1024);
        final Date logDate = new Date(rec.getMillis());
        buffer.append(DATE_FORMAT.format(logDate))
                .append(" [")
                .append(rec.getThreadID())
                .append("] ")
                .append(rec.getLevel().getName())
                .append(" ")
                .append(rec.getLoggerName())
                .append(" : ");
        if (rec.getMessage() != null) {
            buffer.append(rec.getMessage());
        }
        buffer.append(LINE_SEPARATOR);
        if (rec.getThrown() != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            rec.getThrown().printStackTrace(printWriter);
            buffer.append(stringWriter.getBuffer()).append(LINE_SEPARATOR);
        }
        return buffer.toString();
    }

}
