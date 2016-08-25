//=============================================================================
//===
//=== ThreadUtils
//===
//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//=============================================================================

package org.fao.geonet.util;

import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;

public class ThreadUtils {

    private static SettingManager settingMan;
    private static boolean dbCanUseMultipleThreads = false;

    // -- No public constructor - static methods class
    private ThreadUtils() {
    }

    /**
     * Set thread count from settings. i
     *
     * @return threadCount
     */
    private static int setCountFromSettings() {
        int threadCount = 1;
        String nrThreadsStr = settingMan.getValue(Settings.SYSTEM_THREADEDINDEXING_MAXTHREADS);
        if (nrThreadsStr == null) {
            Log.error(Geonet.GEONETWORK, "Number of Threads for indexing setting is missing from settings table. Using *one* thread");
            nrThreadsStr = "1";
        }

        try {
            threadCount = Integer.parseInt(nrThreadsStr);
        } catch (NumberFormatException nfe) {
            Log.error(Geonet.GEONETWORK, "Number of Threads for indexing setting is not valid. Using *one* thread");
        }
        return threadCount;
    }

    /**
     * Initialize threadUtils during GeoNetwork startup.
     *
     * @param dbUrl database url
     * @param sm    SettingManager. Used to find settings for threaded methods.
     */
    public static void init(String dbUrl, SettingManager sm) throws Exception {
        settingMan = sm;
        if (dbUrl != null) {
            // postgres has been tested with this function
            if (dbUrl.toLowerCase().contains("postgres")) {
                dbCanUseMultipleThreads = true;
                // oracle has also been tested with this function
            } else if (dbUrl.toLowerCase().contains("oracle")) {
                dbCanUseMultipleThreads = true;
            }
        }
    }

    /**
     * Get number of threads calc'd from runtime or settings and restrict if not using capable
     * database or threaded processing not available.
     *
     * @return threadCount
     */
    public static int getNumberOfThreads() {
        int threadCount = setCountFromSettings();
        if (!dbCanUseMultipleThreads && threadCount > 1) {
            threadCount = 1;
            Log.error(Geonet.GEONETWORK, "Theaded Indexing for not supported or hasn't been tested - so only *one* thread will be used");
        }

        Log.info(Geonet.GEONETWORK, "Using " + threadCount + " thread(s) to process indexing job");
        return threadCount;
    }

    /**
     * Get thread priority calc'd from runtime or settings.
     *
     * @return threadPriority
     */
    public static int getPriority() {
        return Thread.NORM_PRIORITY;
    }

    /**
     * Get number of processors allocated to JVM if the database supports threaded access.
     *
     * @return number of processors allocated to JVM.
     */
    public static String getNumberOfProcessors() {
        int result = 1;
        if (dbCanUseMultipleThreads) {
            result = Runtime.getRuntime().availableProcessors();
        }
        return result + "";
    }
}
