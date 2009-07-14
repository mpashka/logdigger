package com.iv.logView.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogFactory {

    private static class LogFactoryHolder {
        private static LogFactory instance = new LogFactory();
    }

    /**
     * private constructor of a singleton class
     */
    private LogFactory() {
        InputStream propStream = getClass().getClassLoader().getResourceAsStream("logging.properties");
        if (propStream == null) throw new RuntimeException("Can't initialize LogManager. propStream is null");
        try {
            LogManager.getLogManager().readConfiguration(propStream);
            propStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Can't initialize LogManager", e);
        }
    }

    private Log get(String name) {
        return new Log(Logger.getLogger(name));
    }

    public static synchronized Log getLogger(final Class clazz) {
        return LogFactoryHolder.instance.get(clazz.getName());
    }

    public static synchronized Log getLogger(final String name) {
        return LogFactoryHolder.instance.get(name);
    }

}
