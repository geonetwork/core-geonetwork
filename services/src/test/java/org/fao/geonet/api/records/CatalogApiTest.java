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
package org.fao.geonet.api.records;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V1_ACCEPT_TYPE;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V3_ACCEPT_TYPE;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for class {@link CatalogApi}.
 */
public class CatalogApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private IMetadataIndexer metadataIndexer;

    private String uuid;

    @Before
    public void setUp() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        AbstractMetadata metadata = injectMetadataInDb(getSampleMetadataXml(), context);
        uuid = metadata.getUuid();
        metadataIndexer.indexMetadata(String.valueOf(metadata.getId()), DirectIndexSubmitter.INSTANCE, IndexingMode.full);
    }

    /**
     * Regression guard for the bug where {@code exportAsMef} - like {@code MetadataApi#getRecordAsZip}
     * before it - always exported MEF version 3 content but labeled the response as version 2
     * regardless of what was actually requested, and never actually honored an explicit request
     * for version 2. Mirrors {@link MetadataApiTest#getRecordAsZip()}'s coverage of the same bug
     * for the single-record endpoint.
     */
    @Test
    public void exportAsMefHonorsRequestedVersion() throws Exception {
        final String zipMagicNumber = "PK";

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        // No specific MEF Accept type (a generic "application/zip") should get MEF version 3 -
        // the modern default export format for this endpoint, not version 2.
        mockMvc.perform(get("/srv/api/records/zip")
                .session(mockHttpSession)
                .param("uuids", uuid)
                .accept("application/zip"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MEF_V3_ACCEPT_TYPE))
            .andExpect(content().string(startsWith(zipMagicNumber)));

        // Explicitly requesting MEF version 2 should actually get version 2 back, not silently
        // upgraded to version 3 with a version-2 label (the bug this test guards against).
        mockMvc.perform(get("/srv/api/records/zip")
                .session(mockHttpSession)
                .param("uuids", uuid)
                .accept(MEF_V2_ACCEPT_TYPE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MEF_V2_ACCEPT_TYPE))
            .andExpect(content().string(startsWith(zipMagicNumber)));

        // Explicitly requesting MEF version 3 should be correctly labeled as version 3, not the
        // version-2 label the buggy version of this endpoint always used to send.
        mockMvc.perform(get("/srv/api/records/zip")
                .session(mockHttpSession)
                .param("uuids", uuid)
                .accept(MEF_V3_ACCEPT_TYPE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MEF_V3_ACCEPT_TYPE))
            .andExpect(content().string(startsWith(zipMagicNumber)));
    }

    /**
     * MEF version 1 only supports a single record and this endpoint always rejects it -
     * unaffected by the version-negotiation fix, kept as a guard that it stays rejected.
     */
    @Test
    public void exportAsMefRejectsVersion1() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        mockMvc.perform(get("/srv/api/records/zip")
                .session(mockHttpSession)
                .param("uuids", uuid)
                .accept(MEF_V1_ACCEPT_TYPE))
            .andExpect(status().isBadRequest());
    }
}
