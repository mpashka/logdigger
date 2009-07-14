package com.iv.logView.xml;

import com.iv.logView.logging.Log;
import com.iv.logView.logging.LogFactory;
import com.iv.logView.model.LogColumnModel;
import com.iv.logView.model.LogTableColumnModel;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
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
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LogFormatReader {

    private static final Log log = LogFactory.getLogger(LogFormatReader.class);
    private static Schema schema;
    private InputStream inputStream;
    public LogTableColumnModel tblColumnModel;

    public LogFormatReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public LogTableColumnModel read() throws LogFormatException {
        try {
            if (schema == null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Source schemaSrc = new StreamSource(getClass().getClassLoader().getResourceAsStream("log-format.xsd"));
                schema = schemaFactory.newSchema(schemaSrc);
            }

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setSchema(schema);
            saxParserFactory.setNamespaceAware(true);
            SAXParser parser = saxParserFactory.newSAXParser();
            parser.parse(new BufferedInputStream(inputStream), new LogHandler());
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

        private int colIndex = 0;
        private Locator locator;
        private final StringBuilder buf = new StringBuilder();

        @Override
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

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (T_NAME.equals(qName)) {
                //do nothing
            } else if (T_PATTERN.equals(qName)) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(buf.toString().trim(), Pattern.DOTALL);
                } catch (PatternSyntaxException e) {
                    throw new SAXParseException(e.getMessage(), locator, e);
                }
                tblColumnModel = new LogTableColumnModel(pattern);
            }
        }


        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            buf.append(ch, start, length);
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            log.warning(e.toString(), e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            log.error(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            log.error(e);
        }

    }

}
