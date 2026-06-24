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

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HarvestersApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SourceRepository sourceRepo;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        createTestData();
    }

    @Test
    public void getHarvesterLogoRedirectsToSourceLogo() throws Exception {
        Source source = sourceRepo.findOneByName("harvester-source");
        assertNotNull(source);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/harvesters/" + source.getUuid() + "/logo"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/srv/api/sources/" + source.getUuid() + "/logo"));
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
