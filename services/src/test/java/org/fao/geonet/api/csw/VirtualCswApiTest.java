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
package org.fao.geonet.api.csw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.Service;
import org.fao.geonet.repository.ServiceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for VirtualCswApi.
 *
 * @author Jose García
 */
public class VirtualCswApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ServiceRepository _serviceRepo;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void setUp() throws Exception {
        createTestData();
    }

    @Test
    public void getAllVirtualCsw() throws Exception {
        List<Service> services = _serviceRepo.findAll();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/csw/virtuals")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(services.size())));
    }

    @Test
    public void getVirtualCsw() throws Exception {
        Service service = _serviceRepo.findOneByName("csw-endpoint1");
        Assert.assertNotNull(service);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/csw/virtuals/" + service.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("csw-endpoint1")));
    }


    @Test
    public void deleteVirtualCsw() throws Exception {
        Service service = _serviceRepo.findOneByName("csw-endpoint1");
        Assert.assertNotNull(service);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/csw/virtuals/" + service.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void deleteNonExistingVirtualCsw() throws Exception {
        Service service = _serviceRepo.findOne(222);
        Assert.assertNull(service);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/csw/virtuals/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    @Test
    public void addService() throws Exception {
        Service service = _serviceRepo.findOneByName("csw-endpoint-toadd");
        Assert.assertNull(service);

        service = new Service();
        // TODO: Would be better that id is an Integer to use null for new records
        service.setId(-99);
        service.setClassName(".services.main.CswDiscoveryDispatcher");
        service.setName("csw-endpoint-toadd");
        service.setDescription("A CSW test endpoint - serviceToAdd");
        service.setExplicitQuery("query");


        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(service);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/csw/virtuals")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));
    }

    @Test
    public void addExistingService() throws Exception {
        Service service = _serviceRepo.findOneByName("csw-endpoint1");
        Assert.assertNotNull(service);

        Service serviceToAdd = new Service();
        // TODO: Would be better that id is an Integer to use null for new records
        serviceToAdd.setId(-99);
        serviceToAdd.setName("csw-endpoint1");
        serviceToAdd.setDescription("A CSW test endpoint - 1 - toadd");
        serviceToAdd.setExplicitQuery("query");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(serviceToAdd);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/csw/virtuals")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.description", is("A service already exist with this name 'csw-endpoint1'. Choose another name.")));
    }

    @Test
    public void updateVirtualCsw() throws Exception {
        Service service = _serviceRepo.findOneByName("csw-endpoint1");
        Assert.assertNotNull(service);

        service.setDescription("A CSW test endpoint - 1 - updated");
        service.setExplicitQuery("query");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(service);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/csw/virtuals/" + service.getId())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void updateNonexistingVirtualCsw() throws Exception {
        Service serviceToUpdate = _serviceRepo.findOne(222);
        Assert.assertNull(serviceToUpdate);

        serviceToUpdate = new Service();
        serviceToUpdate.setId(222);
        serviceToUpdate.setDescription("A CSW test endpoint - 1 - updated");
        serviceToUpdate.setExplicitQuery("query");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(serviceToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/csw/virtuals/" + serviceToUpdate.getId())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    private void createTestData() {
        Service service1 = new Service();
        service1.setClassName(".services.main.CswDiscoveryDispatcher");
        service1.setName("csw-endpoint1");
        service1.setDescription("A CSW test endpoint - 1");
        _serviceRepo.save(service1);

        Service service2 = new Service();
        service2.setClassName(".services.main.CswDiscoveryDispatcher");
        service2.setName("csw-endpoint2");
        service2.setDescription("A CSW test endpoint - 2");

        _serviceRepo.save(service2);
    }
}
