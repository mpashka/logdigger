package com.iv.logView.io;

import junit.framework.TestCase;

import java.io.*;

public class TestIO extends TestCase {

    private File file;

    protected void setUp() throws Exception {
        file = new File("test.log");
    }

    public void testLogReader() throws Exception {
        LogReader reader = new FilteredLogReader(file, null);
        reader.reload();

        assertEquals(reader.getRowCount(), 2);
        
    }

    public void testRandomAccessReader() throws Exception {

        RandomAccessReader rar = new RandomAccessReader(file);

        assertEquals(rar.getFilePointer(), 0L);

        rar.seek(0);
        assertEquals(rar.getFilePointer(), 0L);

        assertEquals(rar.read(), '1');
        assertEquals(rar.read(), '1');

        rar.seek(0);
        String str;

        str = rar.readLine();
        assertNotNull(str);
        assertTrue(str.startsWith("11"));

        str = rar.readLine();
        assertNotNull(str);
        assertTrue(str.startsWith("22"));

        str = rar.readLine();
        assertNull(str);

        str = rar.readLine();
        assertNull(str);

        rar.close();

    }

}
