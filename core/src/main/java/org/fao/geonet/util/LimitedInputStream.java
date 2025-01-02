package org.fao.geonet.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Like a {@link org.apache.commons.io.input.BoundedInputStream} but includes a method to check if the limit has been reached.
 */
public class LimitedInputStream extends InputStream {
    private final InputStream in;
    private final long max;
    private long pos;
    private long mark;
    private boolean propagateClose;
    private boolean limitReached;

    /**
     * Creates a new instance.
     *
     * @param in   the input stream
     * @param size the maximum number of bytes to read
     */
    public LimitedInputStream(InputStream in, long size) {
        this.pos = 0L;
        this.mark = -1L;
        this.propagateClose = true;
        this.max = size;
        this.in = in;
    }

    /**
     * Creates a new instance.
     *
     * @param in the input stream
     */
    public LimitedInputStream(InputStream in) {
        this(in, -1L);
    }

    /**
     * {@inheritDoc}
     *
     * <p>In this implementation if the limit has been reached, returns -1.</p>
     */
    @Override
    public int read() throws IOException {
        if (this.max >= 0L && this.pos >= this.max) {
            this.limitReached = true;
            return -1;
        } else {
            int result = this.in.read();
            ++this.pos;
            return result;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>In this implementation if the limit has been reached, returns -1.</p>
     */
    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     *
     * <p>In this implementation if the limit has been reached, returns -1.</p>
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.max >= 0L && this.pos >= this.max) {
            this.limitReached = true;
            return -1;
        } else {
            long maxRead = this.max >= 0L ? Math.min((long)len, this.max - this.pos) : (long)len;
            int bytesRead = this.in.read(b, off, (int)maxRead);
            if (bytesRead == -1) {
                return -1;
            } else {
                this.pos += (long)bytesRead;
                return bytesRead;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>In this implementation the number of bytes skipped is limited by the maximum number of bytes to read.</p>
     */
    @Override
    public long skip(long n) throws IOException {
        long toSkip = this.max >= 0L ? Math.min(n, this.max - this.pos) : n;
        long skippedBytes = this.in.skip(toSkip);
        this.pos += skippedBytes;
        return skippedBytes;
    }

    /**
     * {@inheritDoc}
     *
     * <p>In this implementation the number of bytes available is limited by the maximum number of bytes to read.</p>
     */
    @Override
    public int available() throws IOException {
        return this.max >= 0L && this.pos >= this.max ? 0 : this.in.available();
    }

    /**
     * Returns whether the limit has been reached.
     *
     * @return {@code true} if the limit has been reached, {@code false} otherwise
     */
    public boolean isLimitReached() {
        return this.limitReached;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.in.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (this.propagateClose) {
            this.in.close();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
        this.pos = this.mark;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
        this.mark = this.pos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }
}
