package com.iv.logView.xml;

import com.iv.logView.model.LogColumnModel;
import com.iv.logView.model.LogTableColumnModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class LogFormatReader {

    private static Schema schema;
    private File file;
    public LogTableColumnModel tblColumnModel;

    public LogFormatReader(File file) {
        this.file = file;
    }

    public LogTableColumnModel read() throws LogFormatException {
        LogHandler handler;
        try {
            if (schema == null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Source schemaSrc = new StreamSource(ClassLoader.getSystemResourceAsStream("log-format.xsd"));
                schema = schemaFactory.newSchema(schemaSrc);
            }

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setSchema(schema);
            saxParserFactory.setNamespaceAware(true);
            SAXParser parser = saxParserFactory.newSAXParser();
            handler = new LogHandler();
            parser.parse(new FileInputStream(file), handler);
        } catch (Exception e) {
            throw new LogFormatException(e);
        }
        return tblColumnModel;
    }

    public class LogHandler extends DefaultHandler {

        // tag names
        private static final String T_NAME = "name";
        private static final String T_PATTERN = "pattern";
        private static final String T_COLUMN = "column";
        private static final String T_HIGHLIGHT = "highlight";
        private static final String T_MSG_COLUMN = "message-column";
        // attribute names
        private static final String A_NAME = T_NAME;
        private static final String A_GROUP = "group";
        private static final String A_INDEXABLE = "indexable";
        private static final String A_VALUE = "value";
        private static final String A_COLOR = "color";

        private final StringBuilder buf = new StringBuilder();
        private int colIndex = 0;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            buf.setLength(0);
            if (T_COLUMN.equals(qName)) {
                LogColumnModel col = new LogColumnModel(
                        colIndex++,
                        attributes.getValue(A_NAME),
                        Integer.parseInt(attributes.getValue(A_GROUP)),
                        Boolean.valueOf(attributes.getValue(A_INDEXABLE))
                );
                tblColumnModel.addColumn(col);
            } else if (T_HIGHLIGHT.equals(qName)) {
                LogColumnModel col = (LogColumnModel) tblColumnModel.getColumn(tblColumnModel.getColumnCount() - 1);
                col.addHighlighting(
                        attributes.getValue(A_VALUE),
                        Color.decode(attributes.getValue(A_COLOR))
                );
            } else if (T_MSG_COLUMN.equals(qName)) {
                LogColumnModel col = new LogColumnModel(
                        colIndex++,
                        attributes.getValue(A_NAME),
                        Integer.parseInt(attributes.getValue(A_GROUP)),
                        false
                );
                tblColumnModel.addColumn(col, true);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (T_NAME.equals(qName)) {
                //do nothing
            } else if (T_PATTERN.equals(qName)) {
                String pattern = buf.toString().trim();
                tblColumnModel = new LogTableColumnModel(pattern);
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            buf.append(ch, start, length);
        }

        public void warning(SAXParseException e) throws SAXException {
            System.err.println(e.toString());
        }

        public void error(SAXParseException e) throws SAXException {
            System.err.println(e.toString());
        }

        public void fatalError(SAXParseException e) throws SAXException {
            System.err.println(e.toString());
        }

    }

}
