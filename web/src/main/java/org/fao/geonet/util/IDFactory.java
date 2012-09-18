package org.fao.geonet.util;

import java.util.UUID;

/**
 * Generator for IDs to be used as database keys.
 *
 * @author heikki doeleman
 */
public class IDFactory {

    /**
     * Generates a random UUID and replaces the hyphens in it by underscores. This is to avoid problems if database
     * IDs are used as identifiers in Javascript.
     *
     * Also replace any leading digit by a dedicated letter, as js identifiers can't start with digits.
     *
     * @return id
     */
    public static String newID() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace('-', '_');
        if(Character.isDigit(uuid.charAt(0))) {
            uuid = LETTER4DIGIT.map(uuid.charAt(0)).name() + uuid.substring(1);
        }
        return uuid;
    }

    /**
     * Letters to encode digits from 0 to 9.
     */
    private enum LETTER4DIGIT {
        a, b, c, d, e, f, g, h, i, j;

        static LETTER4DIGIT map(char digit) {
            switch (digit) {
                case '0':
                    return a;
                case '1':
                    return b;
                case '2':
                    return c;
                case '3':
                    return d;
                case '4':
                    return e;
                case '5':
                    return f;
                case '6':
                    return g;
                case '7':
                    return h;
                case '8':
                    return i;
                case '9':
                    return j;
                default:
                    throw new IllegalArgumentException("Unrecognized digit: " + digit);
            }
        }
    }
}