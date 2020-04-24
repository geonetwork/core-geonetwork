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

import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V1_ACCEPT_TYPE;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.Lists;

import jeeves.server.context.ServiceContext;


/**
 * Tests for class {@link MetadataApi}.
 *
 * @author juanluisrp
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

    @PersistenceContext
    private EntityManager _entityManager;

    @Autowired private MetadataRepository metadataRepository;

    private String uuid;
    private int id;
    private AbstractMetadata md;
    private ServiceContext context;


    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    private void createTestData() throws Exception {
        loginAsAdmin(context);

        final Element sampleMetadataXml = getSampleMetadataXml();
        this.uuid = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(this.uuid);

        String source = sourceRepository.findAll().get(0).getUuid();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        final Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(sampleMetadataXml).setUuid(uuid);
        metadata.getDataInfo().setRoot(sampleMetadataXml.getQualifiedName()).setSchemaId(schema).setType(MetadataType.METADATA);
        metadata.getDataInfo().setPopularity(1000);
        metadata.getSourceInfo().setOwner(1).setSourceId(source);
        metadata.getHarvestInfo().setHarvested(false);


        this.id = dataManager.insertMetadata(context, metadata, sampleMetadataXml, false, false, false, UpdateDatestamp.NO,
            false, false).getId();


        dataManager.indexMetadata(Lists.newArrayList("" + this.id));
        this.md = metadataRepository.findOne(this.id);
    }


    @Test
    public void getNonExistentRecordRecord() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();
        String nonExistentUuid = UUID.randomUUID().toString();

        List<String> contentTypeWithoutBodyList = Lists.newArrayList(
            MediaType.TEXT_HTML_VALUE,
            "application/pdf",
            "application/zip",
            MEF_V1_ACCEPT_TYPE,
            MEF_V2_ACCEPT_TYPE
        );

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.code").value(equalTo("resource_not_found")));

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string("resource_not_found"));


        for (String contentTypeWithoutBody : contentTypeWithoutBodyList) {
            mockMvc.perform(get("/srv/api/records/" + nonExistentUuid)
                .session(mockHttpSession)
                .accept(contentTypeWithoutBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string(isEmptyOrNullString()));
        }
    }

    @Test
    public void getNonAllowedRecord() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();

        List<String> contentTypeWithoutBodyList = Lists.newArrayList(
            MediaType.TEXT_HTML_VALUE,
            "application/pdf",
            "application/zip",
            MEF_V1_ACCEPT_TYPE,
            MEF_V2_ACCEPT_TYPE
        );


        mockMvc.perform(get("/srv/api/records/" + this.uuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.code").value(equalTo("forbidden")))
            .andExpect(jsonPath("$.message").value(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));

        mockMvc.perform(get("/srv/api/records/" + this.uuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string(equalTo("forbidden")))
            .andExpect(xpath("/apiError/message").string(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));

        mockMvc.perform(get("/srv/api/records/" + this.uuid)
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XHTML_XML_VALUE))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_XHTML_XML_VALUE))
            .andExpect(xpath("/apiError/code").string(equalTo("forbidden")))
            .andExpect(xpath("/apiError/message").string(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));


        for (String contentTypeWihoutBody : contentTypeWithoutBodyList) {
            mockMvc.perform(get("/srv/api/records/" + this.uuid)
                .session(mockHttpSession)
                .accept(contentTypeWihoutBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string(isEmptyOrNullString()));
        }
    }

    @Test
    public void getRecord() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        Map<String, String> contentTypes = new LinkedHashMap<>();
        contentTypes.put(MediaType.TEXT_HTML_VALUE, this.uuid + "/formatters/xsl-view");
        contentTypes.put(MediaType.APPLICATION_XHTML_XML_VALUE, this.uuid + "/formatters/xsl-view");
        contentTypes.put("application/pdf", this.uuid + "/formatters/xsl-view");
        contentTypes.put(MediaType.APPLICATION_XML_VALUE, this.uuid + "/formatters/xml");
        contentTypes.put(MediaType.APPLICATION_JSON_VALUE, this.uuid + "/formatters/xml");
        contentTypes.put("application/zip", this.uuid + "/formatters/zip");
        contentTypes.put(MEF_V1_ACCEPT_TYPE, this.uuid + "/formatters/zip");
        contentTypes.put(MEF_V2_ACCEPT_TYPE, this.uuid + "/formatters/zip");

        for(Map.Entry<String, String> entry : contentTypes.entrySet()) {
            mockMvc.perform(get("/srv/api/records/" + this.uuid)
                .session(mockHttpSession)
                .accept(entry.getKey()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(entry.getValue()));
        }
    }

    @Test
    public void getRecordThruSpringLocalServiceInvoker() throws Exception {
        MockMvcBuilders.webAppContextSetup(this.wac).build();
        loginAsAdmin();
        SpringLocalServiceInvoker toTest = super._applicationContext.getBean(SpringLocalServiceInvoker.class);
        super._applicationContext.getBean(NodeInfo.class).setId("srv");
        toTest.init();

        Object resp = toTest.invoke("local://srv/api/records/" + uuid + "/formatters/xml");

        assertEquals("MD_Metadata", ((Element)resp).getName());
    }

    @Test
    public void getRecordAsXML() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/xml")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "xml"))))
            .andExpect(content().string(containsString(this.uuid)))
            .andExpect(xpath("/MD_Metadata/fileIdentifier/CharacterString").string(this.uuid));

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/json")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "json"))))
            .andExpect(content().string(containsString(this.uuid)))
            .andExpect(jsonPath("$.['gmd:fileIdentifier'].['gco:CharacterString'].['#text']").value(this.uuid));
    }

    @Test
    public void getRecordAsXMLAddSchemaLocation() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        // Add Schema locations
        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/xml").param("addSchemaLocation", "true")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "xml"))))
            .andExpect(content().string(containsString(this.uuid)))
            .andExpect(content().string(containsString(".xsd")))
            .andExpect(xpath("/MD_Metadata/fileIdentifier/CharacterString").string(this.uuid));

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/xml").param("addSchemaLocation", "false")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "xml"))))
            .andExpect(content().string(containsString(this.uuid)))
            .andExpect(content().string(not(containsString(".xsd"))))
            .andExpect(xpath("/MD_Metadata/fileIdentifier/CharacterString").string(this.uuid));
    }

    @Test
    public void getRecordAsXMLIncreasePopularity() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        int popularity = md.getDataInfo().getPopularity();

        // Add Schema locations
        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/xml").param("increasePopularity", "true")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "xml"))))
            .andExpect(content().string(containsString(this.uuid)))
            .andExpect(xpath("/MD_Metadata/fileIdentifier/CharacterString").string(this.uuid));

        // Seem some issue with the transaction in the tests, requires to use explicitly the entity manager.
        // In the application looks working fine with the @Transactional annotation in MetadataRepository.incrementPopularity
        _entityManager.flush();
        _entityManager.clear();

        int newPopularity = metadataRepository.findOneByUuid(this.uuid).getDataInfo().getPopularity();
        Assert.assertThat("Popularity has not been incremented by one", newPopularity, equalTo(popularity + 1));

        popularity = newPopularity;

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/xml").param("increasePopularity", "false")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "xml"))))
            .andExpect(content().string(containsString(this.uuid)))
            .andExpect(xpath("/MD_Metadata/fileIdentifier/CharacterString").string(this.uuid));

        // Seem some issue with the transaction in the tests, requires to use explicitly the entity manager.
        // In the application looks working fine with the @Transactional annotation in MetadataRepository.incrementPopularity
        _entityManager.flush();
        _entityManager.clear();

        newPopularity = metadataRepository.findOneByUuid(this.uuid).getDataInfo().getPopularity();
        Assert.assertThat("Popularity has changed", newPopularity, equalTo(popularity));
    }

    @Test
    public void getNonAllowedRecordAsXml() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/json")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.code").value(equalTo("forbidden")))
            .andExpect(jsonPath("$.message").value(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/xml")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string(equalTo("forbidden")))
            .andExpect(xpath("/apiError/message").string(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));
    }

    @Test
    public void getNonExistentRecordAsXml() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();
        String nonExistentUuid = UUID.randomUUID().toString();

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid + "/formatters/json")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.code").value(equalTo("resource_not_found")));

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid + "/formatters/xml")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string("resource_not_found"));
    }

    @Test
    public void getRecordAsZip() throws Exception {

        final String zipMagicNumber = "PK\u0003\u0004";

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept("application/zip"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MEF_V2_ACCEPT_TYPE))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "zip"))))
            .andExpect(content().string(startsWith(zipMagicNumber)));

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept(MEF_V1_ACCEPT_TYPE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MEF_V1_ACCEPT_TYPE))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "zip"))))
            .andExpect(content().string(startsWith(zipMagicNumber)));

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept(MEF_V1_ACCEPT_TYPE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MEF_V1_ACCEPT_TYPE))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                equalTo(String.format("inline; filename=\"%s.%s\"", this.uuid, "zip"))))
            .andExpect(content().string(startsWith(zipMagicNumber)));

    }

    @Test
    public void getNonAllowedRecordAsZip() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept("application/zip"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept(MEF_V1_ACCEPT_TYPE))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept(MEF_V2_ACCEPT_TYPE))
            .andExpect(status().isForbidden());
    }

    @Test
    public void getNonExistentRecordAsZip() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();
        String nonExistentUuid = UUID.randomUUID().toString();

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept("application/zip"))
            .andExpect(status().isNotFound())
            .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE))
            .andExpect(content().string(isEmptyOrNullString()));

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept(MEF_V1_ACCEPT_TYPE))
            .andExpect(status().isNotFound())
            .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE))
            .andExpect(content().string(isEmptyOrNullString()));

        mockMvc.perform(get("/srv/api/records/" + nonExistentUuid + "/formatters/zip")
            .session(mockHttpSession)
            .accept(MEF_V2_ACCEPT_TYPE))
            .andExpect(status().isNotFound())
            .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE))
            .andExpect(content().string(isEmptyOrNullString()));
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
    public void getRelatedNonAllowed() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/related")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.code").value(equalTo("forbidden")))
            .andExpect(jsonPath("$.message").value(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/related")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/apiError/code").string(equalTo("forbidden")))
            .andExpect(xpath("/apiError/message").string(equalTo(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)));
    }

    @Test
    public void getRelated() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        addThumbnails(this.context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("/org/fao/geonet/api/records/samples/mef2-related.zip");
        importMetadata.invoke();
        final String MAIN_UUID = "655bb8a1-0324-470f-8dff-bb64e849291c";
        final String DATASET_UUID = "842f9143-fd7d-452c-96b4-425ca1281642";


        mockMvc.perform(get("/srv/api/records/" + MAIN_UUID + "/related")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));

        mockMvc.perform(get("/srv/api/records/" + MAIN_UUID + "/related")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/related/onlines").exists());

        mockMvc.perform(get("/srv/api/records/" + this.uuid + "/related")
            .param("type", RelatedItemType.thumbnails.toString())
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            //.andExpect(jsonPath("$.children").isNotEmpty())
            .andExpect(jsonPath("$." + RelatedItemType.thumbnails, notNullValue()));

        // Request each type
        for (RelatedItemType type : RelatedItemType.values()) {
            if (type == RelatedItemType.hassources ||
                type == RelatedItemType.related ||
                type == RelatedItemType.hasfeaturecats ||
                type == RelatedItemType.brothersAndSisters ||
                type == RelatedItemType.thumbnails) {
                // TODO modify mef2-related.zip test metadata to contain a valid hassources value
                continue;
            }
            String uuidToTest = MAIN_UUID;
            if (type == RelatedItemType.datasets) {
                uuidToTest = DATASET_UUID;
            }
            mockMvc.perform(get("/srv/api/records/" + uuidToTest + "/related")
                .param("type", type.toString())
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$." + type).isNotEmpty())
                .andExpect(jsonPath("$").value(hasKey(type.toString())));

            mockMvc.perform(get("/srv/api/records/" + uuidToTest + "/related")
                .param("type", type.toString())
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/related/" + type.toString() + "/item").exists());
        }

        // Check start and row parameters


        mockMvc.perform(get("/srv/api/records/" + MAIN_UUID + "/related")
            .param("type", RelatedItemType.children.toString())
            .param("start", "2")
            .param("rows", "1")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$." + RelatedItemType.children).isArray())
            .andExpect(jsonPath("$." + RelatedItemType.children, hasSize(1)));

        mockMvc.perform(get("/srv/api/records/" + MAIN_UUID + "/related")
            .param("type", RelatedItemType.children.toString())
            .param("start", "2")
            .param("rows", "1")
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_XML))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(xpath("/related/" + RelatedItemType.children + "/item").exists())
            .andExpect(xpath("/related/" + RelatedItemType.children + "/item").nodeCount(1));

    }

    private void addThumbnails(ServiceContext context) throws Exception {
        Path mdPublicDataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, id);
        Path mdPrivateDataDir = Lib.resource.getDir(context, Params.Access.PRIVATE, id);
        final Path smallImage = mdPublicDataDir.resolve("small.gif");
        final Path largeImage = mdPublicDataDir.resolve("large.gif");
        createImage("gif", smallImage);
        createImage("gif", largeImage);

        final Path privateImage = mdPrivateDataDir.resolve("privateFile.gif");
        createImage("gif", privateImage);

        dataManager.setThumbnail(context, Integer.toString(this.id), true,
            smallImage.toAbsolutePath().normalize().toString(), false);
        dataManager.setThumbnail(context, Integer.toString(this.id), false,
            largeImage.toAbsolutePath().normalize().toString(), false);
    }

    private String createImage(String format, Path outFile) throws IOException {

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = image.createGraphics();
        g2d.drawRect(1, 1, 5, 5);
        g2d.dispose();
        Files.createDirectories(outFile.getParent());
        Files.createFile(outFile);
        try (OutputStream out = Files.newOutputStream(outFile)) {
            final boolean writerWasFound = ImageIO.write(image, format, out);
            assertTrue(writerWasFound);
        }

        return outFile.toAbsolutePath().normalize().toString();
    }
}
