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

package org.fao.geonet.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.utils.DateUtil;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Represents a date at a given time. Provides methods for representing the date as a string and
 * parsing from string.
 * <p>String format is: {@code yyyy-mm-ddThh:mm:ss}.</p>
 */
@Embeddable
@XmlRootElement
public class ISODate implements Cloneable, Comparable<ISODate>, Serializable, XmlEmbeddable {
    @XmlTransient public static final DateTimeFormatter YEAR_MONTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    @XmlTransient public static final DateTimeFormatter YEAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    @XmlTransient public static final DateTimeFormatter YEAR_MONTH_DAYS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @XmlTransient private static final Pattern SIMPLE_SPLIT_PATTERN = Pattern.compile("[-/]");

    /**
     * {@code true} if the format is {@code yyyy-mm-dd}.
     */
    @XmlTransient private boolean _shortDate;

    /**
     * {@code true} if the format is {@code yyyy}.
     */
    @XmlTransient private boolean _shortDateYear;

    /**
     * {@code true} if the format is {@code yyyy-mm}.
     */
    @XmlTransient private boolean _shortDateYearMonth;

    @XmlTransient private ZonedDateTime internalDateTime;

    /**
     * Constructs an instance of <code>ISODate</code> with current date, time
     * and system default timezone.
     */
    public ISODate() {
        internalDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
    }

    // ---------------------------------------------------------------------------

    /**
     * Constructs an instance of {@code ISODate} with the time and format passed as parameters.
     *
     * The local timezone is used, unless the argument {@code shortDate} value {@code true} is supplied
     * to to force the timezone to UTC. THis setting is used to respect a time provided in the short
     * format {@code yyyy-mm-dd}.
     *
     * @param timeInEpochMillis milliseconds passed from 1970-01-01T00:00:00Z (Unix epoch).
     * @param shortDate         {@code true} if the format is {@code yyyy-mm-dd} forcing timezone to UTC.
     */
    public ISODate(final long timeInEpochMillis, final boolean shortDate) {
        _shortDate = shortDate;

        Instant instantParam = Instant.ofEpochMilli(timeInEpochMillis);
        internalDateTime = instantParam.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
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
    @Transient
    @JsonSerialize
    @JsonProperty
    public String getDateAndTime() {
        if (_shortDate || _shortDateYearMonth || _shortDateYear) {
            return getDateAsString();
        } else {
            return internalDateTime.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    public void setDateAndTime(String isoDate) {

        String timeAndDate = isoDate;
        if (timeAndDate == null) {
            throw new IllegalArgumentException("Date string is null");
        }

        if (timeAndDate.indexOf('T') >= 0 && StringUtils.contains(timeAndDate, ':')) {
            timeAndDate = DateUtil.convertToISOZuluDateTime(timeAndDate);

            if (timeAndDate == null) {
                throw new IllegalArgumentException("Not parsable date: " + isoDate);
            }
            internalDateTime = ZonedDateTime.parse(timeAndDate, DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS);
            return;
        }

        if (StringUtils.contains(timeAndDate, ':')) {
            // its a time
            try {
                internalDateTime = DateUtil.parseTime(StringUtils.remove(StringUtils.remove(timeAndDate, 't'), 'T'));
                _shortDate = false;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid ISO time: " + isoDate, e);
            }
        } else {
            // its a date
            parseDate(StringUtils.remove(StringUtils.remove(timeAndDate, 't'), 'T'));

        }
    }

    /**
     * Get the date and time in ISO format and UTC offset format.
     *
     * @return The date and time in ISO format.
     */
    @JsonProperty("dateAndTimeUtc")
    @XmlTransient
    public String getDateAndTimeUtc() {
        return internalDateTime.withZoneSameInstant(ZoneOffset.UTC).format(DateUtil.ISO_OFFSET_DATE_TIME_NANOSECONDS);
    }

    public void setDateAndTimeUtc(String isoDate) {
        setDateAndTime(isoDate);
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
        return internalDateTime.getMonthValue();
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

    @Transient
    public ZonedDateTime getInternalDateTime() {
        return internalDateTime;
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
        return other.internalDateTime.isEqual(this.internalDateTime);
    }

    private void parseDate(@Nonnull String isoDate) {
        try {
            String[] parts = SIMPLE_SPLIT_PATTERN.split(isoDate);
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
            ZoneId offset = ZoneId.systemDefault();
            if (_shortDate) {

                if (parts[2].toLowerCase().endsWith("z")) {
                    offset = ZoneOffset.UTC;
                    day = Integer.parseInt(parts[2].substring(0, parts[2].length() - 1));
                } else {
                    day = Integer.parseInt(parts[2]);
                }
            } else {
                // Calculate the last day for the year/month
                day = YearMonth.of(year, month).atEndOfMonth().getDayOfMonth();
            }

            _shortDate = true;
            internalDateTime = ZonedDateTime.now(offset).withYear(year).withMonth(month).withDayOfMonth(day).withHour(0).withMinute(0)
                    .withSecond(0).withNano(0);
            //..ZonedDateTime.of(year, month, day, hour, minute, second, 0, offset);

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
