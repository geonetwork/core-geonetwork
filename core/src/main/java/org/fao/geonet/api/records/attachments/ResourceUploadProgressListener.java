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

import org.fao.geonet.api.exception.ResourceAlreadyExistException;

import java.io.Closeable;

/**
 * Callback used to report progress while a resource is being streamed into a
 * {@link Store}, eg. when downloading a file from a remote URL. Implementations
 * should be cheap/non-blocking as they may be invoked frequently.
 */
public interface ResourceUploadProgressListener {

    /**
     * A no-op listener used when the caller does not need progress reporting,
     * eg. the default synchronous upload path.
     */
    ResourceUploadProgressListener NO_OP = (bytesTransferred, totalBytes) -> {
    };

    /**
     * Called whenever additional bytes have been read from the source and
     * (attempted to be) written to the store.
     *
     * @param bytesTransferred The total number of bytes transferred so far.
     * @param totalBytes       The total expected size in bytes, or {@code -1} if unknown
     *                         (eg. no {@code Content-Length} header was provided).
     */
    void onProgress(long bytesTransferred, long totalBytes);

    /**
     * Called to check whether the upload should be cancelled. If this method returns
     * {@code true}, the upload will be aborted and the store will be closed.
     *
     * @return {@code true} if the upload should be cancelled, {@code false} otherwise.
     */
    default boolean isCancelled() {
        return false;
    }

    /**
     * Called when the stream to the store is opened. This can be used to perform any
     * necessary setup or initialization before the upload begins.
     *
     * @param stream The stream that has been opened.
     */
    default void onStreamOpened(Closeable stream) {
    }

    /**
     * Called when the stream to the store is closed after completion, failure, or cancellation.
     * This can be used to perform any necessary cleanup or finalization.
     *
     * @param stream The stream that has been closed.
     */
    default void onStreamClosed(Closeable stream) {
    }

    /**
     * Called after the effective resource filename has been resolved and before
     * the response body is streamed.
     *
     * <p>Asynchronous implementations may use this callback to persist the
     * filename and acquire a distributed claim. The callback occurs before the
     * declared content-length check so that failed oversized uploads can still
     * report their resolved filename.
     *
     * @param filename resolved resource filename
     * @throws ResourceAlreadyExistException if another active upload already
     *         owns the filename for the metadata record
     */
    default void onFilenameResolved(String filename) throws ResourceAlreadyExistException {
    }
}
