package org.fao.geonet.services.metadata.inspire;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.TransformerFactoryFactory;
import jeeves.utils.Xml;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.GeonetworkDataDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.EditLibTest;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;

import static org.fao.geonet.Assert.getWebappDir;
import static org.fao.geonet.kernel.search.spatial.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SaveTest {
    private static final List<Namespace> NS = Arrays.asList(Geonet.Namespaces.GCO, Geonet.Namespaces.GMD,
            Namespace.getNamespace("che", "http://www.geocat.ch/2008/che"));

    public static TemporaryFolder _schemaCatalogContainer = new TemporaryFolder();
    private static SchemaManager _schemaManager;

    @BeforeClass
    public static void initSchemaManager() throws Exception {
        _schemaCatalogContainer.create();

        final ServiceConfig serviceConfig = new ServiceConfig(Lists.<Element>newArrayList());
        final String webappDir = getWebappDir(EditLibTest.class);
        new GeonetworkDataDirectory("geonetwork", webappDir, serviceConfig, null);

        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        final String resourcePath = Resources.locateResourcesDir((ServletContext) null);
        final String basePath = webappDir;
        final String schemaPluginsCat = _schemaCatalogContainer.getRoot() + "/" + Geonet.File.SCHEMA_PLUGINS_CATALOG;
        final String schemaPluginsDir = webappDir + "/WEB-INF/data/config/schema_plugins";

        FileUtils.copyFile(new File(webappDir, "WEB-INF/" + Geonet.File.SCHEMA_PLUGINS_CATALOG), new File(schemaPluginsCat));

        SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCat);

        SchemaManager manager = SchemaManager.getInstance(basePath, resourcePath, schemaPluginsCat, schemaPluginsDir, "eng", "iso19139");
        _schemaManager = manager;
    }

    @AfterClass
    public static void cleanUpSchemaCatalogFile() {
        _schemaCatalogContainer.delete();
        _schemaManager = null;
    }

    @Test
    public void testSave() throws Exception {
        final Element testMetadata = Xml.loadString("che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" " +
                                                    "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211" +
                                                    ".org/2005/gco\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" " +
                                                    "xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\"/>", false);
        Save service = new Save() {
            @Override
            protected Element getMetadata(ServiceContext context, String id, AjaxEditUtils ajaxEditUtils) throws Exception {
                return testMetadata;
            }

        };

        String json = loadTestJson();

        GeonetContext geonetContext = Mockito.mock(GeonetContext.class);
        Mockito.when(geonetContext.getSchemamanager()).thenReturn(this._schemaManager);

        ServiceContext context = Mockito.mock(ServiceContext.class);
        Mockito.when(context.getHandlerContext(Mockito.anyString())).thenReturn(geonetContext);

        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);

        assertEquals(Xml.selectString(testMetadata, "gmd:language/gco:CharacterString", NS), "eng");
        assertEquals(Xml.selectString(testMetadata, "gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue", NS), "eng");
        assertEquals(Xml.selectString(testMetadata, "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue", NS), "dataset");
        final List<?> contact = Xml.selectNodes(testMetadata, "gmd:contact", NS);
        assertEquals(2, contact.size());
        assertContact((Element) contact.get(0), "Florent", "Gravin", "florent.gravin@camptocamp.com", "owner", false,
                read("eng", "camptocamp SA"));
        assertContact((Element) contact.get(1), "Jesse", "Eichar", "jesse.eichar@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));
        assertEquals(Xml.selectString(testMetadata,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = 'ger']/@codeListValue", NS), "ger");
        assertEquals(Xml.selectString(testMetadata,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = 'eng']/@codeListValue", NS), "eng");
        final Element identification = Xml.selectElement(testMetadata, "gmd:identificationInfo/che:CHE_MD_DataIdentification", NS);
        assertCorrectTranslation(identification, "gmd:citation/gmd:CI_Citation/gmd:title", "eng", "Title");
        assertCorrectTranslation(identification, "gmd:citation/gmd:CI_Citation/gmd:title", "fre", "Titre");

        assertEquals(Xml.selectString(testMetadata,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date", NS), "2008-06-23");
        assertEquals(Xml.selectString(testMetadata,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue", NS), "creation");
        assertEquals(Xml.selectString(testMetadata,
                "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString", NS), "identifier");
        assertCorrectTranslation(identification, "gmd:abstract", "fre", "Abstract");


        final List<?> pointOfContact = Xml.selectNodes(identification, "gmd:pointOfContact", NS);
        assertEquals(2, pointOfContact.size());
        assertContact((Element) pointOfContact.get(0), "Jesse", "Eichar", "jesse.eichar@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));
        assertContact((Element) pointOfContact.get(1), "Florent", "Gravin", "florent.gravin@camptocamp.com", "owner", false,
                read("eng", "camptocamp SA"));


    }

    @Test
    public void testDateTime() throws Exception {
        fail("to implement");

    }

    private void assertCorrectTranslation(Element metadata, String xpath, String lang, String expectedValue) throws JDOMException {
        assertEquals(Xml.selectString(metadata,
                xpath + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='" + lang + "']", NS), expectedValue);
    }

    private void assertContact(Element contact, String name, String surname, String email, String role,
                               boolean validated, Pair<String, String>... orgs) throws JDOMException {

        assertEquals(Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/che:individualFirstName", NS), name);
        assertEquals(Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/che:individualLastName", NS), surname);
        assertEquals(Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/" +
                                               "gmd:electronicMailAddress/gco:CharacterString", NS), email);
        for (Pair<String, String> org : orgs) {
            assertCorrectTranslation(contact, "che:CHE_CI_ResponsibleParty/gmd:organisationName", org.one(), org.two());
        }
        assertEquals(Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/gmd:role", NS), role);
    }

    private String loadTestJson() throws URISyntaxException, IOException {
        File file = new File(SaveTest.class.getResource(SaveTest.class.getName() + ".class").toURI()).getParentFile();
        final String pathToJsonFile = "web-ui/src/main/resources/catalog/components/edit/inspire/MockFullMetadataFactory.js";
        while (new File(file, pathToJsonFile).exists()) {
            file = file.getParentFile();
        }
        return Files.toString(file, Charset.forName("UTF-8"));
    }

}