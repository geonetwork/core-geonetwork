package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Parses stopword files. Stopword files have lines with zero or more stopwords. Any content from the character | until
 * the end of the line is ignored.
 *
 * @author heikki doeleman
 */
public class StopwordFileParser {

    /**
     * Parses a stopwords file.
     *
     * @param filepath path to stopwords file
     * @return set of stopwords, or null if none found
     */
    public static Set<String> parse(String filepath) {
        if (filepath.endsWith("README.txt")) {
            return null;
        }
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "StopwordParser parsing file: " + filepath);
        Set<String> stopwords = null;
        try {
            File file = new File(filepath);
            if (file.exists() && !file.isDirectory()) {
                Scanner scanner = new Scanner(new FileReader(file));
                try {
                    while (scanner.hasNextLine()) {
                        Set<String> stopwordsFromLine = parseLine(scanner.nextLine());
                        if (stopwordsFromLine != null) {
                            if (stopwords == null) {
                                stopwords = new HashSet<String>();
                            }
                            stopwords.addAll(stopwordsFromLine);
                        }
                    }
                }
                finally {
                    scanner.close();
                }
            }
            // file does not exist or is a directory
            else {
                Log.warning(Geonet.INDEX_ENGINE, "Invalid stopwords file: " + file.getAbsolutePath());
            }
        }
        catch (IOException x) {
            Log.warning(Geonet.INDEX_ENGINE, x.getMessage() + " (this exception is swallowed)");
            x.printStackTrace();
        }
        if (stopwords != null) {
            if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Added # " + stopwords.size() + " stopwords");
        }
        else {
            if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Added 0 stopwords");
        }
        return stopwords;
    }

    /**
     * Parses one line of a stopwords file. A line may contain 0 or more stopwords separated by whitespace. Any content
     * after the character | is ignored.
     *
     * @param line line to parse
     * @return set of stopwords, or null if none found
     */
    private static Set<String> parseLine(String line) {
        Set<String> stopwords = null;
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter("\\|");
        if (scanner.hasNext()) {
            String stopwordsPart = scanner.next();
            Scanner whitespaceTokenizer = new Scanner(stopwordsPart);
            while (whitespaceTokenizer.hasNext()) {
                String stopword = whitespaceTokenizer.next();
                if (stopwords == null) {
                    stopwords = new HashSet<String>();
                }
                stopwords.add(stopword);
            }
        }
        return stopwords;
    }

}