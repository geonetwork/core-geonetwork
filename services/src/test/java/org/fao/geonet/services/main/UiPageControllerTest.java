/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.services.main;

import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * User interface pages and the info service, which no longer go through Jeeves.
 */
public class UiPageControllerTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void pages() throws Exception {
        for (String page : new String[]{"catalog.search", "admin.console", "catalog.signin", "new.password"}) {
            mockMvc.perform(get("/srv/eng/" + page))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(content().string(containsString("<html")));
        }
    }

    @Test
    public void openSearchDescription() throws Exception {
        mockMvc.perform(get("/srv/eng/portal.opensearch"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/xml;charset=UTF-8"))
            .andExpect(xpath("/os:OpenSearchDescription", Map.of("os", "http://a9.com/-/spec/opensearch/1.1/")).exists());
    }

    @Test
    public void otherServicesAreNotFound() throws Exception {
        mockMvc.perform(get("/srv/eng/xml.info"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void info() throws Exception {
        mockMvc.perform(get("/srv/eng/info").param("type", "sources").param("type", "site"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/xml;charset=UTF-8"))
            .andExpect(xpath("/info/sources").exists())
            .andExpect(xpath("/info/site/name").exists());

        mockMvc.perform(get("/srv/eng/info?type=site&_content_type=json"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.site.name").exists());

        mockMvc.perform(get("/srv/eng/info").param("type", "unknown"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType("application/json"));
    }
}
