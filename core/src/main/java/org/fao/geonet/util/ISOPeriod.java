package org.fao.geonet.util;

/**
 * Utility to convert an ISO8601 Period to minutes.
 *
 */
public class ISOPeriod {

    /**
     * Converts a string in ISO8601 Period format to how many minutes that is.
     *
     * @param iso8601Period - the period
     * @return minutes period in minutes
     */
    public static int iso8601Period2Minutes(String iso8601Period) {
        int minutes = 0;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('P') + 1, iso8601Period.indexOf('Y'))) * 365 * 24 * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('Y') + 1, iso8601Period.indexOf('M'))) * 30 * 24 * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('M') + 1, iso8601Period.indexOf('D'))) * 24 * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('T') + 1, iso8601Period.indexOf('H'))) * 60;
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('H') + 1, iso8601Period.indexOf('M', iso8601Period.indexOf('H'))));
        minutes += Integer.parseInt(iso8601Period.substring(iso8601Period.indexOf('M', iso8601Period.indexOf('H')) + 1, iso8601Period.indexOf('S'))) / 60;
        return minutes;
    }

    public final static String ZERO_DURATION = "P0Y0M0DT0H0M0S";

}
