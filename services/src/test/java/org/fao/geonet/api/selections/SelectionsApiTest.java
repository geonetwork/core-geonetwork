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
package org.fao.geonet.api.selections;

import com.vividsolutions.jts.util.Assert;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for SelectionsApi.
 *
 * @author Jose García
 */
public class SelectionsApiTest  extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void setUp() throws Exception {
        this.mockHttpSession = loginAsAdmin();

        UserSession session = ApiUtils.getUserSession( this.mockHttpSession);

        String[] uuids = {"uuid1", "uuid2"};
        ServiceContext context = createServiceContext();

        int nbSelected = SelectionManager.updateSelection(SelectionManager.SELECTION_METADATA,
            session, SelectionManager.ADD_SELECTED, Arrays.asList(uuids), context);
        Assert.isTrue(nbSelected == 2);
    }

    @Test
    public void getSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        UserSession session = ApiUtils.getUserSession( this.mockHttpSession);
        int selected = SelectionManager.getManager(session).getSelection(SelectionManager.SELECTION_METADATA).size();

        this.mockMvc.perform(get("/srv/api/selections/" + SelectionManager.SELECTION_METADATA)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(selected)));
    }

    @Test
    public void addSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(put("/srv/api/selections/" + SelectionManager.SELECTION_METADATA)
            .param("uuid", "uuid3")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(content().string("3"));

        // Check also the SelectionManager
        UserSession session = ApiUtils.getUserSession( this.mockHttpSession);
        int selected = SelectionManager.getManager(session).getSelection(SelectionManager.SELECTION_METADATA).size();
        Assert.isTrue(selected == 3);
    }


    @Test
    public void clearSelectionAll() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/selections/" + SelectionManager.SELECTION_METADATA)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(content().string("0"));

        // Check also the SelectionManager
        UserSession session = ApiUtils.getUserSession( this.mockHttpSession);
        int selected = SelectionManager.getManager(session).getSelection(SelectionManager.SELECTION_METADATA).size();
        Assert.isTrue(selected == 0);
    }

    @Test
    public void clearSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        // Remove only 1 item from the selection
        this.mockMvc.perform(delete("/srv/api/selections/" + SelectionManager.SELECTION_METADATA)
            .param("uuid", "uuid1")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(content().string("1"));

        // Check also the SelectionManager
        UserSession session = ApiUtils.getUserSession( this.mockHttpSession);
        int selected = SelectionManager.getManager(session).getSelection(SelectionManager.SELECTION_METADATA).size();
        Assert.isTrue(selected == 1);
    }
}
