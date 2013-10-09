//==============================================================================
//===
//===   JODAISODate
//===
//==============================================================================
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
//==============================================================================

package org.fao.geonet.util;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

//==============================================================================

public class JODAISODate {
    private static String dt = "3000-01-01T00:00:00.000Z"; // JUNK Value

    // Pattern to check dates
    private static Pattern gsYear = Pattern
            .compile("([0-9]{4})(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");
    private static Pattern gsYearMonth = Pattern
            .compile("([0-9]{4})-([0-1][0-9])(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");

    /*
     * Converts a given ISO date time into the form used to index in Lucene
     * Returns null if it gets back a ridiculous value
     */
    public static String parseISODateTime(String input1) {
        String newDateTime = parseISODateTimes(input1, null);
        if (newDateTime.equals(dt))
            return null;
        else
            return newDateTime;
    }

    /*
     * Converts two ISO date times into standard form used to index in Lucene
     * Always returns something because it is used during the indexing of the
     * metadata record in Lucene - if exception during parsing then it is
     * something ridiculous like JUNK value above
     */
    public static String parseISODateTimes(String input1, String input2) {
        DateTimeFormatter dto = ISODateTimeFormat.dateTime();
        PeriodFormatter p = ISOPeriodFormat.standard();
        DateTime odt1;
        String odt = "";

        // input1 should be some sort of ISO time
        // eg. basic: 20080909, full: 2008-09-09T12:21:00 etc
        // convert everything to UTC so that we remove any timezone
        // problems
        try {
            DateTime idt = parseBasicOrFullDateTime(input1);
            odt1 = dto.parseDateTime(idt.toString()).withZone(
                    DateTimeZone.forID("UTC"));
            odt = odt1.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return dt;
        }

        if (input2 == null || input2.equals(""))
            return odt;

        // input2 can be an ISO time as for input1 but also an ISO time period
        // eg. -P3D or P3D - if an ISO time period then it must be added to the
        // DateTime generated for input1 (odt1)
        // convert everything to UTC so that we remove any timezone
        // problems
        try {
            boolean minus = false;
            if (input2.startsWith("-P")) {
                input2 = input2.substring(1);
                minus = true;
            }

            if (input2.startsWith("P")) {
                Period ip = p.parsePeriod(input2);
                DateTime odt2;
                if (!minus)
                    odt2 = odt1.plus(ip.toStandardDuration().getMillis());
                else
                    odt2 = odt1.minus(ip.toStandardDuration().getMillis());
                odt = odt + "|" + odt2.toString();
            } else {
                DateTime idt = parseBasicOrFullDateTime(input2);
                DateTime odt2 = dto.parseDateTime(idt.toString()).withZone(
                        DateTimeZone.forID("UTC"));
                odt = odt + "|" + odt2.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return odt + "|" + dt;
        }

        return odt;
    }

    public static DateTime parseBasicOrFullDateTime(String input1)
            throws Exception {
        DateTimeFormatter bd = ISODateTimeFormat.basicDate();
        DateTimeFormatter bt = ISODateTimeFormat.basicTime();
        DateTimeFormatter bdt = ISODateTimeFormat.basicDateTime();
        DateTimeFormatter dtp = ISODateTimeFormat.dateTimeParser();
        DateTime idt;
        Matcher matcher;
        if (input1.length() == 8 && !input1.startsWith("T")) {
            idt = bd.parseDateTime(input1);
        } else if (input1.startsWith("T") && !input1.contains(":")) {
            idt = bt.parseDateTime(input1);
        } else if (input1.contains("T") && !input1.contains(":")
                && !input1.contains("-")) {
            idt = bdt.parseDateTime(input1);
        } else if ((matcher = gsYearMonth.matcher(input1)).matches()) {
            String year = matcher.group(1);
            String month = matcher.group(2);
            String minute = "00";
            String hour = "00";
            String timezone = "Z";
            if (matcher.group(4) != null) {
                minute = matcher.group(5);
                hour = matcher.group(4);
                timezone = matcher.group(6);
            }

            idt = generateDate(year, month, minute, hour, timezone);
        } else if ((matcher = gsYear.matcher(input1)).matches()) {
            String year = matcher.group(1);
            String month = "01";
            String minute = "00";
            String hour = "00";
            String timezone = "Z";
            if (matcher.group(3) != null) {
                minute = matcher.group(4);
                hour = matcher.group(3);
                timezone = matcher.group(5);
            }

            idt = generateDate(year, month, minute, hour, timezone);
        } else {
            idt = dtp.parseDateTime(input1);
        }
        return idt;
    }

    /**
     * @param year
     * @param month
     * @param minute
     * @param hour
     * @return
     */
    private static DateTime generateDate(String year, String month,
            String minute, String hour, String timezone) {
        
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.valueOf(year));
        c.set(Calendar.MONTH, Integer.valueOf(month) - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);

        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
        c.set(Calendar.MINUTE, Integer.valueOf(minute));
        
        TimeZone zone = TimeZone.getTimeZone(timezone);
        c.setTimeZone(zone );

        return new DateTime(c.getTimeInMillis());
    }
}

// ==============================================================================
