package com.iv.logView;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Prefs {

    static final int MAX_LIST_SIZE = 20;
    private static final String LAST_DIR_KEY = "last-dir";
    private static final String WINDOW_STATE_KEY = "window-state";
    private static final String WINDOW_SIZE_WIDTH_KEY = "window-size-width";
    private static final String WINDOW_SIZE_HEIGHT_KEY = "window-size-height";
    private static final String WINDOW_LOCATION_X_KEY = "window-location-x";
    private static final String WINDOW_LOCATION_Y_KEY = "window-location-y";
    private static final String SYNC_TIME_KEY = "sync-time";
    private static final String SYNC_VALUE_KEY = "sync-value";
    private static final String FPH_KEY = "filter-pattern-history";
    private static final String STH_KEY = "search-text-history";
    private static final String FILE_HISTORY_KEY = "file-history";
    private Preferences preferences;

    private static class PrefsHolder {
        private static Prefs instance = new Prefs();
    }

    private Prefs() {
        preferences = Preferences.userNodeForPackage(Prefs.class);
    }

    public static Prefs getInstance() {
        return PrefsHolder.instance;
    }

    void reset() {
        preferences = Preferences.userNodeForPackage(Prefs.class);
    }

    String get(String key, String def) {
        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            return def;
        }
        return preferences.get(key, def);
    }

    void put(String key, String value) {
        if (value == null) {
            preferences.remove(key);

        } else {
            preferences.put(key, value);
        }
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            // ignore ?
        }
    }

    private int getInt(String key, int def) {
        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            return def;
        }
        return preferences.getInt(key, def);
    }

    public void putInt(String key, int value) {
        preferences.putInt(key, value);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            // ignore ?
        }
    }

    private long getLong(String key, long def) {
        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            return def;
        }
        return preferences.getLong(key, def);
    }

    public void putLong(String key, long value) {
        preferences.putLong(key, value);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            // ignore ?
        }
    }

    List<String> getList(String key) {
        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            return new ArrayList<String>();
        }
        int size = preferences.getInt(key + "-size", 0);
        List<String> result = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            String str = preferences.get(key + "-item-" + i, "").trim();
            if (!"".equals(str)) {
                result.add(str);
            }
        }
        return result;
    }

    void putList(String key, List<String> items) {
        int size = 0;
        for (String value : items) {
            if (value != null && !"".equals(value.trim())) {
                preferences.put(key + "-item-" + size, value);
                if (++size >= MAX_LIST_SIZE) {
                    break;
                }
            }
        }
        preferences.putInt(key + "-size", size);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            // ignore ?
        }
    }

    public String getLastDir() {
        return get(LAST_DIR_KEY, "/");
    }

    public void setLastDir(String dir) {
        put(LAST_DIR_KEY, dir);
    }

    public int getWindowState() {
        return getInt(WINDOW_STATE_KEY, 0);
    }

    public void setWindowState(int state) {
        putInt(WINDOW_STATE_KEY, state);
    }

    public Dimension getWindowSize() {
        int width = getInt(WINDOW_SIZE_WIDTH_KEY, 1200);
        int height = getInt(WINDOW_SIZE_HEIGHT_KEY, 800);
        return new Dimension(width, height);
    }

    public void setWindowSize(Dimension size) {
        putInt(WINDOW_SIZE_WIDTH_KEY, size.width);
        putInt(WINDOW_SIZE_HEIGHT_KEY, size.height);
    }

    public Point getWindowLocation() {
        int x = getInt(WINDOW_LOCATION_X_KEY, Integer.MIN_VALUE);
        int y = getInt(WINDOW_LOCATION_Y_KEY, Integer.MIN_VALUE);
        return new Point(x, y);
    }

    public void setWindowLocation(Point point) {
        putInt(WINDOW_LOCATION_X_KEY, point.x);
        putInt(WINDOW_LOCATION_Y_KEY, point.y);
    }

    public long getSyncTime() {
        return getLong(SYNC_TIME_KEY, 0);
    }

    public void setSyncTime(long time) {
        putLong(SYNC_TIME_KEY, time);
    }

    public String getSyncValue() {
        return get(SYNC_VALUE_KEY, "");
    }

    public void setSyncValue(String syncValue) {
        put(SYNC_VALUE_KEY, syncValue);
    }

    public List<String> getFilterPatternHistory() {
        return getList(FPH_KEY);
    }

    public void setFilterPatternHistory(List<String> items) {
        putList(FPH_KEY, items);
    }

    public List<String> getSearchTextHistory() {
        return getList(STH_KEY);
    }

    public void setSearchTextHistory(List<String> items) {
        putList(STH_KEY, items);
    }

    private static List<String> cleanFiles(List<String> files) {
        for (Iterator<String> iter = files.iterator(); iter.hasNext();) {
            File file = new File(iter.next());
            if (!file.exists() || !file.canRead()) {
                iter.remove();
            }
        }
        return files;
    }

    public List<String> getFileHistory() {
        return cleanFiles(getList(FILE_HISTORY_KEY));
    }

    public void setFileHistory(List<String> items) {
        putList(FILE_HISTORY_KEY, items);
    }

    public List<String> addFileHistory(String item) {
        List<String> list = getList(FILE_HISTORY_KEY);
        list.remove(item);
        list.add(0, item);
        setFileHistory(list);
        return cleanFiles(list);
    }

    public List<String> removeFileHistory(String item) {
        List<String> list = getList(FILE_HISTORY_KEY);
        list.remove(item);
        setFileHistory(list);
        return list;
    }

}
