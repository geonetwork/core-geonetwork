package org.fao.geonet.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {
    private final InputStream in;
    private final long max;
    private long pos;
    private long mark;
    private boolean propagateClose;

    public LimitedInputStream(InputStream in, long size) {
        this.pos = 0L;
        this.mark = -1L;
        this.propagateClose = true;
        this.max = size;
        this.in = in;
    }

    public LimitedInputStream(InputStream in) {
        this(in, -1L);
    }

    public int read() throws IOException {
        if (this.max >= 0L && this.pos >= this.max) {
            return -1;
        } else {
            int result = this.in.read();
            ++this.pos;
            return result;
        }
    }

    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.max >= 0L && this.pos >= this.max) {
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

    public long skip(long n) throws IOException {
        long toSkip = this.max >= 0L ? Math.min(n, this.max - this.pos) : n;
        long skippedBytes = this.in.skip(toSkip);
        this.pos += skippedBytes;
        return skippedBytes;
    }

    public int available() throws IOException {
        return this.max >= 0L && this.pos >= this.max ? 0 : this.in.available();
    }

    public boolean isLimitReached() {
        return this.pos >= this.max;
    }

    public String toString() {
        return this.in.toString();
    }

    public void close() throws IOException {
        if (this.propagateClose) {
            this.in.close();
        }

    }

    public synchronized void reset() throws IOException {
        this.in.reset();
        this.pos = this.mark;
    }

    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
        this.mark = this.pos;
    }

    public boolean markSupported() {
        return this.in.markSupported();
    }

    public boolean isPropagateClose() {
        return this.propagateClose;
    }

    public void setPropagateClose(boolean propagateClose) {
        this.propagateClose = propagateClose;
    }
}
