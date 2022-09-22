package org.fao.geonet.util;

public class XslUtil {
    public static String twoCharLangCode(String iso3code) {
        return iso3code.substring(0, 2);
    }
}
