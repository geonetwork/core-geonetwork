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
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.geotools.image.test.ImageAssert;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Ignore;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(inheritLocations = true, locations = "classpath:extents-test-context.xml")
public class MetadataExtentApiTest extends AbstractServiceIntegrationTest {

    /**
     * Use <code>-DMetadataExtentApiTest.save.png=true</code> to save geneated images for visual reference.
     */
    private static boolean SAVE_IMAGE_TO_DISK = Boolean.getBoolean("MetadataExtentApiTest.save.png" );

    private static String REFERENCE = "./src/test/resources/org/fao/geonet/api/records/extent/";

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
        assertImage(
            REFERENCE+"getOneRecordExtentAsImage.png",
            reponseBuffer,
            550);
    }

    @Ignore
    public void lastModifiedNotModified() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        String uuid = createTestData();

        byte[] reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents.png", uuid))
            .header("If-Modified-Since", "Wed, 21 Oct 2015 07:29:00 UTC")
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertImage(
            REFERENCE+"lastModifiedNotModified.png",
            reponseBuffer,
            550);
    }

    @Ignore
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

        assertImage(
            REFERENCE+"lastModifiedModified.png",
            reponseBuffer,
            550);
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
        assertImage(
            REFERENCE+"aggregatedWithTwoExtent.png",
            reponseBuffer,
            550);
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
        assertImage(
            REFERENCE+"twoExtentFirstOneWithBothBoundingBoxAndPolygon-overview.png",
            reponseBuffer,
            550);

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/1.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-1");
        assertImage(
            REFERENCE+"twoExtentFirstOneWithBothBoundingBoxAndPolygon-1.png",
            reponseBuffer,
            550);

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/2.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-2");
        assertImage(
            REFERENCE+"twoExtentFirstOneWithBothBoundingBoxAndPolygon-2.png",
            reponseBuffer,
            550);

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/3.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();
        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName() + "-3");
        assertImage(
            REFERENCE+"twoExtentFirstOneWithBothBoundingBoxAndPolygon-3.png",
            reponseBuffer,
            550);
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
        assertImage(
            REFERENCE+"threeExtentThirdOne-overview.png",
            reponseBuffer,
            550);

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/4.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertImage(
            "./src/test/resources/org/fao/geonet/api/records/extent/threeExtentThirdOne.png",
            reponseBuffer,
            550);
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
        assertImage(
            REFERENCE+"threeExtentThirdOne115_3-overview.png",
            reponseBuffer,
            550);

        reponseBuffer = mockMvc.perform(get(String.format("/srv/api/records/%s/extents/3.png", uuid))
            .session(mockHttpSession)
            .accept(MediaType.IMAGE_PNG_VALUE))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_PNG_EXPECTED_ENCODING))
            .andReturn().getResponse().getContentAsByteArray();

        saveImageToDiskIfConfiguredToDoSo(reponseBuffer, name.getMethodName());
        assertImage(
            REFERENCE+"threeExtentThirdOne115_3.png",
            reponseBuffer,
            550);
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
        assertImage(
            REFERENCE+"threeExtentThirdOneIsABoundingBox.png",
            reponseBuffer,
            550);
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

        dataManager.insertMetadata(context, metadata, sampleMetadataXml, false, true, false, UpdateDatestamp.NO,
            false, false).getId();

        return uuid;
    }

    /**
     * Use perception comparison to check generated image against reference.
     * <p>
     * This uses {@link org.geotools.image.test.ImageAssert} controlled with:
     * <ul>
     *  <li><code>-Dorg.geotools.image.test.interactive=true</code>: to enable interactive review, this allows visual comparison and updating the saved file.</li>
     *  <li><code>-Dorg.geotools.image.test.skip=true</code>: to skip image comparison</li>
     * </ul>
     * </p>
     *
     * @param expected filepath to expected image
     * @param reponseBuffer image content
     * @param threshold visual threadshold for match, often a small percentage is acceptable for font changes
     */
    private void assertImage( String expected, byte[] reponseBuffer, int threshold ) throws IOException {
        BufferedImage imag = ImageIO.read(new ByteArrayInputStream(reponseBuffer));
        File file = new File( expected );
        ImageAssert.assertEquals(file, imag, threshold);
    }

    /**
     * Use {@link #SAVE_IMAGE_TO_DISK} to save generated image to disk for visual comparison.
     *
     * @param reponseBuffer png image
     *
     * @param methodName method being tested, used for generated filename
     * @throws IOException
     */
    private void saveImageToDiskIfConfiguredToDoSo(byte[] reponseBuffer, String methodName) throws IOException {
        if (SAVE_IMAGE_TO_DISK) {
            BufferedImage imag = ImageIO.read(new ByteArrayInputStream(reponseBuffer));

            File testResults = new File("./target/test-results");
            if( !testResults.isDirectory() ){
                testResults.mkdirs();
            }
            File file = new File(testResults, String.format("%s.png", methodName));

            ImageIO.write(imag, "png", file );
            System.out.println( "Generated image to: "+file );
        }
    }
}
