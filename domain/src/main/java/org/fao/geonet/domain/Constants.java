package org.fao.geonet.domain;

/**
 * Constants used internally in for defining the domain objects.
 *
 * @author Jesse
 */
public final class Constants {
    private Constants() {
    }

    /**
     * The length to use for IP address columns.
     */
    public static final int IP_ADDRESS_COLUMN_LENGTH = 45;
    /**
     * The character used by the JPAWorkaround columns that need a character for boolean false.
     */
    public static final char YN_FALSE = 'n';
    /**
     * The character used by the JPAWorkaround columns that need a character for boolean true.
     */
    public static final char YN_TRUE = 'y';


    /**
     * Convert a boolean to the corresponding character to use for the boolean characters (A workaround for the API).
     * Do a search for JPAWorkaround in domain package.
     *
     * @param enabled the value to convert
     * @return the corresponding char
     *
     * CSOFF: MethodName
     */
    public static char toYN_EnabledChar(final boolean enabled) {
        char enabledChar;
        if (enabled) {
            enabledChar = YN_TRUE;
        } else {
            enabledChar = YN_FALSE;
        }
        return enabledChar;
    }

    /**
     * Convert a character from one of the JPAWorkaround columns to the corresponding boolean value.
     * Do a search for JPAWorkaround in domain package.
     *
     * @param enabled the value to convert
     * @return the corresponding boolean value
     */
    public static boolean toBoolean_fromYNChar(final char enabled) {
        return enabled == Constants.YN_TRUE;
    }
    // CSON: MethodName

    /**
     * The module name for logging domain information.
     */
    public static final String DOMAIN_LOG_MODULE = "geonetwork.domain";
}
