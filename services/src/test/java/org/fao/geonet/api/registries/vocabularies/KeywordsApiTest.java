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

package org.fao.geonet.api.registries.vocabularies;

import com.sun.net.httpserver.HttpServer;
import org.fao.geonet.api.exception.WebApplicationException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.fao.geonet.csw.common.Csw.NAMESPACE_DC;
import static org.fao.geonet.csw.common.Csw.NAMESPACE_DCT;
import static org.fao.geonet.kernel.rdf.Selectors.RDF_NAMESPACE;
import static org.fao.geonet.kernel.rdf.Selectors.SKOS_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class KeywordsApiTest extends AbstractServiceIntegrationTest {

    public static final int USER_ID = 42;

    @Autowired
    private SpringLocalServiceInvoker invoker;

    @Autowired
    private ThesaurusManager thesaurusMan;

    /**
     * Thesauri are registered in {@link ThesaurusManager}'s in-memory map, which lives for the
     * whole Spring test context (not per-test), so a thesaurus uploaded by a previous run of a
     * test is still registered on the next run and {@code addThesaurus} rejects it as a
     * duplicate. Remove any leftover registration so the test is idempotent.
     */
    private void removeThesaurusIfExists(String thesaurusKey) throws Exception {
        Thesaurus thesaurusObject = thesaurusMan.getThesaurusByName(thesaurusKey);
        if (thesaurusObject != null) {
            Path file = thesaurusObject.getFile();
            thesaurusMan.remove(thesaurusKey);
            if (file != null && Files.exists(file)) {
                IO.deleteFile(file, false, Geonet.THESAURUS);
            }
        }
    }

    @Autowired
    private WebApplicationContext wac;

    @Test
    public void testConvertCsvToSkos() throws Exception {
        createServiceContext();
        User user = new User().setId(USER_ID);
        HttpSession session = loginAs(user);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(session.getServletContext());
        request.setRequestURI("/srv/api/registries/vocabularies/import/csv");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "taxref.csv",
            null,
            getClass().getClassLoader().getResourceAsStream("taxref.csv"));
        request.addFile(file);
        request.setSession(session);
        request.setParameter("thesaurusNs", "https://registry.org/Taxref#");
        request.setParameter("thesaurusTitle", "Tax ref");
        request.setParameter("conceptRelatedIdColumn", "CITATION_FK");
        request.setParameter("importAsThesaurus", "false");


        MockHttpServletResponse response = new MockHttpServletResponse();
        invoker.invoke(request, response);
        assertEquals(200, response.getStatus());

        Element thesaurus = Xml.loadString(response.getContentAsString(), false);
        Element scheme = (Element) thesaurus.getChildren("ConceptScheme", SKOS_NAMESPACE).get(0);
        assertEquals(
            "https://registry.org/Taxref#", scheme.getAttributeValue("about", RDF_NAMESPACE));
        assertEquals(
            "Tax ref", scheme.getChildText("title", NAMESPACE_DC));
        assertEquals(
            1, scheme.getChildren("hasTopConcept", SKOS_NAMESPACE).size());

        List concepts = thesaurus.getChildren("Concept", SKOS_NAMESPACE);
        assertEquals(3, concepts.size());
        Element firstConcept = (Element) concepts.get(0);
        assertEquals("en", firstConcept.getChild("prefLabel", SKOS_NAMESPACE).getAttributeValue("lang", Geonet.Namespaces.XML));
        assertEquals("Nectamia",
            firstConcept.getChildText("prefLabel", SKOS_NAMESPACE));

        List broaders = firstConcept.getChildren("broader", SKOS_NAMESPACE);
        assertEquals(2, broaders.size());
        assertEquals("https://registry.org/Taxref#11746",
            ((Element) broaders.get(0)).getAttributeValue("resource", RDF_NAMESPACE));
    }


    @Test
    public void testConvertCsvToSkosMultilingualWithDescription() throws Exception {
        createServiceContext();
        User user = new User().setId(USER_ID);
        HttpSession session = loginAs(user);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(session.getServletContext());
        request.setRequestURI("/srv/api/registries/vocabularies/import/csv");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "taxref.csv",
            null,
            getClass().getClassLoader().getResourceAsStream("taxref_multilingual.csv"));
        request.addFile(file);
        request.setSession(session);
        request.setParameter("thesaurusNs", "https://registry.org/Taxref#");
        request.setParameter("thesaurusTitle", "Tax ref");
        request.setParameter("conceptDescriptionColumn", "description");
        request.setParameter("languages", "en", "fr", "it");
        request.setParameter("importAsThesaurus", "false");


        MockHttpServletResponse response = new MockHttpServletResponse();
        invoker.invoke(request, response);
        assertEquals(200, response.getStatus());


        Element thesaurus = Xml.loadString(response.getContentAsString(), false);

        Element scheme = (Element) thesaurus.getChildren("ConceptScheme", SKOS_NAMESPACE).get(0);
        assertEquals(
            "https://registry.org/Taxref#", scheme.getAttributeValue("about", RDF_NAMESPACE));
        assertEquals(
            "Tax ref", scheme.getChildText("title", NAMESPACE_DC));

        List concepts = thesaurus.getChildren("Concept", SKOS_NAMESPACE);
        assertEquals(1, concepts.size());
        Element firstConcept = (Element) concepts.get(0);

        List<Element> labels = firstConcept.getChildren("prefLabel", SKOS_NAMESPACE);
        assertEquals("en", ((Element) labels.get(0)).getAttributeValue("lang", Geonet.Namespaces.XML));
        assertEquals("Nectamia", ((Element) labels.get(0)).getText());
        assertEquals("fr", ((Element) labels.get(1)).getAttributeValue("lang", Geonet.Namespaces.XML));
        assertEquals("Nectamia FR", ((Element) labels.get(1)).getText());
        assertEquals("it", ((Element) labels.get(2)).getAttributeValue("lang", Geonet.Namespaces.XML));
        assertEquals("Nectamia", ((Element) labels.get(2)).getText());

        List<Element> definitions = firstConcept.getChildren("scopeNote", SKOS_NAMESPACE);
        assertEquals("en", ((Element) definitions.get(0)).getAttributeValue("lang", Geonet.Namespaces.XML));
        assertEquals("Description", ((Element) definitions.get(0)).getText());
        assertEquals("fr", ((Element) definitions.get(1)).getAttributeValue("lang", Geonet.Namespaces.XML));
        assertEquals("", ((Element) definitions.get(1)).getText());
        assertEquals("it", ((Element) definitions.get(2)).getAttributeValue("lang", Geonet.Namespaces.XML));
        assertEquals("Description", ((Element) definitions.get(2)).getText());

    }


    @Test
    public void testConvertCsvToSkosDefaultTitleAndNamespace() throws Exception {
        createServiceContext();
        User user = new User().setId(USER_ID);
        HttpSession session = loginAs(user);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(session.getServletContext());
        request.setRequestURI("/srv/api/registries/vocabularies/import/csv");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "taxref.csv",
            null,
            getClass().getClassLoader().getResourceAsStream("taxref.csv"));
        request.addFile(file);
        request.setSession(session);
        request.setParameter("importAsThesaurus", "false");


        MockHttpServletResponse response = new MockHttpServletResponse();
        invoker.invoke(request, response);
        assertEquals(200, response.getStatus());

        Element thesaurus = Xml.loadString(response.getContentAsString(), false);

        Element scheme = (Element) thesaurus.getChildren("ConceptScheme", SKOS_NAMESPACE).get(0);
        assertEquals(
            "taxref.csv#", scheme.getAttributeValue("about", RDF_NAMESPACE));
        assertEquals(
            "taxref.csv", scheme.getChildText("title", NAMESPACE_DC));
    }


    @Test
    public void testImportOntologyToSkos() throws Exception {
        String thesaurusKey = "external.theme.mobility-theme";
        // Guard against a leftover registration from a previous run of this test.
        removeThesaurusIfExists(thesaurusKey);

        try {
            createServiceContext();
            User user = new User().setId(USER_ID);
            HttpSession session = loginAs(user);
            MockHttpSession mockHttpSession = loginAsAdmin();

            MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(session.getServletContext());
            request.setRequestURI("/srv/api/registries/vocabularies");
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "mobility-theme.owl",
                null,
                getClass().getClassLoader().getResourceAsStream("mobility-theme.owl"));
            request.addFile(file);
            request.setSession(session);
            request.setParameter("type", "external");
            request.setParameter("dir", "theme");
            MockHttpServletResponse response = new MockHttpServletResponse();
            invoker.invoke(request, response);
            assertEquals(200, response.getStatus());


            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
            MvcResult result = mockMvc.perform(get("/srv/api/registries/vocabularies/" + thesaurusKey)
                    .accept("application/xml")
                    .session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

            Element thesaurus = Xml.loadString(result.getResponse().getContentAsString(), false);
            Element scheme = (Element) thesaurus.getChildren("ConceptScheme", SKOS_NAMESPACE).get(0);
            assertEquals(
                "https://w3id.org/mobilitydcat-ap/mobility-theme", scheme.getAttributeValue("about", RDF_NAMESPACE));
            assertEquals(
                "Mobility Theme", scheme.getChildText("title", NAMESPACE_DCT));

            List concepts = thesaurus.getChildren("Concept", SKOS_NAMESPACE);
            assertEquals(121, concepts.size());
        } finally {
            // Leave no state behind for subsequent runs of this test.
            removeThesaurusIfExists(thesaurusKey);
        }
    }

    /**
     * Uploading a thesaurus file (POST) that is well-formed XML, but not valid RDF/XML,
     * must be rejected with a clear error instead of being silently accepted.
     */
    @Test
    public void testUploadThesaurusFileRejectsInvalidRdfXml() throws Exception {
        createServiceContext();
        User user = new User().setId(USER_ID);
        HttpSession session = loginAs(user);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(session.getServletContext());
        request.setRequestURI("/srv/api/registries/vocabularies");
        // Well-formed XML, but not valid RDF/XML: a node element in RDF/XML may only contain
        // property elements, not direct text content (verified against Apache Jena's RDF/XML
        // parser, which throws a RiotException for this exact shape).
        String notRdfXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<catalog>Not an RDF/XML document at all, just text.</catalog>";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "not-rdf.xml",
            "application/xml",
            notRdfXml.getBytes(StandardCharsets.UTF_8));
        request.addFile(file);
        request.setSession(session);
        request.setParameter("type", "external");
        request.setParameter("dir", "theme");

        MockHttpServletResponse response = new MockHttpServletResponse();
        try {
            invoker.invoke(request, response);
            fail("Expected upload of a non RDF/XML file to be rejected");
        } catch (WebApplicationException e) {
            assertTrue("Error message should mention the file is not a valid RDF/XML file, was: " + e.getMessage(),
                e.getMessage().contains("not a valid RDF/XML file"));
        }
    }

    /**
     * Uploading a thesaurus from a URL (PUT, urlUpload case) that returns well-formed XML, but not
     * valid RDF/XML (e.g. an unrelated document or an error page), must be rejected with a clear
     * error instead of being silently accepted.
     */
    @Test
    public void testUploadThesaurusFromUrlRejectsInvalidRdfXml() throws Exception {
        createServiceContext();
        User user = new User().setId(USER_ID);
        HttpSession session = loginAs(user);

        // Well-formed XML, but not valid RDF/XML: a node element in RDF/XML may only contain
        // property elements, not direct text content (verified against Apache Jena's RDF/XML
        // parser, which throws a RiotException for this exact shape).
        String notRdfXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<catalog>Not an RDF/XML document at all, just text.</catalog>";
        byte[] notRdfXmlBytes = notRdfXml.getBytes(StandardCharsets.UTF_8);

        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.createContext("/not-rdf.rdf", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, notRdfXmlBytes.length);
            exchange.getResponseBody().write(notRdfXmlBytes);
            exchange.close();
        });
        httpServer.start();
        try {
            String url = "http://localhost:" + httpServer.getAddress().getPort() + "/not-rdf.rdf";

            MockHttpServletRequest request = new MockHttpServletRequest(session.getServletContext());
            request.setMethod("PUT");
            request.setRequestURI("/srv/api/registries/vocabularies");
            request.setSession(session);
            request.setParameter("url", url);
            request.setParameter("type", "external");
            request.setParameter("dir", "theme");

            MockHttpServletResponse response = new MockHttpServletResponse();
            try {
                invoker.invoke(request, response);
                fail("Expected upload of a non RDF/XML URL to be rejected");
            } catch (WebApplicationException e) {
                assertTrue("Error message should mention the file is not a valid RDF/XML file, was: " + e.getMessage(),
                    e.getMessage().contains("not a valid RDF/XML file"));
            }
        } finally {
            httpServer.stop(0);
        }
    }
}
