package org.fao.geonet.api.exception;

import java.io.IOException;

/**
 * Custom exception to be thrown when the size of a remote file to be uploaded to the store exceeds the maximum upload size.
 */
public class InputStreamLimitExceededException extends IOException {
    private final long maxUploadSize;
    private final long remoteFileSize;

    /**
     * Create a new InputStreamLimitExceededException with an unknown remote file size.
     *
     * @param maxUploadSize the maximum upload size allowed
     */
    public InputStreamLimitExceededException(long maxUploadSize) {
        this(maxUploadSize, -1L);
    }

    /**
     * Create a new InputStreamLimitExceededException with a known remote file size.
     *
     * @param maxUploadSize the maximum upload size allowed
     * @param remoteFileSize the size of the remote file
     */
    public InputStreamLimitExceededException(long maxUploadSize, long remoteFileSize) {
        super("Remote file size " + (remoteFileSize >= 0L ? "of " + remoteFileSize + " bytes " : "") + "exceeds the maximum upload size" + (maxUploadSize >= 0L ? " of " + maxUploadSize + " bytes" : ""));
        this.maxUploadSize = maxUploadSize;
        this.remoteFileSize = remoteFileSize;
    }

    /**
     * Get the maximum upload size allowed.
     *
     * @return the maximum upload size allowed
     */
    public long getMaxUploadSize() {
        return this.maxUploadSize;
    }

    /**
     * Get the size of the remote file.
     *
     * @return the size of the remote file or -1 if the size is unknown
     */
    public long getRemoteFileSize() {
        return this.remoteFileSize;
    }
}
