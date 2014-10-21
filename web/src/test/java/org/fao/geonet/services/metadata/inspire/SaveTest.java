package org.fao.geonet.services.metadata.inspire;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.TransformerFactoryFactory;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.GeonetworkDataDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.EditLibTest;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
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
import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GEONET;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.constants.Geonet.Namespaces.SRV;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;
import static org.fao.geonet.constants.Geonet.Namespaces.XSI;
import static org.fao.geonet.kernel.search.spatial.Pair.read;
import static org.fao.geonet.services.metadata.inspire.Save.NS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SaveTest {

    public static TemporaryFolder _schemaCatalogContainer = new TemporaryFolder();
    private static SchemaManager _schemaManager;
    private Element testMetadata;
    private SaveServiceTestImpl service;
    private GeonetContext geonetContext;
    private ServiceContext context;
    private DataManager _dataManager;

    @BeforeClass
    public static void initSchemaManager() throws Exception {
        _schemaCatalogContainer.create();

        final ServiceConfig serviceConfig = new ServiceConfig(Lists.<Element>newArrayList());
        final String webappDir = getWebappDir(EditLibTest.class);
        new GeonetworkDataDirectory("geonetwork", webappDir, serviceConfig, null);

        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        final String resourcePath = Resources.locateResourcesDir((ServletContext) null);
        final String schemaPluginsCat = _schemaCatalogContainer.getRoot() + "/" + Geonet.File.SCHEMA_PLUGINS_CATALOG;
        final String schemaPluginsDir = webappDir + "/WEB-INF/data/config/schema_plugins";

        FileUtils.copyFile(new File(webappDir, "WEB-INF/" + Geonet.File.SCHEMA_PLUGINS_CATALOG), new File(schemaPluginsCat));

        SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCat);

        _schemaManager = SchemaManager.getInstance(webappDir, resourcePath, schemaPluginsCat, schemaPluginsDir, "eng", "iso19139");
    }

    @AfterClass
    public static void cleanUpSchemaCatalogFile() {
        _schemaCatalogContainer.delete();
        _schemaManager = null;
    }

    @Before
    public void setUp() throws Exception {
        this.testMetadata = Xml.loadString("<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" " +
                                           "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211" +
                                           ".org/2005/gco\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" " +
                                           "xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\"/>", false);


        this.service = new SaveServiceTestImpl(testMetadata);
        this._dataManager = Mockito.mock(DataManager.class);
        Mockito.when(_dataManager.validate(Mockito.any(Element.class))).thenReturn(true);

        this.geonetContext = Mockito.mock(GeonetContext.class);
        Mockito.when(geonetContext.getSchemamanager()).thenReturn(this._schemaManager);
        Mockito.when(geonetContext.getDataManager()).thenReturn(this._dataManager);

        this.context = Mockito.mock(ServiceContext.class);
        Mockito.when(context.getHandlerContext(Mockito.anyString())).thenReturn(geonetContext);
    }

    @Test
    public void testConformityAddsNewConformityWhenRefIsMissing() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("conformity/metadata.xml"));
        service.setTestMetadata(testMetadata);
        final String testJson = loadTestJson("conformity/postdata.json");

        assertEquals(1, Xml.selectNodes(testMetadata,
                "gmd:dataQualityInfo/*/gmd:report//gmd:DQ_ConformanceResult", NS).size());

        Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(testJson)
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());
        assertEquals(2, Xml.selectNodes(testMetadata,
                "gmd:dataQualityInfo/*/gmd:report//gmd:DQ_ConformanceResult", NS).size());
    }

    @Test
    public void testConformityReplacesOldWhenRefExists() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("conformity/metadata.xml"));
        service.setTestMetadata(testMetadata);
        JSONObject json = new JSONObject(loadTestJson("conformity/postdata.json"));

        json.getJSONObject(Save.JSON_CONFORMITY).put(Save.JSON_CONFORMITY_RESULT_REF, "90");

        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(1, Xml.selectNodes(testMetadata,
                "gmd:dataQualityInfo/*/gmd:report//gmd:DQ_ConformanceResult", NS).size());
    }

    @Test
    public void testSave() throws Exception {
        String json = loadTestJson();

        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);

        assertCorrectlySavedFromEmptyMd();

        EditLib lib = new EditLib(this._schemaManager);
        lib.removeEditingInfo(testMetadata);
        lib.enumerateTree(testMetadata);
        final JSONObject jsonObject = new JSONObject(json);

        String conformanceResultRef = Xml.selectString(testMetadata, "gmd:dataQualityInfo//gmd:DQ_ConformanceResult/geonet:element/@ref", NS);
        jsonObject.getJSONObject(Save.JSON_CONFORMITY).put(Save.JSON_CONFORMITY_RESULT_REF, conformanceResultRef);

        String lineageRef = Xml.selectString(testMetadata, "gmd:dataQualityInfo//gmd:lineage/gmd:LI_Lineage/geonet:element/@ref", NS);
        jsonObject.getJSONObject(Save.JSON_CONFORMITY).getJSONObject(Save.JSON_CONFORMITY_LINEAGE).put(Params.REF, lineageRef);

        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(jsonObject.toString())
        )), context);

        assertCorrectlySavedFromEmptyMd();

    }

    @Test
    public void testUpdateLinks() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("links/linkages.xml"));
        service.setTestMetadata(testMetadata);

        String fre = "fre translation";
        String eng = "eng translation";
        String json = loadTestJson("links/request.json");

        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);

        final Element savedMetadata = service.getSavedMetadata();
        Element eng1 = Xml.selectElement(savedMetadata, "*//gmd:transferOptions[1]//che:LocalisedURL[@locale = '#EN']", NS);
        Element fre1 = Xml.selectElement(savedMetadata, "*//gmd:transferOptions[1]//che:LocalisedURL[@locale = '#FR']", NS);
        Element ger = Xml.selectElement(savedMetadata, "*//gmd:transferOptions[2]//che:LocalisedURL[@locale = '#DE']", NS);
        Element fre2 = Xml.selectElement(savedMetadata, "*//gmd:transferOptions[2]//che:LocalisedURL[@locale = '#FR']", NS);
        assertEquals(2, Xml.selectNodes(savedMetadata, "*//gmd:transferOptions//gmd:CI_OnlineResource", NS).size());

        assertEquals(eng, eng1.getText());
        assertEquals(fre, fre1.getText());
        assertEquals("de url2", ger.getText());
        assertEquals("fr url2", fre2.getText());

        List<Element> linkages = (List<Element>) Xml.selectNodes(savedMetadata, "gmd:distributionInfo//gmd:linkage", NS);
        assertEquals(2, linkages.size());
        for (Element linkage : linkages) {
            assertEquals("che:PT_FreeURL_PropertyType", linkage .getAttributeValue("type", XSI));
        }
    }

    @Test
    public void testAddLink() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("serviceType/metadata.xml"));
        service.setTestMetadata(testMetadata);

        String json = ("{\"links\": [{\"xpath\": \"@@xpath@@\",\"localizedURL\": {\"eng\": \"eng translation\"," +
                       "\"fre\": \"fre translation\"}, \"description\":{\"eng\":\"eng desc\"}}]}");
        json = json.replace("@@xpath@@", GetEditModel.TRANSFER_OPTION_XPATH);
        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);

        final Element savedMetadata = service.getSavedMetadata();
        final List<?> urls = Xml.selectNodes(savedMetadata, "gmd:distributionInfo//gmd:linkage//che:LocalisedURL", NS);
        assertEquals(2, urls.size());
        Element engUrl = Xml.selectElement(savedMetadata, "gmd:distributionInfo//gmd:linkage//che:LocalisedURL[@locale = '#EN']", NS);
        assertNotNull(engUrl);
        assertEquals("eng translation", engUrl.getText());
        Element freUrl = Xml.selectElement(savedMetadata, "gmd:distributionInfo//gmd:linkage//che:LocalisedURL[@locale = '#FR']", NS);
        assertNotNull(freUrl);
        assertEquals("fre translation", freUrl.getText());

        List<Element> linkages = (List<Element>) Xml.selectNodes(savedMetadata, "gmd:distributionInfo//gmd:linkage", NS);
        assertEquals(1, linkages.size());
        for (Element linkage : linkages) {
            assertEquals("che:PT_FreeURL_PropertyType", linkage .getAttributeValue("type", XSI));
        }

        final List freeText = linkages.get(0).getParentElement().getChild("description", GMD).getChildren("PT_FreeText", GMD);
        assertEquals(1, freeText.size());
        final List textGroups = ((Element) freeText.get(0)).getChildren("textGroup", GMD);
        assertEquals(1, textGroups.size());
    }

    @Test
    public void testSaveServiceType() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("serviceType/metadata.xml"));
        service.setTestMetadata(testMetadata);

        String json = "{\"identification\": {\"type\": \"service\", \"serviceType\": \"view\"}}";
        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);

        Element savedMd = service.getSavedMetadata();
        List<?> serviceTypeNodes = Xml.selectNodes(savedMd, "gmd:identificationInfo//srv:serviceType/gco:LocalName", NS);
        assertEquals(1, serviceTypeNodes.size());
        assertEquals("view", ((Element)serviceTypeNodes.get(0)).getText());

        service.setTestMetadata(savedMd);

        String json2 = "{\"identification\": {\"type\": \"service\", \"serviceType\": \"discovery\"}}";
        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json2)
        )), context);

        savedMd = service.getSavedMetadata();
        assertNull(Xml.selectElement(savedMd, "gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code"));
        serviceTypeNodes = Xml.selectNodes(savedMd, "gmd:identificationInfo//srv:serviceType/gco:LocalName", NS);
        assertEquals(1, serviceTypeNodes.size());
        assertEquals("discovery", ((Element)serviceTypeNodes.get(0)).getText());

        service.setTestMetadata(savedMd);
    }

    @Test
    public void testStackOverFlowBug() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("stackoverflow/metadata.xml"));
        service.setTestMetadata(testMetadata);

        String json = loadTestJson("stackoverflow/request.json");
        Element response = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);

        assertEquals("data", response.getName());
    }


    protected void assertCorrectlySavedFromEmptyMd() throws JDOMException {
        final Element savedMetadata = service.getSavedMetadata();

        assertEquals(Xml.selectString(savedMetadata, "gmd:language/gco:CharacterString", NS), "eng");
        assertEquals(Xml.selectString(savedMetadata, "gmd:hierarchyLevelName/gco:CharacterString", NS), "");
        assertEquals(Xml.selectString(savedMetadata, "gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue", NS), "utf8");

        assertEquals(1, savedMetadata.getChildren("referenceSystemInfo", GMD).size());
        final Element referenceSystemInfo = savedMetadata.getChild("referenceSystemInfo", GMD);
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/4936",
                Xml.selectString(referenceSystemInfo, "*//gmd:code//gmd:LocalisedCharacterString", NS));

        assertEquals(Xml.selectString(savedMetadata, "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue", NS), "dataset");
        final List<?> contact = Xml.selectNodes(savedMetadata, "gmd:contact", NS);
        assertEquals(3, contact.size());
        String metadataMainLang = "eng";
        assertContact(metadataMainLang, (Element) contact.get(0), "Florent", "Gravin", "florent.gravin@camptocamp.com", "owner", false,
                read("eng", "camptocamp SA"));
        assertContact(null, (Element) contact.get(1), "Jesse", "Eichar", "jesse.eichar@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));
        assertContact(metadataMainLang, (Element) contact.get(2), "New", "User", "new.user@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));

        assertNotNull(Xml.selectElement(savedMetadata, "gmd:dateStamp/gco:DateTime", NS));
        assertEquals(19, Xml.selectString(savedMetadata, "gmd:dateStamp/gco:DateTime", NS).length());

        assertEquals(Xml.selectString(savedMetadata,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = 'ger']/@codeListValue", NS), "ger");
        assertEquals(Xml.selectString(savedMetadata,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode[@codeListValue = 'eng']/@codeListValue", NS), "eng");
        final Element identification = Xml.selectElement(savedMetadata, "gmd:identificationInfo/che:CHE_MD_DataIdentification", NS);
        assertNotNull(identification);
        assertEquals("gmd:MD_DataIdentification", identification.getAttributeValue("isoType", GCO));
        assertCorrectTranslation(metadataMainLang, identification, "gmd:citation/gmd:CI_Citation/gmd:title", read("eng", "Title"), read("fre", "Titre"));

        assertEquals(Xml.selectString(identification,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date", NS), "2008-06-23");
        assertEquals(Xml.selectString(identification,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue", NS), "creation");


        assertEquals("Citation Identifier", Xml.selectString(identification,
                "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString", NS));

        assertCorrectTranslation(metadataMainLang, identification, "gmd:abstract", read("eng", "Abstract EN"), read("fre", "Abstract FR"));


        final List<?> pointOfContact = Xml.selectNodes(identification, "gmd:pointOfContact", NS);
        assertEquals(2, pointOfContact.size());
        assertContact(null, (Element) pointOfContact.get(0), "Jesse", "Eichar", "jesse.eichar@camptocamp.com", "pointOfContact", true,
                read("eng", "Camptocamp SA"), read("ger", "Camptocamp AG"));
        assertContact(metadataMainLang, (Element) pointOfContact.get(1), "Florent", "Gravin", "florent.gravin@camptocamp.com", "owner", false,
                read("eng", "camptocamp SA"));

        assertEquals(Xml.selectString(identification,
                "gmd:language/gco:CharacterString", NS), "ger");


        List<?> keywords = identification.getChildren("descriptiveKeywords", GMD);
        assertEquals(3, keywords.size());
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
                "local://xml.extent.get?id=2196&amp;wfs=default&amp;typename=gn:gemeindenBB&amp;format=gmd_complete&amp;" +
                "extentTypeCode=true";
        assertSharedObject(identification, "gmd:extent", fribourgXlink, true);

        List<Element> topicCategory = identification.getChildren("topicCategory", GMD);
        assertEquals(2, topicCategory.size());
        assertEquals("transportation", topicCategory.get(0).getChildText("MD_TopicCategoryCode", GMD));
        assertEquals("imageryBaseMapsEarthCover_BaseMaps", topicCategory.get(1).getChildText("MD_TopicCategoryCode", GMD));

        assertCorrectConstraints(identification);

        assertConformity(savedMetadata, metadataMainLang);
        assertDistributionFormats(savedMetadata);

        assertTrue(service.isSaved());
    }

    private void assertDistributionFormats(Element savedMetadata) throws JDOMException {
        final List<?> formats = Xml.selectNodes(savedMetadata, "gmd:distributionInfo/*/gmd:distributionFormat/gmd:MD_Format", NS);
        assertEquals(1, formats.size());

        Element format = (Element) formats.get(0);

        final String name = format.getChild("name", GMD).getChildText("CharacterString", GCO);
        final String version = format.getChild("version", GMD).getChildText("CharacterString", GCO);
        final String href = format.getParentElement().getAttributeValue(XLink.HREF, XLINK);
        final String role = format.getParentElement().getAttributeValue(XLink.ROLE, XLINK);

        assertEquals("newname", name);
        assertEquals("newversion", version);
        assertEquals("local://xml.format.get?id=3", href);
        assertEquals(ReusableObjManager.NON_VALID_ROLE, role);


    }

    private void assertConformity(Element testMetadata, String metadataMainLang) throws JDOMException {
        List<?> conformityCitations = Xml.selectNodes(testMetadata,
                "gmd:dataQualityInfo/*/gmd:report//gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation", NS);

        assertEquals(1, conformityCitations.size());
        Element conformityCitation = (Element) conformityCitations.get(0);

        assertNotNull(Xml.selectElement(testMetadata, "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope", NS));

        final String title = Xml.selectString(conformityCitation, "gmd:title/gco:CharacterString", NS);
        assertEquals("conformity title",title);

        final String date = Xml.selectString(conformityCitation, "gmd:date/gmd:CI_Date/gmd:date/gco:DateTime", NS);
        assertEquals("2002-06-23T12:00", date);

        final String dateType = Xml.selectString(conformityCitation, "gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue", NS);
        assertEquals("modification", dateType);

        final Element conformanceResult = conformityCitation.getParentElement().getParentElement();
        assertEquals("true", conformanceResult.getChild("pass", GMD).getChildText("Boolean", GCO));
        assertEquals("explanation", conformanceResult.getChild("explanation", GMD).getChildText("CharacterString", GCO));
        assertEquals("explanation", conformanceResult.getChild("explanation", GMD).getChildText("CharacterString", GCO));

        List<?> lineageNodes = Xml.selectNodes(testMetadata, "gmd:dataQualityInfo//gmd:LI_Lineage");
        assertEquals(1, lineageNodes.size());

        assertCorrectTranslation(metadataMainLang, (Element) lineageNodes.get(0), "gmd:statement", read("eng", "lineage EN"), read("ger", "lineage DE"));
        assertEquals("feature", Xml.selectString(testMetadata, "gmd:dataQualityInfo/*/gmd:scope//gmd:MD_ScopeCode/@codeListValue", NS));
        assertEquals("levelDescription", Xml.selectString(testMetadata, "gmd:dataQualityInfo/*/gmd:scope//gmd:MD_ScopeDescription/gmd:other/gco:CharacterString", NS));

    }

    protected void assertCorrectConstraints(Element identification) throws JDOMException {
        List<Element> constraints = (List<Element>) Xml.selectNodes(identification,
                "gmd:resourceConstraints/che:CHE_MD_LegalConstraints", NS);
        assertEquals(2, constraints.size());
        for (Element constraint : constraints) {
            assertEquals("gmd:MD_LegalConstraints", constraint.getAttributeValue("isoType", GCO));
        }

        final Element constraint1 = constraints.get(0);
        assertEquals("copyright", Xml.selectString(constraint1, "gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"));
        assertEquals("http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode",
                Xml.selectString(constraint1, "gmd:accessConstraints/gmd:MD_RestrictionCode/@codeList"));
        assertEquals("intellectualPropertyRights", Xml.selectString(constraint1, "gmd:useConstraints/gmd:MD_RestrictionCode" +
                                                                                 "/@codeListValue"));
        assertEquals("http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode",
                Xml.selectString(constraint1, "gmd:useConstraints/gmd:MD_RestrictionCode/@codeList"));
        assertEquals(2, constraint1.getChildren("useLimitation", GMD).size());
        assertCorrectTranslation(null, constraint1, "gmd:useLimitation[1]", read("eng", "leg limitation 1"));
        assertCorrectTranslation(null, constraint1, "gmd:useLimitation[2]", read("eng", "leg limitation 2"));

        assertEquals(2, constraint1.getChildren("otherConstraints", GMD).size());
        assertCorrectTranslation(null, constraint1, "gmd:otherConstraints[1]", read("eng", "otherConstraint"));
        assertCorrectTranslation(null, constraint1, "gmd:otherConstraints[2]", read("eng", "other constraint 2"));

        final List<Element> legislationElems = (List<Element>) Xml.selectNodes(constraint1,
                "che:legislationConstraints/che:CHE_MD_Legislation", NS);
        assertEquals(1, legislationElems.size());

        assertEquals("gmd:MD_Legislation", legislationElems.get(0).getAttributeValue("isoType", GCO));
        assertCorrectTranslation(null, legislationElems.get(0), "che:title/gmd:CI_Citation/gmd:title", read("eng",
                "legislation constraint title"));

        final Element constraint2 = constraints.get(1);
        assertEquals("otherRestrictions",
                Xml.selectString(constraint2, "gmd:accessConstraints[1]/gmd:MD_RestrictionCode/@codeListValue"));
        assertEquals("copyright",
                Xml.selectString(constraint2, "gmd:accessConstraints[2]/gmd:MD_RestrictionCode/@codeListValue"));
        assertEquals("intellectualPropertyRights",
                Xml.selectString(constraint2, "gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue"));

        assertEquals(0, constraint2.getChildren("otherConstraints", GMD).size());
    }

    @Test
    public void testEmptySpec() throws Exception {
        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText("{}")
        )), context);
        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText("{identification:{}, constraints:{}}")
        )), context);

    }

    @Test
    public void testConstraintsAreDeletedWhenDeletedFromInspireData() throws Exception {
        JSONObject json = new JSONObject(loadTestJson());
        json.remove("identification");
        json.remove("language");
        json.remove("characterSet");
        json.remove("hierarchyLevel");
        json.remove("hierarchyLevelName");
        json.remove("contact");
        json.remove("otherLanguages");

        final Element testMetadata = Xml.loadFile(SaveTest.class.getResource("metadataWithContraints.xml"));

        service.setTestMetadata(testMetadata);
        service.setEnumerateOnGetMetadata(false);
        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals("data", result.getName());

        final Element identification = Xml.selectElement(service.getSavedMetadata(), "gmd:identificationInfo/*", NS);

        assertEquals(0, Xml.selectNodes(identification, "geonet:element", Arrays.asList(GEONET)).size());
        assertEquals(0, Xml.selectNodes(identification, "*//geonet:element", Arrays.asList(GEONET)).size());
        assertCorrectConstraints(identification);


        assertEquals(1, Xml.selectNodes(identification, "gmd:resourceConstraints/che:CHE_MD_LegalConstraints[not(gmd:useLimitation)]",
                NS).size());
        assertEquals(1, Xml.selectNodes(identification, "gmd:resourceConstraints/che:CHE_MD_LegalConstraints[gmd:useLimitation]",
                NS).size());
        assertEquals(1, Xml.selectNodes(identification, "gmd:resourceConstraints/gmd:MD_Constraints", NS).size());
        assertCorrectTranslation(null, identification, "gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation[1]", read("eng",
                "limitation 1"));
        assertCorrectTranslation(null, identification, "gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation[2]", read("eng",
                "limitation 2"));
        assertEquals(1, Xml.selectNodes(identification, "gmd:resourceConstraints/gmd:MD_SecurityConstraints", NS).size());
        assertCorrectTranslation(null, identification, "gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation[1]", read("eng",
                "Sec Limitation"));
    }

    @Test
    public void testUpdateExceptionSaving_Keywords() throws Exception {
        final String json = loadTestJson("exception-saving-keywords/postdata.json");

        final Element testMetadata = Xml.loadFile(SaveTest.class.getResource("exception-saving-keywords/testMetadata.xml"));

        service.setTestMetadata(testMetadata);
        service.addXLink("local://xml.user.get?id=8&amp;schema=iso19139.che&amp;role=pointOfContact",
                Xml.loadFile(SaveTest.class.getResource("exception-saving-keywords/user-8.xml")));
        service.addXLink("local://xml.user.get?id=10&amp;schema=iso19139.che&amp;role=pointOfContact",
                Xml.loadFile(SaveTest.class.getResource("exception-saving-keywords/user-10.xml")));
        service.addXLink("local://xml.extent.get?id=0&amp;wfs=default&amp;typename=gn:countries&amp;format=gmd_complete&amp;extentTypeCode=true",
                Xml.loadFile(SaveTest.class.getResource("exception-saving-keywords/extent-countries-0.xml")));

        service.addXLink("local://che.keyword.get?thesaurus=external.theme.inspire-service-taxonomy&amp;id=http%3A%2F%2Frdfdata.eionet.europa.eu%2Finspirethemes%2Fthemes%2F5&amp;locales=fr,en,de,it",
                null);
        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

    }

    @Test
    public void testUpdateExistingNonValidatedContact() throws Exception {

        final String json = loadTestJson("updateExistingNonValidatedContact/postdata.json");

        service.addXLink("local://xml.user.get?id=updateExistingNonValidatedContact&amp;schema=iso19139.che&amp;role=editor",
                Xml.loadFile(SaveTest.class.getResource("updateExistingNonValidatedContact/shared-contact.xml")));
        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);


    }

    @Test
    public void testNewContact() throws Exception {

        final String json = loadTestJson("new-contact/postdata.json");

        service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json)
        )), context);

        assertCorrectTranslation("ger", testMetadata, "gmd:contact/che:CHE_CI_ResponsibleParty/gmd:organisationName",
                read("ger", "asdfasf"), read("fre", "asdfasdf"), read("ita", "asfasdf"), read("eng", "asdfasdf"), read("roh", "asfsadf"));
        assertEquals("asdfasfasfdsa@asdfasdf.dfd",
                Xml.selectString(testMetadata, "gmd:contact/che:CHE_CI_ResponsibleParty//gmd:electronicMailAddress/gco:CharacterString"));
        assertEquals("editor",
                Xml.selectString(testMetadata, "gmd:contact/che:CHE_CI_ResponsibleParty//gmd:role/gmd:CI_RoleCode/@codeListValue"));
        assertEquals("dfsa",
                Xml.selectString(testMetadata, "gmd:contact/che:CHE_CI_ResponsibleParty//che:individualFirstName/gco:CharacterString"));
        assertEquals("asdfa",
                Xml.selectString(testMetadata, "gmd:contact/che:CHE_CI_ResponsibleParty//che:individualLastName/gco:CharacterString"));
    }

    @Test
    public void testChangeIdentificationInfoType_DataToService() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("inspire-valid-che.xml"));
        service.setTestMetadata(testMetadata);


        final String jsonString = loadTestJson();
        JSONObject json = new JSONObject(jsonString);
        json.getJSONObject(Save.JSON_IDENTIFICATION).put(Save.JSON_IDENTIFICATION_TYPE, "service");

        service.addXLink("local://che.keyword.get?thesaurus=external.theme.inspire-service-taxonomy&amp;id=http%3A%2F%2Frdfdata.eionet.europa.eu%2Finspirethemes%2Fthemes%2F15&amp;locales=fr,en,de,it",
                service.createKeyword("eng", "someword", "external.theme.inspire-service-taxonomy"));
        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());

        final Element savedMetadata = service.getSavedMetadata();
        final Element identificationInfo = Xml.selectElement(savedMetadata, "gmd:identificationInfo/*", Save.NS);
        assertEquals("CHE_SV_ServiceIdentification", identificationInfo.getName());

        assertEquals(2, Xml.selectNodes(identificationInfo, "srv:extent", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:title", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:date//gmd:date", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:dateType", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:abstract", Save.NS).size());
        assertEquals(3, Xml.selectNodes(identificationInfo, "gmd:descriptiveKeywords", Save.NS).size());
    }

    @Test
    public void testGetIdentificationInfo_DataToService() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("inspire-valid-che.xml"));

        final String jsonString = loadTestJson();
        JSONObject json = new JSONObject(jsonString);
        final JSONObject identificationJson = json.getJSONObject(Save.JSON_IDENTIFICATION);
        identificationJson.put(Save.JSON_IDENTIFICATION_TYPE, "service");

        MetadataSchema schema = this._schemaManager.getSchema("iso19139.che");
        EditLib editLib = new EditLib(this._schemaManager);

        final Element identificationInfo = service.getIdentification(editLib, testMetadata, schema, identificationJson);
        assertEquals("CHE_SV_ServiceIdentification", identificationInfo.getName());

        assertEquals(0, Xml.selectNodes(identificationInfo, "gmd:extent", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "srv:extent", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:title", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:date//gmd:date", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:dateType", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:abstract", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:descriptiveKeywords", Save.NS).size());
    }

    @Test
    public void testGetIdentificationInfo_ServiceToData() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("inspire-valid-service-che.xml"));

        final String jsonString = loadTestJson();
        JSONObject json = new JSONObject(jsonString);
        final JSONObject identificationJson = json.getJSONObject(Save.JSON_IDENTIFICATION);

        MetadataSchema schema = this._schemaManager.getSchema("iso19139.che");
        EditLib editLib = new EditLib(this._schemaManager);

        final Element identificationInfo = service.getIdentification(editLib, testMetadata, schema, identificationJson);
        assertEquals("CHE_MD_DataIdentification", identificationInfo.getName());

        assertEquals(0, Xml.selectNodes(identificationInfo, "srv:extent", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:extent", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:title", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:date//gmd:date", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:dateType", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:abstract", Save.NS).size());
        assertEquals(7, Xml.selectNodes(identificationInfo, "gmd:descriptiveKeywords", Save.NS).size());
    }

    @Test
    public void testService_SetCouplingInformation() throws Exception {
        service.addXLink("local://xml.user.get?id=15&amp;amp;schema=iso19139.che&amp;amp;role=pointOfContact&#xD",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.user.get?id=15&amp;schema=iso19139.che&amp;role=pointOfContact",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.user.get?id=10&amp;schema=iso19139.che&amp;role=pointOfContact",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        final String jsonString = loadTestJson("updateServiceCouplingInformation.json");
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());

        final Element savedMetadata = service.getSavedMetadata();
        final Element identificationInfo = Xml.selectElement(savedMetadata, "gmd:identificationInfo/*", Save.NS);

        assertEquals("loose", Xml.selectString(identificationInfo, "srv:couplingType/srv:SV_CouplingType/@codeListValue", NS));
        final List<?> operations = Xml.selectNodes(identificationInfo, "srv:containsOperations/srv:SV_OperationMetadata", NS);
        assertEquals(3, operations.size());

        assertContainsOperation((Element) operations.get(0), "GetCapabilities", "WebServices", 1, read("fre",
                "http://www.geocat.ch/geonetwork/srv/fre/csw?SERVICE=CSW&VERSION=2.0.2&REQUEST=GetCapabilities"));
        assertContainsOperation((Element) operations.get(1), "GetRecords", "WebServices", 1, read("fre",
                "http://www.geocat.ch/geonetwork/srv/fre/csw?"));
        assertContainsOperation((Element) operations.get(2), "GetRecordById", "WebServices", 1, read("fre",
                "http://www.geocat.ch/geonetwork/srv/fre/csw?"));
    }

    @Test
    public void testService_UpdateCouplingInformation2() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("updateContainsOperation/metadata2.xml"));

        service.setTestMetadata(testMetadata);
        service.addXLink("local://xml.user.get?id=7&amp;amp;schema=iso19139.che&amp;amp;role=pointOfContact&#xD;",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.user.get?id=7&amp;schema=iso19139.che&amp;role=pointOfContact",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.user.get?id=5&amp;amp;schema=iso19139.che&amp;amp;role=pointOfContact&#xD;",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.user.get?id=5&amp;schema=iso19139.che&amp;role=pointOfContact",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://che.keyword.get?thesaurus=external.theme.inspire-service-taxonomy&amp;" +
                         "id=urn%3Ainspire%3Aservice%3Ataxonomy%3AcomGeographicCompressionService&amp;locales=fr,en,de,it",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.extent.get?id=0&amp;wfs=default&amp;typename=gn:countries&amp;format=gmd_complete&amp;extentTypeCode=true",
                new Element("extent", SRV));
        final String jsonString = loadTestJson("updateContainsOperation/request2.json");
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());

        final Element savedMetadata = service.getSavedMetadata();
        final Element identificationInfo = Xml.selectElement(savedMetadata, "gmd:identificationInfo/*", Save.NS);

        assertEquals(1, Xml.selectNodes(identificationInfo, "srv:containsOperations", NS).size());
        final List<?> operations = Xml.selectNodes(identificationInfo, "srv:containsOperations/srv:SV_OperationMetadata", NS);

        assertContainsOperation((Element) operations.get(0), "asdfasdf", "WebServices", 1,
                read("ger", "http://www.geocat.ch"));
    }

    @Test
    public void testService_UpdateCouplingInformation() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("updateContainsOperation/metadata.xml"));

        service.setTestMetadata(testMetadata);

        service.addXLink("local://xml.user.get?id=15&amp;amp;schema=iso19139.che&amp;amp;role=pointOfContact&#xD",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.user.get?id=15&amp;schema=iso19139.che&amp;role=pointOfContact",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        service.addXLink("local://xml.user.get?id=10&amp;schema=iso19139.che&amp;role=pointOfContact",
                new Element("CHE_CI_ResponsibleParty", XslUtil.CHE_NAMESPACE));
        final String jsonString = loadTestJson("updateContainsOperation/request.json");
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());

        final Element savedMetadata = service.getSavedMetadata();
        final Element identificationInfo = Xml.selectElement(savedMetadata, "gmd:identificationInfo/*", Save.NS);

        assertEquals("updated", Xml.selectString(identificationInfo, "srv:couplingType/srv:SV_CouplingType/@codeListValue", NS));
        final List<?> operations = Xml.selectNodes(identificationInfo, "srv:containsOperations/srv:SV_OperationMetadata", NS);
        assertEquals(1, operations.size());

        final Element containsOperations1 = (Element) operations.get(0);
        assertContainsOperation(containsOperations1, "UpdatedOp", "UpdatedDCP", 1, read("fre", "http://updated.fre"));
        assertEquals("opdesc", containsOperations1.getChild("operationDescription", SRV).getChildText("CharacterString", GCO));
        assertEquals("invocationName", containsOperations1.getChild("invocationName", SRV).getChildText("CharacterString", GCO));
    }

    private void assertContainsOperation(Element containsOperations1, String operationName, String dcpList, int connectPointCount, Pair<String, String> translation) throws JDOMException {
        assertEquals(operationName, Xml.selectString(containsOperations1, "srv:operationName/gco:CharacterString", NS));
        assertEquals(dcpList, Xml.selectString(containsOperations1, "srv:DCP/srv:DCPList/@codeListValue", NS));

        assertEquals(connectPointCount, Xml.selectNodes(containsOperations1, "srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage", NS).size());


        String langCode = "#"+SaveServiceTestImpl.LANGUAGES_MAPPER.iso639_2_to_iso639_1(translation.one()).toUpperCase();
        assertEquals(translation.two(), Xml.selectString(containsOperations1, "srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage//node" +
                                                                              "()[@locale = '" + langCode + "']", NS));
    }

    @Test
    public void testService_UpdateExistingRefSys() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("refSysUpdateExistingEl/metadata.xml"));
        service.setTestMetadata(testMetadata);

        final String jsonString = loadTestJson("refSysUpdateExistingEl/requestData.json");
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());

        final Element savedMetadata = service.getSavedMetadata();

        final Element code = Xml.selectElement(savedMetadata, "gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code", NS);
        assertNotNull(code);
        assertEquals(5, code.getChild("PT_FreeText", GMD).getChildren().size());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/3034", Xml.selectString(code, "*//gmd:LocalisedCharacterString[@locale='#DE']", NS));

        assertNotNull(code.getParentElement().getChild("codeSpace", GMD));
        assertNotNull(code.getParentElement().getChild("version", GMD));
    }

    @Test
    public void testService_UpdateRefSysLoosesData() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("refSysLoosesOtherData/metadata.xml"));
        service.setTestMetadata(testMetadata);

        final String jsonString = loadTestJson("refSysLoosesOtherData/requestData.json");
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());

        final Element savedMetadata = service.getSavedMetadata();

        @SuppressWarnings("unchecked")
        final List<Element> codes = (List<Element>) Xml.selectNodes(savedMetadata, "gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd" +
                                                                                  ":referenceSystemIdentifier/gmd:RS_Identifier/gmd:code", NS);
        assertEquals(2, codes.size());
        final Element OldEl = codes.get(0);
        assertEquals(1, OldEl.getChild("PT_FreeText", GMD).getChildren().size());
        assertEquals("invalid code", Xml.selectString(OldEl, "*//gmd:LocalisedCharacterString[@locale='#DE']", NS));
        assertNotNull(OldEl.getParentElement().getChild("codeSpace", GMD));
        assertNotNull(OldEl.getParentElement().getChild("version", GMD));


        final Element validEl = codes.get(1);
        assertEquals(5, validEl.getChild("PT_FreeText", GMD).getChildren().size());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/3044", Xml.selectString(validEl, "*//gmd:LocalisedCharacterString[@locale='#DE']", NS));
        assertNull(validEl.getParentElement().getChild("codeSpace", GMD));
        assertNull(validEl.getParentElement().getChild("version", GMD));
    }

    /**
     * Make sure that children of serviceInfo are copied to data (only ones that are part of dataInfo of course).
     */
    @Test
    public void testChangeIdentificationInfoType_ServiceToDate() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("inspire-valid-service-che.xml"));
        service.setTestMetadata(testMetadata);


        final String jsonString = loadTestJson();
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals(Xml.getString(result), "data", result.getName());

        final Element savedMetadata = service.getSavedMetadata();
        final Element identificationInfo = Xml.selectElement(savedMetadata, "gmd:identificationInfo/*", Save.NS);
        assertEquals("CHE_MD_DataIdentification", identificationInfo.getName());

        assertEquals(2, Xml.selectNodes(identificationInfo, "gmd:extent", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:title", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:date//gmd:date", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:citation//gmd:dateType", Save.NS).size());
        assertEquals(1, Xml.selectNodes(identificationInfo, "gmd:abstract", Save.NS).size());
        assertEquals(3, Xml.selectNodes(identificationInfo, "gmd:descriptiveKeywords", Save.NS).size());
    }

    @Test
    public void testUpdate_Conformity_Bug_Causes_Invalid_XML() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("updateConformityMultipleResultInResultElem/metadata.xml"));
        service.setTestMetadata(testMetadata);

        final String jsonString = loadTestJson("updateConformityMultipleResultInResultElem/request.json");
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);

        assertEquals("data", result.getName());
        final Element savedMetadata = service.getSavedMetadata();
        List<Element> conformanceResults = (List<Element>) Xml.selectNodes(savedMetadata, "gmd:dataQualityInfo//gmd:result", NS);

        assertEquals(2, conformanceResults.size());

        for (Element conformanceResult : conformanceResults) {
            assertTrue(conformanceResult.getChildren("DQ_ConformanceResult", GMD).size() < 2);
        }

        final Element scope = Xml.selectElement(savedMetadata, "gmd:dataQualityInfo//gmd:scope[" +
                                                                              ".//gmd:MD_ScopeCode/@codeListValue = 'dataset']", NS);

        assertNotNull(scope);
    }

    @Test
    public void testShouldNotAddEmptyContacts() throws Exception {
        testMetadata = Xml.loadFile(SaveTest.class.getResource("save-should-not-add-contact/metadata.xml"));
        service.setTestMetadata(testMetadata);

        final String jsonString = loadTestJson("save-should-not-add-contact/request.json");
        JSONObject json = new JSONObject(jsonString);

        final Element result = service.exec(new Element("request").addContent(Arrays.asList(
                new Element("id").setText("12"),
                new Element("data").setText(json.toString())
        )), context);


        assertEquals("data", result.getName());
        assertEquals(0, Xml.selectNodes(service.getSavedMetadata(), "*//che:CHE_CI_ResponsibleParty", NS).size());
    }

    private void assertSharedObject(Element identification, String elemName, String xlink, boolean validated) throws JDOMException {
        final Element obj = Xml.selectElement(identification, elemName + "[@xlink:href = '" + xlink + "']", NS);
        assertNotNull("No shared object found with href: " + xlink, obj);

        isValidatedSharedObject(obj, validated);
    }

    private void assertCorrectTranslation(String metadataLanguage, Element metadata, String xpath, Pair<String, String>... expectedValue) throws JDOMException {
        int numTranslations = Xml.selectNodes(metadata, xpath + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString").size();
        assertEquals(expectedValue.length, numTranslations);


        for (Pair<String, String> pair : expectedValue) {
            String lang = pair.one();

            String twoCharLang = SaveServiceTestImpl.LANGUAGES_MAPPER.iso639_2_to_iso639_1(lang);
            assertNotNull(twoCharLang);
            twoCharLang = "#" + twoCharLang.toUpperCase();
            String translation = pair.two();

            if (metadataLanguage != null && lang.equals(metadataLanguage)) {
                assertEquals(translation, Xml.selectString(metadata, xpath + "/gco:CharacterString", NS));
            }

            assertEquals("Wrong translation for language: " + lang, translation, Xml.selectString(metadata,
                    xpath + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='" + twoCharLang + "']",
                    NS).trim());
        }
    }

    private void assertContact(String metadataMainLang, Element contact, String name, String surname, String email, String role,
                               boolean validated, Pair<String, String>... orgs) throws JDOMException {

        assertEquals(name, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/che:individualFirstName", NS).trim());
        assertEquals(surname, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/che:individualLastName", NS).trim());
        assertEquals(email, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/" +
                                                      "che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString", NS).trim());

        assertCorrectTranslation(metadataMainLang, contact, "che:CHE_CI_ResponsibleParty/gmd:organisationName", orgs);

        assertEquals(role, Xml.selectString(contact, "che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue", NS).trim());

        isValidatedSharedObject(contact, validated);
    }

    private void isValidatedSharedObject(Element sharedObject, boolean validated) {
        final boolean isValid = sharedObject.getAttributeValue("role", Geonet.Namespaces.XLINK) == null;
        assertEquals("Expected shared user to be validated: " + Xml.getString(sharedObject), validated, isValid);
    }

    private String loadTestJson() throws URISyntaxException, IOException {
        return loadTestJson("MockFullMetadataFactory.json");
    }

    private String loadTestJson(String jsonName) throws URISyntaxException, IOException {
        File file = new File(SaveTest.class.getResource(jsonName).toURI());
        return Files.toString(file, Charset.forName("UTF-8"));
    }

}