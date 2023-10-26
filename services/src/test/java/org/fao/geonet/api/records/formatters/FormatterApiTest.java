/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.api.records.formatters;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FormatterApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SourceRepository sourceRepository;

    private Map<String, String> testDataUuidBySchema = new HashMap<>();

    private ServiceContext context;

    public static Collection<String[]> data() throws Exception {
        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[]{"citation", "?format=?", "iso19139", "formats.txt"});
        data.add(new String[]{"citation", "?format=ris", "iso19139", "ris.txt"});
        data.add(new String[]{"citation", "?format=bibtex", "iso19139", "bibtex.txt"});
        data.add(new String[]{"citation", "?format=text", "iso19139", "text.txt"});
        data.add(new String[]{"citation", "?format=html", "iso19139", "html.html"});
        data.add(new String[]{"citation", "?format=?", "iso19115-3.2018", "formats.txt"});
        data.add(new String[]{"citation", "?format=ris", "iso19115-3.2018", "ris.txt"});
        data.add(new String[]{"citation", "?format=bibtex", "iso19115-3.2018", "bibtex.txt"});
        data.add(new String[]{"citation", "?format=text", "iso19115-3.2018", "text.txt"});
        data.add(new String[]{"citation", "?format=html", "iso19115-3.2018", "html.html"});
        data.add(new String[]{"citation", "?format=text&authorRoles=processor&publisherRoles=owner,custodian", "iso19115-3.2018", "text-custom-role.txt"});
        return data;
    }

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    private void createTestData() throws Exception {
        loginAsAdmin(context);
        loadFile(getSampleISO19139MetadataXml());
        loadFile(getSampleISO19115MetadataXml());
    }

    private void loadFile(Element sampleMetadataXml) throws Exception {
        String uuid = UUID.randomUUID().toString();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        Xml.selectElement(sampleMetadataXml,
                "iso19139".equals(schema)
                    ? "gmd:fileIdentifier/gco:CharacterString"
                    : "mdb:metadataIdentifier/*/mcc:code/*",
                "iso19139".equals(schema)
                    ? ISO19139SchemaPlugin.allNamespaces.asList()
                    : ISO19115_3_2018SchemaPlugin.allNamespaces.asList())
            .setText(uuid);

        String source = sourceRepository.findAll().get(0).getUuid();
        final Metadata metadata = new Metadata();
        metadata
            .setDataAndFixCR(sampleMetadataXml)
            .setUuid(uuid);
        metadata.getDataInfo()
            .setRoot(sampleMetadataXml.getQualifiedName())
            .setSchemaId(schema)
            .setType(MetadataType.METADATA)
            .setPopularity(1000);
        metadata.getSourceInfo()
            .setOwner(1)
            .setSourceId(source);
        metadata.getHarvestInfo()
            .setHarvested(false);

        dataManager.insertMetadata(context, metadata, sampleMetadataXml, IndexingMode.none, false, UpdateDatestamp.NO,
            false, false);

        testDataUuidBySchema.put(schema, uuid);
    }

    @Test
    public void checkFormatter() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        for (String[] testParameter : data()) {
            String formatter = testParameter[0];
            String urlParams = testParameter[1];
            String schema = testParameter[2];
            String checkfile = testParameter[3];
            String url = "/srv/api/records/"
                + testDataUuidBySchema.get(schema)
                + "/formatters/" + formatter + urlParams;
            try {
                MvcResult result = mockMvc.perform(get(url)
                        .session(mockHttpSession)
                        .accept(MediaType.ALL_VALUE))
                    .andExpect(status().isOk())
                    .andReturn();

                assertEquals(
                    url,
                    StreamUtils.copyToString(
                        FormatterApiTest.class.getResourceAsStream(
                            String.format("%s-%s-%s",
                                schema, formatter, checkfile)
                        ),
                        StandardCharsets.UTF_8)
                        .trim()
                        .replace("{uuid}", testDataUuidBySchema.get(schema)),
                    result.getResponse().getContentAsString()
                        .replaceAll("\\r\\n?", "\n")
                );
            } catch (Exception e) {
                fail(url);
            }
        }
    }
}
