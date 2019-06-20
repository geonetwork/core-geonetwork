/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.tools.i18n;

import java.util.*;

/**
 * Created by francois on 05/02/16.
 */
public class LanguageUtils {
    private final Set<String> iso3code;
    private final String defaultLanguage;
    Collection<Locale> locales = new ArrayList<>();

    public LanguageUtils(final Set<String> localesToLoad,
                         final String defaultLanguage) {
        iso3code = Collections.unmodifiableSet(localesToLoad);
        this.defaultLanguage = defaultLanguage;
        for (String l : iso3code) {
            locales.add(Locale.forLanguageTag(l));
        }
    }
//    Require Java 8
//    public String parseAcceptLanguage(final String language) {
//        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(language);
//        Locale locale = Locale.lookup(list, locales);
//        if (locale != null) {
//            return locale.getISO3Language();
//        } else {
//            return defaultLanguage;
//        }
//    }

    public Locale parseAcceptLanguage(final Enumeration<Locale> listOfLocales) {
        while (listOfLocales.hasMoreElements()) {
            Locale l = listOfLocales.nextElement();
            if (iso3code.contains(locale2gnCode(l.getISO3Language()))) {
                return l;
            }
        }
        return Locale.forLanguageTag(defaultLanguage);
    }

    public String getIso3langCode(Enumeration<Locale> locales) {
        Locale l = parseAcceptLanguage(locales);
        return locale2gnCode(l.getISO3Language());
    }

    static public String locale2gnCode (String code) {
        if (code.equals("fra")) {
            return "fre";
        } else if (code.equals("slk")) { // transforms ISO 639-2/T into ISO 639-2/B
            return "slo";
        } else {
            return code;
        }
    }

    public Locale parseAcceptLanguage(final Locale locale) {
        Vector<Locale> locales = new Vector<>();
        locales.add(locale);

        return parseAcceptLanguage(locales.elements());
    }
}
