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

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.api.records.formatters.XmlViewFormatterTest;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.equalTo;


/**
 * Tests for class {@link MetadataApi}.
 *
 * @author juanluisrp
 *
 **/
public class MetadataApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SourceRepository sourceRepository;

    private String uuid;
    private int id;


    @Before
    public void setUp() throws Exception {
        createTestdata();
    }

    private void createTestdata() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element sampleMetadataXml = getSampleMetadataXml();
        this.uuid = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(this.uuid);

        String source = sourceRepository.findAll().get(0).getUuid();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        final Metadata metadata = new Metadata().setDataAndFixCR(sampleMetadataXml).setUuid(uuid);
        metadata.getDataInfo().setRoot(sampleMetadataXml.getQualifiedName()).setSchemaId(schema).setType(MetadataType.METADATA);
        metadata.getSourceInfo().setOwner(1).setSourceId(source);
        metadata.getHarvestInfo().setHarvested(false);

        this.id = dataManager.insertMetadata(serviceContext, metadata, sampleMetadataXml, false, false, false, UpdateDatestamp.NO,
            false, false).getId();

        dataManager.indexMetadata(Lists.newArrayList("" + this.id));
    }


    @Test
    public void getNonExistentRecordRecord() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();
        String nonExistentUuid = UUID.randomUUID().toString();
        mockMvc.perform(get("/api/records/" + nonExistentUuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value(equalTo("resource_not_found")));

        mockMvc.perform(get("/api/records/" + nonExistentUuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string("resource_not_found"));

        mockMvc.perform(get("/api/records/" + nonExistentUuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string("resource_not_found"));

        mockMvc.perform(get("/api/records/" + nonExistentUuid)
            .session(mockHttpSession)
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andExpect(xpath("/apiError/code").string("resource_not_found"));
    }

    @Test
    public void getRecordAsXML() throws Exception {

    }

    @Test
    public void getRecordAsZip() throws Exception {

    }

    @Test
    public void getRelated() throws Exception {

    }

}
