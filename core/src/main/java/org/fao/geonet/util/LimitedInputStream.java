package org.fao.geonet.util;

import org.fao.geonet.api.exception.RemoteFileTooLargeException;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class LimitedInputStream extends org.apache.commons.fileupload.util.LimitedInputStream {


    /**
     * Creates a new instance.
     *
     * @param inputStream The input stream, which shall be limited.
     * @param pSizeMax    The limit; no more than this number of bytes
     *                    shall be returned by the source stream.
     */
    public LimitedInputStream(InputStream inputStream, long pSizeMax) {
        super(inputStream, pSizeMax);
    }

    @Override
    protected void raiseError(long pSizeMax, long pCount) throws IOException {
        throw new RemoteFileTooLargeException(pSizeMax);
    }
}
