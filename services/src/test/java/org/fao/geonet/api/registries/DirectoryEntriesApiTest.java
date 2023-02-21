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

package org.fao.geonet.api.registries;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class DirectoryEntriesApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private MetadataRepository metadataRepo;

    @Autowired
    private WebApplicationContext wac;

    @Test
    public void exportLetSubtemplateUnchanged() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        URL resource = DirectoryEntriesApiTest.class.getResource("extent_with_default_namespace_in_gml_for_srs.xml");
        int id = importMetadataXML(context, "uuid", resource.openStream(),
                MetadataType.SUB_TEMPLATE,
                ReservedGroup.all.getId(),
                Params.GENERATE_UUID);

        Metadata record = metadataRepo.findOne((Specification<Metadata>)MetadataSpecs.hasMetadataId(id)).get();

        MockHttpSession mockHttpSession = loginAsAdmin();
        MockHttpServletResponse response = mockMvc.perform(get(String.format("/srv/api/registries/entries/%s", record.getUuid()))
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_XML_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(API_XML_EXPECTED_ENCODING))
                .andReturn().getResponse();

        String returned = response.getContentAsString().replaceAll("\r", "").trim();
        String inserted = IOUtils.toString(resource).trim();
        assertFalse(returned.contains("xmlns=\"\""));
        assertEquals(inserted, returned);  // strictly not necessary ?
    }
}
