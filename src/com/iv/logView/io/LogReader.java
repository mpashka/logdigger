package com.iv.logView.io;

import com.iv.logView.logging.Log;
import com.iv.logView.logging.LogFactory;
import com.iv.logView.model.LogColumnModel;
import com.iv.logView.model.LogTableColumnModel;
import com.iv.logView.xml.LogFormatReader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//todo think about synchronization
public class LogReader {

    private static final Log log = LogFactory.getLogger(LogReader.class);

    private final Pattern pattern;
    private final File file;
    private final RandomAccessReader dataIn;
    private final List<IndexRecord> index = new ArrayList<IndexRecord>();
    private final Map<LogColumnModel, Set<String>> idxMap = new HashMap<LogColumnModel, Set<String>>();
    private final LogTableColumnModel tblColumnModel;
    private final ColumnCache columnCache = new ColumnCache();
    private ProgressListener progressListener;

    protected LogReader(File file, ProgressListener progressListener) throws IOException {
        if (progressListener != null) {
            setProgressListener(progressListener);
        }
        this.file = file;
        tblColumnModel = recognizeLogFormat(file);
        pattern = tblColumnModel.getPattern();
        dataIn = new RandomAccessReader(file);
        reload();
    }

    public synchronized void reload() throws IOException {
            columnCache.invalidate();
            skipInvalidChars(dataIn);
            createIndex();
    }

    public synchronized void close() {
        try {
            dataIn.close();
        } catch (IOException e) {
            //ignore
        }
    }

    public File getFile() {
        return file;
    }

