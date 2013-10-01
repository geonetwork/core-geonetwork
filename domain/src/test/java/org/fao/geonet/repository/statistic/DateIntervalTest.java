package org.fao.geonet.repository.statistic;

import org.fao.geonet.repository.AbstractSpringDataTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test DateInterval class
 * User: Jesse
 * Date: 10/1/13
 * Time: 8:04 PM
 */
public class DateIntervalTest extends AbstractSpringDataTest {
    final String date = "1980-10-12T06:45";
    final DateInterval.Day day = new DateInterval.Day(date);
    final DateInterval.Month month = new DateInterval.Month(date);
    final DateInterval.Year year = new DateInterval.Year(date);

    @Test
    public void testGetDateString() throws Exception {
        assertEquals("1980-10-12", day.getDateString());
        assertEquals("1980-10", month.getDateString());
        assertEquals("1980", year.getDateString());
    }

    @Test
    public void testCreateFromString() throws Exception {
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

        assertFalse(new DateInterval.Month(date).equals(day));
        assertFalse(new DateInterval.Day(date).equals(month));
        assertFalse(new DateInterval.Day(date).equals(year));

        final String date1 = "1999-12-11T08:12";
        assertFalse(new DateInterval.Day(date1).equals(day));
        assertFalse(new DateInterval.Month(date1).equals(month));
        assertFalse(new DateInterval.Year(date1).equals(year));
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
