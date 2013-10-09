package org.fao.geonet.util;

import org.fao.geonet.test.GeonetworkTestCase;

/**
 * Unit test for JODAISODate class.
 * 
 * @author Jose García
 */
public class JODAISODateTest extends GeonetworkTestCase {
	public void testParseISODateTime() throws Exception {
		// Format: yyyy-mm-ddThh.mm:ss[+hh:mm|=+hh:mm] Time zone
		String jodaISODate = JODAISODate
				.parseISODateTime("2010-10-10T00:00:00+02:00");
		assertTrue(jodaISODate.equals("2010-10-09T22:00:00.000Z"));

		// Format: yyyy-mm-ddThh.mm:ss.ms[+hh:mm|=+hh:mm] Time zone
		jodaISODate = JODAISODate
				.parseISODateTime("2010-10-10T00:00:00.000+02:00");
		assertTrue(jodaISODate.equals("2010-10-09T22:00:00.000Z"));

		// Format: yyyy-mm-ddThh.mm:ssZ (UTC)
		jodaISODate = JODAISODate.parseISODateTime("2010-10-10T00:00:00Z");
		assertTrue(jodaISODate.equals("2010-10-10T00:00:00.000Z"));

		// Format: yyyy-mm-ddThh.mm:ss.msZ (UTC)
		jodaISODate = JODAISODate.parseISODateTime("2010-10-10T00:00:00.000Z");
		assertTrue(jodaISODate.equals("2010-10-10T00:00:00.000Z"));
	}

	public void testParsegYearMonthDateTime() throws Exception {
		// xs:gYearMonth
		for (int i = 0; i < 10; i++) {
			String year = "20" + getRandom(1, 0) + getRandom(9, 0);
			String month = "0" + getRandom(9, 1);
			String hour = getRandom(1, 0) + "" + getRandom(9, 0);
			String minutes = getRandom(5, 1) + "" + getRandom(9, 0);

			// Format: yyyy-MM-hh:mm Time zone
			String tmp = year + "-" + month + "-" + hour + ":" + minutes + "Z";
			String jodaISODate = JODAISODate.parseISODateTime(tmp);
			assertEquals(jodaISODate, year + "-" + month + "-01T" + hour + ":"
					+ minutes + ":00.000Z");

			year = "20" + getRandom(1, 0) + getRandom(9, 0);
			month = "0" + getRandom(9, 1);
			// Format: yyyy-MM
			tmp = year + "-" + month;
			jodaISODate = JODAISODate.parseISODateTime(tmp);
			assertEquals(jodaISODate, tmp + "-01T00:00:00.000Z");

		}
	}

	public void testParsegYearDateTime() throws Exception {
		// xs:gYear
		for (int i = 0; i < 10; i++) {
			String year = "20" + getRandom(1, 0) + getRandom(9, 0);
			String hour = getRandom(1, 0) + "" + getRandom(9, 0);
			String minutes = getRandom(5, 1) + "" + getRandom(9, 0);

			// Format: yyyy-hh:mm Time zone
			String jodaISODate = JODAISODate.parseISODateTime(year + "-" + hour
					+ ":" + minutes + "Z");
			assertEquals(jodaISODate, year + "-01-01T" + hour + ":" + minutes
					+ ":00.000Z");

			year = "20" + getRandom(1, 0) + getRandom(9, 0);

			// Format: yyyy
			jodaISODate = JODAISODate.parseISODateTime(year);
			assertEquals(jodaISODate, year + "-01-01T00:00:00.000Z");
		}
	}

	private String getRandom(int max, int min) {
		return Integer
				.toString(min + (int) (Math.random() * ((max - min) + 1)));
	}
}
