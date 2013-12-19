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

package org.fao.geonet.bean;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.persistence.Transient;

/**
 * Represents a date at a given time.  Provides methods for representing the date as a string and parsing from string.
 * <p>
 * String format is: yyyy-mm-ddThh:mm:ss
 * </p>
 */
public class ISODate implements Cloneable, Comparable<ISODate>, Serializable {
    private static final String DEFAULT_DATE_TIME = "3000-01-01T00:00:00.000Z"; // JUNK Value

    // Pattern to check dates
    private static Pattern gsYear = Pattern
            .compile("([0-9]{4})(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");
    private static Pattern gsYearMonth = Pattern
            .compile("([0-9]{4})-([0-1][0-9])(-([0-2][0-9]):([0-5][0-9])([A-Z]))?");

    private boolean _shortDate; // --- 'true' if the format is yyyy-mm-dd

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
            return clone;
        } catch (CloneNotSupportedException e) {
            return new ISODate(_calendar.getTimeInMillis(), _shortDate);
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * Subtract a date from this date and return the seconds between them.
     * <p>
     * The value is always positive so that date1.timeDifferenceInSeconds(date2) == date2.timeDifferenceInSeconds(date1)
     * </p>
     */
    public long timeDifferenceInSeconds(ISODate date) {
        return getTimeInSeconds() - date.getTimeInSeconds();
    }

    // --------------------------------------------------------------------------
   
    public String getDateAsString() {
        return getYears() + "-" + pad(getMonths()) + "-" + pad(getDays());
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
   
    public long getTimeInSeconds() {
        return _calendar.getTimeInMillis() / 1000;
    }

    /**
     * Get the Time and Date encoded as a String.
     */
    public String getDateAndTime() {
        if (_shortDate) {
            return getDateAsString();
        }
        return getDateAsString() + "T" + getTimeAsString();
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

    // ------------------------------- Private methods ----------------------------------------------------

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

    public String getTimeAsString() {
        return pad(getHours()) + ":" + pad(getMinutes()) + ":" + pad(getSeconds());
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
}
