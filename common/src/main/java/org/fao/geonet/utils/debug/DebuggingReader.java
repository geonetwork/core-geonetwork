package org.fao.geonet.utils.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A useful class for detecting unclosed input streams.  It keeps track of all the open input streams and provides a way to
 * write the stacktrace where each stream was created to a file to allow debugging.
 *
 * @author Jesse on 1/17/2015.
 */
public class DebuggingReader extends BufferedReader {
    private final OpenResourceTracker exception = new OpenResourceTracker();
    private final String descriptor;

    public DebuggingReader(String descriptor, Reader in) throws IOException {
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
