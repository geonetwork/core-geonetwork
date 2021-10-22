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

import org.fao.geonet.languages.IsoLanguagesMapper;

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

    /**
     * Review provided, and return the first supported one (or default language if none are acceptable).
     *
     * @param listOfLocales Locales to parse
     * @return supported locale from the provided list, or default language if none are acceptable
     */
    public Locale parseAcceptLanguage(final Enumeration<Locale> listOfLocales) {
        while (listOfLocales.hasMoreElements()) {
            Locale locale = listOfLocales.nextElement();
            if (iso3code.contains(iso3code(locale))) {
                return locale;
            }
        }
        return Locale.forLanguageTag(defaultLanguage);
    }
    @Deprecated
    public String getIso3langCode(Enumeration<Locale> locales){
        return iso3code(locales);
    }

    /**
     * Review provided, and return the iso3code of the first selected one.
     *
     * @param locales Locales to check
     * @return iso3code of the selected locale, or iso3code of the default language if none are acceptable
     */
    public String iso3code(Enumeration<Locale> locales) {
        Locale locale = parseAcceptLanguage(locales);
        return iso3code(locale);
    }

    /**
     * Obtain into GeoNetwork ISO Language 639-2/B representation for locale.
     *
     * Translate locale three-letter abbreviation to language code (providing a special case for 'fra' and 'slk' locales.
     *
     * @param locale Locale, providing {@link Locale#getISO3Language()} 639-2/T language code
     * @return Geonetwork ISO 639-2/B language code
     */
    public static String iso3code(Locale locale) {
        if (locale == null){
            return null;
        }
        return IsoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language());
    }

    /**
     * Review provided locale, and check if it is supported.
     * @param locale Locale to check
     * @return  provided locale if acceptable, or the default language as a fallback
     */
    public Locale parseAcceptLanguage(final Locale locale) {
        Vector<Locale> locales = new Vector<>();
        locales.add(locale);

        return parseAcceptLanguage(locales.elements());
    }
}
