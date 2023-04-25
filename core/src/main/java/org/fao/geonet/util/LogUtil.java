/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.util;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogUtil {

    private LogUtil() {
    }

    /**
     * Used to configure Log4J to route harvester messages to an individual file.
     * <p>
     * This method has the side effect of setting Log4J ThreadContext values:
     * <ul>
     *     <li>harvester</li>
     *     <li>logfile</li>
     *     <li>timeZone</li>
     * </ul>
     * <p>
     * Log4J checks for {@code ThreadContext.put("logfile", name)} to route messages
     * the logfile location.
     *
     * @return the location of the logfile
     */
    public static String initializeHarvesterLog(String type, String name) {
        // Filename safe representation of harvester name (using '_' as needed).
        final String harvesterName = name.replaceAll("\\W+", "_");
        final String harvesterType = type.replaceAll("\\W+", "_");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

        String logfile = "harvester_"
            + harvesterType
            + "_" + harvesterName
            + "_" + dateFormat.format(new Date(System.currentTimeMillis()))
            + ".log";

        String timeZoneSetting = ApplicationContextHolder.get().getBean(SettingManager.class).getValue(Settings.SYSTEM_SERVER_TIMEZONE);
        if (StringUtils.isBlank(timeZoneSetting)) {
            timeZoneSetting = TimeZone.getDefault().getID();
        }

        ThreadContext.put("harvest", harvesterName);
        ThreadContext.putIfNull("logfile", logfile);
        ThreadContext.put("timeZone", timeZoneSetting);

        return logfile;
    }
}
