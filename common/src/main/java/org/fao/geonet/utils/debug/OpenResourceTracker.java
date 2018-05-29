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

package org.fao.geonet.utils.debug;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.fao.geonet.Constants;
import org.fao.geonet.utils.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Jesse on 1/25/2015.
 */
public class OpenResourceTracker extends RuntimeException {
    private static Multimap<String, OpenResourceTracker> openResources = HashMultimap.create();
    private static Map<Integer, Boolean> reports = Maps.newHashMap();

    public static synchronized void open(String descriptor, OpenResourceTracker openStackTrace) throws IOException {
        openResources.put(descriptor, openStackTrace);
        reportOpenResources(1000);
        reportOpenResources(2000);
        reportOpenResources(3000);
        reportOpenResources(4000);
    }

    public static void reportOpenResources(int cutOff) throws IOException {
        if (openResources.size() > cutOff && !isReported(cutOff)) {
            printExceptions(100);
        }
    }

    private static boolean isReported(Integer numOpenResources) {
        if (reports.containsKey(numOpenResources)) {
            return reports.get(numOpenResources);
        }
        return false;
    }

    public static synchronized void close(String descriptor, OpenResourceTracker openStackTrace) {
        openResources.remove(descriptor, openStackTrace);
    }

    public static synchronized void printExceptions(int numExceptionsToPrint) throws IOException {
        Path log = Files.createTempFile("openFileTraces", ".txt");
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(log, Constants.CHARSET))) {
            printExceptions(out, numExceptionsToPrint);
        }

        final String message = "Resource Leak detected: First " + numExceptionsToPrint + " of " + openResources.size() + " open file stacktraces to: " + log;
        Log.error(Log.JEEVES, message);
    }

    public static synchronized void printExceptions(PrintWriter out, int numExceptionsToPrint) throws IOException {
        int count = 0;
        for (Map.Entry<String, OpenResourceTracker> entry : openResources.entries()) {
            if (count >= numExceptionsToPrint) {
                return;
            }
            out.println(entry.getKey());
            entry.getValue().printStackTrace(out);
            count++;
        }

    }

    public static int numberOfOpenResources() {
        return openResources.size();
    }
}
