/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.api.links;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LinksApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private UrlAnalyzer urlAnalyzer;

    private AbstractMetadata md;
    private MockMvc mockMvc;
    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    @Test
    public void getLinks() throws Exception {
        final MockHttpSession httpSession = this.loginAsAdmin();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Assert.assertEquals(1, linkRepository.count());

        this.mockMvc.perform(get("/srv/api/records/links")
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].url").value(equalTo("http://services.sandre.eaufrance.fr/geo/ouvrage")))
            .andExpect(jsonPath("$.content[0].records", hasSize(1)))
            .andExpect(jsonPath("$.content[0].records[0].metadataId").value(equalTo(md.getId())))
            .andExpect(jsonPath("$.content[0].records[0].metadataUuid").value(equalTo(md.getUuid())));;

        this.mockMvc.perform(delete("/srv/api/records/links")
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNoContent());

        Assert.assertEquals(0, linkRepository.count());
    }

    private void createTestData() throws Exception {
        loginAsAdmin(context);
        this.md = injectMetadataInDb(getSampleMetadataXml(), context);
        Element xmlData = this.md.getXmlData(false);

        urlAnalyzer.processMetadata(xmlData, this.md);
    }
}
