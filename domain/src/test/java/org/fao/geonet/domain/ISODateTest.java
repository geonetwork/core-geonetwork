package org.fao.geonet.domain;

import static java.util.Calendar.YEAR;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Ignore;
import org.junit.Test;

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
        date.setDateAndTime(shortYear+"-6-3");
        assertEquals(expectedYear, date.getYears());
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
    @Ignore
    // not supported yet
    public void testSetDateAndTime_TimeZone() {
        ISODate date = new ISODate();
        date.setDateAndTime("1976-6-3T1:2:3Z+1:00");

        assertEquals(false, date.isDateOnly());
        assertEquals(1976, date.getYears());
        assertEquals(6, date.getMonths());
        assertEquals(3, date.getDays());
        assertEquals(1, date.getHours());
        assertEquals(2, date.getMinutes());
        assertEquals(3, date.getSeconds());

    }

    @Test
    public void testTimeDifferenceInSeconds() {
        ISODate date1 = new ISODate("1976-6-3T1:2:3");
        ISODate date2 = new ISODate("1976-6-3T1:2:30");
        assertEquals(27, date2.timeDifferenceInSeconds(date1));
        assertEquals(27, date1.timeDifferenceInSeconds(date2));
    }

    @Test
    public void testGetTimeInSeconds() {
        final long time = 123456789L;

        assertEquals(time / 1000, new ISODate(time, false).getTimeInSeconds());
    }

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
        assertEquals("1990-12-05", date.getDate());

        date = new ISODate(cal.getTimeInMillis(), true);
        
        assertEquals("1990-12-05", date.getDateAndTime());
        assertEquals("1990-12-05", date.getDate());
    }

}
