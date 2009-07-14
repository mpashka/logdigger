package com.iv.logView.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private static final Level ERROR = new Level("ERROR", 1000) {
    };
    private static final Level DEBUG = new Level("DEBUG", 1000) {
    };

    private final Logger logger;

    public Log(Logger logger) {
        this.logger = logger;
    }

    public void info(String msg) {
        logger.log(Level.INFO, msg);
    }

    public void warning(String msg, Throwable throwable) {
        logger.log(Level.WARNING, msg, throwable);
    }

    public void warning(String msg) {
        logger.log(Level.WARNING, msg);
    }

    public void error(String msg, Throwable throwable) {
        logger.log(ERROR, msg, throwable);
    }

    public void error(String msg) {
        logger.log(ERROR, msg);
    }

    public void error(Throwable throwable) {
        logger.log(ERROR, "", throwable);
    }

    public void debug(String msg, Throwable throwable) {
        logger.log(DEBUG, msg, throwable);
    }

    public void debug(String msg) {
        logger.log(DEBUG, msg);
    }

    public void debug(Throwable throwable) {
        logger.log(DEBUG, "", throwable);
    }

}
