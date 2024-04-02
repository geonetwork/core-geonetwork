/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.api.pages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.page.Page;
import org.fao.geonet.domain.page.PageIdentity;
import org.fao.geonet.repository.page.PageRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PagesApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PageRepository pageRepository;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;


    @Test
    public void putPage() throws Exception {
        String language = "eng";
        String pageId = "license";
        String link = "https://myorg/license-policy";

        Optional<Page> page = pageRepository.findById(new PageIdentity(language, pageId));
        Assert.assertFalse(page.isPresent());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        PageProperties newPage = new PageProperties();
        newPage.setPageId(pageId);
        newPage.setLanguage(language);
        newPage.setLabel(pageId);
        newPage.setLink(link);
        ArrayList<Page.PageSection> sections = new ArrayList<>();
        sections.add(Page.PageSection.TOP);
        newPage.setSections(sections);
        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();

        this.mockHttpSession = loginAsAdmin();
        MockHttpServletRequestBuilder createPageBuilder = put("/srv/api/pages")
            .content(gson.toJson(newPage))
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json"));

        this.mockMvc.perform(createPageBuilder)
            .andExpect(status().is(HttpStatus.CREATED.value()));

        page = pageRepository.findById(new PageIdentity(language, pageId));
        Assert.assertTrue(page.isPresent());
        Assert.assertEquals(link, page.get().getLink());
        Assert.assertEquals(pageId, page.get().getLabel());
        Assert.assertTrue(page.get().getSections().contains(Page.PageSection.TOP));

        this.mockMvc.perform(createPageBuilder)
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));


        language = "fre";
        pageId = "licence";

        newPage.setLanguage(language);
        newPage.setPageId(pageId);
        newPage.setLink(link + "updated");
        newPage.setLabel(pageId + "updated");
        newPage.setIcon("dummy-icon");
        newPage.getSections().add(Page.PageSection.FOOTER);
        MockHttpServletRequestBuilder updatePageBuilder = put("/srv/api/pages/eng/license")
            .content(gson.toJson(newPage))
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json"));

        this.mockMvc.perform(updatePageBuilder)
            .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        page = pageRepository.findById(new PageIdentity(language, pageId));
        Assert.assertTrue(page.isPresent());
        Assert.assertEquals(link + "updated", page.get().getLink());
        Assert.assertEquals(pageId + "updated", page.get().getLabel());
        Assert.assertEquals("dummy-icon", page.get().getIcon());
        Assert.assertTrue(page.get().getSections().contains(Page.PageSection.TOP));
        Assert.assertTrue(page.get().getSections().contains(Page.PageSection.FOOTER));


        MockHttpServletRequestBuilder deletePageBuilder =
            delete("/srv/api/pages/" + language + "/" + pageId)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json"));

        this.mockMvc.perform(deletePageBuilder)
            .andExpect(status().is(HttpStatus.OK.value()));
        this.mockMvc.perform(deletePageBuilder)
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }
}
