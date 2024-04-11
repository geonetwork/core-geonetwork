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
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FormatterApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    private Map<String, String> testDataUuidBySchema = new HashMap<>();
    private ServiceContext context;

    public static Collection<String[]> data() throws Exception {
        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[]{"iso19139", "citation", "?format=?", "iso19139", "formats.txt"});
        data.add(new String[]{"iso19139", "citation", "?format=ris", "iso19139", "ris.txt"});
        data.add(new String[]{"iso19139", "citation", "?format=bibtex", "iso19139", "bibtex.txt"});
        data.add(new String[]{"iso19139", "citation", "?format=text", "iso19139", "text.txt"});
        data.add(new String[]{"iso19139", "citation", "?format=html", "iso19139", "html.html"});
        data.add(new String[]{"iso19139", "citation", "?format=?", "iso19115-3.2018", "formats.txt"});
        data.add(new String[]{"iso19115-3.2018", "citation", "?format=ris", "iso19115-3.2018", "ris.txt"});
        data.add(new String[]{"iso19115-3.2018", "citation", "?format=bibtex", "iso19115-3.2018", "bibtex.txt"});
        data.add(new String[]{"iso19115-3.2018", "citation", "?format=text", "iso19115-3.2018", "text.txt"});
        data.add(new String[]{"iso19115-3.2018", "citation", "?format=html", "iso19115-3.2018", "html.html"});
        data.add(new String[]{"iso19115-3.2018", "citation", "?format=text&authorRoles=processor&publisherRoles=owner,custodian", "iso19115-3.2018", "text-custom-role.txt"});

        data.add(new String[]{"iso19115-3.2018-dcat-dataset.xml", "dcat", "", "iso19115-3.2018", "dataset-core.rdf"});
        data.add(new String[]{"iso19115-3.2018-dcat-dataset.xml", "eu-dcat-ap", "", "iso19115-3.2018", "dataset-core.rdf"});
        data.add(new String[]{"iso19115-3.2018-dcat-dataset.xml", "eu-geodcat-ap", "", "iso19115-3.2018", "dataset-core.rdf"});
        data.add(new String[]{"iso19115-3.2018-dcat-dataset.xml", "eu-dcat-ap-hvd", "", "iso19115-3.2018", "dataset-core.rdf"});
        data.add(new String[]{"iso19115-3.2018-dcat-service.xml", "dcat", "", "iso19115-3.2018", "service-core.rdf"});

        return data;
    }

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    @Test
    public void checkFormatter() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        for (String[] testParameter : data()) {
            String testFile = testParameter[0];
            String formatter = testParameter[1];
            String urlParams = testParameter[2];
            String schema = testParameter[3];
            String checkfile = testParameter[4];
            String url = "/srv/api/records/"
                    + testDataUuidBySchema.get(testFile)
                    + "/formatters/" + formatter + urlParams;
            try {
                MvcResult result = mockMvc.perform(get(url)
                                .session(mockHttpSession)
                                .accept(MediaType.ALL_VALUE))
                        .andExpect(status().isOk())
                        .andReturn();

                String expected = StreamUtils.copyToString(
                                FormatterApiTest.class.getResourceAsStream(
                                        String.format("%s-%s-%s",
                                                schema, formatter, checkfile)
                                ),
                                StandardCharsets.UTF_8)
                        .trim()
                        .replace("{uuid}", testDataUuidBySchema.get(testFile));

                String actual = result.getResponse().getContentAsString();

                boolean isRdf = checkfile.endsWith(".rdf");
                boolean isXml = checkfile.endsWith(".xml");

                if (isXml || isRdf) {
                    if (isRdf) {
                        try {
                            Model model = ModelFactory.createMemModelMaker().createDefaultModel();
                            RDFDataMgr.read(model,
                                    IOUtils.toInputStream(actual, StandardCharsets.UTF_8),
                                    Lang.RDFXML);
                        } catch (Exception rdfException) {
                            fail(String.format("%s. Checked with %s. RDF model error. %s. Checked with: %s",
                                    url, checkfile, rdfException.getMessage(), actual));
                        }
                    }


//                    FileUtils.writeStringToFile(new File("/tmp/" + String.format("%s-%s-%s",
//                            schema, formatter, checkfile)), actual, StandardCharsets.UTF_8);

                    Diff diff = DiffBuilder
                            .compare(Input.fromString(actual))
                            .withTest(Input.fromString(expected))
                            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                            .normalizeWhitespace()
                            .ignoreComments()
                            .checkForSimilar()
                            .build();
                    assertFalse(
                            String.format("%s. Checked with %s. Differences: %s", url, checkfile, diff.toString()),
                            diff.hasDifferences());

                    if (isRdf) {
                        String[] shaclValidation = {};
                        if ("eu-dcat-ap".equalsIgnoreCase(formatter)) {
//                            shaclValidation = new String[]{"shacl/dcat-ap-2.1.1-base-SHACL.ttl"};
//                            shaclValidation = new String[]{"shacl/dcat-ap-3.0.0-full.ttl"};
                            // https://github.com/ISAITB/validator-resources-dcat-ap/blob/master/resources/config.properties#L117-L128
                            //+ shapes.ttl + range.ttl + shapes_recommended.ttl + imports.ttl + deprecateduris.ttl+
                            // shaclValidation = new String[]{"shacl/eu-dcat-ap-3.0.0/shapes.ttl"};
//                        } else  if("eu-dcat-ap-hvd".equalsIgnoreCase(formatter)){
//                            shaclValidation = new String[]{"shacl/dcat-ap-hvd-2.2.0-SHACL.ttl"};
//                        } else  if("eu-geodcat-ap".equalsIgnoreCase(formatter)){
//                            shaclValidation = new String[]{"shacl/geodcat-ap-2.0.1-SHACL.ttl"};
                        }
                        for (String shaclShapes : shaclValidation) {
                            applyShaclValidation(formatter, schema, checkfile, url, shaclShapes);
                        }
                    }
                } else {
                    assertEquals(
                            url,
                            expected,
                            actual.replaceAll("\\r\\n?", "\n")
                    );
                }
            } catch (Exception e) {
                fail(String.format("Failure on %s. Error is: %s", url, e.getMessage()));
            }
        }
    }


    @Test
    @Ignore
    public void quickTestToValidateRdfModelAndShaclRules() throws IOException {
        String formatter = "eu-dcat-ap";
        String schema = "iso19115-3.2018";
        String checkfile = "dataset-core.rdf";
        String file = String.format("%s-%s-%s", schema, formatter, checkfile);
        String expected = StreamUtils.copyToString(
                FormatterApiTest.class.getResourceAsStream(file),
                StandardCharsets.UTF_8);
        try {
            Model model = ModelFactory.createMemModelMaker().createDefaultModel();
            RDFDataMgr.read(model,
                    IOUtils.toInputStream(expected, StandardCharsets.UTF_8),
                    Lang.RDFXML);
        } catch (Exception rdfException) {
            fail(String.format("%s. RDF model error. %s.",
                    file, rdfException.getMessage()));
        }
        String[] shaclValidation = new String[]{"dcat-ap-2.1.1-base-SHACL.ttl"};
//        String[] shaclValidation = new String[]{"dcat-ap-3-SHACL.ttl"};
//        String[] shaclValidation = new String[]{"dcat-ap-hvd-2.2.0-SHACL.ttl"};
//        String[] shaclValidation = new String[]{"geodcat-ap-2.0.1-SHACL.ttl"};
        for (String shaclShapes : shaclValidation) {
            applyShaclValidation(formatter, schema, checkfile, "", shaclShapes);
        }
    }

    private static void applyShaclValidation(String formatter, String schema, String checkfile, String url, String shaclShapes) {
        String SHAPES = FormatterApiTest.class.getResource(shaclShapes).getFile();
        if (SHAPES.startsWith("/")) {
            SHAPES.replaceFirst("/", "");
        }

        //Load document to validate.
        String DATA = FormatterApiTest.class.getResource(
                String.format("%s-%s-%s",
                        schema, formatter, checkfile)
        ).getFile();
        if (DATA.startsWith("/")) {
            DATA.replaceFirst("/", "");
        }
        Graph shapesGraph;
        Shapes shapes;
        try {
            shapesGraph = RDFDataMgr.loadGraph(SHAPES);
            shapes = Shapes.parse(shapesGraph);
        } catch (Exception e) {
            fail(String.format(
                    "%s. Checked with %s [%s]. SHACL graph error. Error is: %s",
                    url, checkfile, shaclShapes, e.getMessage()));
            return;
        }

        Graph dataGraph = RDFDataMgr.loadGraph(DATA);

        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        if (!report.conforms()) {
            long count = report.getEntries().stream()
                    .filter(e -> e.severity().level().getURI().equals("http://www.w3.org/ns/shacl#Violation"))
                    .count();

            ShLib.printReport(report);
            System.out.println();
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
            fail(String.format("%s. Checked with %s [%s]. Invalid DCAT-AP document. %d violations found. See report in the test console output.",
                    url, checkfile, shaclShapes, count));
        }
    }

    private void createTestData() throws Exception {
        loginAsAdmin(context);

        Set<String> testFiles = new HashSet<>();
        for (String[] testParameter : data()) {
            testFiles.add(testParameter[0]);
        }
        for (String file : testFiles) {
            if (file.equals("iso19139")) {
                loadFile("iso19139", getSampleISO19139MetadataXml());
            } else if (file.equals("iso19115-3.2018")) {
                loadFile("iso19115-3.2018", getSampleISO19115MetadataXml());
            } else {
                loadFile(file,
                        Xml.loadStream(
                                FormatterApiTest.class.getResourceAsStream(file)));
            }
        }
    }

    private void loadFile(String key, Element sampleMetadataXml) throws Exception {
        AbstractMetadata metadata = injectMetadataInDbDoNotRefreshHeader(sampleMetadataXml, context);
        testDataUuidBySchema.put(key, metadata.getUuid());
    }
}
