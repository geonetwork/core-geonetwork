//==============================================================================
//===   Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;

public final class LocaleUtil {

    /**
     * Attempts to match the code to a Locale.  If no match then the default Locale is returned Case
     * is ignored
     *
     * @param langCode A 2 or 3 character ISO language code
     * @return the associated Locale or the default locale if no match is founde
     */
    public static Locale toLocale(String langCode) {
        if (StringUtils.isEmpty(langCode)) {
            return Locale.getDefault();
        }
        if (langCode.length() < 2 || langCode.length() > 3) {
            throw new AssertionError(langCode + " must be a 2 or 3 letter ISO code");
        }
        return toLocale(langCode, Locale.getDefault());
    }

    /**
     * Attempts to match the code to a Locale.  If no match then the default Locale is returned Case
     * is ignored
     *
     * @param langCode      A 2 or 3 character ISO language code
     * @param defaultLocale the local returned if not match is made
     * @return the associated Locale or the default locale if no match is founde
     */
    public static Locale toLocale(String langCode, Locale defaultLocale) {
        for (Locale loc : Locale.getAvailableLocales()) {
            if (loc.getISO3Language().toLowerCase().startsWith(langCode)) {
                return loc;
            }

        }
        return defaultLocale;
    }


}
