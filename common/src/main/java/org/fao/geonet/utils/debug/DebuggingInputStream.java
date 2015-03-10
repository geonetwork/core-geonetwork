package org.fao.geonet.utils.debug;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A useful class for detecting unclosed input streams.  It keeps track of all the open input streams and provides a way to
 * write the stacktrace where each stream was created to a file to allow debugging.
 *
 * @author Jesse on 1/17/2015.
 */
public class DebuggingInputStream extends FilterInputStream {
    private final OpenResourceTracker exception = new OpenResourceTracker();
    private final String descriptor;

    public DebuggingInputStream(String descriptor, InputStream in) throws IOException {
        super(in);
        this.descriptor = descriptor;
        OpenResourceTracker.open(descriptor, exception);
    }

    @Override
    public void close() throws IOException {
        super.close();
        OpenResourceTracker.close(descriptor, exception);
    }

}
