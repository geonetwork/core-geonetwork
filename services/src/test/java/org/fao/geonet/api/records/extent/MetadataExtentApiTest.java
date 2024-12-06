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

package org.fao.geonet.api.records.extent;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(inheritLocations = true, locations = "classpath:extents-test-context.xml")
public class MetadataExtentApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private WebApplicationContext wac;

    private ServiceContext context;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        context = createServiceContext();
    }

    @Test
    public void getAllRecordExtentAsJson() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestData();

        mockMvc.perform(get(String.format("/srv/api/records/%s/extents.json", uuid))
            .session(mockHttpSession)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].href", is(String.format("http://localhost:8080/srv/api/records/%s/extents.png", uuid))))
            .andExpect(jsonPath("$[0].type", is("ALL")))
            .andExpect(jsonPath("$[1].href", is(String.format("http://localhost:8080/srv/api/records/%s/extents/1.png", uuid))))
            .andExpect(jsonPath("$[1].type", is("EX_BoundingPolygon")))
            .andExpect(jsonPath("$[1].description", is("")))
            .andExpect(jsonPath("$[2].type", is("EX_GeographicBoundingBox")));
    }

    @Test
    public void getOneRecordExtentAsImage() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestData();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("f4a5b9c2c6b49db0f2f5bdbefd3736aa", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    @Test
    public void lastModifiedNotModified() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestData();

        mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .header("If-Modified-Since", "Wed, 21 Oct 2015 07:29:00 UTC")
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().isNotModified());
    }

    @Test
    public void lastModifiedModified() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestData();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .header("If-Modified-Since", "Wed, 21 Oct 2015 07:27:00 UTC")
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("f4a5b9c2c6b49db0f2f5bdbefd3736aa", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    @Test
    public void aggregatedWithTwoExtent() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestDataTwoExtent();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("e8971ac1840c77b7bdc3cb026e921455", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    @Test
    public void twoExtentFirstOneWithBothBoundingBoxAndPolygon() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestDataTwoExtent();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-overview");

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/1.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-1");

        assertEquals("c4818d1c164fdcbcb66ac581780423c9", DigestUtils.md5DigestAsHex(reponseBuffer));

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/2.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-2");

        assertEquals("87416d6291ec1d19d0635d3bf17f10b4", DigestUtils.md5DigestAsHex(reponseBuffer));

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/3.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-3");

        assertEquals("323634b78d6bc2cba92912d78401e954", DigestUtils.md5DigestAsHex(reponseBuffer));
    }


    @Test
    public void threeExtentThirdOne() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestDataThreeExtent();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-overview");

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/4.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("827f96a4c37ef13f0dc2c33b92196afe", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    @Test
    public void threeExtentThirdOne115_3() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestDataIso191153ThreeExtent();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-overview");

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/3.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("64494e094033417a86dfd66304379d2c", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    @Test
    public void threeExtentThirdOneIsABoundingBox() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestDataThreeExtent();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/3.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertEquals("323634b78d6bc2cba92912d78401e954", DigestUtils.md5DigestAsHex(reponseBuffer));
    }

    private String createTestData() throws Exception {
        return createMdFromXmlRessources(getSampleMetadataXml());
    }

    private String createTestDataTwoExtent() throws Exception {
        URL resource = MetadataExtentApiTest.class.getResource("valid-metadata.iso19139_with_two_extent.xml");
        Element sampleMetadataXml = Xml.loadStream(resource.openStream());

        return createMdFromXmlRessources(sampleMetadataXml);
    }

    private String createTestDataThreeExtent() throws Exception {
        URL resource = MetadataExtentApiTest.class.getResource("valid-metadata.iso19139_with_three_extent.xml");
        Element sampleMetadataXml = Xml.loadStream(resource.openStream());

        return createMdFromXmlRessources(sampleMetadataXml);
    }

    private String createTestDataIso191153ThreeExtent() throws Exception {
        URL resource = MetadataExtentApiTest.class.getResource("metadata.iso19115-3_with_three_extent.xml");
        Element sampleMetadataXml = Xml.loadStream(resource.openStream());

        return createMdFromXmlRessources(sampleMetadataXml);
    }

    private String createMdFromXmlRessources(Element sampleMetadataXml) throws Exception {
        loginAsAdmin(context);
        String uuid = UUID.randomUUID().toString();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2015, Calendar.OCTOBER, 21, 07, 28, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        String source = sourceRepository.findAll().get(0).getUuid();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        Metadata metadata = (Metadata) new Metadata()
            .setDataAndFixCR(sampleMetadataXml)
            .setUuid(uuid);
        metadata.getDataInfo()
            .setRoot(sampleMetadataXml.getQualifiedName())
            .setSchemaId(schema)
            .setType(MetadataType.METADATA)
            .setPopularity(1000)
            .setChangeDate(new ISODate(calendar.getTimeInMillis()));
        metadata.getSourceInfo()
            .setOwner(1)
            .setSourceId(source);
        metadata.getHarvestInfo()
            .setHarvested(false);

        dataManager.insertMetadata(context, metadata, sampleMetadataXml, IndexingMode.full, false, UpdateDatestamp.NO,
            false, DirectIndexSubmitter.INSTANCE).getId();

        return uuid;
    }

    public static void saveImageToDiskIfConfiguredToDoSo(byte[] reponseBuffer, String methodName) throws IOException {
        boolean SAVE_IMAGE_TO_DISK = false;
        if (!SAVE_IMAGE_TO_DISK) {
            return;
        }
        BufferedImage imag= ImageIO.read(new ByteArrayInputStream(reponseBuffer));
        ImageIO.write(imag, "png", new File("/tmp", String.format("%s.png", methodName)));
    }
}
