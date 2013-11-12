package org.fao.geonet.repository.statistic;

import org.fao.geonet.repository.AbstractSpringDataTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test DateInterval class
 * User: Jesse
 * Date: 10/1/13
 * Time: 8:04 PM
 */
public class DateIntervalTest {
    final String date = "1980-10-12T06:45:23";
    final DateInterval.Day day = new DateInterval.Day(date);
    final DateInterval.Month month = new DateInterval.Month(date);
    final DateInterval.Year year = new DateInterval.Year(date);
    final DateInterval.Hour hour = new DateInterval.Hour(date);
    final DateInterval.Minute minute = new DateInterval.Minute(date);
    final DateInterval.Second second = new DateInterval.Second(date);

    @Test
    public void testGetDateString() throws Exception {
        assertEquals("1980-10-12T06:45:23", second.getDateString());
        assertEquals("1980-10-12T06:45", minute.getDateString());
        assertEquals("1980-10-12T06", hour.getDateString());
        assertEquals("1980-10-12", day.getDateString());
        assertEquals("1980-10", month.getDateString());
        assertEquals("1980", year.getDateString());
    }

    @Test
    public void testCreateFromString() throws Exception {
        assertEquals("1980-10-12T06:45:23", second.createFromString(date).getDateString());
        assertEquals("1980-10-12T06:45", minute.createFromString(date).getDateString());
        assertEquals("1980-10-12T06", hour.createFromString(date).getDateString());
        assertEquals("1980-10-12", day.createFromString(date).getDateString());
        assertEquals("1980-10", month.createFromString(date).getDateString());
        assertEquals("1980", year.createFromString(date).getDateString());

        assertTrue(day.createFromString(date) instanceof DateInterval.Day);
        assertTrue(month.createFromString(date) instanceof DateInterval.Month);
        assertTrue(year.createFromString(date) instanceof DateInterval.Year);
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(new DateInterval.Day(date).equals(day));
        assertTrue(new DateInterval.Month(date).equals(month));
        assertTrue(new DateInterval.Year(date).equals(year));
        assertTrue(new DateInterval.Hour(date).equals(hour));
        assertTrue(new DateInterval.Minute(date).equals(minute));
        assertTrue(new DateInterval.Second(date).equals(second));

        assertFalse(new DateInterval.Month(date).equals(day));
        assertFalse(new DateInterval.Day(date).equals(month));
        assertFalse(new DateInterval.Day(date).equals(year));
        assertFalse(new DateInterval.Month(date).equals(hour));
        assertFalse(new DateInterval.Day(date).equals(minute));
        assertFalse(new DateInterval.Day(date).equals(second));

        final String date1 = "1999-12-11T08:12:21";
        assertFalse(new DateInterval.Day(date1).equals(day));
        assertFalse(new DateInterval.Month(date1).equals(month));
        assertFalse(new DateInterval.Year(date1).equals(year));
        assertFalse(new DateInterval.Hour(date1).equals(hour));
        assertFalse(new DateInterval.Minute(date1).equals(minute));
        assertFalse(new DateInterval.Second(date1).equals(second));
    }

    @Test
    public void testOutOfBounds() throws Exception {
        String smallDate = "90";
        new DateInterval.Year(smallDate);
        new DateInterval.Month(smallDate);
        new DateInterval.Day(smallDate);
        new DateInterval.Hour(smallDate);
        new DateInterval.Minute(smallDate);
        new DateInterval.Second(smallDate);
        // no exceptions is a pass
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(day.hashCode(), new DateInterval.Day(date).hashCode());
        assertEquals(month.hashCode(), new DateInterval.Month(date).hashCode());
        assertEquals(year.hashCode(), new DateInterval.Year(date).hashCode());

        final String date1 = "1999-12-11T08:12";
        assertFalse(day.hashCode() == new DateInterval.Day(date1).hashCode());
        assertFalse(month.hashCode() == new DateInterval.Month(date1).hashCode());
        assertFalse(year.hashCode() == new DateInterval.Year(date1).hashCode());
    }
}
