package com.iv.logView.io;

import com.iv.logView.model.LogColumnModel;
import com.iv.logView.model.LogTableColumnModel;
import com.iv.logView.xml.LogFormatReader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LogReader {

    public static final String[] KNOWN_TYPES = new String[]{"smp", "slee", "sleeNew", "guard"};

    private final Pattern pattern;
    private final RandomAccessReader dataIn;
    private final List<IndexRecord> index = new ArrayList<IndexRecord>();
    private final Map<LogColumnModel, Set<String>> idxMap = new HashMap<LogColumnModel, Set<String>>();
    private final LogTableColumnModel tblColumnModel;
    private final ColumnCache columnCache = new ColumnCache();


    public LogReader(File file) throws IOException {
        tblColumnModel = recognizeLogFormat(file);
        pattern = Pattern.compile(tblColumnModel.getPattern(), Pattern.DOTALL);
        dataIn = new RandomAccessReader(file, 0x8000);
        reload();
    }

    public void reload() throws IOException {
        columnCache.invalidate();
        skipInvalidChars(dataIn);
        createIndex();
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

        File[] files = new File(System.getProperty("xml.dir", "./xml")).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });

        if (files != null) {
            for (File f : files) {
                System.out.println("Trying format '" + f.getName() + "'");
                LogFormatReader reader = new LogFormatReader(f);
                LogTableColumnModel tblColumnModel = reader.read();
                Matcher m = Pattern.compile(tblColumnModel.getPattern()).matcher(str);
                if (m.find()) {
                    System.out.println("Format '" + f.getName() + "' is OK");
                    return tblColumnModel;
                }
            }
        }
        throw new IOException("Can not recognize log format");
    }

    /**
     * skip characters that great or equal to 0 and less then 0x20
     *
     * @throws IOException
     */
    private void skipInvalidChars(RandomAccessReader rar) throws IOException {
        rar.seek(0);
        int ch;
        do {
            ch = rar.read();
        } while (ch > -1 && ch < 0x20);
        rar.seek(rar.getFilePointer() - 1);
    }


    private void createIndex() throws IOException {
        StringCache sc = new StringCache();
        idxMap.clear();
        index.clear();

        for (int i = 0; i < tblColumnModel.getColumnCount(); i++) {
            LogColumnModel cm = (LogColumnModel) tblColumnModel.getColumn(i);
            if (cm.isIndexable()) {
                idxMap.put(cm, new TreeSet<String>());
            }
        }

        long pos = dataIn.getFilePointer();

        String str;
        final Matcher m = pattern.matcher("");
        while ((str = dataIn.readLine()) != null) {
            m.reset(str);
            if (m.matches()) {
                final IndexRecord lastRec = getLastRecord();
                if (lastRec != null) {
                    lastRec.size = (int) (pos - lastRec.position);
                }
                IndexRecord rec = new IndexRecord(pos);

                for (Map.Entry<LogColumnModel, Set<String>> entry : idxMap.entrySet()) {
                    String value = sc.get(m.group(entry.getKey().getGroup()));
                    entry.getValue().add(value);
                    rec.setValue(entry.getKey(), value);
                }
                index.add(rec);
            }
            pos = dataIn.getFilePointer();
        }
        final IndexRecord lastRec = getLastRecord();
        if (lastRec != null) {
            lastRec.size = (int) (dataIn.getFilePointer() - lastRec.position);
        }
    }

    public Map<LogColumnModel, Set<String>> getIdx() {
        return Collections.unmodifiableMap(idxMap);
    }

    public int getRowCount() {
        return getIndex().size();
    }

    public String get(int rowIdx) throws IOException {
        final IndexRecord rec = getIndex().get(rowIdx);
        return get(rec);
    }

    public String get(int rowIdx, int group) throws IOException {
        return columnCache.getValue(rowIdx, group - 1);
    }

    public String get(IndexRecord rec, int group) throws IOException {
        return columnCache.getValue(rec, group - 1);
    }

    public LogTableColumnModel getTableColumnModel() {
        return tblColumnModel;
    }

    protected String get(IndexRecord rec) throws IOException {
        dataIn.seek(rec.position);
        char[] buf = new char[rec.size];
        dataIn.read(buf);
        return new String(buf);
    }

    public RowId getId(int rowIdx) {
        return getIndex().get(rowIdx);
    }

    public int findRow(RowId id) {
        return Collections.binarySearch(getIndex(), id);
    }

    public int findNearestRow(final LogColumnModel model, final String value) {
        final int group = model.getGroup();
        int rowIdx = Collections.binarySearch(getIndex(), null, new Comparator<IndexRecord>() {
            public int compare(IndexRecord o1, IndexRecord o2) {
                try {
                    return get(o1, group).compareTo(value);
                } catch (IOException e) {
                    return 0; //stop search
                }
            }
        });
        if (rowIdx < 0) {
            return - rowIdx - 1;
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
        return index.size() > 0 ? index.get(index.size() - 1) : null;
    }

    protected List<IndexRecord> getIndex() {
        return index;
    }

    protected static class IndexRecord implements RowId {
        private final long position;
        private int size;
        private final Map<LogColumnModel, String> values = new HashMap<LogColumnModel, String>();

        public IndexRecord(long position) {
            this.position = position;
        }

        public void setValue(LogColumnModel columnModel, String value) {
            values.put(columnModel, value);
        }

        public String getValue(LogColumnModel columnModel) {
            return values.get(columnModel);
        }

        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof IndexRecord) {
                IndexRecord that = (IndexRecord) obj;
                return that.position == this.position;
            }
            return false;
        }

        public int hashCode() {
            return (int) (position ^ (position >>> 32));
        }

        public int compareTo(RowId o) {
            long thisVal = this.position;
            long anotherVal = ((IndexRecord) o).position;
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
            IndexRecord rec = getIndex().get(row);
            return getValue(rec, col);
        }

        public void invalidate() {
            cachedRowId = null;
            columns.clear();
        }
    }

}
