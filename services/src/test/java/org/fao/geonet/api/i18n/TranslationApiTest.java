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
package org.fao.geonet.api.i18n;

import org.fao.geonet.domain.Translations;
import org.fao.geonet.repository.TranslationsRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TranslationApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private TranslationsRepository translationsRepository;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Test
    public void testTranslationsApiAndRepo() throws Exception {
        List<Translations> t = translationsRepository.findAllByFieldName("test");
        assertEquals(0, t.size());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/i18n/db/translations/test")
            .content("{\"eng\": \"Africa\", \"fre\": \"Afrique\"}")
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        t = translationsRepository.findAllByFieldName("test");
        assertEquals(2, t.size());


        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/i18n/db/translations")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.test", is("Africa")));


        this.mockMvc.perform(put("/srv/api/i18n/db/translations/test")
            .content("{\"eng\": \"West Africa\", \"fre\": \"Afrique\"}")
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        Translations value = translationsRepository.findOneByLangIdAndFieldName("eng", "test");
        assertEquals("West Africa", value.getValue());

        this.mockMvc.perform(delete("/srv/api/i18n/db/translations/test")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk());

        t = translationsRepository.findAllByFieldName("test");
        assertEquals(0, t.size());


        this.mockMvc.perform(delete("/srv/api/i18n/db/translations/notExisting")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    @Test
    public void testPutNewTranslationsDbWithoutReplace() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        // first define some translations
        this.mockMvc.perform(put("/srv/api/i18n/db/translations/test")
                        .content("{\"eng\": \"Africa\", \"fre\": \"Afrique\"}")
                        .contentType(API_JSON_EXPECTED_ENCODING)
                        .session(this.mockHttpSession)
                        .accept(MediaType.parseMediaType("application/json")))
                        .andExpect(status().is(201));

        assertEquals(2, translationsRepository.findAllByFieldName("test").size());

        // then add a new language
        this.mockMvc.perform(put("/srv/api/i18n/db/translations/test")
                        .content("{\"fre\": \"Afrique de l'ouest\", \"ger\": \"Afrika\"}")
                        .contentType(API_JSON_EXPECTED_ENCODING)
                        .session(this.mockHttpSession)
                        .accept(MediaType.parseMediaType("application/json")))
                        .andExpect(status().is(201));

        assertEquals(3, translationsRepository.findAllByFieldName("test").size());

        Translations translatedGer = translationsRepository.findOneByLangIdAndFieldName("ger", "test");
        Translations translatedFre = translationsRepository.findOneByLangIdAndFieldName("fre", "test");
        Translations translatedEng = translationsRepository.findOneByLangIdAndFieldName("eng", "test");
        assertEquals("Afrika", translatedGer.getValue());
        assertEquals("Afrique de l'ouest", translatedFre.getValue());
        assertEquals("Africa", translatedEng.getValue());
    }
}
