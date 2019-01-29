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

package org.fao.geonet.domain;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;

import static java.util.Calendar.YEAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ISODateTest {

    @Test
    public void testSetDateAndTime_OnlyDate() {
        ISODate date = new ISODate();
        date.setDateAndTime("1976-06-03");
        assertEquals(true, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("1976-6-3");
        assertEquals(true, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("1976-6-3T");
        assertEquals(true, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("99-6-3");
        assertEquals(true, date.isDateOnly());
        assertEquals(1999, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("10-6-3");
        assertEquals(true, date.isDateOnly());
        assertEquals(2010, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());

        date = new ISODate();
        int expectedYear = Calendar.getInstance().get(YEAR);
        String shortYear = String.valueOf(expectedYear).substring(2);
        date.setDateAndTime(shortYear + "-6-3");
        assertEquals(expectedYear, date.getYears());
        assertEquals(0, date.getSeconds());
    }

    @Test
    public void testSetDateAndTime_OnlyYear() {
        ISODate date = new ISODate();
        date.setDateAndTime("2019");
        assertEquals(true, date.isDateYearOnly());
        assertEquals(2019, date.getYears());
        assertEquals(12, date.getMonths());
        assertEquals(31, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());

    }

    @Test
    public void testSetDateAndTime_OnlyYearMonth() {
        ISODate date = new ISODate();
        date.setDateAndTime("2019-10");
        assertEquals(true, date.isDateYearMonthOnly());
        assertEquals(2019, date.getYears());
        assertEquals(10, date.getMonths());
        assertEquals(31, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("2019-04");
        assertEquals(true, date.isDateYearMonthOnly());
        assertEquals(2019, date.getYears());
        assertEquals(4, date.getMonths());
        assertEquals(30, date.getDays());
        assertEquals(0, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
    }

    @Test
    public void testSetDateAndTime() {

        ISODate date = new ISODate();
        date.setDateAndTime("1976-06-03T01:02:03");

        assertEquals(false, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(1, date.getHours());
        assertEquals(2, date.getMinutes());
        assertEquals(3, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("T01:02:03");

        assertEquals(false, date.isDateOnly());
        assertEquals(Calendar.getInstance().get(Calendar.YEAR), date.getYears());
        assertEquals(Calendar.getInstance().get(Calendar.MONTH) + 1, date.getMonths());
        assertEquals(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), date.getDays());
        assertEquals(1, date.getHours());
        assertEquals(2, date.getMinutes());
        assertEquals(3, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("01:02:03");

        assertEquals(false, date.isDateOnly());
        assertEquals(Calendar.getInstance().get(Calendar.YEAR), date.getYears());
        assertEquals(Calendar.getInstance().get(Calendar.MONTH) + 1, date.getMonths());
        assertEquals(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), date.getDays());
        assertEquals(1, date.getHours());
        assertEquals(2, date.getMinutes());
        assertEquals(3, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("1976-6-3T1:2:3");

        assertEquals(false, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(1, date.getHours());
        assertEquals(2, date.getMinutes());
        assertEquals(3, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("1976-6-3T1:2:3Z");

        assertEquals(false, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(1, date.getHours());
        assertEquals(2, date.getMinutes());
        assertEquals(3, date.getSeconds());

        date = new ISODate();
        date.setDateAndTime("1976-6-3T10:02");

        assertEquals(false, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(10, date.getHours());
        assertEquals(2, date.getMinutes());
        assertEquals(0, date.getSeconds());
    }

    @Test
    public void testSetDateAndTime_TimeZone() {
        ISODate date = new ISODate();
        date.setDateAndTime("1976-06-03T00:02:03+01:30");

        assertEquals(false, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(2, date.getDays());
        assertEquals(22, date.getHours());
        assertEquals(32, date.getMinutes());
        assertEquals(3, date.getSeconds());

    }

    @Test
    public void testTimeDifferenceInSeconds() {
        ISODate date1 = new ISODate("1976-6-3T1:2:3");
        ISODate date2 = new ISODate("1976-6-3T1:2:30");
        assertEquals(27, date2.timeDifferenceInSeconds(date1));
        assertEquals(-27, date1.timeDifferenceInSeconds(date2));
    }

    @Test
    public void testGetTimeInSeconds() {
        final long time = 123456789L;

        assertEquals(time / 1000, new ISODate(time, false).getTimeInSeconds());
    }

    @Test
    public void testGetDateAndTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1990);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 2);
        cal.set(Calendar.SECOND, 3);

        ISODate date = new ISODate(cal.getTimeInMillis(), false);

        assertEquals("1990-12-05T23:02:03", date.getDateAndTime());
        assertEquals("1990-12-05", date.getDateAsString());

        date = new ISODate(cal.getTimeInMillis(), true);

        assertEquals("1990-12-05", date.getDateAndTime());
        assertEquals("1990-12-05", date.getDateAsString());
    }

    @Test
    public void testParseISODateTime() throws Exception {
        // Format: yyyy-mm-ddThh.mm:ss[+hh:mm|=+hh:mm] Time zone
        String jodaISODate = ISODate
            .parseISODateTime("2010-10-10T00:00:00+02:00");
        assertTrue(jodaISODate.equals("2010-10-09T22:00:00.000Z"));

        // Format: yyyy-mm-ddThh.mm:ss.ms[+hh:mm|=+hh:mm] Time zone
        jodaISODate = ISODate
            .parseISODateTime("2010-10-10T00:00:00.000+02:00");
        assertTrue(jodaISODate.equals("2010-10-09T22:00:00.000Z"));

        // Format: yyyy-mm-ddThh.mm:ssZ (UTC)
        jodaISODate = ISODate.parseISODateTime("2010-10-10T00:00:00Z");
        assertTrue(jodaISODate.equals("2010-10-10T00:00:00.000Z"));

        // Format: yyyy-mm-ddThh.mm:ss.msZ (UTC)
        jodaISODate = ISODate.parseISODateTime("2010-10-10T00:00:00.000Z");
        assertTrue(jodaISODate.equals("2010-10-10T00:00:00.000Z"));
    }

    @Test
    public void testParseYearMonthDateTime() throws Exception {
        // xs:gYearMonth
        for (int i = 0; i < 10; i++) {
            String year = "20" + getRandom(1, 0) + getRandom(9, 0);
            String month = "0" + getRandom(9, 1);
            String hour = getRandom(1, 0) + "" + getRandom(9, 0);
            String minutes = getRandom(5, 1) + "" + getRandom(9, 0);

            // Format: yyyy-MM-hh:mm Time zone
            String tmp = year + "-" + month + "-" + hour + ":" + minutes + "Z";
            String jodaISODate = ISODate.parseISODateTime(tmp);
            assertEquals(jodaISODate, year + "-" + month + "-01T" + hour + ":"
                + minutes + ":00.000Z");

            year = "20" + getRandom(1, 0) + getRandom(9, 0);
            month = "0" + getRandom(9, 1);
            // Format: yyyy-MM
            tmp = year + "-" + month;
            jodaISODate = ISODate.parseISODateTime(tmp);
            assertEquals(jodaISODate, tmp + "-01T00:00:00.000Z");

        }
    }

    @Test
    public void testParseYearDateTime() throws Exception {
        // xs:gYear
        for (int i = 0; i < 10; i++) {
            String year = "20" + getRandom(1, 0) + getRandom(9, 0);
            String hour = getRandom(1, 0) + "" + getRandom(9, 0);
            String minutes = getRandom(5, 1) + "" + getRandom(9, 0);

            // Format: yyyy-hh:mm Time zone
            String jodaISODate = ISODate.parseISODateTime(year + "-" + hour + ":" + minutes + "Z");
            assertEquals(jodaISODate, year + "-01-01T" + hour + ":" + minutes + ":00.000Z");

            year = "20" + getRandom(1, 0) + getRandom(9, 0);

            // Format: yyyy
            jodaISODate = ISODate.parseISODateTime(year);
            assertEquals(jodaISODate, year + "-01-01T00:00:00.000Z");
        }
    }

    private String getRandom(int max, int min) {
        return Integer
            .toString(min + (int) (Math.random() * ((max - min) + 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateISODateExceptionBecauseOfNull() throws Exception {
        new ISODate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateISODateExceptionBecauseOfEmpty() throws Exception {
        new ISODate("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateISODateExceptionBecauseOfBadFormat() throws Exception {
        new ISODate("2019hh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateExceptionBecauseOfNull() throws Exception {
        ISODate isoDate = new ISODate();
        isoDate.setDateAndTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateExceptionBecauseOfBadFormat() throws Exception {
        ISODate isoDate = new ISODate();
        isoDate.setDateAndTime("2019hh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateExceptionBecauseOfEmpty() throws Exception {
        ISODate isoDate = new ISODate();
        isoDate.setDateAndTime("");
    }

    @Test
    public void testCreateISODateValid() throws Exception {
        // Date format
        ISODate isoDate = new ISODate("2013-10-10");
        assertEquals("2013-10-10", isoDate.toString());
        assertTrue(isoDate.isDateOnly());

        isoDate = new ISODate("2013-10-10Z");
        assertEquals("2013-10-10", isoDate.toString());

        // Datetime format
        isoDate = new ISODate("2013-10-10T13:20:00");
        assertEquals("2013-10-10T13:20:00", isoDate.toString());

        isoDate = new ISODate("2013-10-10T13:20:00Z");
        assertEquals("2013-10-10T13:20:00", isoDate.toString());
    }

    @Test
    public void testSetDateISODateValid() throws Exception {
        // Date format
        ISODate isoDate = new ISODate();
        isoDate.setDateAndTime("2013-10-10");
        assertEquals("2013-10-10", isoDate.toString());

        isoDate = new ISODate();
        isoDate.setDateAndTime("2013-10-10Z");
        assertEquals("2013-10-10", isoDate.toString());

        // Datetime format
        isoDate = new ISODate();
        isoDate.setDateAndTime("2013-10-10T13:20:00");
        assertEquals("2013-10-10T13:20:00", isoDate.toString());

        isoDate = new ISODate();
        isoDate.setDateAndTime("2013-10-10T13:20:00Z");
        assertEquals("2013-10-10T13:20:00", isoDate.toString());

        // Datetime with non UTC zone
        isoDate = new ISODate();
        isoDate.setDateAndTime("2013-10-10T13:20:00+02:00");
        assertEquals("2013-10-10T11:20:00", isoDate.toString());
    }

    @Test
    public void testGetDate() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:00Z");
        assertEquals("2013-10-10", isoDate.getDateAsString());


        isoDate = new ISODate("2019-01");
        assertEquals("2019-01", isoDate.getDateAsString());

        isoDate = new ISODate("2019");
        assertEquals("2019", isoDate.getDateAsString());
    }

    @Test
    public void testGetTime() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:00Z");
        assertEquals("13:20:00", isoDate.getTimeAsString());
    }

    @Test
    public void testGetSeconds() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:40Z");

        // Convert seconds to milliseconds
        ISODate testDate = new ISODate(isoDate.getTimeInSeconds() * 1000);
        assertEquals(testDate.toString(), isoDate.toString());
    }

    @Test
    public void testClone() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:40Z");
        ISODate clonedDate = isoDate.clone();

        assertEquals(clonedDate.toString(), isoDate.toString());
    }

    @Test
    public void testSub() throws Exception {
        ISODate isoDate1 = new ISODate("2013-10-10T13:20:40Z");
        ISODate isoDate2 = new ISODate("2013-10-10T13:21:40Z");

        assertEquals(60, isoDate2.timeDifferenceInSeconds(isoDate1));

        isoDate2 = new ISODate("2013-10-10T13:19:40Z");
        org.junit.Assert.assertEquals(-60, isoDate2.timeDifferenceInSeconds(isoDate1));
    }

}
