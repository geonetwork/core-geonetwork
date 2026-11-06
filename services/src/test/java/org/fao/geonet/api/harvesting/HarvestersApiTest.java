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
package org.fao.geonet.api.harvesting;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.kernel.harvest.HarvestManagerImpl;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HarvestersApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SourceRepository sourceRepo;

    @Autowired
    private HarvestManagerImpl harvestManager;

    @Autowired
    private HarvestHistoryRepository historyRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        createTestData();
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        harvestManager.init(context, false);
    }

    @Test
    public void harvesterLifecycle() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession session = loginAsAdmin();

        String uuid = addHarvester(session, "API harvester");

        mockMvc.perform(get("/srv/api/harvesters").session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].site.uuid", hasItem(uuid)));

        // Harvester definitions keep the format converted from their XML configuration.
        mockMvc.perform(get("/srv/api/harvesters/" + uuid).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['@type']", is("csw")))
            .andExpect(jsonPath("$.site.name", is("API harvester")))
            .andExpect(jsonPath("$.options.status", is("inactive")));

        mockMvc.perform(get("/srv/api/harvesters/status").param("uuid", uuid).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].info").exists())
            .andExpect(jsonPath("$[0].site").doesNotExist());

        mockMvc.perform(put("/srv/api/harvesters/" + uuid).session(session)
                .contentType(MediaType.APPLICATION_XML).content(cswHarvester("Renamed harvester", uuid)))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/srv/api/harvesters/" + uuid).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.site.name", is("Renamed harvester")));

        MvcResult clone = mockMvc.perform(post("/srv/api/harvesters/" + uuid + "/clone").session(session)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.uuid", not(is(uuid))))
            .andReturn();
        String cloneUuid = uuidOf(clone);

        mockMvc.perform(post("/srv/api/harvesters/" + uuid + "/start").session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("ok")));
        mockMvc.perform(post("/srv/api/harvesters/" + uuid + "/stop").session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/srv/api/harvesters/" + cloneUuid).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("ok")));
        mockMvc.perform(get("/srv/api/harvesters/" + cloneUuid).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void harvesterHistory() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession session = loginAsAdmin();
        String uuid = addHarvester(session, "History harvester");

        HarvestHistory first = addHistory(uuid, "1980-01-01T10:00:00");
        HarvestHistory latest = addHistory(uuid, "1980-02-01T10:00:00");
        HarvestHistory oldest = addHistory(uuid, "1979-02-01T10:00:00");

        mockMvc.perform(get("/srv/api/harvesters/" + uuid + "/history").param("size", "2").session(session)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total", is(3)))
            .andExpect(jsonPath("$.pages", is(2)))
            .andExpect(jsonPath("$.history", hasSize(2)))
            .andExpect(jsonPath("$.history[0].id", is(String.valueOf(latest.getId()))))
            .andExpect(jsonPath("$.history[1].id", is(String.valueOf(first.getId()))));
        mockMvc.perform(get("/srv/api/harvesters/" + uuid + "/history").param("page", "1").param("size", "2").session(session)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.history[0].id", is(String.valueOf(oldest.getId()))));

        mockMvc.perform(delete("/srv/api/harvesters/" + uuid + "/history").session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deleted", is(3)));
        assertEquals(0, historyRepository.count());
    }

    @Test
    public void harvesterConfigurationOptions() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/srv/api/harvesters/types").session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasItem("csw")));
        mockMvc.perform(get("/srv/api/harvesters/validation-options").session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasItem("NOVALIDATION")));
        mockMvc.perform(get("/srv/api/harvesters/stylesheets/fragments").param("type", "wfs").param("schema", "iso19139")
                .session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].schema", is("iso19139")));
        mockMvc.perform(get("/srv/api/harvesters/stylesheets/fragments").param("type", "sos")
                .session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void harvesterLogOnlyServesHarvesterLogFiles() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession session = loginAsAdmin();
        for (String file : new String[]{"geonetwork.log", "../harvester_secret.log", "sub/../../harvester_secret.log",
            "harvester_missing.log"}) {
            mockMvc.perform(get("/srv/api/harvesters/logs").param("file", file).session(session))
                .andExpect(status().isNotFound());
        }
    }

    private String addHarvester(MockHttpSession session, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/srv/api/harvesters").session(session)
                .contentType(MediaType.APPLICATION_XML).content(cswHarvester(name, ""))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn();
        return uuidOf(result);
    }

    private static String uuidOf(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString().replaceAll(".*\"uuid\":\"([^\"]+)\".*", "$1");
    }

    private HarvestHistory addHistory(String harvesterUuid, String date) throws Exception {
        return historyRepository.save(new HarvestHistory()
            .setDeleted(false)
            .setElapsedTime(3000)
            .setHarvestDate(new ISODate(date))
            .setHarvesterName("History harvester")
            .setHarvesterType("csw")
            .setHarvesterUuid(harvesterUuid)
            .setInfo(new org.jdom.Element("a").setText("b"))
            .setParams(Xml.loadString("<param1>1</param1>", false)));
    }

    /**
     * The icon does not exist on purpose: copying an existing one fails with the in-memory data directory of the tests.
     */
    private static String cswHarvester(String name, String uuid) {
        return "<node type=\"csw\" owner=\"1\">"
            + "<site><ownerGroup>2</ownerGroup><name>" + name + "</name><uuid>" + uuid + "</uuid><icon>icon.png</icon>"
            + "<capabilitiesUrl>http://localhost:9/csw</capabilitiesUrl>"
            + "<outputSchema>http://www.isotc211.org/2005/gmd</outputSchema></site>"
            + "<content><validate>NOVALIDATION</validate><importxsl>none</importxsl></content>"
            + "<options><every>0 0 5 ? * MON</every><oneRunOnly>false</oneRunOnly><status>inactive</status></options>"
            + "<privileges><group id=\"1\"><operation name=\"view\"/></group></privileges>"
            + "</node>";
    }

    @Test
    public void getHarvesterLogoReturnsLogo() throws Exception {
        Source source = sourceRepo.findOneByName("harvester-source");
        assertNotNull(source);

        // The harvester has no logo so a transparent 1x1 PNG is returned.
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/harvesters/" + source.getUuid() + "/logo"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING));
    }

    @Test
    public void getHarvesterLogoWithNonExistingHarvester() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/harvesters/not-an-existing-harvester/logo"))
            .andExpect(status().isNotFound());
    }

    private void createTestData() {
        Source source = new Source();
        source.setName("harvester-source");
        source.setUuid("harvester-source-uuid");
        source.setCreationDate(new ISODate());
        source.setType(SourceType.harvester);
        sourceRepo.save(source);
    }
}
