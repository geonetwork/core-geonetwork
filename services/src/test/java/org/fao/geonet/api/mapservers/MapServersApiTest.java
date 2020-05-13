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
package org.fao.geonet.api.mapservers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.fao.geonet.api.FieldNameExclusionStrategy;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.MapServer;
import org.fao.geonet.repository.MapServerRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for MapServersApi.
 *
 * @author Jose García
 */
public class MapServersApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MapServerRepository mapServerRepo;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void setUp() {
        createTestData();
    }

    @Test
    public void getMapservers() throws Exception {
        Long mapServersCount = mapServerRepo.count();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/mapservers")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(mapServersCount.intValue())));
    }

    @Test
    public void getExistingMapserver() throws Exception {
        MapServer mapServer = mapServerRepo.findAll().get(0);
        Assert.assertNotNull(mapServer);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/mapservers/" + mapServer.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.name", is(mapServer.getName())));
    }

    @Test
    public void getNonExistingMapserver() throws Exception {
        MapServer mapServer = mapServerRepo.findOneById(100);
        Assert.assertNull(mapServer);

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/mapservers/100")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void addMapserver() throws Exception {
        MapServer mapServerToAdd = new MapServer();
        mapServerToAdd.setName("mapserver-toadd");
        mapServerToAdd.setConfigurl("http://mapserver-toadd/rest");
        mapServerToAdd.setDescription("A test mapserver");
        mapServerToAdd.setWmsurl("http://mapserver-toadd/wms");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_pushstyleinworkspace"))
            .create();
        String json = gson.toJson(mapServerToAdd);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/mapservers")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));
    }

    @Test
    public void updateMapserver() throws Exception {
        MapServer mapServerToUpdate = mapServerRepo.findAll().get(0);
        Assert.assertNotNull(mapServerToUpdate);

        mapServerToUpdate.setName(mapServerToUpdate.getName() + "-update");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_pushstyleinworkspace"))
            .create();
        String json = gson.toJson(mapServerToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/mapservers/" + mapServerToUpdate.getId())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }


    @Test
    public void updateNonExistingMapServer() throws Exception {
        MapServer mapServerToUpdate = mapServerRepo.findOne(222);
        Assert.assertNull(mapServerToUpdate);

        mapServerToUpdate = new MapServer();
        mapServerToUpdate.setId(222);
        mapServerToUpdate.setName(mapServerToUpdate.getName() + "-update");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_pushstyleinworkspace"))
            .create();
        String json = gson.toJson(mapServerToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/mapservers/222")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    @Test
    public void deleteExistingMapserver() throws Exception {
        MapServer mapServerToDelete = mapServerRepo.findAll().get(0);
        Assert.assertNotNull(mapServerToDelete);

        int mapServerId = mapServerToDelete.getId();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/mapservers/" + mapServerToDelete.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        mapServerToDelete = mapServerRepo.findOne(mapServerId);
        Assert.assertNull(mapServerToDelete);
    }

    @Test
    public void deleteNonExistingMapserver() throws Exception {
        MapServer mapServerToDelete = mapServerRepo.findOne(222);
        Assert.assertNull(mapServerToDelete);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 404 is returned
        this.mockMvc.perform(delete("/srv/api/mapservers/222")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    /**
     * Create sample data for the tests.
     */
    private void createTestData() {
        MapServer mapServerWms = new MapServer();
        mapServerWms.setName("mapserverwms");
        mapServerWms.setConfigurl("http://mapserverwms/rest");
        mapServerWms.setDescription("A test mapserver wms");
        mapServerWms.setWmsurl("http://mapserverwms/wms");

        mapServerRepo.save(mapServerWms);


        MapServer mapServerWfs = new MapServer();
        mapServerWfs.setName("mapserverwfs");
        mapServerWfs.setConfigurl("http://mapserverwfs/rest");
        mapServerWfs.setDescription("A test mapserver wfs");
        mapServerWfs.setWmsurl("http://mapserverwfs/wfs");

        mapServerRepo.save(mapServerWfs);

    }
}
