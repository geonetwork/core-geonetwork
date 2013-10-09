package org.fao.geonet.util;

import org.fao.geonet.test.GeonetworkTestCase;

/**
 * Unit test for ISODate class.
 *
 * @author Jose García
 */
public class ISODateTest extends GeonetworkTestCase {

    public ISODateTest(String name) throws Exception {
        super(name);
    }

    public void testCreateISODateException() throws Exception {
        try {
            ISODate isoDate = new ISODate(null);
            fail("iso date is null, IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException ex) {


        }

        try {
            ISODate isoDate = new ISODate("2001");
            fail("iso date is not valid, IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException ex) {

        }

    }

    public void testSetDateException() throws Exception {
        try {
            ISODate isoDate = new ISODate();
            isoDate.setDate(null);
            fail("iso date is null, IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException ex) {


        }

        try {
            ISODate isoDate = new ISODate();
            isoDate.setDate("2001");
            fail("iso date is not valid, IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException ex) {

        }

    }

    public void testCreateISODateValid() throws Exception {
        // Date format
        ISODate isoDate = new ISODate("2013-10-10");
        assertTrue(isoDate.toString().equals("2013-10-10T00:00:00"));

        isoDate = new ISODate("2013-10-10Z");
        assertTrue(isoDate.toString().equals("2013-10-10T00:00:00"));

        // Datetime format
        isoDate = new ISODate("2013-10-10T13:20:00");
        assertTrue(isoDate.toString().equals("2013-10-10T13:20:00"));

        isoDate = new ISODate("2013-10-10T13:20:00Z");
        assertTrue(isoDate.toString().equals("2013-10-10T13:20:00"));
    }

    public void testSetDateISODateValid() throws Exception {
        // Date format
        ISODate isoDate = new ISODate();
        isoDate.setDate("2013-10-10");
        assertTrue(isoDate.toString().equals("2013-10-10T00:00:00"));

        isoDate = new ISODate();
        isoDate.setDate("2013-10-10Z");
        assertTrue(isoDate.toString().equals("2013-10-10T00:00:00"));

        // Datetime format
        isoDate = new ISODate();
        isoDate.setDate("2013-10-10T13:20:00");
        assertTrue(isoDate.toString().equals("2013-10-10T13:20:00"));

        isoDate = new ISODate();
        isoDate.setDate("2013-10-10T13:20:00Z");
        assertTrue(isoDate.toString().equals("2013-10-10T13:20:00"));

        // Datetime with non UTC zone
        isoDate = new ISODate();
        isoDate.setDate("2013-10-10T13:20:00+02:00");
        assertTrue(isoDate.toString().equals("2013-10-10T11:20:00"));
    }

    public void testGetDate() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:00Z");
        assertTrue(isoDate.getDate().equals("2013-10-10"));
    }

    public void testGetTime() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:00Z");
        assertTrue(isoDate.getTime().equals("13:20:00"));
    }

    public void testGetSeconds() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:40Z");

        // Convert seconds to milliseconds
        ISODate testDate = new ISODate(isoDate.getSeconds() * 1000);
        assertTrue(isoDate.toString().equals(testDate.toString()));
    }

    public void testClone() throws Exception {
        ISODate isoDate = new ISODate("2013-10-10T13:20:40Z");
        ISODate clonedDate = isoDate.clone();

        assertTrue(isoDate.toString().equals(clonedDate.toString()));
    }

    public void testSub() throws Exception {
        ISODate isoDate1 = new ISODate("2013-10-10T13:20:40Z");
        ISODate isoDate2 = new ISODate("2013-10-10T13:21:40Z");

        assertTrue(isoDate2.sub(isoDate1) == 60);

        isoDate2 = new ISODate("2013-10-10T13:19:40Z");
        assertTrue(isoDate2.sub(isoDate1) == -60);
    }
}
