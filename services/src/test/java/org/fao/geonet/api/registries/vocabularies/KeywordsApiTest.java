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

import java.nio.file.Files;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.util.List;

import static org.fao.geonet.csw.common.Csw.NAMESPACE_DC;
import static org.fao.geonet.csw.common.Csw.NAMESPACE_DCT;
import static org.fao.geonet.kernel.rdf.Selectors.RDF_NAMESPACE;
import static org.fao.geonet.kernel.rdf.Selectors.SKOS_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * export CATALOG=http://localhost:8080/geonetwork
 * export CATALOGUSER=admin
 * export CATALOGPASS=admin
 * rm -f /tmp/cookie;
 * curl -s -c /tmp/cookie -o /dev/null -H "accept: application/json" "$CATALOG/srv/api/me";
 * export TOKEN=`grep XSRF-TOKEN /tmp/cookie | cut -f 7`;
 * curl -H "accept: application/json" -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie "$CATALOG/srv/api/me"
 * <p>
 * curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie -F 'file=@reftax_SIH_20201216.csv' "http://localhost:8080/geonetwork/srv/api/registries/vocabularies/import/csv?importAsThesaurus=true&thesaurusTitle=Taxons&thesaurusNs=https://registry.ifremer.fr/taxref/&languages=en&languages=fr&conceptIdColumn=ID&conceptLabelColumn=NAME&conceptBroaderColumn=PARENT_TAXON_NAME_FK&encoding=ISO-8859-1"
 */
public class KeywordsApiTest extends AbstractServiceIntegrationTest {

    public static final int USER_ID = 42;

    @Autowired
    private SpringLocalServiceInvoker invoker;

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

    // see finally block in #testImportOntologyToSkos
    // this is required to locate the file to be deleted.
    @Autowired
    GeonetworkDataDirectory geonetworkDataDirectory;


    @Test
    public void testImportOntologyToSkos() throws Exception {
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
            MvcResult result = mockMvc.perform(get("/srv/api/registries/vocabularies/external.theme.mobility-theme")
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
        }
        finally {
            //clean up
            // this test case uploads a thesaurus.
            // if you don't delete it, then, on the next run, it will be picked up and you'll get an error because
            // the thesaurus already exists.  This will clean up and there will not be a problem on the next run.
            // This is typically only an issue if you are running the test locally - on the build server it gets a
            // new, clean, filesystem so there isn't a problem.
            var uploadedThesauras = geonetworkDataDirectory.resolveWebResource(
                "WEB-INF/data/config/codelist/external/thesauri/theme/mobility-theme.rdf");
            Files.deleteIfExists(uploadedThesauras);
        }
    }
}