    private LogTableColumnModel recognizeLogFormat(File file) throws IOException {
        final RandomAccessReader rar = new RandomAccessReader(file);
        final String str;
        try {
            skipInvalidChars(rar);
            str = rar.readLine();
        } finally {
            rar.close();
        }
        if (str == null) {
            throw new IOException("Log file is empty");
        }

        final List<URL> urls = new LinkedList<URL>();
        new File(System.getProperty("xml.dir", "./xml")).listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".xml")) {
                    try {
                        urls.add(f.toURI().toURL());
                    } catch (MalformedURLException e) {
                        // ignore
                    }
                }
                return true;
            }
        });
        addDefaultFiles(urls);
        for (URL u : urls) {
            log.info("Trying format '" + u.getFile() + "'");
            LogFormatReader reader = new LogFormatReader(u.openStream());
            LogTableColumnModel model = reader.read();
            Matcher m = model.getPattern().matcher(str);
            if (m.find()) {
                log.info("Format '" + u.getFile() + "' is OK");
                return model;
            }
        }
        throw new IOException("Can not recognize log format");
    }

    private void addDefaultFiles(List<URL> urls) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("META-INF/xml-list.properties");
        Properties props = new Properties();
        props.load(stream);
        String lst = props.getProperty("xml-list");
        for (StringTokenizer tok = new StringTokenizer(lst, ",", false); tok.hasMoreElements();) {
            URL url = getClass().getClassLoader().getResource(tok.nextToken());
            if (url != null) {
                urls.add(url);
            }
        }
    }

    /**
     * skip characters that great or equal to 0 and less then 0x20
     *
     * @param rar reader
     * @throws IOException if error
     */
    private void skipInvalidChars(RandomAccessReader rar) throws IOException {
        rar.seek(0);
        if (rar.length() == 0) {
            return;
        }
        int ch;
        do {
            ch = rar.read();
        } while ((ch & 0x1f) == ch);
        rar.seek(rar.getFilePointer() - 1);
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    private synchronized void createIndex() throws IOException {
        if (progressListener != null) {
            progressListener.onBegin();
        }
        StringCache sc = new StringCache();
        idxMap.clear();
        index.clear();

        for (int i = 0; i < tblColumnModel.getColumnCount(); i++) {
            LogColumnModel cm = (LogColumnModel) tblColumnModel.getColumn(i);
            if (cm.isIndexable()) {
                idxMap.put(cm, new TreeSet<String>());
            }
        }

        int pos = dataIn.getFilePointer();
        String str;
        final Matcher m = pattern.matcher("");
        while ((str = dataIn.readLine()) != null) {
            if (progressListener != null) {
                progressListener.onProgress((int) (100f * pos / dataIn.length()));
            }
            if (m.reset(str).matches()) {
                final IndexRecord lastRec = getLastRecord();
                if (lastRec != null) {
                    lastRec.size = pos - lastRec.position;
                }
                final IndexRecord rec = new IndexRecord(pos);
                for (Map.Entry<LogColumnModel, Set<String>> entry : idxMap.entrySet()) {
                    String value = sc.get(m.group(entry.getKey().getGroup()));
                    entry.getValue().add(value);
                    rec.setValue(entry.getKey(), value);
                }
                index.add(rec);
            }
            pos = dataIn.getFilePointer();
        }
        if (progressListener != null) {
            progressListener.onEnd();
        }
        final IndexRecord lastRec = getLastRecord();
        if (lastRec != null) {
            lastRec.size = dataIn.getFilePointer() - lastRec.position;
        }
    }

    public synchronized Map<LogColumnModel, Set<String>> getIdx() {
        return Collections.unmodifiableMap(idxMap);
    }

    public synchronized int getRowCount() {
        return getIndex().size();
    }

    public synchronized String get(int rowIdx) throws IOException {
        final IndexRecord rec = getIndex().get(rowIdx);
        return get(rec);
    }

    public synchronized String get(int rowIdx, int group) throws IOException {
        return columnCache.getValue(rowIdx, group - 1);
    }

    public synchronized String get(IndexRecord rec, int group) throws IOException {
        return columnCache.getValue(rec, group - 1);
    }

    public LogTableColumnModel getTableColumnModel() {
        return tblColumnModel;
    }

    protected synchronized String get(IndexRecord rec) throws IOException {
        dataIn.seek(rec.position);
        char[] inBuf = new char[rec.size];
        char[] outBuf = new char[rec.size];
        dataIn.read(inBuf);
        int skip = 0;
        for (int i = 0; i < inBuf.length; i++) {
            if (inBuf[i] == '\r') {
                skip++;
            } else {
                outBuf[i - skip] = inBuf[i];
            }
        }
        return new String(outBuf, 0, outBuf.length - skip);
    }

    public synchronized RowId getId(int rowIdx) {
        return getIndex().get(rowIdx);
    }

    public synchronized int findRow(RowId id) {
        return Collections.binarySearch(getIndex(), id);
    }

    public synchronized int findNearestRow(final LogColumnModel model, final String value) {
        final int group = model.getGroup();
        int rowIdx = Collections.binarySearch(getIndex(), null, new Comparator<IndexRecord>() {
            @Override
            public int compare(IndexRecord o1, IndexRecord o2) {
                try {
                    return get(o1, group).compareTo(value);
                } catch (IOException e) {
                    return 0; //stop search
                }
            }
        });
        if (rowIdx < 0) {
            return -rowIdx - 1;
        }
        try {
            while (rowIdx > 0 && value.equals(get(rowIdx - 1, group))) {
                rowIdx--;
            }
        } catch (IOException e) {
            // ignore            
        }
        return rowIdx;
    }

    public int findNearestRow(RowId id) {
        int rowNum = findRow(id);
        if (rowNum < 0) {
            rowNum = (-rowNum) - 1;
        }
        return rowNum;
    }

    private IndexRecord getLastRecord() {
        final int last = index.size() - 1;
        return last < 0 ? null : index.get(last);
    }

    protected List<IndexRecord> getIndex() {
        return index;
    }

    protected static class IndexRecord implements RowId {
        private final int position;
        private int size;
        private final Map<LogColumnModel, String> values = new HashMap<LogColumnModel, String>();

        public IndexRecord(int position) {
            this.position = position;
        }

        public void setValue(LogColumnModel columnModel, String value) {
            values.put(columnModel, value);
        }

        public String getValue(LogColumnModel columnModel) {
            return values.get(columnModel);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof IndexRecord && ((IndexRecord) obj).position == this.position;
        }

        @Override
        public int hashCode() {
            return position;
        }

        @Override
        public int compareTo(RowId other) {
            long thisVal = this.position;
            long anotherVal = ((IndexRecord) other).position;
            return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
        }

    }

    private static class StringCache {
        private final Map<String, String> strCache = new HashMap<String, String>();

        public String get(String str) {
            String value = strCache.get(str);
            if (value == null) {
                strCache.put(str, str);
                return str;
            } else {
                return value;
            }
        }
    }

    private class ColumnCache {
        private RowId cachedRowId;
        private final List<String> columns = new ArrayList<String>();

        public String getValue(IndexRecord rec, int col) throws IOException {
            if (cachedRowId != null && cachedRowId.equals(rec)) {
                return columns.get(col);
            }
            String str = LogReader.this.get(rec);
            Matcher m = pattern.matcher(str);
            if (m.find()) {
                columns.clear();
                for (int i = 1; i <= m.groupCount(); i++) {
                    columns.add(m.group(i));
                }
                cachedRowId = rec;
                return columns.get(col);
            }
            return "???";
        }

        public String getValue(int row, int col) throws IOException {
            if (row < 0 || row >= getRowCount()) {
                throw new IOException("Row index out of bounds. index:" + row + " size:" + getRowCount());
            }
            IndexRecord rec = getIndex().get(row);
            return getValue(rec, col);
        }

        public synchronized void invalidate() {
            cachedRowId = null;
            columns.clear();
        }
    }

}
