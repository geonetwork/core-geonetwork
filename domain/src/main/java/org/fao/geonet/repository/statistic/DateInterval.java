package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.ISODate;

/**
 * Represents a date at a certain granularity like year, month, day.
 * <p/>
 * User: Jesse
 * Date: 10/1/13
 * Time: 8:32 AM
 */
public abstract class DateInterval {
    private final int _substringEnd;
    private final String _dateString;

    /**
     * General Constructor.
     *
     * @param date         the date of this object
     * @param substringEnd how much of the date string that is of interest.
     */
    DateInterval(final ISODate date, final int substringEnd) {
        this(date.getDateAsString(), substringEnd);
    }

    /**
     * General Constructor.
     *
     * @param date         the date of this object
     * @param substringEnd how much of the date string that is of interest.
     */
    DateInterval(final String date, final int substringEnd) {
        this._substringEnd = substringEnd;
        if (date.length() < substringEnd) {
            this._dateString = date;
        } else {
            this._dateString = date.substring(0, substringEnd);
        }
    }

    /**
     * Get the end of the substring of {@link ISODate} that makes up this type.
     * <p>
     * For example Year would be 4 because ISODate format has yyyy-mm-dd.
     * </p>
     */
    public final int getSubstringEnd() {
        return _substringEnd;
    }

    /**
     * Get the date string for this type.
     */
    public final String getDateString() {
        return _dateString;
    }

    @Override
    public String toString() {
        return _dateString;
    }

    /**
     * Create a new instance of the same type.
     *
     * @param dateInterval and ISODate formatted string
     */
    public abstract DateInterval createFromString(final String dateInterval);

    // CSOFF: NeedBraces
    // CSOFF: MagicNumber
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DateInterval)) return false;

        DateInterval that = (DateInterval) o;

        if (_substringEnd != that._substringEnd) return false;
        if (!_dateString.equals(that._dateString)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _substringEnd;
        result = 31 * result + _dateString.hashCode();
        return result;
    }
    // CSON: NeedBraces
    // CSON: MagicNumber

    /**
     * Represents a year interval.
     */
    public static class Year extends DateInterval {

        private static final int SUBSTRING_END = 4;

        /**
         * default constructor.
         */
        public Year() {
            super(new ISODate(), SUBSTRING_END);
        }

        /**
         * constructor.
         *
         * @param date a formatted {@link ISODate} string
         */
        public Year(final String date) {
            super(date, SUBSTRING_END);
        }

        /**
         * constructor.
         *
         * @param date a {@link ISODate}
         */
        public Year(final ISODate date) {
            super(date, SUBSTRING_END);
        }

        @Override
        public DateInterval createFromString(final String dateInterval) {
            return new Year(dateInterval);
        }
    }
    /**
     * Represents a month interval.
     */
    public static class Month extends DateInterval {

        private static final int SUBSTRING_END = 7;
        /**
         * default constructor.
         */
        public Month() {
            super(new ISODate(), SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a formatted {@link ISODate} string
         */
        public Month(final String date) {
            super(date, SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a {@link ISODate}
         */
        public Month(final ISODate date) {
            super(date, SUBSTRING_END);
        }

        @Override
        public DateInterval createFromString(final String dateInterval) {
            return new Month(dateInterval);
        }
    }
    /**
     * Represents a day interval.
     */
    public static class Day extends DateInterval {

        private static final int SUBSTRING_END = 10;
        /**
         * default constructor.
         */
        public Day() {
            super(new ISODate(), SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a formatted {@link ISODate} string
         */
        public Day(final ISODate date) {
            super(date, SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a {@link ISODate}
         */
        public Day(final String date) {
            super(date, SUBSTRING_END);
        }

        @Override
        public DateInterval createFromString(final String dateInterval) {
            return new Day(dateInterval);
        }
    }
    /**
     * Represents a hour interval.
     */
    public static class Hour extends DateInterval {

        private static final int SUBSTRING_END = 13;
        /**
         * default constructor.
         */
        public Hour() {
            super(new ISODate(), SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a formatted {@link ISODate} string
         */
        public Hour(final ISODate date) {
            super(date, SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a {@link ISODate}
         */
        public Hour(final String date) {
            super(date, SUBSTRING_END);
        }

        @Override
        public DateInterval createFromString(final String dateInterval) {
            return new Hour(dateInterval);
        }
    }
    /**
     * Represents a minute interval.
     */
    public static class Minute extends DateInterval {

        private static final int SUBSTRING_END = 16;
        /**
         * default constructor.
         */
        public Minute() {
            super(new ISODate(), SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a formatted {@link ISODate} string
         */
        public Minute(final ISODate date) {
            super(date, SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a {@link ISODate}
         */
        public Minute(final String date) {
            super(date, SUBSTRING_END);
        }

        @Override
        public DateInterval createFromString(final String dateInterval) {
            return new Minute(dateInterval);
        }
    }
    /**
     * Represents a second interval.
     */
    public static class Second extends DateInterval {

        private static final int SUBSTRING_END = 19;
        /**
         * default constructor.
         */
        public Second() {
            super(new ISODate(), SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a formatted {@link ISODate} string
         */
        public Second(final ISODate date) {
            super(date, SUBSTRING_END);
        }
        /**
         * constructor.
         *
         * @param date a {@link ISODate}
         */
        public Second(final String date) {
            super(date, SUBSTRING_END);
        }

        @Override
        public DateInterval createFromString(final String dateInterval) {
            return new Second(dateInterval);
        }
    }
}
