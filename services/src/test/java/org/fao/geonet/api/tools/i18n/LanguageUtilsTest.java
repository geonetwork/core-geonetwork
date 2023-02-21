/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

import org.fao.geonet.languages.IsoLanguagesMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageUtilsTest  {
    LanguageUtils languageUtils;

    @Before
    public void setUp() throws Exception {
        Set<String> localesToLoad = Stream.of("dut", "ger", "eng", "fre")
            .collect(Collectors.toCollection(HashSet::new));

        languageUtils = new LanguageUtils(localesToLoad, "eng");
    }

    @Test
    public void getDefaultLanguage() throws Exception {
        Assert.assertEquals("eng", languageUtils.getDefaultUiLanguage());
    }

    @Test
    public void getUiLanguages() throws Exception {
        Assert.assertEquals(4, languageUtils.getUiLanguages().size());
    }

    @Test
    public void parseAcceptLanguage() throws Exception {
        Set<String> localesToLoad = Stream.of("dut", "ger", "eng", "fre")
            .collect(Collectors.toCollection(HashSet::new));

        LanguageUtils languageUtils = new LanguageUtils(localesToLoad, "eng");

        Locale gerLocale = new Locale("de");
        String iso3lang = languageUtils.parseAcceptLanguage(gerLocale).getISO3Language();
        Assert.assertEquals("ger", IsoLanguagesMapper.iso639_2T_to_iso639_2B(iso3lang));

        // Get default language as not in the list of supported locales
        Locale spaLocale = new Locale("es");
        iso3lang = languageUtils.parseAcceptLanguage(spaLocale).getISO3Language();
        Assert.assertEquals("eng", IsoLanguagesMapper.iso639_2T_to_iso639_2B(iso3lang));
    }
}
