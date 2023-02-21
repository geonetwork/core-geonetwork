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
package org.fao.geonet.api.records;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Tests for class {@link MetadataApi}.
 *
 * @author juanluisrp
 **/
@ContextConfiguration(inheritLocations = true, locations = "classpath:schema-test-context.xml")
public class MetadataAssociatedApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SourceRepository sourceRepository;

    @PersistenceContext
    private EntityManager _entityManager;

    @Autowired private MetadataRepository metadataRepository;

    private String uuid;
    private int id;
    private ServiceContext context;


    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
    }

    @Test
    public void getRelatedNonExistent() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();
        String nonExistentUuid = UUID.randomUUID().toString();

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid + "/related")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.code").value(equalTo("resource_not_found")));

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid + "/related")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string("resource_not_found"));

    }

    @Test
    public void getAssociated() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        serviceManager.setBaseUrl("/geonetwork");

        // Load sample
        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("/org/fao/geonet/api/records/samples/related-test.zip");
        importMetadata.invoke();
        final String SERIE_UUID = "87e54d56-323f-4201-88ac-7ac7f9d8ee25";
        final String DATASET_UUID = "842f9143-fd7d-452c-96b4-425ca1281642";


        // Can't retrieve association of a private record
        mockHttpSession = loginAsAnonymous();
        mockMvc.perform(get("/srv/api/records/" + SERIE_UUID + "/associated")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.code").value(equalTo("forbidden")))
            .andExpect(jsonPath("$.message").value(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));


        // Once publish, we can retrieve them
        mockHttpSession = loginAsAdmin();
        mockMvc.perform(put("/srv/api/records/" + SERIE_UUID + "/publish")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(204));
        mockHttpSession = loginAsAnonymous();
        mockMvc.perform(get("/srv/api/records/" + SERIE_UUID + "/associated")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());


        // Private records are not listed. eg. parent
        ResultActions resultActions = mockMvc.perform(get("/srv/api/records/" + SERIE_UUID + "/associated")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.siblings", hasSize(1)))  // Remote record are always public
            .andExpect(jsonPath("$.parent", hasSize(0)));


        // Authenticated user can access private records
        mockHttpSession = loginAsAdmin();
        resultActions = mockMvc.perform(get("/srv/api/records/" + SERIE_UUID + "/associated")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.parent", hasSize(2)))
            .andExpect(jsonPath("$.parent[0].origin").value("catalog"))
            .andExpect(jsonPath("$.parent[0]._source.resourceTitleObject.default").value("TESTASSOCIATED - parent FR"));


        // Siblings have association and initiative type properties
        // and are flagged remote when needed.
        resultActions = mockMvc.perform(get("/srv/api/records/" + SERIE_UUID + "/associated?type=siblings")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.siblings", hasSize(3)))
            .andExpect(jsonPath("$.siblings[0].origin").value("catalog"))
            .andExpect(jsonPath("$.siblings[0].properties.associationType").value("crossReference"))
            .andExpect(jsonPath("$.siblings[0].properties.initiativeType").value("collection"))
            .andExpect(jsonPath("$.siblings[2].origin").value("remote"))
            .andExpect(jsonPath("$.siblings[2]._source.resourceTitleObject.default").value("Les offres touristiques en Wallonie"));



        // Check parent / children relation
        String PARENT_UUID = "54d0d235-fa85-4b8a-9ce6-8246b430a810";
        resultActions = mockMvc.perform(get("/srv/api/records/" + PARENT_UUID + "/associated?type=children")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.children", hasSize(1)))
            .andExpect(jsonPath("$.children[0]._source.uuid").value(SERIE_UUID));


        // Check sources / hassources relation
        String SOURCE_UUID = "0ab09197-4b5b-40b2-9f05-0e34355ff806";
        resultActions = mockMvc.perform(get("/srv/api/records/" + SERIE_UUID + "/associated?type=sources")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.sources", hasSize(1)))
            .andExpect(jsonPath("$.sources[0]._source.uuid").value(SOURCE_UUID));


        resultActions = mockMvc.perform(get("/srv/api/records/" + SOURCE_UUID + "/associated?type=hassources")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.hassources", hasSize(1)))
            .andExpect(jsonPath("$.hassources[0]._source.uuid").value(SERIE_UUID));


        // Check siblings / associated relation
        String ASSOCIATED_UUID = "54d0d235-fa85-4b8a-9ce6-8246b430a810";
        resultActions = mockMvc.perform(get("/srv/api/records/" + ASSOCIATED_UUID + "/associated?type=associated")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.associated", hasSize(1)))
            .andExpect(jsonPath("$.associated[0]._source.uuid").value(SERIE_UUID));
    }
}
