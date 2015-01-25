package org.fao.geonet.utils.debug;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.fao.geonet.Constants;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Jesse on 1/25/2015.
 */
public class OpenResourceTracker extends RuntimeException {
    private static Multimap<String, OpenResourceTracker> openExceptions = HashMultimap.create();

    public static synchronized void open(String descriptor, OpenResourceTracker openStackTrace) throws IOException {
        openExceptions.put(descriptor, openStackTrace);
        if (openExceptions.size()> 1000) {
            printExceptions();
        }
    }

    public static synchronized void close(String descriptor, OpenResourceTracker openStackTrace) {
        openExceptions.remove(descriptor, openStackTrace);
    }

    public static synchronized void printExceptions() throws IOException {
        Path log = Files.createTempFile("openFileTraces", ".txt");
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(log, Constants.CHARSET))){
            for (Map.Entry<String, OpenResourceTracker> entry : openExceptions.entries()) {
                out.println(entry.getKey());
                entry.getValue().printStackTrace(out);
            }
        }

        System.out.println("Write all open file stacktraces to: " + log);
    }
}
