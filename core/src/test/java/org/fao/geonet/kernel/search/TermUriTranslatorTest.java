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

package org.fao.geonet.kernel.search;

import org.fao.geonet.kernel.SingleThesaurusFinder;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.junit.Test;
import org.openrdf.sesame.config.ConfigurationException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class TermUriTranslatorTest {

    private final IsoLanguagesMapper isoLangMapper = new IsoLanguagesMapper() {
        {
            _isoLanguagesMap639.put("en", "eng");
            _isoLanguagesMap639.put("de", "ger");
        }
    };

    @Test
    public void testTermWithPreferredLabelForLanguage() throws IOException, ConfigurationException {
        ThesaurusFinder finder = createThesaurusFinderFor(isoLangMapper, "TermUriTranslatorTest.rdf");
        Translator translator = new TermUriTranslator(finder, "eng", "http://www.my.com/test");
        String label = translator.translate("http://www.my.com/test#ocean_temperature");
        assertEquals("ocean temperature", label);
    }

    @Test
    public void testTermWithNoPreferredLabelForLanguage() throws IOException, ConfigurationException {
        ThesaurusFinder finder = createThesaurusFinderFor(isoLangMapper, "TermUriTranslatorTest.rdf");
        Translator translator = new TermUriTranslator(finder, "ger", "http://www.my.com/test");
        String label = translator.translate("http://www.my.com/test#ocean_temperature");
        assertEquals("http://www.my.com/test#ocean_temperature", label);
    }

    @Test
    public void testMissingTerm() throws IOException, ConfigurationException {
        ThesaurusFinder finder = createThesaurusFinderFor(isoLangMapper, "TermUriTranslatorTest.rdf");
        Translator translator = new TermUriTranslator(finder, "ger", "http://www.my.com/test");
        String label = translator.translate("http://www.my.com/test#unknown_term");
        assertEquals("http://www.my.com/test#unknown_term", label);
    }

    @Test
    public void testDelayedFinder() throws Exception {
        NullableThesaurusFinder finder = new NullableThesaurusFinder();
        Translator translator = new TermUriTranslator(finder, "eng", "http://www.my.com/test");

        // thesaurus not yet set
        String label = translator.translate("http://www.my.com/test#ocean_temperature");
        assertEquals("http://www.my.com/test#ocean_temperature", label);

        Path thesaurusFile = new ClassPathResource("TermUriTranslatorTest.rdf", this.getClass()).getFile().toPath();
        Thesaurus thesaurus = loadThesaurusFile(isoLangMapper, thesaurusFile);
        finder.setThesaurus(thesaurus);

        // try again the translation
        label = translator.translate("http://www.my.com/test#ocean_temperature");
        assertEquals("ocean temperature", label);
    }

    private ThesaurusFinder createThesaurusFinderFor(IsoLanguagesMapper isoLangMapper, String fileName) throws IOException, ConfigurationException {
        //TODO: Load from in memory data directory?
        Path thesaurusFile = new ClassPathResource(fileName, this.getClass()).getFile().toPath();
        Thesaurus thesaurus = loadThesaurusFile(isoLangMapper, thesaurusFile);
        return new SingleThesaurusFinder(thesaurus);
    }

    private Thesaurus loadThesaurusFile(IsoLanguagesMapper isoLanguagesMapper, Path thesaurusFile)
        throws ConfigurationException, IOException {
        Thesaurus thesaurus = new Thesaurus(isoLanguagesMapper, thesaurusFile.getFileName().toString(), "external", "theme", thesaurusFile, "http://dummy.org/geonetwork");
        thesaurus.initRepository();
        return thesaurus;
    }
}
