package org.fao.geonet.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.fao.geonet.Constants;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * A useful class for detecting unclosed input streams.  It keeps track of all the open input streams and provides a way to
 * write the stacktrace where each stream was created to a file to allow debugging.
 *
 * @author Jesse on 1/17/2015.
 */
public class DebuggingInputStream extends FilterInputStream {
    private static Multimap<String, Exception> openExceptions = HashMultimap.create();
    private final RuntimeException exception = new RuntimeException();
    private final String descriptor;

    protected DebuggingInputStream(InputStream in, String descriptor) {
        super(in);
        synchronized (DebuggingInputStream.class) {
            this.descriptor = descriptor;
            openExceptions.put(descriptor, exception);
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (DebuggingInputStream.class) {
            openExceptions.remove(descriptor, exception);
        }
        super.close();
    }

    public static synchronized void printExceptions() throws IOException {
        Path log = Files.createTempFile("openFileTraces",".txt");
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(log, Constants.CHARSET))){
            for (Map.Entry<String, Exception> entry : openExceptions.entries()) {
                out.println(entry.getKey());
                entry.getValue().printStackTrace(out);
            }
        }

        System.out.println("Write all open file stacktraces to: " + log);
    }
}
