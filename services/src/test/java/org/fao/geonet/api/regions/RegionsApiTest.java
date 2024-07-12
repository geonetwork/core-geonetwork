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
package org.fao.geonet.api.regions;

import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.fao.geonet.api.records.extent.MetadataExtentApiTest.saveImageToDiskIfConfiguredToDoSo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for RegionsApi.
 *
 * @author Jose García
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:extents-test-context.xml")
public class RegionsApiTest  extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Rule
    public TestName name = new TestName();

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Test
    public void getRegions() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/regions")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.regions", hasSize(greaterThan(0))));
    }

    @Test
    public void getRegionsForType() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Retrieve continent regions
        this.mockMvc.perform(get("/srv/api/regions")
            .session(this.mockHttpSession)
            .param("categoryId", "http://www.naturalearthdata.com/ne_admin#Continent")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.regions", hasSize(6)));
    }


    @Test
    public void getMapOfMultiPoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        byte[] reponseBuffer = mockMvc.perform(get("/srv/api/regions/geom.png?geomsrs=EPSG:4326&geom=GEOMETRYCOLLECTION(POINT(-32.277667%2037.291833),%20POINT(-44.949833%2023.367667),%20POINT(-31.556167%2037.841167))&width=600&strokeColor=255,255,0,255")
                        .session(mockHttpSession)
                        .accept(MediaType.IMAGE_PNG_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
                .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("3a817d8835c902dc86c65d833d9e4a55", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    @Test
    public void getMapOfMultiBbox() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        byte[] reponseBuffer = mockMvc.perform(get("/srv/api/regions/geom.png?geomsrs=EPSG:4326&geom=GEOMETRYCOLLECTION(POLYGON((10.5253%206.7632,10.5253%2023.2401,-18.3746%2023.2401,-18.3746%206.7632,10.5253%206.7632)),POLYGON((-28.8471%20-34.1253,-28.8471%205.2718,-73.9828%205.2718,-73.9828%20-34.1253,-28.8471%20-34.1253)))")
                        .session(mockHttpSession)
                        .accept(MediaType.IMAGE_PNG_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
                .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("f82fa4618ad696a3561c0d4159bc5f1b", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    @Test
    public void getRegionTypes() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/regions/types")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$[*].id", hasItem("http://www.naturalearthdata.com/ne_admin#Continent")))
            .andExpect(jsonPath("$[*].id", hasItem("http://www.naturalearthdata.com/ne_admin#Country")));

    }
}
