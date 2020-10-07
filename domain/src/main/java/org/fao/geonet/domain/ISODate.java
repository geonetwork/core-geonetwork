//==============================================================================
//===
//===   ISODate
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

package org.fao.geonet.domain;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * Represents a date at a given time. Provides methods for representing the date as a string and
 * parsing from string.
 * <p>String format is: {@code yyyy-mm-ddThh:mm:ss}.</p>
 */
@Embeddable
@XmlRootElement
public class ISODate implements Cloneable, Comparable<ISODate>, Serializable, XmlEmbeddable {
    private static final String DEFAULT_DATE_TIME = "3000-01-01T00:00:00.000Z"; // JUNK

    public static final DateTimeFormatter ISO_OFFSET_DATE_TIME_NANOSECONDS;
    public static final DateTimeFormatter YEAR_MONTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter YEAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    public static final DateTimeFormatter YEAR_MONTH_DAYS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {
        ISO_OFFSET_DATE_TIME_NANOSECONDS = new DateTimeFormatterBuilder().parseCaseInsensitive().append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral('T').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2)
                .appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).appendFraction(NANO_OF_SECOND, 3, 9, true).appendOffsetId()
                .toFormatter();
    }

    // Pattern to check dates
    @XmlTransient private static final Pattern gsYear = Pattern.compile("([0-9]{4})(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");
    @XmlTransient private static final Pattern gsYearMonth = Pattern.compile("([0-9]{4})-([0-1][0-9])(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");

    // Fri Jan 01 2010 00:00:00 GMT+0100 (CET)
    @XmlTransient private static final Pattern htmlFormat = Pattern
            .compile("([a-zA-Z]{3}) ([a-zA-Z]{3}) ([0-9]{2}) ([0-9]{4}) ([0-2][0-9]):([0-5][0-9]):([0-5][0-9]) (.+)");

    @XmlTransient private boolean _shortDate; // --- 'true' if the format is yyyy-mm-dd

    @XmlTransient private boolean _shortDateYear; // --- 'true' if the format is yyyy

    @XmlTransient private boolean _shortDateYearMonth; // --- 'true' if the format is yyyy-mm

    @XmlTransient
    //private Calendar _calendar = Calendar.getInstance();
    private ZonedDateTime internalDateTime;

    // ---------------------------------------------------------------------------
    // ---
    // --- Constructor
    // ---
    // ---------------------------------------------------------------------------

    /**
     * Constructs an instance of <code>ISODate</code> with current date, time
     * and system default timezone.
     */
    public ISODate() {
        internalDateTime = ZonedDateTime.now();
    }

    // ---------------------------------------------------------------------------
    /**
     * Constructs an instance of {@code ISODate} with the time and format passed as parameters, local timezone.
     *
     * @param timeInEpochMillis milliseconds passed from 1970-01-01T00:00:00Z (Unix epoch).
     * @param shortDate         {@code true} if the format is {@code yyyy-mm-dd}.
     */
    public ISODate(final long timeInEpochMillis, final boolean shortDate) {
        Instant instantParam = Instant.ofEpochMilli(timeInEpochMillis);
        internalDateTime = ZonedDateTime.ofInstant(instantParam, ZoneOffset.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
        _shortDate = shortDate;
    }

    /**
     * Constructs an instance of {@code ISODate} with the time passed as parameter and long format  (UTC).
     *
     * @param timeInEpochMillis milliseconds passed from 1970-01-01T00:00:00Z (Unix epoch).
     */
    public ISODate(final long timeInEpochMillis) {
        Instant instantParam = Instant.ofEpochMilli(timeInEpochMillis);
        internalDateTime = ZonedDateTime.ofInstant(instantParam, ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        _shortDate = false;
    }

    /**
     * Constructs an instance of {@code ISODate} parsing the value passed as parameter.
     *
     * @param isoDateString the string to be parsed.
     */
    public ISODate(@Nonnull final String isoDateString) {
        setDateAndTime(isoDateString);
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
        String parsedDateTime = parseISODateTimes(stringToParse, null);
        if (parsedDateTime.equals(DEFAULT_DATE_TIME)) {
            return null;
        } else {
            return parsedDateTime;
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
     * @return A date and time in ISO 8601 format or {@link ISODate#DEFAULT_DATE_TIME} if the
     * parameters cannot be parsed as valid values. If {@code durationOrDateTimeString} is {@code null}
     * then it is ignored.
     * If it is a duration then its value is added or subtracted to {@code dateTimeString} and the result
     * is concatenated to the parsed value of {@code dateTimeString} using the {@code "|"} as separator character.
     * If it is a dateTime string then the result is the parsed value of {@code dateTimeString}, a {@code "|"}
     * character and the parsed value of {@code durationOrDateTimeString}.
     */
    public static String parseISODateTimes(String dateTimeString, String durationOrDateTimeString) {
        //DateTimeFormatter dto =ISODateTimeFormat.dateTime();
        DateTimeFormatter dto = DateTimeFormatter.ISO_DATE_TIME;
        //PeriodFormatter p = ISOPeriodFormat.standard();
        ZonedDateTime odt1;
        String odt;

        // dateTimeString should be some sort of ISO time
        // eg. basic: 20080909, full: 2008-09-09T12:21:00 etc
        // convert everything to UTC so that we remove any timezone
        // problems
        try {
            ZonedDateTime idt = parseBasicOrFullDateTime(dateTimeString);
            //odt1 = dto.parseDateTime(idt.toString())
            //    .withZone(DateTimeZone.forID("UTC"));
            odt1 = idt.withZoneSameInstant(ZoneOffset.UTC);
            //odt = odt1.toString();
            odt = idt.withZoneSameInstant(ZoneOffset.UTC).format(ISO_OFFSET_DATE_TIME_NANOSECONDS);

        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error parsing ISO DateTimes, error: " + e.getMessage(), e);
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

                // Period ip =p.parsePeriod(durationOrDateTimeString);
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
                //DateTime odt2 = dto.parseDateTime(idt.toString())
                //    .withZone(DateTimeZone.forID("UTC"));
                odt = odt + "|" + odt2.toString();
            }
        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error parsing ISO DateTimes, error: " + e.getMessage(), e);
            return odt + "|" + DEFAULT_DATE_TIME;
        }

        return odt;
    }

    /**
     * Parse the parameter and return a {@link ZonedDateTime} object.
     *
     * @param stringToParse the string to parse. It can have multiple formats:
     *                      {@link DateTimeFormatter#BASIC_ISO_DATE}, {@link DateTimeFormatter#ISO_TIME},
     *                      {@link DateTimeFormatter#ISO_DATE_TIME}, or {@code yyyy[-MM][-dd['T'HH[:mm[:ss[.SSS]][XXX]]]]}.
     *                      Stings in HTML format (e.g. {@code Fri Jan 01 2010 00:00:00 GMT+0100 (CET)}) are also accepted.
     * @return a {@link ZonedDateTime}
     */
    public static ZonedDateTime parseBasicOrFullDateTime(String stringToParse) {
        final DateTimeFormatter catchAllDateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive()
                .appendPattern("yyyy[-M][-d['T'H[:m[:s[.SSS]][XXX]]]]").parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1).parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .parseDefaulting(ChronoField.OFFSET_SECONDS, ZoneOffset.of("Z").getTotalSeconds()).toFormatter();
        //DateTimeFormatter catchAllDateTimeFormatter = ISODateTimeFormat.dateTimeParser();
        //DateTime result;
        ZonedDateTime result;
        Matcher matcher;
        if (stringToParse.length() == 8 && !stringToParse.startsWith("T")) {
            //result = basicIsoDateFormatter.parseDateTime(stringToParse);
            result = ZonedDateTime.parse(stringToParse, DateTimeFormatter.BASIC_ISO_DATE);
        } else if (stringToParse.startsWith("T") && !stringToParse.contains(":")) {
            //result = isoTimeFormatter.parseDateTime(stringToParse);
            result = ZonedDateTime.parse(stringToParse, DateTimeFormatter.ISO_TIME);
        } else if (stringToParse.contains("T") && !stringToParse.contains(":") && !stringToParse.contains("-")) {
            //result = isoDateTimeFormatter.parseDateTime(stringToParse);
            result = ZonedDateTime.parse(stringToParse, DateTimeFormatter.ISO_DATE_TIME);
        } else if ((matcher = gsYearMonth.matcher(stringToParse)).matches()) {
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

            result = generateDate(year, month, minute, hour, timezone);
        } else if ((matcher = gsYear.matcher(stringToParse)).matches()) {
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

            result = generateDate(year, month, minute, hour, timezone);
        } else if ((matcher = htmlFormat.matcher(stringToParse)).matches()) {
            // Fri Jan 01 2010 00:00:00 GMT+0100 (CET)
            String month = matcher.group(2);
            switch (month) {
            case "Jan":
                month = "1";
                break;
            case "Feb":
                month = "2";
                break;
            case "Mar":
                month = "3";
                break;
            case "Apr":
                month = "4";
                break;
            case "May":
                month = "5";
                break;
            case "Jun":
                month = "6";
                break;
            case "Jul":
                month = "7";
                break;
            case "Aug":
                month = "8";
                break;
            case "Sep":
                month = "9";
                break;
            case "Oct":
                month = "10";
                break;
            case "Nov":
                month = "11";
                break;
            default:
                month = "12";
                break;
            }
            String day = matcher.group(3);
            String year = matcher.group(4);
            String hour = matcher.group(5);
            String minute = matcher.group(6);
            String second = matcher.group(7);
            String timezone = matcher.group(8);

            result = generateDate(year, month, day, second, minute, hour, timezone);
        } else {
            //result = catchAllDateTimeFormatter.parseDateTime(stringToParse);
            result = ZonedDateTime.parse(stringToParse, catchAllDateTimeFormatter);
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
    private static ZonedDateTime generateDate(String year, String month, String day, String second, String minute, String hour,
            String timezone) {
        TimeZone zone = TimeZone.getTimeZone(timezone);
        ZonedDateTime result = ZonedDateTime
                .of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), Integer.parseInt(hour),
                        Integer.parseInt(minute), Integer.parseInt(second), 0, zone.toZoneId());

        //return new DateTime(c.getTimeInMillis());
        return result.withZoneSameInstant(TimeZone.getDefault().toZoneId());

    }

    /**
     * Generate a datetime with the parameters used. Sets the day as the first of the month.
     *
     * @param year     the year
     * @param month    the month, from 1 to 12
     * @param minute   the minute of the hour, from 0 to 59
     * @param hour     the hour of the day, from 0 to 23
     * @param timezone the timezone
     * @return a new {@link ZonedDateTime} with the values passed and the day as teh first day of the month
     */
    private static ZonedDateTime generateDate(String year, String month, String minute, String hour, String timezone) {

        return generateDate(year, month, "1", "00", minute, hour, timezone);
    }

    /**
     * Creates a clone of the object.
     *
     * @return a copy of the object
     */
    public ISODate clone() {
        ISODate clone;
        try {
            clone = (ISODate) super.clone();
            // Since java.time.ZonedDateTime is immutable we don't need to duplicate the var,
            // just assign it to the new ISODate instance.
            clone.internalDateTime = internalDateTime;

            clone._shortDate = _shortDate;
            clone._shortDateYear = _shortDateYear;
            clone._shortDateYearMonth = _shortDateYearMonth;
            return clone;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * Subtract a date from this date and return the seconds between them.
     * Value can be negative if parameter is before the caller.
     *
     * @param date the ISODate to subtract to this.
     */
    public long timeDifferenceInSeconds(ISODate date) {
        return ChronoUnit.SECONDS.between(date.internalDateTime, this.internalDateTime);

    }

    /**
     * Return a String representing the date part of this object.
     *
     * @return a string representing the date part of this object. The format can be
     * {@code yyyy}, {@code yyyy-MM} or {@code yyyy-MM-dd} depending on the date parsed
     * when the object was built.
     */
    @Transient
    public String getDateAsString() {
        if (_shortDateYearMonth) {
            return internalDateTime.format(YEAR_MONTH_DATE_FORMATTER);
        } else if (_shortDateYear) {
            return internalDateTime.format(YEAR_DATE_FORMATTER);
        } else {
            return internalDateTime.format(YEAR_MONTH_DAYS_DATE_FORMATTER);
        }
    }

    // --------------------------------------------------------------------------

    public String toString() {
        return getDateAndTime();
    }

    /**
     * Create a {@link Date} object from this.
     *
     * @return an old {@code java.utilDate} object from this.
     */
    public Date toDate() {
        return java.util.Date.from(internalDateTime.toInstant());
    }

    /**
     * @return Return the number of seconds since epoch of 1970-01-01T00:00:00Z.
     */
    @Transient
    public long getTimeInSeconds() {
        return internalDateTime.toEpochSecond();
    }

    /**
     * Get the Time and Date encoded as a String.
     */
    @XmlValue
    public String getDateAndTime() {
        if (_shortDate || _shortDateYearMonth || _shortDateYear) {
            return getDateAsString();
        } else {
            return internalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            // return getDateAsString() + "T" + getTimeAsString();
        }
    }

    public void setDateAndTime(String isoDate) {

        String timeAndDate = isoDate;
        if (timeAndDate == null) {
            throw new IllegalArgumentException("Date string is null");
        }

        int indexOfT = timeAndDate.indexOf('T');
        if (indexOfT > -1) {
            // Check if ISO date contains time info and if using non UTC time
            // zone. This class converts to UTC format to avoid timezones
            // issues.
            String afterT = timeAndDate.substring(indexOfT + 1);
            boolean timeZoneInfo = afterT.contains("+") || afterT.contains("-")
                || afterT.toUpperCase().endsWith("Z");

            if (timeZoneInfo) {
                timeAndDate = convertToISOZuluDateTime(timeAndDate);
            }
        }
        if (timeAndDate == null) {
            throw new IllegalArgumentException("Not parsable date: " + isoDate);
        }

        String[] parts = timeAndDate.toUpperCase().split("T", 2);

        if (parts.length == 1) {
            if (parts[0].contains(":")) {
                parseTime(parts[0]);
            } else {
                parseDate(parts[0]);
            }
        } else {
            if (!parts[0].trim().isEmpty()) {
                parseDate(parts[0]);
            }
            if (!parts[1].trim().isEmpty()) {
                parseTime(parts[1]);
            }
        }
    }

    /**
     * This method returns {@code true} if this object is only the date (year, month, day) and time should be ignored.
     *
     * @return true if this object is only the date (year, month, day) and time should be ignored
     */
    @Transient
    public boolean isDateOnly() {
        return _shortDate;
    }

    /**
     * This method returns {@code true} if this object is only the date (year) and time should be ignored.
     *
     * @return true if this object is only the date (year) and time should be ignored
     */
    @Transient
    public boolean isDateYearOnly() {
        return _shortDateYear;
    }

    /**
     * This method returns {@code true} if this object is only the date (year, month) and time should be ignored.
     *
     * @return true if this object is only the date (year, month) and time should be ignored
     */
    @Transient
    public boolean isDateYearMonthOnly() {
        return _shortDateYearMonth;
    }

    /**
     * This method returns the date's year.
     *
     * @return the year
     */
    @Transient
    public int getYears() {
        return internalDateTime.getYear();
    }

    /**
     * This method returns the date's day of month starting at 1.
     *
     * @return the day of the month from 1 to 31
     */
    @Transient
    public int getDays() {
        return internalDateTime.getDayOfMonth();
    }

    /**
     * Get the date's month of the year starting at 1 and going to 12.
     *
     * @return the number of the month from 1 (January) to 12 (December)
     */
    @Transient
    public int getMonths() {
        return internalDateTime.getMonth().getValue();
    }

    /**
     * Get the date's hour-of-day, from 0 to 23.
     *
     * @return the hour-of-day from 0 to 23
     */
    @Transient
    public int getHours() {
        return internalDateTime.getHour();
    }

    /**
     * Get the date's minute starting at 0 and going to 59.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    @Transient
    public int getMinutes() {
        return internalDateTime.getMinute();
    }

    /**
     * Get the date's second starting at 0 and going to 59.
     *
     * @return the second-of-minute, from 0 to 59
     */
    @Transient
    public int getSeconds() {
        return internalDateTime.getSecond();
    }

    @Override
    public int hashCode() {
        return getTimeAsString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ISODate other = (ISODate) obj;
        return getTimeAsString().equals(other.getTimeAsString());
    }

    private void parseDate(@Nonnull String isoDate) {
        try {
            String[] parts = isoDate.split("[-/]");
            if ((parts.length == 0) || (parts.length > 3)) {
                throw new IllegalArgumentException("Invalid ISO date: " + isoDate);
            }

            _shortDate = (parts.length == 3);
            _shortDateYearMonth = (parts.length == 2);
            _shortDateYear = (parts.length == 1);

            int year;
            if (parts[0].length() < 4) {
                int shortYear = Integer.parseInt(parts[0]);
                String thisYear = String.valueOf(ZonedDateTime.now(ZoneOffset.UTC).getYear());
                int century = Integer.parseInt(thisYear.substring(0, 2)) * 100;
                int yearInCentury = Integer.parseInt(thisYear.substring(2));

                if (shortYear <= yearInCentury) {
                    year = century + shortYear;
                } else {
                    year = century - 100 + shortYear;
                }
            } else {
                year = Integer.parseInt(parts[0]);
            }

            int month;
            if (_shortDate || _shortDateYearMonth) {
                month = Integer.parseInt(parts[1]);
            } else {
                month = 12;
            }

            int day;
            if (_shortDate) {

                if (parts[2].toLowerCase().endsWith("z")) {
                    day = Integer.parseInt(parts[2].substring(0, parts[2].length() - 1));
                } else {
                    day = Integer.parseInt(parts[2]);
                }
            } else {
                // Calculate the last day for the year/month
                day = YearMonth.of(year, month).atEndOfMonth().getDayOfMonth();
            }

            _shortDate = true;

            int hour = 0;
            int minute = 0;
            int second = 0;
            internalDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneOffset.UTC);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO date: " + isoDate, e);
        }
    }

    private void parseTime(@Nonnull String isoDate) {
        try {
            String[] parts = isoDate.split(":");
            if (parts.length == 1 || parts.length > 3) {
                throw new IllegalArgumentException("Invalid ISO date: " + isoDate);
            }

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = 0;
            if (parts.length == 3) {
                String secondsToParse = parts[2];
                int indexOfZ = secondsToParse.toUpperCase().indexOf('Z');
                if (indexOfZ > -1) {
                    secondsToParse = secondsToParse.substring(0, indexOfZ);
                    internalDateTime = internalDateTime.withZoneSameInstant(ZoneOffset.UTC);
                }

                second = (int) Float.parseFloat(secondsToParse);
            }

            internalDateTime = internalDateTime.with(HOUR_OF_DAY, hour).with(MINUTE_OF_HOUR, minute).with(SECOND_OF_MINUTE, second)
                    .with(MILLI_OF_SECOND, 0).with(NANO_OF_SECOND, 0);

            _shortDate = false;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO date: " + isoDate, e);
        }
    }

    @Transient
    public String getTimeAsString() {
        return pad(getHours()) + ":" + pad(getMinutes()) + ":" + pad(getSeconds());
    }

    private String pad(int value) {
        if (value > 9)
            return Integer.toString(value);

        return "0" + value;
    }

    @Override
    public int compareTo(ISODate other) {
        return this.internalDateTime.compareTo(other.internalDateTime);
    }

    @Override
    public void addToXml(Element element) {
        if (isDateOnly()) {
            element.addContent(getDateAsString());
        } else {
            element.addContent(getDateAndTime());
        }
    }
}
