/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

/**
 * Parses stopword files. Stopword files have lines with zero or more stopwords. Any content from
 * the character | until the end of the line is ignored.
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
    public static Set<String> parse(Path filepath) {
        if (filepath.endsWith("README.txt")) {
            return null;
        }
        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "StopwordParser parsing file: " + filepath);
        Set<String> stopwords = null;
        try {
            Path file = filepath;
            if (Files.isRegularFile(file)) {
                try (
                    InputStream fin = IO.newInputStream(file);
                    Reader reader = new BufferedReader(new InputStreamReader(fin, Constants.ENCODING));
                    Scanner scanner = new Scanner(reader)) {

                    while (scanner.hasNextLine()) {
                        Set<String> stopwordsFromLine = parseLine(scanner.nextLine());
                        if (stopwordsFromLine != null) {
                            if (stopwords == null) {
                                stopwords = new LinkedHashSet<String>();
                            }
                            stopwords.addAll(stopwordsFromLine);
                        }
                    }
                }
            }
            // file does not exist or is a directory
            else {
                Log.warning(Geonet.INDEX_ENGINE, "Invalid stopwords file: " + file.toAbsolutePath());
            }
        } catch (IOException x) {
            Log.warning(Geonet.INDEX_ENGINE, x.getMessage() + " (this exception is swallowed)", x);
        }
        if (stopwords != null) {
            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Added # " + stopwords.size() + " stopwords");
        } else {
            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Added 0 stopwords");
        }
        return stopwords;
    }

    /**
     * Parses one line of a stopwords file. A line may contain 0 or more stopwords separated by
     * whitespace. Any content after the character | is ignored.
     *
     * @param line line to parse
     * @return set of stopwords, or null if none found
     */
    private static Set<String> parseLine(String line) {
        Set<String> stopwords = null;
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter("\\|");
        if (scanner.hasNext()) {
            String stopwordsPart = scanner.next();
            @SuppressWarnings("resource")
            Scanner whitespaceTokenizer = new Scanner(stopwordsPart);
            while (whitespaceTokenizer.hasNext()) {
                String stopword = whitespaceTokenizer.next();
                if (stopwords == null) {
                    stopwords = new LinkedHashSet<String>();
                }
                stopwords.add(stopword);
            }
        }
        return stopwords;
    }

}
