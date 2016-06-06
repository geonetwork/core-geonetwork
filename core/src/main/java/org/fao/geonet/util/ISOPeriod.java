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

package org.fao.geonet.util;

/**
 * Utility to convert an ISO8601 Period to minutes.
 */
public class ISOPeriod {

    public final static String ZERO_DURATION = "P0Y0M0DT0H0M0S";

    /**
     * Converts a string in ISO8601 Period format to how many minutes that is.
     *
     * @param iso8601Period - the period
     * @return minutes period in minutes
     */
    public static int iso8601Period2Minutes(String iso8601Period) {
        int minutes = 0;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('P') + 1, iso8601Period.indexOf('Y'))) * 365 * 24 * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('Y') + 1, iso8601Period.indexOf('M'))) * 30 * 24 * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('M') + 1, iso8601Period.indexOf('D'))) * 24 * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('T') + 1, iso8601Period.indexOf('H'))) * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('H') + 1, iso8601Period.indexOf('M', iso8601Period.indexOf('H'))));
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('M', iso8601Period.indexOf('H')) + 1, iso8601Period.indexOf('S'))) / 60;
        return minutes;
    }

}
