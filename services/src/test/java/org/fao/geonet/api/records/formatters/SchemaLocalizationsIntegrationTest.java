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

package org.fao.geonet.api.records.formatters;

import org.fao.geonet.api.records.formatters.groovy.CurrentLanguageHolder;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertTrue;

public class SchemaLocalizationsIntegrationTest extends AbstractServiceIntegrationTest {


    @Autowired
    private ApplicationContext context;

    @Test
    public void testNodeTranslation() throws Exception {
        CurrentLanguageHolder currentLanguageHolder = new TestLanguageHolder("fre");
        final SchemaLocalizations freIso19139 = new SchemaLocalizations(context, currentLanguageHolder, "iso19139", null);

        final String keywordLabel = freIso19139.nodeTranslation("gmd:descriptiveKeywords", "srv:SV_ServiceIdentification", "label");
        assertTrue("'" + keywordLabel + "' should contain 'mots'", keywordLabel.toLowerCase().contains("mots"));
        assertTrue("'" + keywordLabel + "' should contain 'clés'", keywordLabel.toLowerCase().contains("clés"));

        String identifierLabel = freIso19139.nodeTranslation("gmd:identifier", "gmd:CI_Citation", "label");
        assertTrue("'" + identifierLabel + "' should contain 'identificateur'", identifierLabel.toLowerCase().contains("identificateur"));
    }

    private final class TestLanguageHolder implements CurrentLanguageHolder {
        private final String currentLang;

        public TestLanguageHolder(String currentLang) {
            this.currentLang = currentLang;
        }

        @Override
        public String getLang3() {
            return currentLang;
        }

        @Override
        public String getLang2() {
            return context.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(currentLang);
        }
    }
}
