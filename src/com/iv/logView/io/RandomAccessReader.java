package com.iv.logView.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RandomAccessReader extends Reader {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final FileChannel fc;
    private final ByteBuffer bytes;
    private final char[] buffer;

    private int bufferPos = 0;
    private int bufferEnd = 0;
    private long raPtrPos = 0;

    private boolean atEOF = false;

    /**
     * Creates a new <code>RandomAccessReader</code> wrapping the
     * <code>File</code> and using a default-sized buffer (8192 bytes).
     *
     * @param file a <code>File</code> to wrap.
     * @throws IOException if an error occurs.
     */
    public RandomAccessReader(File file) throws IOException {
        this(file, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new <code>RandomAccessReader</code> wrapping the
     * <code>RandomAccessFile</code> and using a buffer of the specified getRowCount.
     *
     * @param file    a <code>File</code> to wrap.
     * @param bufSize an <code>int</code> buffer size.
     */
    public RandomAccessReader(File file, int bufSize) throws IOException {
        fc = new RandomAccessFile(file, "r").getChannel();
        buffer = new char[bufSize];
        bytes = ByteBuffer.allocateDirect(bufSize);
        resetBuffer();
    }

    /**
     * <code>close</code> closes the underlying
     * <code>RandomAccessFile</code>.
     *
     * @throws IOException if an error occurs.
     */
    public void close() throws IOException {
        fc.close();
    }

    /**
     * <code>length</code> returns the length of the underlying
     * <code>RandomAccessFile</code>.
     *
     * @return a <code>long</code>.
     * @throws IOException if an error occurs.
     */
    public long length() throws IOException {
        return fc.size();
    }

    /**
     * <code>read</code> reads one byte from the underlying
     * <code>RandomAccessFile</code>.
     *
     * @return an <code>int</code>, -1 if the end of the stream has
     *         been reached.
     * @throws IOException if an error occurs.
     */
    public final int read() throws IOException {
        if (atEOF) return -1;

        if (bufferPos >= bufferEnd) {
            if (fill() < 0) return -1;
        }

        return (bufferEnd == 0) ? -1 : buffer[bufferPos++];
    }

    /**
     * <code>read</code> reads from the underlying
     * <code>RandomAccessFile</code> into an array.
     *
     * @param cbuf a <code>char []</code> array to read into.
     * @param off  an <code>int</code> offset in the array at which to
     *             start storing chars.
     * @param len  an <code>int</code> maximum number of char to read.
     * @return an <code>int</code> number of chars read, or -1 if the
     *         end of the stream has been reached.
     * @throws IOException if an error occurs.
     */
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (atEOF) return -1;

        int remainder = bufferEnd - bufferPos;

        // If there are enough chars in the buffer to handle this
        // call, use those
        if (len <= remainder) {
            System.arraycopy(buffer, bufferPos, cbuf, off, len);
            bufferPos += len;
            return len;
        }

        // Otherwise start getting more chars from the delegate
        for (int i = 0; i < len; i++) {
            // Read from our own method which checks the buffer
            // first
            int c = read();

            if (c != -1) {
                cbuf[off + i] = (char) c;
            } else {
                // Need to remember that EOF was reached to return -1
                // next read
                atEOF = true;

                return i;
            }
        }

        return len;
    }

    /**
     * <code>getFilePointer</code> returns the effective position of
     * the pointer in the underlying <code>RandomAccessFile</code>.
     *
     * @return a <code>long</code> offset.
     */
    public long getFilePointer() {
        return raPtrPos - bufferEnd + bufferPos;
    }

    /**
     * <code>seek</code> moves the pointer to the specified position.
     *
     * @param pos a <code>long</code> offset.
     * @throws IOException if an error occurs.
     */
    public void seek(long pos) throws IOException {
        // If we seek backwards after reaching EOF, we are no longer at EOF.
        if (pos < fc.size())
            atEOF = false;

        int p = (int) (raPtrPos - pos);

        // Check if we can seek within the buffer
        if (p >= 0 && p <= bufferEnd) {
            bufferPos = bufferEnd - p;
        } else {
            // Otherwise delegate to do a "real" seek and clean the dirty buffer
            fc.position(pos);
            resetBuffer();
        }
    }

    /**
     * Fills the buffer from the <code>RandomAccessFile</code>
     *
     * @return an <code>int</code>.
     * @throws IOException if an error occurs.
     */
    private int fill() throws IOException {
        // Read bytes from random access delegate
        bytes.clear();
        int b = fc.read(bytes);
        bytes.rewind();

        // Copy and cast bytes read to char buffer
        ByteBuffer src = bytes;
        char[] dst = buffer;
        for (int i = b; --i >= 0;) {
            dst[i] = (char) src.get(i);
        }

        // If read any bytes
        if (b >= 0) {
            raPtrPos += b;
            bufferPos = 0;
            bufferEnd = b;
        }

        // Return number bytes read
        return b;
    }

    /**
     * <code>resetBuffer</code> resets the buffer when the pointer
     * leaves its boundaries.
     *
     * @throws IOException if an error occurs.
     */
    private void resetBuffer() throws IOException {
        bufferPos = 0;
        bufferEnd = 0;
        raPtrPos = fc.position();
    }

    public final String readLine() throws IOException {
        StringBuilder input = new StringBuilder();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = getFilePointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }

        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }

}
