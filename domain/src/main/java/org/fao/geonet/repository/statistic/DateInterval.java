package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.ISODate;

import java.util.Calendar;

/**
* Represents a date at a certain granularity like year, month, day.
 *
* User: Jesse
* Date: 10/1/13
* Time: 8:32 AM
*/
public abstract class DateInterval {
    private final int substringEnd;
    private final String dateString;

    /**
     * General Constructor
     *
     * @param date the date of this object
     * @param substringEnd how much of the date string that is of interest.
     */
    DateInterval(final ISODate date, final int substringEnd) {
        this(date.getDate(), substringEnd);
    }

    /**
     * General Constructor
     *
     * @param date the date of this object
     * @param substringEnd how much of the date string that is of interest.
     */
    DateInterval(final String date, final int substringEnd) {
        this.substringEnd = substringEnd;
        this.dateString = date.substring(0, substringEnd);
    }

    /**
     * Get the end of the substring of {@link ISODate} that makes up this type.
     * <p>
     *     For example Year would be 4 because ISODate format has yyyy-mm-dd.
     * </p>
     */
    public final int getSubstringEnd() {
        return substringEnd;
    }

    /**
     * Get the date string for this type.
     */
    public final String getDateString() {
        return dateString;
    }

    /**
     * Create a new instance of the same type.
     *
     * @param dateInterval and ISODate formatted string
     */
    public abstract DateInterval createFromString(final String dateInterval);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateInterval)) return false;

        DateInterval that = (DateInterval) o;

        if (substringEnd != that.substringEnd) return false;
        if (!dateString.equals(that.dateString)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = substringEnd;
        result = 31 * result + dateString.hashCode();
        return result;
    }

    public static class Year extends DateInterval {
        public Year() {
            super(new ISODate(), 4);
        }
        public Year(String date) {
            super(date, 4);
        }
        public Year(ISODate date) {
            super(date, 4);
        }
        @Override
        public DateInterval createFromString(String dateInterval) {
            return new Year(dateInterval);
        }
    }

    public static class Month extends DateInterval {
        public Month() {
            super(new ISODate(), 7);
        }
        public Month(String date) {
            super(date, 7);
        }
        public Month(ISODate date) {
            super(date, 7);
        }
        @Override
        public DateInterval createFromString(String dateInterval) {
            return new Month(dateInterval);
        }
    }

    public static class Day extends DateInterval {
        public Day() {
            super(new ISODate(), 10);
        }
        public Day(ISODate date) {
            super(date, 10);
        }
        public Day(String date) {
            super(date, 10);
        }
        @Override
        public DateInterval createFromString(String dateInterval) {
            return new Day(dateInterval);
        }
    }
}
