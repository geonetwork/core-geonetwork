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

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 * Represents a date at a given time. Provides methods for representing the date as a string and
 * parsing from string. <p> String format is: yyyy-mm-ddThh:mm:ss </p>
 */
@Embeddable
@XmlRootElement
public class ISODate
    implements Cloneable, Comparable<ISODate>, Serializable, XmlEmbeddable {
    private static final String DEFAULT_DATE_TIME = "3000-01-01T00:00:00.000Z"; // JUNK
    // Value

    // Pattern to check dates
    @XmlTransient
    private static Pattern gsYear = Pattern
        .compile("([0-9]{4})(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");
    @XmlTransient
    private static Pattern gsYearMonth = Pattern.compile(
        "([0-9]{4})-([0-1][0-9])(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");

    // Fri Jan 01 2010 00:00:00 GMT+0100 (CET)
    @XmlTransient
    private static Pattern htmlFormat = Pattern.compile(
        "([a-zA-Z]{3}) ([a-zA-Z]{3}) ([0-9]{2}) ([0-9]{4}) ([0-2][0-9]):([0-5][0-9]):([0-5][0-9]) (.+)");

    @XmlTransient
    private boolean _shortDate; // --- 'true' if the format is yyyy-mm-dd

    @XmlTransient
    private boolean _shortDateYear; // --- 'true' if the format is yyyy

    @XmlTransient
    private boolean _shortDateYearMonth; // --- 'true' if the format is yyyy-mm

    @XmlTransient
    private Calendar _calendar = Calendar.getInstance();

    // ---------------------------------------------------------------------------
    // ---
    // --- Constructor
    // ---
    // ---------------------------------------------------------------------------

    public ISODate() {
    }

    // ---------------------------------------------------------------------------

    public ISODate(final long time, final boolean shortDate) {
        _calendar.setTimeInMillis(time);
        _shortDate = shortDate;
    }

    public ISODate(final long time) {
        _calendar.setTimeInMillis(time);
        _shortDate = false;
    }

    // ---------------------------------------------------------------------------

    public ISODate(@Nonnull final String isoDate) {
        setDateAndTime(isoDate);
    }

    /**
     * Converts a given ISO date time into the form used to index in Lucene. Returns null if it gets
     * back a ridiculous value
     *
     * @param stringToParse the string to parse
     */
    public static String parseISODateTime(final String stringToParse) {
        String newDateTime = parseISODateTimes(stringToParse, null);
        if (newDateTime.equals(DEFAULT_DATE_TIME)) {
            return null;
        } else {
            return newDateTime;
        }
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
            odt1 = dto.parseDateTime(idt.toString())
                .withZone(DateTimeZone.forID("UTC"));
            odt = odt1.toString();

        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error parsing ISO DateTimes, error: " + e.getMessage(), e);
            return DEFAULT_DATE_TIME;
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
                DateTime odt2 = dto.parseDateTime(idt.toString())
                    .withZone(DateTimeZone.forID("UTC"));
                odt = odt + "|" + odt2.toString();
            }
        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error parsing ISO DateTimes, error: " + e.getMessage(), e);
            return odt + "|" + DEFAULT_DATE_TIME;
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
        } else if ((matcher = htmlFormat.matcher(input1)).matches()) {
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

            idt = generateDate(year, month, day, second, minute, hour,
                timezone);
        } else {
            idt = dtp.parseDateTime(input1);
        }
        return idt;
    }

    /**
     * @param year
     * @param month
     * @param day
     * @param second
     * @param minute
     * @param hour
     * @param timezone
     * @return
     */
    private static DateTime generateDate(String year, String month, String day,
                                         String second, String minute, String hour, String timezone) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.valueOf(year));
        c.set(Calendar.MONTH, Integer.valueOf(month) - 1);
        c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));

        c.set(Calendar.SECOND, Integer.valueOf(second));
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
        c.set(Calendar.MINUTE, Integer.valueOf(minute));

        TimeZone zone = TimeZone.getTimeZone(timezone);
        c.setTimeZone(zone);

        return new DateTime(c.getTimeInMillis());
    }

    /**
     * @param year
     * @param month
     * @param minute
     * @param hour
     * @param timezone
     * @return
     */
    private static DateTime generateDate(String year, String month,
                                         String minute, String hour, String timezone) {

        return generateDate(year, month, "1", "00", minute, hour, timezone);
    }
    // ---------------------------------------------------------------------------
    // ---
    // --- API methods
    // ---
    // ---------------------------------------------------------------------------

    public ISODate clone() {
        ISODate clone;
        try {
            clone = (ISODate) super.clone();
            clone._calendar = (Calendar) _calendar.clone();
            clone._shortDate = _shortDate;
            clone._shortDateYear = _shortDateYear;
            clone._shortDateYearMonth = _shortDateYearMonth;
            return clone;
        } catch (CloneNotSupportedException e) {
            return new ISODate(_calendar.getTimeInMillis(), _shortDate);
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * Subtract a date from this date and return the seconds between them. <p> The value is always
     * positive so that date1.timeDifferenceInSeconds(date2) == date2.timeDifferenceInSeconds(date1)
     * </p>
     */
    public long timeDifferenceInSeconds(ISODate date) {
        return getTimeInSeconds() - date.getTimeInSeconds();
    }

    // --------------------------------------------------------------------------
    @Transient
    public String getDateAsString() {
        if (_shortDateYearMonth) {
            return getYears() + "-" + pad(getMonths()) ;
        } else if (_shortDateYear) {
            return getYears() + "";
        } else {
            return getYears() + "-" + pad(getMonths()) + "-" + pad(getDays());
        }
    }

    // --------------------------------------------------------------------------

    public String toString() {
        return getDateAndTime();
    }

    /**
     * Create a java.util.Date object from this.
     *
     * @return a java.util.Date object from this.
     */
    public Date toDate() {
        return (Date) _calendar.getTime().clone();
    }

    // ---------------------------------------------------------------------------
    @Transient
    public long getTimeInSeconds() {
        return _calendar.getTimeInMillis() / 1000;
    }

    /**
     * Get the Time and Date encoded as a String.
     */

    @XmlValue
    public String getDateAndTime() {
        if (_shortDate || _shortDateYearMonth || _shortDateYear) {
            return getDateAsString();
        } else {
            return getDateAsString() + "T" + getTimeAsString();
        }
    }

    public void setDateAndTime(String isoDate) {

        String timeAndDate = isoDate;
        if (timeAndDate == null) {
            throw new IllegalArgumentException("date string is null");
        }

        int indexOfT = timeAndDate.indexOf('T');
        if (indexOfT > -1) {
            // Check if iso date contains time info and if using non UTC time
            // zone to parse the date with
            // JODAISODate. This class converts to UTC format to avoid timezones
            // issues.
            String afterT = timeAndDate.substring(indexOfT + 1);
            boolean timeZoneInfo = afterT.contains("+") || afterT.contains("-");

            if (timeZoneInfo) {
                timeAndDate = parseISODateTime(timeAndDate);
            }
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
     * Return true if this object is only the date (year, month, day) and time should be ignored.
     *
     * @return true if this object is only the date (year, month, day) and time should be ignored.
     */
    @Transient
    public boolean isDateOnly() {
        return _shortDate;
    }

    /**
     * Return true if this object is only the date (year) and time should be ignored.
     *
     * @return true if this object is only the date (year) and time should be ignored.
     */
    @Transient
    public boolean isDateYearOnly() {
        return _shortDateYear;
    }

    /**
     * Return true if this object is only the date (year, month) and time should be ignored.
     *
     * @return true if this object is only the date (year, month) and time should be ignored.
     */
    @Transient
    public boolean isDateYearMonthOnly() {
        return _shortDateYearMonth;
    }

    /**
     * Get the date's year.
     */
    @Transient
    public int getYears() {
        return _calendar.get(YEAR);
    }

    /**
     * Get the date's day of month starting at 1.
     */
    @Transient
    public int getDays() {
        return _calendar.get(DAY_OF_MONTH);
    }

    /**
     * Get the date's month of the year starting at 1 and going to 12.
     */
    @Transient
    public int getMonths() {
        return _calendar.get(MONTH) + 1;
    }

    /**
     * Get the date's hour in 24 hour time starting at 0 and going to 23
     */
    @Transient
    public int getHours() {
        return _calendar.get(HOUR_OF_DAY);
    }

    /**
     * Get the date's minute starting at 0 and going to 59
     */
    @Transient
    public int getMinutes() {
        return _calendar.get(MINUTE);
    }

    /**
     * Get the date's second starting at 0 and going to 59
     */
    @Transient
    public int getSeconds() {
        return _calendar.get(SECOND);
    }

    // ------------------------------- Private methods
    // ----------------------------------------------------

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
            String[] parts = isoDate.split("-|/");
            if ((parts.length == 0) || (parts.length > 3)) {
                throw new IllegalArgumentException(
                    "Invalid ISO date : " + isoDate);
            }

            _shortDate = (parts.length == 3);
            _shortDateYearMonth = (parts.length == 2);
            _shortDateYear = (parts.length == 1);

            int year;
            if (parts[0].length() < 4) {
                int shortYear = Integer.parseInt(parts[0]);
                String thisYear = String
                    .valueOf(Calendar.getInstance().get(YEAR));
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
                    day = Integer
                        .parseInt(parts[2].substring(0, parts[2].length() - 1));
                } else {
                    day = Integer.parseInt(parts[2]);
                }
            } else {
                _calendar.set(year, month - 1, 1);

                // Calculate the last day for the year/month
                day = _calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }

            _shortDate = true;

            int hour = 0;
            int minute = 0;
            int second = 0;

            _calendar.set(year, month - 1, day, hour, minute, second);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO date : " + isoDate,
                e);
        }
    }

    private void parseTime(@Nonnull String isoDate) {
        try {
            String[] parts = isoDate.split(":");
            if (parts.length == 1 || parts.length > 3) {
                throw new IllegalArgumentException(
                    "Invalid ISO date : " + isoDate);
            }

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = 0;
            if (parts.length == 3) {
                String secondsToParse = parts[2];
                int indexOfZ = secondsToParse.toUpperCase().indexOf('Z');
                if (indexOfZ > -1) {
                    secondsToParse = secondsToParse.substring(0, indexOfZ);
                }

                second = (int) Float.parseFloat(secondsToParse);
            }

            _calendar.set(HOUR_OF_DAY, hour);
            _calendar.set(MINUTE, minute);
            _calendar.set(SECOND, second);

            _shortDate = false;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO date : " + isoDate,
                e);
        }
    }

    @Transient
    public String getTimeAsString() {
        return pad(getHours()) + ":" + pad(getMinutes()) + ":"
            + pad(getSeconds());
    }

    private String pad(int value) {
        if (value > 9)
            return Integer.toString(value);

        return "0" + value;
    }

    @Override
    public int compareTo(ISODate o) {
        return _calendar.compareTo(o._calendar);
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
