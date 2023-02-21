/*
 * Copyright (C) 2001-2020 Food and Agriculture Organization of the
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
package org.fao.geonet.utils;

import org.apache.commons.lang.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * This class contains some utils related with dates and times.s
 */
public class DateUtil {
    public static final DateTimeFormatter ISO_OFFSET_DATE_TIME_NANOSECONDS;
    public static final DateTimeFormatter CATCH_ALL_DATE_TIME_FORMATTER;
    private static final String DEFAULT_DATE_TIME = "3000-01-01T00:00:00.000Z"; // JUNK

    // Pattern to check dates
    private static final Pattern gsYear = Pattern.compile("([0-9]{4})(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");
    private static final Pattern gsYearMonth = Pattern.compile("([0-9]{4})-([0-1][0-9])(-([0-2][0-9]):([0-5][0-9])([A-Z]{0,1}))?");

    // Some catalogs are using 2012-09-12Z
    private static final Pattern gsYearMonthDayZ = Pattern.compile("([0-9]{4})-([0-1][0-9])-([0-3][0-9])Z");
    private static final Pattern gsDayMonthYear = Pattern.compile("([0-3][0-9])/([0-1][0-9])/([0-9]{4})");

    // Fri Jan 01 2010 00:00:00 GMT+0100 (CET)
    private static final Pattern htmlFormat = Pattern
            .compile("([a-zA-Z]{3}) ([a-zA-Z]{3}) ([0-9]{2}) ([0-9]{4}) ([0-2][0-9]):([0-5][0-9]):([0-5][0-9]) (.+)");

    static {
        ISO_OFFSET_DATE_TIME_NANOSECONDS = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendFraction(NANO_OF_SECOND, 3, 9, true)
            .appendOffsetId()
            .toFormatter();

        CATCH_ALL_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .appendPattern("yyyy[[-]M][[-]d['T'H[:m[:s[.SSSSSSSSS][.SSSSSSSS][.SSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]][XXX]]]]")
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(HOUR_OF_DAY, 0)
            .parseDefaulting(MINUTE_OF_HOUR, 0)
            .parseDefaulting(SECOND_OF_MINUTE, 0)
            .parseDefaulting(NANO_OF_SECOND, 0).toFormatter();
    }

