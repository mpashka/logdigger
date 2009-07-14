package com.iv.logView;

import junit.framework.TestCase;

import java.awt.Dimension;
import java.awt.Point;
import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;

public class TestPrefs extends TestCase {

//    final File tmpFile;


    public TestPrefs() throws Throwable {
//        tmpFile = File.createTempFile("logViewTest", "");
//        tmpFile.deleteOnExit();
    }

    @Override
    protected void setUp() throws Exception {
        Preferences preferences = Preferences.userNodeForPackage(Prefs.class);
//        preferences.exportSubtree(new BufferedOutputStream(new FileOutputStream(tmpFile)));
        preferences.removeNode();
        preferences.flush();
        Prefs.getInstance().reset();
    }

    @Override
    protected void tearDown() throws Exception {
//        Preferences.importPreferences(new BufferedInputStream(new FileInputStream(tmpFile)));
    }

    public void testGetInstance() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertNotNull(prefs);
        assertSame(prefs, Prefs.getInstance());
    }

    public void testNullValue() throws Exception {
        Prefs prefs = Prefs.getInstance();
        final String key = "test-null-value-key";
        assertNull(prefs.get(key, null));
        prefs.put(key, null);
        assertNull(prefs.get(key, null));

        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add(null);
        list.add(null);
        prefs.putList(key, list);
        list = prefs.getList(key);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    public void testLastDir() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals("/", prefs.getLastDir());
        prefs.setLastDir("/test");
        assertEquals("/test", prefs.getLastDir());
    }

    public void testWindowState() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals(0, prefs.getWindowState());
        prefs.setWindowState(1);
        assertEquals(1, prefs.getWindowState());
    }

    public void testWindowSize() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals(new Dimension(1200, 800), prefs.getWindowSize());
        Dimension d = new Dimension(600, 400);
        prefs.setWindowSize(d);
        assertEquals(d, prefs.getWindowSize());
    }

    public void testWindowLocation() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals(new Point(Integer.MIN_VALUE, Integer.MIN_VALUE), prefs.getWindowLocation());
        Point p = new Point(20, 10);
        prefs.setWindowLocation(p);
        assertEquals(p, prefs.getWindowLocation());
    }

    public void testSyncTime() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals(0, prefs.getSyncTime());
        prefs.setSyncTime(12345L);
        assertEquals(12345L, prefs.getSyncTime());
    }

    public void testSyncValue() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals("", prefs.getSyncValue());
        prefs.setSyncValue("sync_value");
        assertEquals("sync_value", prefs.getSyncValue());
    }

    public void testFilterPatternHistory() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals(0, prefs.getFilterPatternHistory().size());
        List<String> lst = new ArrayList<String>();
        for (int i = 0; i < Prefs.MAX_LIST_SIZE + 10; i++) {
            lst.add("value-" + i);
        }
        prefs.setFilterPatternHistory(lst);
        List<String> newLst = prefs.getFilterPatternHistory();
        assertEquals(Prefs.MAX_LIST_SIZE, newLst.size());
        for (int i = 0; i < Prefs.MAX_LIST_SIZE; i++) {
            assertEquals(lst.get(i), newLst.get(i));
        }
    }

    public void testSearchTextHistory() throws Exception {
        Prefs prefs = Prefs.getInstance();
        assertEquals(0, prefs.getSearchTextHistory().size());
        List<String> lst = new ArrayList<String>();
        for (int i = 0; i < Prefs.MAX_LIST_SIZE + 10; i++) {
            lst.add("value-" + i);
        }
        prefs.setSearchTextHistory(lst);
        List<String> newLst = prefs.getSearchTextHistory();
        assertEquals(Prefs.MAX_LIST_SIZE, newLst.size());
        for (int i = 0; i < Prefs.MAX_LIST_SIZE; i++) {
            assertEquals(lst.get(i), newLst.get(i));
        }
    }

}
