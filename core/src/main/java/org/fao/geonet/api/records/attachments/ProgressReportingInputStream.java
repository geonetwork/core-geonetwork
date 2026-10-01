/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */
package org.fao.geonet.api.records.attachments;

import org.fao.geonet.util.KnownSizeInputStream;

import javax.annotation.Nonnull;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * An {@link InputStream} wrapper that reports the number of bytes read so far
 * to a {@link ResourceUploadProgressListener} as the stream is consumed and
 * performs cooperative cancellation checks through the supplied listener.
 */
public class ProgressReportingInputStream extends FilterInputStream implements KnownSizeInputStream {

    private final ResourceUploadProgressListener listener;
    private final long totalBytes;
    private long bytesTransferred = 0;

    /**
     * Creates a stream that reports cumulative bytes read and checks for
     * cooperative cancellation.
     *
     * @param in wrapped source stream
     * @param totalBytes expected source size, or {@code -1} when unknown
     * @param listener non-null listener receiving progress, cancellation, and
     *                 stream-lifecycle callbacks
     */
    public ProgressReportingInputStream(InputStream in, long totalBytes, @Nonnull ResourceUploadProgressListener listener) {
        super(in);
        this.totalBytes = totalBytes;
        this.listener = listener;
    }

    @Override
    public int read() throws IOException {
        checkCancelled();
        int b = super.read();
        checkCancelled();

        if (b != -1) {
            reportProgress(1);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkCancelled();
        int n = super.read(b, off, len);
        checkCancelled();

        if (n > 0) {
            reportProgress(n);
        }
        return n;
    }

    private void checkCancelled() throws InterruptedIOException {
        if (listener.isCancelled() || Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("Resource upload was cancelled.");
        }
    }

    private void reportProgress(int n) {
        bytesTransferred += n;
        listener.onProgress(bytesTransferred, totalBytes);
    }

    /**
     * Returns the number of bytes successfully consumed from the wrapped stream.
     *
     * @return cumulative number of bytes read
     */
    public long getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * @return the total/expected size of the stream in bytes as given to this wrapper's
     * constructor, if known, and unless the wrapped stream itself reports a (more authoritative)
     * known size (eg. it is/wraps a {@link KnownSizeInputStream} such as {@link org.fao.geonet.util.LimitedInputStream}).
     * Returns {@code -1} if unknown.
     */
    @Override
    public long getKnownSize() {
        if (in instanceof KnownSizeInputStream) {
            long wrappedSize = ((KnownSizeInputStream) in).getKnownSize();
            if (wrappedSize >= 0) {
                return wrappedSize;
            }
        }
        return totalBytes;
    }


    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            listener.onStreamClosed(this);
        }
    }
}
