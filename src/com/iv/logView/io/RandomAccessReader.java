package com.iv.logView.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class RandomAccessReader extends Reader {

    private final FileChannel channel;
    private final MappedByteBuffer byteBuffer;

    /**
     * Creates a new <code>RandomAccessReader</code> wrapping the
     * <code>File</code> and using a default-sized buffer (32768 bytes).
     *
     * @param file a <code>File</code> to wrap.
     * @throws IOException if an error occurs.
     */
    public RandomAccessReader(File file) throws IOException {
        if (file.length() > Integer.MAX_VALUE) {
            throw new IOException("File to large");
        }
        channel = new RandomAccessFile(file, "r").getChannel();
        byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        byteBuffer.load();
    }

    /**
     * <code>close</code> closes the underlying
     * <code>RandomAccessFile</code>.
     *
     * @throws IOException if an error occurs.
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }

    /**
     * <code>length</code> returns the length of the underlying
     * <code>RandomAccessFile</code>.
     *
     * @return a <code>int</code>.
     */
    public int length() {
        return byteBuffer.limit();
    }

    /**
     * <code>read</code> reads one byte from the underlying
     * <code>RandomAccessFile</code>.
     *
     * @return an <code>int</code>, -1 if the end of the stream has
     *         been reached.
     * @throws IOException if an error occurs.
     */
    @Override
    public final int read() throws IOException {
        if (atEoF()) return -1;
        return byteBuffer.get();
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
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            // Read from our own method which checks the buffer
            // first
            int c = read();
            if (c != -1) {
                cbuf[off + i] = (char) c;
            } else {
                return i;
            }
        }
        return len;
    }

    /**
     * this method returns the effective position of
     * the pointer in the underlying <code>RandomAccessFile</code>.
     *
     * @return a <code>long</code> offset.
     */
    public int getFilePointer() {
        return byteBuffer.position();
    }

    /**
     * <code>seek</code> moves the pointer to the specified position.
     *
     * @param pos a <code>long</code> offset.
     * @throws IOException if pos is less than 0 or if an I/O error occurs
     */
    public void seek(int pos) throws IOException {
        if (pos < 0) throw new IOException("Seek position is negative");
        byteBuffer.position(StrictMath.min(pos, length()));
    }

    @Override
    public long skip(long n) throws IOException {
        int oldPos = getFilePointer();
        seek(oldPos + (int) n);
        return getFilePointer() - oldPos;
    }

    public final String readLine() throws IOException {
        StringBuilder input = new StringBuilder(128);
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
                    int cur = getFilePointer();
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

    private boolean atEoF() {
        return byteBuffer.position() >= byteBuffer.limit();
    }
}
