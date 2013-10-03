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

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Represents a date at a given time.  Provides methods for representing the date as a string and parsing from string.
 * <p>
 *     String format is: yyyy-mm-ddThh:mm:ss
 * </p>
 */
@Embeddable
public class ISODate implements Cloneable, Comparable<ISODate> {
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

    public ISODate(long time, boolean shortDate) {
        _calendar.setTimeInMillis(time);
        _shortDate = shortDate;
    }

    // ---------------------------------------------------------------------------

    public ISODate(String isoDate) {
        setDateAndTime(isoDate);
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
        return Math.abs(getTimeInSeconds() - date.getTimeInSeconds());
    }

    // --------------------------------------------------------------------------
    @Transient
    public String getDate() {
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
    @Transient
    public long getTimeInSeconds() {
        return _calendar.getTimeInMillis() / 1000;
    }

    /**
     * Get the Time and Date encoded as a String.
     */
    public String getDateAndTime() {
        return getDate() + "T" + timeAsString();
    }

    public void setDateAndTime(String timeAndDate) {
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
        return timeAsString().hashCode();
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
        return timeAsString().equals(other.timeAsString());
    }

    private void parseDate(@Nonnull String isoDate) {
        try {
            String[] parts = isoDate.split("-|/");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid ISO date : " + isoDate);
            }
            int year;
            if (parts[0].length() < 4) {
                int shortYear = Integer.parseInt(parts[0]);
                String thisYear = String.valueOf(Calendar.getInstance().get(YEAR));
                int century = Integer.parseInt(thisYear.substring(0,2))*100;
                int yearInCentury = Integer.parseInt(thisYear.substring(2));
                
                if (shortYear <= yearInCentury) {
                    year = century + shortYear;
                } else {
                    year = century - 100 + shortYear;
                }
            } else {
                year = Integer.parseInt(parts[0]);
            }
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            _shortDate = true;

            int hour = 0;
            int minute = 0;
            int second = 0;

            _calendar.set(year, month - 1, day, hour, minute, second);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO date : " + isoDate);
        }
    }

    private void parseTime(@Nonnull String isoDate) {
        try {
            String[] parts = isoDate.split(":");
            if (parts.length == 1 || parts.length > 3) {
                throw new IllegalArgumentException("Invalid ISO date : " + isoDate);
            }

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = 0;
            if (parts.length == 3) {
                if (parts[2].toUpperCase().contains("Z")) {
                    String[] secondParts = parts[2].toUpperCase().split("Z");
                    second = Integer.parseInt(secondParts[0]);
                } else {
                    second = Integer.parseInt(parts[2]);
                }
            }

            _calendar.set(HOUR_OF_DAY, hour);
            _calendar.set(MINUTE, minute);
            _calendar.set(SECOND, second);

            _shortDate = false;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ISO date : " + isoDate);
        }
    }

    private String timeAsString() {
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
