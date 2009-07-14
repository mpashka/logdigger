package com.iv.logView.io;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;

public class TestRandomAccessReader extends TestCase {

    static final String BIG_FILE_NAME = "test-big.log";

    public TestRandomAccessReader() throws Exception {
        File file = new File(BIG_FILE_NAME);
        PrintWriter pw = new PrintWriter(file);
        Calendar c = new GregorianCalendar(1970, 1, 1, 0, 0, 0);
        for (int i = 0; i < 10000; i++) {
            pw.printf("%tT,000 [thread_1] level_1 category_1 : message #%d\r\n", c.getTime(), i);
            c.add(Calendar.SECOND, 1);
        }
        pw.close();
    }

    public void testOpenClose() throws Exception {
        try {
            new RandomAccessReader(new File("file" + System.nanoTime()));
            fail("file not exists");
        } catch (IOException e) {
            // expected exception
        }

        RandomAccessReader rar = new RandomAccessReader(new File(BIG_FILE_NAME));
        rar.close();
    }

    public void testNegativeSeek() throws Exception {
        RandomAccessReader rar = new RandomAccessReader(new File(BIG_FILE_NAME));

        int pos = rar.getFilePointer();
        assertEquals(0, pos);
        try {
            rar.seek(-1);
            fail("negative seek");
        } catch (IOException e) {
            // expected
        }
        assertEquals(rar.getFilePointer(), pos);
        rar.close();
    }

    public void testRead() throws Exception {
        RandomAccessReader rar = new RandomAccessReader(new File(BIG_FILE_NAME));

        assertEquals(rar.read(), '0');
        assertEquals(rar.read(), '0');
        assertEquals(rar.read(), ':');
        assertEquals(rar.read(), '0');
        assertEquals(rar.read(), '0');

        rar.seek(0);

        rar.close();
    }

}
