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
package org.fao.geonet.api.schematron;

import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaGroupId;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SchematronApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SchematronRepository schematronRepository;

    @Autowired
    private SchematronCriteriaGroupRepository groupRepository;

    @Autowired
    private SchematronCriteriaRepository criteriaRepository;

    @Autowired
    private SchemaManager schemaManager;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private Schematron schematron;

    @Before
    public void setUp() {
        groupRepository.deleteAll();
        schematronRepository.deleteAll();
        schematron = addSchematron("iso19139");
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        session = loginAsAdmin();
    }

    @Test
    public void getSchematrons() throws Exception {
        addGroup("group1");

        mockMvc.perform(get("/srv/api/schematron").session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requirements", hasSize(SchematronRequirement.values().length)))
            .andExpect(jsonPath("$.schemas", hasSize(1)))
            .andExpect(jsonPath("$.schemas[0].name", is("iso19139")))
            .andExpect(jsonPath("$.schemas[0].schematrons[0].id", is(schematron.getId())))
            .andExpect(jsonPath("$.schemas[0].schematrons[0].title", not(isEmptyOrNullString())))
            .andExpect(jsonPath("$.schemas[0].schematrons[0].groupCount", is(1)))
            // Types defined by the standard come first, then the generic ones.
            .andExpect(jsonPath("$.schemas[0].criteriaTypes[0].name", is("Keyword")))
            .andExpect(jsonPath("$.schemas[0].criteriaTypes[0].type", is("XPATH")))
            .andExpect(jsonPath("$.schemas[0].criteriaTypes[*].name", hasItem("ALWAYS_ACCEPT")))
            .andExpect(jsonPath("$.schemas[0].criteriaTypes[?(@.name == 'XPATH')].value", hasItem("@@value@@")));
    }

    @Test
    public void updateDisplayPriority() throws Exception {
        mockMvc.perform(put("/srv/api/schematron/" + schematron.getId()).param("displayPriority", "7").session(session))
            .andExpect(status().isNoContent());
        assertEquals(7, schematronRepository.findById(schematron.getId()).get().getDisplayPriority());

        mockMvc.perform(put("/srv/api/schematron/" + (schematron.getId() + 1000)).param("displayPriority", "7").session(session))
            .andExpect(status().isNotFound());
    }

    @Test
    public void groupLifecycle() throws Exception {
        String groupsUrl = "/srv/api/schematron/" + schematron.getId() + "/groups";

        mockMvc.perform(post(groupsUrl).param("name", "a/b group").param("requirement", "REPORT_ONLY").session(session)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id.name", is("a/b group")))
            .andExpect(jsonPath("$.id.schematronId", is(schematron.getId())))
            .andExpect(jsonPath("$.requirement", is("REPORT_ONLY")));

        mockMvc.perform(post(groupsUrl).param("name", "a/b group").session(session))
            .andExpect(status().isBadRequest());

        addCriteria("a/b group", "value1");
        mockMvc.perform(get(groupsUrl).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].criteria", hasSize(1)))
            .andExpect(jsonPath("$[0].criteria[0].uiValue", is("value1")));

        mockMvc.perform(put(groupsUrl).param("name", "a/b group").param("requirement", "DISABLED").session(session))
            .andExpect(status().isNoContent());
        assertEquals(SchematronRequirement.DISABLED, getGroup("a/b group", schematron).getRequirement());

        // Rename and move to another schematron, criteria are kept.
        Schematron other = addSchematron("dublin-core");
        mockMvc.perform(put(groupsUrl).param("name", "a/b group").param("newName", "renamed")
                .param("newSchematronId", "" + other.getId()).session(session))
            .andExpect(status().isNoContent());
        assertFalse(groupRepository.existsById(new SchematronCriteriaGroupId("a/b group", schematron.getId())));
        SchematronCriteriaGroup moved = getGroup("renamed", other);
        assertEquals(SchematronRequirement.DISABLED, moved.getRequirement());
        assertEquals(1, moved.getCriteria().size());

        String otherGroupsUrl = "/srv/api/schematron/" + other.getId() + "/groups";
        mockMvc.perform(delete(otherGroupsUrl).param("name", "renamed").session(session))
            .andExpect(status().isNoContent());
        assertEquals(0, criteriaRepository.count());
        mockMvc.perform(delete(otherGroupsUrl).param("name", "renamed").session(session))
            .andExpect(status().isNotFound());
    }

    @Test
    public void criteriaLifecycle() throws Exception {
        addGroup("group1");
        int criteriaId = addCriteria("group1", "value1");

        mockMvc.perform(put("/srv/api/schematron/criteria/" + criteriaId).param("uiValue", "value2").session(session))
            .andExpect(status().isNoContent());
        SchematronCriteria criteria = criteriaRepository.findById(criteriaId).get();
        assertEquals("value2", criteria.getUiValue());
        assertEquals("Keyword", criteria.getUiType());

        mockMvc.perform(put("/srv/api/schematron/criteria/" + criteriaId).param("type", "NOT_A_TYPE").session(session))
            .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/srv/api/schematron/criteria/" + criteriaId).session(session))
            .andExpect(status().isNoContent());
        assertTrue(getGroup("group1", schematron).getCriteria().isEmpty());
        mockMvc.perform(delete("/srv/api/schematron/criteria/" + criteriaId).session(session))
            .andExpect(status().isNotFound());
    }

    private int addCriteria(String groupName, String uiValue) throws Exception {
        MvcResult result = mockMvc.perform(post("/srv/api/schematron/" + schematron.getId() + "/groups/criteria")
                .param("groupName", groupName)
                .param("type", SchematronCriteriaType.XPATH.name())
                .param("value", "*//gmd:keyword = '" + uiValue + "'")
                .param("uiType", "Keyword")
                .param("uiValue", uiValue)
                .session(session)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.uiValue", is(uiValue)))
            .andReturn();
        return Integer.parseInt(result.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private void addGroup(String name) {
        groupRepository.save(new SchematronCriteriaGroup()
            .setId(new SchematronCriteriaGroupId(name, schematron.getId()))
            .setRequirement(SchematronRequirement.REQUIRED));
    }

    private SchematronCriteriaGroup getGroup(String name, Schematron owner) {
        return groupRepository.findById(new SchematronCriteriaGroupId(name, owner.getId())).get();
    }

    private Schematron addSchematron(String schemaName) {
        Schematron schematron = new Schematron();
        schematron.setSchemaName(schemaName);
        schematron.setFile(schemaManager.getSchemaDir(schemaName) + File.separator + "schematron" + File.separator
            + "schematron-rules-geonetwork.sch");
        return schematronRepository.save(schematron);
    }
}
