package org.fao.geonet.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

public class DateUtilTest {

    @Test
    public void convertToISOZuluDateTime() {
        // Format: yyyy-mm-ddThh.mm:ss[+hh:mm|=+hh:mm] Time zone
        String datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2010-10-10T00:00:00+02:00");
        assertEquals("2010-10-09T22:00:00.000Z", datetimeInIsoFormat);

        // Format: yyyy-mm-ddThh.mm:ss.ms[+hh:mm|=+hh:mm] Time zone
        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2010-10-10T00:00:00.000+02:00");
        assertEquals("2010-10-09T22:00:00.000Z", datetimeInIsoFormat);

        // Format: yyyy-mm-ddThh.mm:ssZ (UTC)
        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2010-10-10T00:00:00Z");
        assertEquals("2010-10-10T00:00:00.000Z", datetimeInIsoFormat);

        // Format: yyyy-mm-ddThh.mm:ss.msZ (UTC)
        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2010-10-10T00:00:00.000Z");
        assertEquals("2010-10-10T00:00:00.000Z", datetimeInIsoFormat);

        // Format: yyyy-mm-ddThh.mm:ss.msZ (UTC) (ms with less than 3 digits)
        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2020-11-23T07:41:29.1Z");
        assertEquals("2020-11-23T07:41:29.100Z", datetimeInIsoFormat);
        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2020-11-23T07:41:29.12Z");
        assertEquals("2020-11-23T07:41:29.120Z", datetimeInIsoFormat);
        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2020-11-23T07:41:29.123Z");
        assertEquals("2020-11-23T07:41:29.123Z", datetimeInIsoFormat);

        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2020-11-23");
        LocalDate ld = LocalDate.parse("2020-11-23");
        ZonedDateTime zdt = ld.atStartOfDay(ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneOffset.UTC);
        assertEquals(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS.format(zdt), datetimeInIsoFormat);

        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2020-12-31");
        ld = LocalDate.parse("2020-12-31");
        zdt = ld.atStartOfDay(ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneOffset.UTC);
        assertEquals(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS.format(zdt), datetimeInIsoFormat);

        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2020-11");
        ld = LocalDate.of(2020, 11, 1);
        zdt = ld.atStartOfDay(ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneOffset.UTC);
        assertEquals("Testing 2020-11", DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS.format(zdt), datetimeInIsoFormat);

        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2012-09-12Z");
        ld = LocalDate.parse("2012-09-12");
        zdt = ld.atStartOfDay(ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneOffset.UTC);
        assertEquals(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS.format(zdt), datetimeInIsoFormat);

        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2012-12-31Z");
        ld = LocalDate.parse("2012-12-31");
        zdt = ld.atStartOfDay(ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneOffset.UTC);
        assertEquals(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS.format(zdt), datetimeInIsoFormat);

        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("2015-10-18T19:59:30.2675269Z");
        assertEquals("2015-10-18T19:59:30.2675269Z", datetimeInIsoFormat);

        datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime("20170322");
        ld = LocalDate.parse("2017-03-22");
        zdt = ld.atStartOfDay(ZoneId.systemDefault());
        zdt = zdt.withZoneSameInstant(ZoneOffset.UTC);
        assertEquals(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS.format(zdt), datetimeInIsoFormat);
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
            String dateTimeInIsoFormat = DateUtil.convertToISOZuluDateTime(tmp);
            assertEquals("Testing " + tmp, dateTimeInIsoFormat, year + "-" + month + "-" + "01T" + hour + ":" + minutes + ":00.000Z");

            tmp = year + "-" + month + "-" + hour + ":" + minutes;
            dateTimeInIsoFormat = DateUtil.convertToISOZuluDateTime(tmp);
            String expected = ZonedDateTime.of(Integer.parseInt(year), Integer.parseInt(month), 1, Integer.parseInt(hour), Integer.parseInt(minutes), 0, 0, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS);
            assertEquals("Testing " + tmp, expected, dateTimeInIsoFormat);

            year = "20" + getRandom(1, 0) + getRandom(9, 0);
            month = "0" + getRandom(9, 1);
            // Format: yyyy-MM
            tmp = year + "-" + month;
            expected = ZonedDateTime.of(Integer.parseInt(year), Integer.parseInt(month), 1, 0, 0, 0, 0, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS);
            dateTimeInIsoFormat = DateUtil.convertToISOZuluDateTime(tmp);
            assertEquals("Testing " + tmp, expected , dateTimeInIsoFormat);

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
            String tmp = year + "-" + hour + ":" + minutes + "Z";
            String datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime(tmp);
            assertEquals("Testing " + tmp, year + "-01-01T" + hour + ":" + minutes + ":00.000Z", datetimeInIsoFormat);

            year = "20" + getRandom(1, 0) + getRandom(9, 0);

            // Format: yyyy
            datetimeInIsoFormat = DateUtil.convertToISOZuluDateTime(year);
            String expected = ZonedDateTime.of(Integer.parseInt(year), 1, 1 ,0, 0 , 0, 0, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS);
            assertEquals("Testing " + year, expected, datetimeInIsoFormat);
        }
    }

    @Test
    public void parseISODateTimes() {
    }

    @Test
    public void parseBasicOrFullDateTime() {
    }

    @Test
    public void parseTime() {
    }

    @Test
    public void generateDate() {
    }

    @Test
    public void testGenerateDate() {
    }

    private String getRandom(int max, int min) {
        return Integer.toString(min + (int) (Math.random() * ((max - min) + 1)));
    }
}
