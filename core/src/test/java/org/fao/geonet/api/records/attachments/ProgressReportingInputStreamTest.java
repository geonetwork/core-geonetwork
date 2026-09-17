//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.records.attachments;

import org.fao.geonet.util.LimitedInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ProgressReportingInputStreamTest {

    @Test
    public void reportsCumulativeProgressForBulkReads() throws Exception {
        byte[] data = new byte[1000];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }

        RecordingListener listener = new RecordingListener();

        try (ProgressReportingInputStream input =
                 new ProgressReportingInputStream(
                     new ByteArrayInputStream(data),
                     data.length,
                     listener
                 )) {
            byte[] actual = new byte[data.length];
            byte[] buffer = new byte[128];
            int offset = 0;
            int count;

            while ((count = input.read(buffer)) != -1) {
                System.arraycopy(buffer, 0, actual, offset, count);
                offset += count;
            }

            assertArrayEquals(data, actual);
            assertEquals(data.length, input.getBytesTransferred());
        }

        assertEquals(
            data.length,
            listener.transferred.get(listener.transferred.size() - 1)
                .longValue()
        );

        for (Long total : listener.totals) {
            assertEquals(data.length, total.longValue());
        }
    }

    @Test
    public void reportsCumulativeProgressForSingleByteReads()
        throws Exception {

        byte[] data = {1, 2, 3};
        RecordingListener listener = new RecordingListener();

        try (ProgressReportingInputStream input =
                 new ProgressReportingInputStream(
                     new ByteArrayInputStream(data),
                     data.length,
                     listener
                 )) {
            while (input.read() != -1) {
                // Consume the stream.
            }
        }

        assertEquals(3, listener.transferred.size());
        assertEquals(Long.valueOf(1), listener.transferred.get(0));
        assertEquals(Long.valueOf(2), listener.transferred.get(1));
        assertEquals(Long.valueOf(3), listener.transferred.get(2));
    }

    @Test
    public void doesNotReportProgressAtEndOfStream() throws Exception {
        RecordingListener listener = new RecordingListener();

        try (ProgressReportingInputStream input =
                 new ProgressReportingInputStream(
                     new ByteArrayInputStream(new byte[0]),
                     0,
                     listener
                 )) {
            assertEquals(-1, input.read());
        }

        assertTrue(listener.transferred.isEmpty());
    }

    @Test
    public void stopsBeforeReadingWhenListenerIsCancelled()
        throws Exception {

        RecordingListener listener = new RecordingListener();
        listener.cancelled = true;

        try (ProgressReportingInputStream input =
                 new ProgressReportingInputStream(
                     new ByteArrayInputStream(new byte[]{1}),
                     1,
                     listener
                 )) {
            assertThrows(InterruptedIOException.class, input::read);
        }
    }

    @Test
    public void stopsWhenWorkerThreadIsInterrupted() throws Exception {
        RecordingListener listener = new RecordingListener();

        try (ProgressReportingInputStream input =
                 new ProgressReportingInputStream(
                     new ByteArrayInputStream(new byte[]{1}),
                     1,
                     listener
                 )) {
            Thread.currentThread().interrupt();

            try {
                assertThrows(InterruptedIOException.class, input::read);
            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    public void notifiesListenerWhenClosed() throws Exception {
        RecordingListener listener = new RecordingListener();
        ProgressReportingInputStream input =
            new ProgressReportingInputStream(
                new ByteArrayInputStream(new byte[]{1}),
                1,
                listener
            );

        input.close();

        assertSame(input, listener.closedStream);
    }

    @Test
    public void closeNotificationOccursWhenWrappedCloseFails() {
        RecordingListener listener = new RecordingListener();

        ProgressReportingInputStream input =
            new ProgressReportingInputStream(
                new ByteArrayInputStream(new byte[]{1}) {
                    @Override
                    public void close() throws IOException {
                        throw new IOException("close failed");
                    }
                },
                1,
                listener
            );

        assertThrows(IOException.class, input::close);
        assertSame(input, listener.closedStream);
    }

    @Test
    public void usesKnownSizeFromWrappedStream() {
        LimitedInputStream limited =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[10]),
                100,
                10
            );

        ProgressReportingInputStream progress =
            new ProgressReportingInputStream(
                limited,
                -1,
                new RecordingListener()
            );

        assertEquals(10, progress.getKnownSize());
    }

    @Test
    public void usesConstructorSizeWhenWrappedSizeIsUnknown() {
        LimitedInputStream limited =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[10]),
                100,
                -1
            );

        ProgressReportingInputStream progress =
            new ProgressReportingInputStream(
                limited,
                20,
                new RecordingListener()
            );

        assertEquals(20, progress.getKnownSize());
    }

    @Test
    public void resolveExpectedSizeUsesKnownSize() throws Exception {
        LimitedInputStream limited =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[10]),
                100,
                25
            );

        ProgressReportingInputStream progress =
            new ProgressReportingInputStream(
                limited,
                -1,
                new RecordingListener()
            );

        assertEquals(25, AbstractStore.resolveExpectedSize(progress));
    }

    @Test
    public void resolveExpectedSizeFallsBackToAvailable() throws Exception {
        LimitedInputStream limited =
            new LimitedInputStream(
                new ByteArrayInputStream(new byte[10]),
                100,
                -1
            );

        ProgressReportingInputStream progress =
            new ProgressReportingInputStream(
                limited,
                -1,
                new RecordingListener()
            );

        assertEquals(10, AbstractStore.resolveExpectedSize(progress));
    }

    private static final class RecordingListener
        implements ResourceUploadProgressListener {

        private final List<Long> transferred = new ArrayList<>();
        private final List<Long> totals = new ArrayList<>();
        private boolean cancelled;
        private Closeable closedStream;

        @Override
        public void onProgress(long bytesTransferred, long totalBytes) {
            transferred.add(bytesTransferred);
            totals.add(totalBytes);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void onStreamClosed(Closeable stream) {
            closedStream = stream;
        }
    }
}