    /**
     * Converts a given ISO date time into the form used to index in Lucene.
     * Returns null if it gets back a ridiculous value.
     *
     * @param stringToParse the string to parse.
     * @return the parameter parsed and shown with {@code yyyy-MM-dd'T'HH:mm:ss.SSSXXX'Z'} format
     * or {@code null} if it can't be parsed.
     */
    public static String convertToISOZuluDateTime(final String stringToParse) {
        if (StringUtils.trimToNull(stringToParse) == null) {
            return stringToParse;
        }
        try {
            String parsedDateTime = parseISODateTimes(StringUtils.trim(stringToParse), null);
            if (parsedDateTime.equals(DEFAULT_DATE_TIME)) {
                return null;
            } else {
                return parsedDateTime;
            }
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Converts two ISO date times into standard form used to index in Lucene
     * Always returns something because it is used during the indexing of the
     * metadata record in Lucene - if exception during parsing then it is
     * something ridiculous like JUNK value above.
     *
     * @param dateTimeString           a string containing a date or a date with time in ISO 8601 format.
     * @param durationOrDateTimeString a string containing datetime or a duration in ISO 8601 format
     *                                 ({@code P[n]Y[n]M[n]DT[n]H[n]M[n]S or P[n]W}. For example,
     *                                 {@code "P3Y6M4DT12H30M5S"} represents a duration of
     *                                 "three years, six months, four days, twelve hours, thirty
     *                                 minutes, and five seconds").
     * @return A date and time in ISO 8601 format or {@link DateUtil#DEFAULT_DATE_TIME} if the
     * parameters cannot be parsed as valid values. If {@code durationOrDateTimeString} is {@code null}
     * then it is ignored.
     * If it is a duration then its value is added or subtracted to {@code dateTimeString} and the result
     * is concatenated to the parsed value of {@code dateTimeString} using the {@code "|"} as separator character.
     * If it is a dateTime string then the result is the parsed value of {@code dateTimeString}, a {@code "|"}
     * character and the parsed value of {@code durationOrDateTimeString}.
     */
    public static String parseISODateTimes(String dateTimeString, String durationOrDateTimeString) {

        ZonedDateTime odt1;
        String odt;

        // dateTimeString should be some sort of ISO time
        // eg. basic: 20080909, full: 2008-09-09T12:21:00 etc
        // convert everything to UTC so that we remove any timezone
        // problems
        try {
            ZonedDateTime idt = parseBasicOrFullDateTime(dateTimeString);

            odt1 = idt.withZoneSameInstant(ZoneOffset.UTC);
            odt = idt.withZoneSameInstant(ZoneOffset.UTC).format(ISO_OFFSET_DATE_TIME_NANOSECONDS);
        } catch (Exception e) {
            Log.error("geonetwork.domain",
                String.format("Error parsing ISO DateTimes '%s'. Error is: %s",
                    dateTimeString, e.getMessage()), e);
            return DEFAULT_DATE_TIME;
        }

        if (StringUtils.isBlank(durationOrDateTimeString))
            return odt;

        // durationOrDateTimeString can be an ISO time as for dateTimeString but also an ISO time period
        // eg. -P3D or P3D - if an ISO time period then it must be added to the
        // DateTime generated for dateTimeString (odt1)
        // convert everything to UTC so that we remove any timezone
        // problems
        try {
            boolean minus = false;
            if (durationOrDateTimeString.startsWith("-P")) {
                durationOrDateTimeString = durationOrDateTimeString.substring(1);
                minus = true;
            }

            if (durationOrDateTimeString.startsWith("P")) {
                String[] periodAndDurationArray = durationOrDateTimeString.split("T");
                String periodString = periodAndDurationArray[0];
                String durationString = "PT" + periodAndDurationArray[1];

                Period ip = Period.parse(periodString);
                Duration duration = Duration.parse(durationString);
                ZonedDateTime odt2;
                if (!minus) {
                    odt2 = odt1.plus(ip).plus(duration);
                } else {
                    odt2 = odt1.minus(ip).minus(duration);
                }
                odt = odt + "|" + odt2.toString();
            } else {
                ZonedDateTime idt = parseBasicOrFullDateTime(durationOrDateTimeString);
                ZonedDateTime odt2 = idt.withZoneSameInstant(ZoneOffset.UTC);
                odt = odt + "|" + odt2.toString();
            }
        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error parsing ISO DateTimes, error: " + e.getMessage(), e);
            return odt + "|" + DEFAULT_DATE_TIME;
        }

        return odt;
    }

    /**
     * Parse the parameter and return a {@link ZonedDateTime} object. For Dates in {@code "yyyy"} or {@code "yyyy-MM"} format
     * the dates are considered at the first instant  of the year or year and month (in local time if no zone is specified.
     *
     * @param stringToParse the string to parse. It can have multiple formats:
     *                      {@link DateTimeFormatter#BASIC_ISO_DATE}, {@link DateTimeFormatter#ISO_TIME},
     *                      {@link DateTimeFormatter#ISO_DATE_TIME}, or {@code yyyy[-MM][-dd['T'HH[:mm[:ss[.SSS]][XXX]]]]}.
     *                      Stings in HTML format (e.g. {@code Fri Jan 01 2010 00:00:00 GMT+0100 (CET)}) are also accepted.
     *                      Also string in no ISO format (e.g. {@code yyyy[-MM][-dd][-hh:mm][Z]})
     * @return a {@link ZonedDateTime}.
     */
     public static ZonedDateTime parseBasicOrFullDateTime(String stringToParse) {
        ZonedDateTime result;
        Matcher matcher;
        if (stringToParse.length() == 8 && !stringToParse.startsWith("T")) {
            result = LocalDate
                .parse(stringToParse, DateTimeFormatter.BASIC_ISO_DATE)
                .atStartOfDay(ZoneId.systemDefault());
        } else if (stringToParse.startsWith("T") && stringToParse.contains(":")) {
            result = parseTime(stringToParse);
        } else if (stringToParse.contains("T") && !stringToParse.contains(":") && !stringToParse.contains("-")) {
            result = ZonedDateTime.parse(stringToParse, DateTimeFormatter.ISO_DATE_TIME);
        } else if ((matcher = gsYearMonth.matcher(stringToParse)).matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = 1;
            int minute = 0;
            int hour = 0;
            int seconds = 0;
            String timezone = ZoneId.systemDefault().getId();
            if (matcher.group(4) != null) {
                minute = Integer.parseInt(matcher.group(5));
                hour = Integer.parseInt(matcher.group(4));
                if (StringUtils.isNotBlank(matcher.group(6))) {
                    timezone = matcher.group(6);
                }
            }

            result = generateDate(year, month, day, seconds, minute, hour, timezone);
        } else if ((matcher = gsYear.matcher(stringToParse)).matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = 1;
            int day = 1;
            int minute = 0;
            int hour = 0;
            int seconds = 0;
            String timezone = ZoneId.systemDefault().getId();
            if (matcher.group(3) != null) {
                minute = Integer.parseInt(matcher.group(4));
                hour = Integer.parseInt(matcher.group(3));
                if (StringUtils.isNotBlank(matcher.group(5))) {
                    timezone = matcher.group(5);
                }
            }

            result = generateDate(year, month, day, seconds, minute, hour, timezone);
        } else if ((matcher = gsYearMonthDayZ.matcher(stringToParse)).matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            int minute = 0;
            int hour = 0;
            int seconds = 0;
            String timezone = ZoneId.systemDefault().getId();
            result = generateDate(year, month, day, seconds, minute, hour, timezone);
        } else if ((matcher = gsDayMonthYear.matcher(stringToParse)).matches()) {
            int year = Integer.parseInt(matcher.group(3));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(1));
            int minute = 0;
            int hour = 0;
            int seconds = 0;
            String timezone = ZoneId.systemDefault().getId();
            result = generateDate(year, month, day, seconds, minute, hour, timezone);
        } else if ((matcher = htmlFormat.matcher(stringToParse)).matches()) {
            // Fri Jan 01 2010 00:00:00 GMT+0100 (CET)
            String monthParsed = matcher.group(2);
            int month;
            switch (monthParsed) {
            case "Jan":
                month = 1;
                break;
            case "Feb":
                month = 2;
                break;
            case "Mar":
                month = 3;
                break;
            case "Apr":
                month = 4;
                break;
            case "May":
                month = 5;
                break;
            case "Jun":
                month = 6;
                break;
            case "Jul":
                month = 7;
                break;
            case "Aug":
                month = 8;
                break;
            case "Sep":
                month = 9;
                break;
            case "Oct":
                month = 10;
                break;
            case "Nov":
                month = 11;
                break;
            default:
                month = 12;
                break;
            }
            int day = Integer.parseInt(matcher.group(3));
            int year = Integer.parseInt(matcher.group(4));
            int hour = Integer.parseInt(matcher.group(5));
            int minute = Integer.parseInt(matcher.group(6));
            int second = Integer.parseInt(matcher.group(7));
            String timezone = matcher.group(8);

            result = generateDate(year, month, day, second, minute, hour, timezone);
        } else {
            try {
                TemporalAccessor dt = CATCH_ALL_DATE_TIME_FORMATTER.parseBest(stringToParse, ZonedDateTime::from, LocalDateTime::from);
                if (dt instanceof ZonedDateTime) {
                    result = (ZonedDateTime) dt;
                } else if (dt instanceof LocalDateTime) {
                    LocalDateTime ldt = (LocalDateTime) dt;
                    result = ldt.atZone(TimeZone.getDefault().toZoneId());
                } else {
                    result = null;
                }
            } catch(DateTimeParseException e) {
                result = null;
            }
            //result = ZonedDateTime.parse(stringToParse, CATCH_ALL_DATE_TIME_FORMATTER);
        }
        return result;
    }

    /**
     * Parses a time an return current date at that time. Both local and offset formats are supported, such as '10:15', '10:15:30' or
     * '10:15:30+01:00' starting by 'T' or not. If offset is present then it is used set the date in UTC zone. If the time doesn't have an
     * offset it is interpreted as a time in local time and converted to UTC.
     *
     * @param time the string to parse
     * @return a date and time in UTC zone with date the being the current day.
     * @throws DateTimeParseException if the time can'\t be parsed.
     */
    public static ZonedDateTime parseTime(String time) throws DateTimeParseException {
        ZonedDateTime result;
        TemporalAccessor ta = DateTimeFormatter.ISO_TIME
                .parseBest(StringUtils.removeStartIgnoreCase(time, "T"), OffsetTime::from, LocalTime::from);
        if (ta instanceof OffsetTime) {
            result = ((OffsetTime) ta).atDate(LocalDate.now()).atZoneSameInstant(ZoneOffset.UTC);
        } else if (ta instanceof LocalTime) {
            result = ((LocalTime) ta).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * @param year     the year
     * @param month    the month, from 1 to 12
     * @param day      the day of the month, from 1 to 31
     * @param second   the second of the minute, from 0 to 59
     * @param minute   the minute of the hour, from 0 to 59
     * @param hour     the hour of the day, from 0 to 23.
     * @param timezone the time zone
     * @return a new {@link ZonedDateTime} created with the values passed
     */
    public static ZonedDateTime generateDate(int year, int month, int day, int second, int minute, int hour, String timezone) {
        TimeZone zone = TimeZone.getTimeZone(timezone);

        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, zone.toZoneId());

    }
}
