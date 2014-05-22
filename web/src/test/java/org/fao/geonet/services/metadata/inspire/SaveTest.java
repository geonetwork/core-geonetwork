package org.fao.geonet.services.metadata.inspire;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;

import static org.fao.geonet.Assert.getWebappDir;
import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.kernel.search.spatial.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SaveTest {
    private static final List<Namespace> NS = Arrays.asList(
            GCO,
            GMD,
            Geonet.Namespaces.XLINK,
            Namespace.getNamespace("che", "http://www.geocat.ch/2008/che"));

    private final Pattern USER_XLINK_ID_PATTERN = Pattern.compile(".*id=(.+?).*");
    private final Pattern USER_XLINK_ROLE_PATTERN = Pattern.compile(".*role=(.+?).*");

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

        _schemaManager = SchemaManager.getInstance(basePath, resourcePath, schemaPluginsCat, schemaPluginsDir, "eng", "iso19139");
    }

    @AfterClass
    public static void cleanUpSchemaCatalogFile() {
        _schemaCatalogContainer.delete();
        _schemaManager = null;
    }

    @Test
    public void testSave() throws Exception {
        final Element testMetadata = Xml.loadString("<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" " +
                                                    "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211" +
                                                    ".org/2005/gco\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" " +
                                                    "xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\"/>", false);


        SaveServiceTestImpl service = new SaveServiceTestImpl(testMetadata);

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
        assertEquals(Xml.selectString(testMetadata, "gmd:hierarchyLevelName/gco:CharacterString", NS), "");
        assertEquals(Xml.selectString(testMetadata, "gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue", NS), "utf8");
        assertEquals(Xml.selectString(testMetadata, "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue", NS), "dataset");
        final List<?> contact = Xml.selectNodes(testMetadata, "gmd:contact", NS);
        assertEquals(3, contact.size());
        assertContact((Element) contact.get(0), "Florent", "Gravin", "florent.gravin@camptocamp.com", "owner", false,
                read("eng", "camptocamp SA"));
        assertContact((Element) contact.get(1), "Jesse", "Eichar", "jesse.eichar@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));
        assertContact((Element) contact.get(2), "New", "User", "new.user@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));
        assertEquals(Xml.selectString(testMetadata,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = 'ger']/@codeListValue", NS), "ger");
        assertEquals(Xml.selectString(testMetadata,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = 'eng']/@codeListValue", NS), "eng");
        final Element identification = Xml.selectElement(testMetadata, "gmd:identificationInfo/che:CHE_MD_DataIdentification", NS);
        assertNotNull(identification);
        assertEquals("gmd:MD_DataIdentification", identification.getAttributeValue("isoType", GCO));
        assertCorrectTranslation(identification, "gmd:citation/gmd:CI_Citation/gmd:title", read("eng", "Title"), read("fre", "Titre"));

        assertEquals(Xml.selectString(identification,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date", NS), "2008-06-23");
        assertEquals(Xml.selectString(identification,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue", NS), "creation");

        assertEquals(Xml.selectString(identification,
                "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString", NS), "Citation Identifier");

        assertCorrectTranslation(identification, "gmd:abstract", read("eng", "Abstract EN"), read("fre", "Abstract FR"));


        final List<?> pointOfContact = Xml.selectNodes(identification, "gmd:pointOfContact", NS);
        assertEquals(2, pointOfContact.size());
        assertContact((Element) pointOfContact.get(0), "Jesse", "Eichar", "jesse.eichar@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));
        assertContact((Element) pointOfContact.get(1), "Florent", "Gravin", "florent.gravin@camptocamp.com", "owner", false,
                read("eng", "camptocamp SA"));

        assertEquals(Xml.selectString(identification,
                "gmd:language/gco:CharacterString", NS), "ger");


        List<?> keywords = identification.getChildren("descriptiveKeywords", GMD);
        assertEquals(2, keywords.size());
        final String buildingsKeywordXlink =
                "local://che.keyword.get?thesaurus=external.theme.inspire-theme&amp;id=http%3A%2F%2Frdfdata.eionet.europa" +
                ".eu%2Finspirethemes%2Fthemes%2F15&amp;locales=fr,en,de,it";
        assertSharedObject(identification, "gmd:descriptiveKeywords", buildingsKeywordXlink, true);
        final String hydrographyKeywordXlink =
                "local://che.keyword.get?thesaurus=external.theme.inspire-theme&amp;id=http%3A%2F%2Frdfdata.eionet.europa" +
                ".eu%2Finspirethemes%2Fthemes%2F9&amp;locales=fr,en,de,it";
        assertSharedObject(identification, "gmd:descriptiveKeywords", hydrographyKeywordXlink, true);


        List<?> extents = identification.getChildren("extent", GMD);
        assertEquals(2, extents.size());
        final String bernXlink =
                "local://xml.extent.get?id=2&amp;wfs=default&amp;typename=gn:kantoneBB&amp;format=gmd_complete&amp;extentTypeCode=true";
        assertSharedObject(identification, "gmd:extent", bernXlink, true);
        final String fribourgXlink =
                "local://xml.extent.get?id=2196&amp;wfs=default&amp;typename=gn:gemeindenBB&amp;format=gmd_complete&amp;extentTypeCode=true";
        assertSharedObject(identification, "gmd:extent", fribourgXlink, true);

        List<Element> topicCategory = identification.getChildren("topicCategory", GMD);
        assertEquals(2, topicCategory.size());
        assertEquals("transportation", topicCategory.get(0).getChildText("MD_TopicCategoryCode", GMD));
        assertEquals("imageryBaseMapsEarthCover_BaseMaps", topicCategory.get(1).getChildText("MD_TopicCategoryCode", GMD));

        assertTrue(service.isSaved());
    }


    @Test
    public void testDateTime() throws Exception {
        fail("to implement");

    }

    @Test
    public void testUpdateExistingFullMetadata_ReplaceContacts() throws Exception {
        fail("to implement");

    }

    @Test
    public void testExcludeExtentThatHasNotChangedIsNotModified() throws Exception {
        fail("to implement");
    }

    @Test
    public void testNonValidatedExtent() throws Exception {
        fail("to implement");
    }

    @Test
    public void testNonValidatedKeyword() throws Exception {
        fail("to implement");
    }

    @Test
    public void testNonDataSetHeirarchyName() throws Exception {
        fail("to implement");
    }

    /**
     * Make sure that children of serviceInfo are copied to data (only ones that are part of dataInfo of course).
     */
    @Test
    public void testChangeIdentificationInfoType() throws Exception {

        fail("to implement");

    }

    private void assertSharedObject(Element identification, String elemName, String xlink, boolean validated) throws JDOMException {
        final Element obj = Xml.selectElement(identification, elemName + "[@xlink:href = '" + xlink + "']", NS);
        assertNotNull("No shared object found with href: " + xlink, obj);

        isValidatedSharedObject(obj, validated);
    }

    private void assertCorrectTranslation(Element metadata, String xpath, Pair<String, String>... expectedValue) throws JDOMException {
        Map<String, String> langMap = Maps.newHashMap();
        langMap.put("eng", "#EN");
        langMap.put("fre", "#FR");
        langMap.put("ger", "#DE");
        langMap.put("ita", "#IT");

        int numTranslations = Xml.selectNodes(metadata, xpath + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString").size();
        assertEquals(expectedValue.length, numTranslations);

        for (Pair<String, String> pair : expectedValue) {
            String lang = pair.one();
            assertTrue(langMap.containsKey(lang));
            String translation = pair.two();

            assertEquals("Wrong translation for language: " + lang, translation, Xml.selectString(metadata,
                    xpath + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='" + langMap.get(lang) + "']", NS).trim());

        }
    }

    private void assertContact(Element contact, String name, String surname, String email, String role,
                               boolean validated, Pair<String, String>... orgs) throws JDOMException {

        assertEquals(name, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/che:individualFirstName", NS).trim());
        assertEquals(surname, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/che:individualLastName", NS).trim());
        assertEquals(email, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/" +
                                                      "che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString", NS).trim());

        assertCorrectTranslation(contact, "che:CHE_CI_ResponsibleParty/gmd:organisationName", orgs);

        assertEquals(role, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue", NS).trim());

        isValidatedSharedObject(contact, validated);
    }

    private void isValidatedSharedObject(Element sharedObject, boolean validated) {
        final boolean isValid = sharedObject.getAttributeValue("role", Geonet.Namespaces.XLINK) == null;
        assertEquals("Expected shared user to be validated: " + Xml.getString(sharedObject), validated, isValid);
    }

    private String loadTestJson() throws URISyntaxException, IOException {
        File file = new File(SaveTest.class.getResource(SaveTest.class.getSimpleName() + ".class").toURI()).getParentFile();
        final String pathToJsonFile = "web-ui/src/main/resources/catalog/components/edit/inspire/MockFullMetadataFactory.js";
        while (!new File(file, pathToJsonFile).exists()) {
            file = file.getParentFile();
        }
        String javascript = Files.toString(new File(file, pathToJsonFile), Charset.forName("UTF-8"));

        final Matcher matcher = Pattern.compile("(?s).*// START TEST DATA(.*)// END TEST DATA.*").matcher(javascript);
        assertTrue(matcher.find());

        return matcher.group(1).trim();
    }

}