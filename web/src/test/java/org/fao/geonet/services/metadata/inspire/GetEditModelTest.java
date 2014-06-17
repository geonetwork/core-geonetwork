package org.fao.geonet.services.metadata.inspire;

import com.google.common.io.Files;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fao.geonet.kernel.search.spatial.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GetEditModelTest {
    @Test
    public void testConformityLeavesOtherConformity() throws Exception {
        final Element testMetadata = Xml.loadFile(GetEditModelTest.class.getResource("conformity/metadata.xml"));


        GetEditModel service = new TestGetEditModel(testMetadata);

        Element params = new Element("params").addContent(new Element("id").setText("2"));
        ServiceContext context = Mockito.mock(ServiceContext.class);
        Mockito.when(context.getLanguage()).thenReturn("eng");

        final Element result = service.exec(params, context);

        final String inspireModelText = result.getTextTrim();
        final JSONObject inspireModel = new JSONObject(inspireModelText);
        assertEquals(0, inspireModel.getJSONObject(Save.JSON_CONFORMITY).getJSONObject(Save.JSON_TITLE).length());
        assertEquals(0, inspireModel.getJSONObject(Save.JSON_CONFORMITY).getString(Save.JSON_CONFORMITY_RESULT_REF).length());

    }

    @Test
    public void testEmpty() throws Exception {
        final Element testMetadata = Xml.loadString("<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" " +
                                                    "xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gco=\"http://www.isotc211" +
                                                    ".org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gmd=\"http://www" +
                                                    ".isotc211.org/2005/gmd\" xmlns:geonet=\"http://www.fao.org/geonetwork\" " +
                                                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                                    "gco:isoType=\"gmd:MD_Metadata\"></che:CHE_MD_Metadata>", false);

        GetEditModel service = new TestGetEditModel(testMetadata);

        Element params = new Element("params").addContent(new Element("id").setText("2"));
        ServiceContext context = Mockito.mock(ServiceContext.class);
        Mockito.when(context.getLanguage()).thenReturn("eng");

        final Element result = service.exec(params, context);
        final String inspireModelText = result.getTextTrim();
        final JSONObject inspireModel = new JSONObject(inspireModelText);

        JSONObject expectedJson = new JSONObject(loadTestJson());
        expectedJson.remove("roleOptions");
        expectedJson.remove("dateTypeOptions");
        expectedJson.remove("hierarchyLevelOptions");
        expectedJson.remove("topicCategoryOptions");
        expectedJson.remove("constraintOptions");
        expectedJson.remove("serviceTypeOptions");
        expectedJson.remove("scopeCodeOptions");

        assertEqualJsonObjects("<root>", inspireModel, expectedJson);
    }

    @Test
    public void testExecDataIdentification() throws Exception {
        final Element testMetadata = Xml.loadFile(GetEditModelTest.class.getResource("inspire-valid-che.xml"));

        GetEditModel service = new TestGetEditModel(testMetadata);

        Element params = new Element("params").addContent(new Element("id").setText("2"));
        ServiceContext context = Mockito.mock(ServiceContext.class);

        final Element result = service.exec(params, context);
        final JSONObject inspireModel = new JSONObject(result.getTextTrim());

        assertEquals(true, inspireModel.getBoolean(Save.JSON_VALID_METADATA));
        assertEquals("ger", inspireModel.getString(Save.JSON_LANGUAGE));
        assertEquals("utf8", inspireModel.getString(Save.JSON_CHARACTER_SET));
        assertEquals("dataset", inspireModel.getString(Save.JSON_HIERARCHY_LEVEL));
        assertEquals("", inspireModel.getString(Save.JSON_HIERARCHY_LEVEL_NAME));

        assertEquals(1, inspireModel.getJSONArray(Save.JSON_CONTACT).length());
        assertContact(inspireModel.getJSONArray(Save.JSON_CONTACT).getJSONObject(0), "8", false,"", "",
                "metadata@swisstopo.ch", "pointOfContact", read("ger", "Bundesamt für Landestopografie"),
                read("fre", "Office fédéral de topographie"), read("ita", "Ufficio federale di topografia"),
                read("eng", "Federal Office of Topography"));

        assertJSONArray(inspireModel.getJSONArray(Save.JSON_OTHER_LANGUAGES), "ger", "fre", "eng", "ita");

        JSONObject identification = inspireModel.getJSONObject(Save.JSON_IDENTIFICATION);

        assertEquals("data", identification.getString(Save.JSON_IDENTIFICATION_TYPE));
        assertTranslations(identification, Save.JSON_TITLE,
                read("ger", "Inspire Test August 2013 v8 mit Vertriebsinfo/Qualität"),
                read("fre", "Test INSPIRE aout 2013 v8 avec Infos distribution et qualité"));

        JSONObject dateJSON = identification.getJSONObject(Save.JSON_DATE);
        assertEquals("2012-08-23", dateJSON.getString(Save.JSON_DATE));
        assertEquals("gco:Date", dateJSON.getString(Save.JSON_DATE_TAG_NAME));
        assertEquals("creation", dateJSON.getString(Save.JSON_DATE_TYPE));

        assertEquals("INSPIRE Nr. 123", identification.getString(Save.JSON_IDENTIFICATION_IDENTIFIER));

        assertEquals("ger", identification.getString(Save.JSON_LANGUAGE));

        assertTranslations(identification, Save.JSON_IDENTIFICATION_ABSTRACT,
                read("ger", "Zusammenfassung Test INSPIRE"),
                read("fre", "Résumé Test INSPIRE"));

        assertEquals(1, identification.getJSONArray(Save.JSON_IDENTIFICATION_POINT_OF_CONTACT).length());
        assertContact(identification.getJSONArray(Save.JSON_IDENTIFICATION_POINT_OF_CONTACT).getJSONObject(0), "10", false, "", "",
                "geodata@swisstopo.ch", "pointOfContact", read("ger", "Bundesamt für Landestopografie"),
                read("fre", "Office fédéral de topographie"), read("ita", "Ufficio federale di topografia"),
                read("eng", "Federal Office of Topography"));

        final JSONArray keywords = identification.getJSONArray(Save.JSON_IDENTIFICATION_KEYWORDS);
        assertEquals(1, keywords.length());
        assertEquals("http://rdfdata.eionet.europa.eu/inspirethemes/themes/5",
                keywords.getJSONObject(0).getString(Save.JSON_IDENTIFICATION_KEYWORD_CODE));
        assertJsonObjectHasProperties(keywords.getJSONObject(0).getJSONObject(Save.JSON_IDENTIFICATION_KEYWORD_WORD),
                read("ger", "Adressen"), read("fre", "Adresses"),
                read("ita", "Indirizzi"), read("eng", "Addresses"));


        final JSONArray topicCategories = identification.getJSONArray(Save.JSON_IDENTIFICATION_TOPIC_CATEGORIES);
        assertEquals(1, topicCategories.length());
        assertEquals("structure", topicCategories.getString(0));

        final JSONArray extents = identification.getJSONArray(Save.JSON_IDENTIFICATION_EXTENTS);
        assertEquals(1, extents.length());
        assertEquals("country:0", extents.getJSONObject(0).getString("geom"));
        assertTranslations(extents.getJSONObject(0), "description", read("ger", "Schweiz"), read("fre", "Schweiz"),
                read("ita", "Schweiz"), read("eng", "Schweiz"), read("roh", "Schweiz"));


        JSONArray legalConstraints = inspireModel.getJSONObject(Save.JSON_CONSTRAINTS).getJSONArray(Save.JSON_CONSTRAINTS_LEGAL);
        assertEquals(1, legalConstraints.length());
        final JSONObject legalConstraint = legalConstraints.getJSONObject(0);
        assertEquals(2,legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS).length());
        assertEquals("intellectualPropertyRights", legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS).getString(0));
        assertEquals("patent", legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS).getString(1));
        assertEquals(1, legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_USE_CONSTRAINTS).length());
        assertEquals("license", legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_USE_CONSTRAINTS).getString(0));
        assertEquals("222", legalConstraint.getString(Params.REF));

        assertEquals(1, legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_OTHER_CONSTRAINTS).length());
        assertJsonObjectHasProperties(legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_OTHER_CONSTRAINTS).getJSONObject(0),
                read("ger", "other constraint DE"), read("fre", "other constraint FR"));
        assertEquals(1, legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS).length());
        assertJsonObjectHasProperties(legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS).getJSONObject(0),
                read("ger", "Legislation Constraint title DE"), read("fre", "Legislation Constraint title DE"));
        assertJsonObjectHasProperties(legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_USE_LIMITATIONS).getJSONObject(0),
                Pair.read("ger", "uselim de"));

        JSONArray genericConstraints = inspireModel.getJSONObject(Save.JSON_CONSTRAINTS).getJSONArray(Save.JSON_CONSTRAINTS_GENERIC);
        assertEquals(1, genericConstraints.length());
        JSONObject genericConstraint = genericConstraints.getJSONObject(0);
        assertEquals("266", genericConstraint.getString(Params.REF));
        assertJsonObjectHasProperties(genericConstraint.getJSONArray(Save.JSON_CONSTRAINTS_USE_LIMITATIONS).getJSONObject(0),
                Pair.read("ger", "test use limitation for INSPIRE"));

        JSONArray securityConstraints = inspireModel.getJSONObject(Save.JSON_CONSTRAINTS).getJSONArray(Save.JSON_CONSTRAINTS_SECURITY);
        assertEquals(1, securityConstraints.length());
        JSONObject securityConstraint = securityConstraints.getJSONObject(0);
        assertEquals("258", securityConstraint.getString(Params.REF));
        assertJsonObjectHasProperties(securityConstraint.getJSONArray(Save.JSON_CONSTRAINTS_USE_LIMITATIONS).getJSONObject(0),
                Pair.read("ger", "test security constraints for INSPIRE"));

        JSONObject conformity = inspireModel.getJSONObject(Save.JSON_CONFORMITY);
        assertTranslations(conformity, Save.JSON_TITLE,
                read("ger", "VERORDNUNG (EG) Nr. 1089/2010 DER KOMMISSION vom 23. November 2010 zur Durchführung " +
                            "der Richtlinie 2007/2/EG des Europäischen Parlaments und des Rates hinsichtlich der Interoperabilität " +
                            "von Geodatensätzen und -diensten"));
        assertEquals("358", conformity.getString(Save.JSON_CONFORMITY_RESULT_REF));

        assertEquals("false", conformity.getString(Save.JSON_CONFORMITY_PASS));
        assertEquals("INSPIRE Implementing rules", conformity.getString(Save.JSON_CONFORMITY_EXPLANATION));
        final JSONObject dateJson = conformity.getJSONObject(Save.JSON_DATE);
        assertEquals("2010-12-08", dateJson.getString(Save.JSON_DATE));
        assertEquals("gco:Date", dateJson.getString(Save.JSON_DATE_TAG_NAME));
        assertEquals("publication", dateJson.getString(Save.JSON_DATE_TYPE));
        final JSONObject conformityJSONObject = conformity.getJSONObject(Save.JSON_CONFORMITY_LINEAGE);
        assertEquals("376", conformityJSONObject.getString(Params.REF));
        assertTranslations(conformityJSONObject, Save.JSON_CONFORMITY_LINEAGE_STATEMENT,
                read("ger", "INSPIRE Testdaten"));


        JSONArray links = inspireModel.getJSONArray(Save.JSON_LINKS);
        assertEquals(1, links.length());

        for (int i = 0; i < links.length(); i++) {
            final JSONObject link = links.getJSONObject(i);

            assertTrue(!link.optString(Save.JSON_LINKS_LOCALIZED_URL, "").isEmpty());
            assertTrue(!link.optString(Params.REF, "").isEmpty());
        }
    }

    @Test
    public void testExecServiceIdentification() throws Exception {
        final Element testMetadata = Xml.loadFile(GetEditModelTest.class.getResource("inspire-valid-service-che.xml"));

        GetEditModel service = new TestGetEditModel(testMetadata);

        Element params = new Element("params").addContent(new Element("id").setText("2"));
        ServiceContext context = Mockito.mock(ServiceContext.class);

        final Element result = service.exec(params, context);
        final JSONObject inspireModel = new JSONObject(result.getTextTrim());

        assertEquals("fre", inspireModel.getString(Save.JSON_LANGUAGE));
        assertEquals("utf8", inspireModel.getString(Save.JSON_CHARACTER_SET));
        assertEquals("service", inspireModel.getString(Save.JSON_HIERARCHY_LEVEL));
        assertEquals("Service", inspireModel.getString(Save.JSON_HIERARCHY_LEVEL_NAME));

        assertEquals(1, inspireModel.getJSONArray(Save.JSON_CONTACT).length());
        assertContact(inspireModel.getJSONArray(Save.JSON_CONTACT).getJSONObject(0), "15", false, "André", "Schneider",
                "andre.schneider@swisstopo.ch", "pointOfContact", read("ger", "Bundesamt für Landestopographie"),
                read("fre", "Office fédéral de topographie"));

        assertJSONArray(inspireModel.getJSONArray(Save.JSON_OTHER_LANGUAGES), "fre", "ger", "eng", "ita");

        JSONObject identification = inspireModel.getJSONObject(Save.JSON_IDENTIFICATION);

        assertEquals("service", identification.getString(Save.JSON_IDENTIFICATION_TYPE));
        assertEquals("view", identification.getString(Save.JSON_IDENTIFICATION_SERVICETYPE));
        assertTranslations(identification, Save.JSON_TITLE,
                read("ger", "Suchdienst auf www.geocat.ch (CSW 2.0.2)"),
                read("eng", "Search Service for www.geocat.ch (CSW 2.0.2)"),
                read("fre", "Service de recherche sur www.geocat.ch (CSW 2.0.2)"));
        JSONObject dateJSON = identification.getJSONObject(Save.JSON_DATE);
        assertEquals("2009-12-01", dateJSON.getString(Save.JSON_DATE));
        assertEquals("gco:Date", dateJSON.getString(Save.JSON_DATE_TAG_NAME));
        assertEquals("creation", dateJSON.getString(Save.JSON_DATE_TYPE));

        assertEquals("", identification.getString(Save.JSON_IDENTIFICATION_IDENTIFIER));

        assertEquals("", identification.getString(Save.JSON_LANGUAGE));

        assertTranslations(identification, Save.JSON_IDENTIFICATION_ABSTRACT,
                read("ger", "Webdienst (M2M), für die Suche nach Metadaten auf www.geocat.ch. Der Suchdienst basiert auf dem OGC CSW 2.0.2 Standard"),
                read("eng", "This M2M Service allows to search for metadata provided by www.geocat.ch. It is based on the OGC Standard CSW 2.0.2"),
                read("fre", "Service de recherche (M2M) permettant de rechercher les métadonnées disponibles sur www.geocat.ch. Le service est basé sur le standard OGC CSW 2.0.2"));

        assertEquals(1, identification.getJSONArray(Save.JSON_IDENTIFICATION_POINT_OF_CONTACT).length());
        assertContact(identification.getJSONArray(Save.JSON_IDENTIFICATION_POINT_OF_CONTACT).getJSONObject(0), "10", false, "", "",
                "geodata@swisstopo.ch", "pointOfContact", read("ger", "Bundesamt für Landestopografie"),
                read("fre", "Office fédéral de topographie"), read("ita", "Ufficio federale di topografia"),
                read("eng", "Federal Office of Topography"));

        final JSONArray keywords = identification.getJSONArray(Save.JSON_IDENTIFICATION_KEYWORDS);
        assertEquals(7, keywords.length());
        assertEquals("urn:inspire:service:taxonomy:spatialFeatureMatchingService",
                keywords.getJSONObject(6).getString(Save.JSON_IDENTIFICATION_KEYWORD_CODE));
        assertJsonObjectHasProperties(keywords.getJSONObject(6).getJSONObject(Save.JSON_IDENTIFICATION_KEYWORD_WORD),
                read("ger", "spatialFeatureMatchingService"), read("fre", "spatialFeatureMatchingService"),
                read("ita", "spatialFeatureMatchingService"), read("eng", "spatialFeatureMatchingService"));


        final JSONArray topicCategories = identification.getJSONArray(Save.JSON_IDENTIFICATION_TOPIC_CATEGORIES);
        assertEquals(1, topicCategories.length());
        assertEquals("", topicCategories.getString(0));

        final JSONArray extents = identification.getJSONArray(Save.JSON_IDENTIFICATION_EXTENTS);
        assertEquals(1, extents.length());
        assertEquals("country:0", extents.getJSONObject(0).getString("geom"));
        assertTranslations(extents.getJSONObject(0), "description", read("ger", "Schweiz"), read("fre", "Schweiz"),
                read("ita", "Schweiz"), read("eng", "Schweiz"), read("roh", "Schweiz"));


        JSONArray legalConstraints = inspireModel.getJSONObject(Save.JSON_CONSTRAINTS).getJSONArray(Save.JSON_CONSTRAINTS_LEGAL);
        assertEquals(1, legalConstraints.length());
        final JSONObject legalConstraint = legalConstraints.getJSONObject(0);
        assertEquals(1,legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS).length());
        assertEquals("restricted", legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS).getString(0));
        assertEquals(1, legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_USE_CONSTRAINTS).length());
        assertEquals("trademark", legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_USE_CONSTRAINTS).getString(0));
        assertEquals("364", legalConstraint.getString(Params.REF));

        assertEquals(0, legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_OTHER_CONSTRAINTS).length());
        assertEquals(0, legalConstraint.getJSONArray(Save.JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS).length());

        JSONArray genericConstraints = inspireModel.getJSONObject(Save.JSON_CONSTRAINTS).getJSONArray(Save.JSON_CONSTRAINTS_GENERIC);
        assertEquals(0, genericConstraints.length());

        JSONArray securityConstraints = inspireModel.getJSONObject(Save.JSON_CONSTRAINTS).getJSONArray(Save.JSON_CONSTRAINTS_SECURITY);
        assertEquals(0, securityConstraints.length());


        JSONObject conformity = inspireModel.getJSONObject(Save.JSON_CONFORMITY);

        assertTranslations(conformity, Save.JSON_TITLE,
                read("fre", "règlement (ue) n o 1089/2010 de la commission du 23 novembre 2010 portant modalités " +
                            "d'application de la directive 2007/2/ce du parlement européen et du conseil en ce qui concerne" +
                            " l'interopérabilité des séries et des services de données géographiques"));
        assertEquals("529", conformity.getString(Save.JSON_CONFORMITY_RESULT_REF));
        assertEquals("false", conformity.getString(Save.JSON_CONFORMITY_PASS));
        assertEquals("INSPIRE Implementing rules", conformity.getString(Save.JSON_CONFORMITY_EXPLANATION));
        final JSONObject dateJson = conformity.getJSONObject(Save.JSON_DATE);
        assertEquals("2010-12-08", dateJson.getString(Save.JSON_DATE));
        assertEquals("gco:Date", dateJson.getString(Save.JSON_DATE_TAG_NAME));
        assertEquals("publication", dateJson.getString(Save.JSON_DATE_TYPE));
        final JSONObject conformityJSONObject = conformity.getJSONObject(Save.JSON_CONFORMITY_LINEAGE);
        assertEquals("547", conformityJSONObject.getString(Params.REF));
        assertTranslations(conformityJSONObject, Save.JSON_CONFORMITY_LINEAGE_STATEMENT,
                read("ger", "INSPIRE Testdaten"), read("fre", "INSPIRE Testdaten"));

    }

    @Test
    public void testConformity_Existing_NonInspire() throws Exception {
        final Element testMetadata = Xml.loadFile(GetEditModelTest.class.getResource("updateConformityMultipleResultInResultElem/metadata.xml"));

        GetEditModel service = new TestGetEditModel(testMetadata);

        Element params = new Element("params").addContent(new Element("id").setText("2"));
        ServiceContext context = Mockito.mock(ServiceContext.class);

        final Element result = service.exec(params, context);
        final JSONObject inspireModel = new JSONObject(result.getTextTrim());


        JSONObject conformity = inspireModel.getJSONObject(Save.JSON_CONFORMITY);

        final JSONArray otherReports = conformity.getJSONArray(Save.JSON_CONFORMITY_ALL_CONFORMANCE_REPORTS);

        assertEquals(1, otherReports.length());
        assertFalse(otherReports.getJSONObject(0).getString(Params.REF).isEmpty());
        final JSONObject titleObject = otherReports.getJSONObject(0).getJSONObject(Save.JSON_TITLE);
        assertEquals(1, titleObject.length());
        assertEquals(titleObject.toString(), "INSPIRE Implementing rules", titleObject.getString("ger"));
    }

    private void assertJSONArray(JSONArray jsonArray, String... langs) throws JSONException {
        for (int i = 0; i < langs.length; i++) {
            String lang = langs[i];
            assertEquals(lang, jsonArray.getString(i));
        }
    }

    private void assertContact(JSONObject contactJson, String id, boolean validated, String firstName, String lastName, String email, String role,
                               Pair<String, String>... orgNames) throws JSONException {
        assertEquals(id, contactJson.getString(Save.JSON_CONTACT_ID));
        assertEquals(validated, contactJson.getBoolean(Save.JSON_VALIDATED));
        assertEquals(firstName, contactJson.getString(Save.JSON_CONTACT_FIRST_NAME));
        assertEquals(lastName, contactJson.getString(Save.JSON_CONTACT_LAST_NAME));
        assertEquals(email, contactJson.getString(Save.JSON_CONTACT_EMAIL));
        assertEquals(role, contactJson.getString(Save.JSON_CONTACT_ROLE));

        final String jsonKey = Save.JSON_CONTACT_ORG_NAME;
        assertTranslations(contactJson, jsonKey, orgNames);
    }

    private void assertTranslations(JSONObject contactJson, String jsonKey, Pair<String, String>... orgNames) throws JSONException {
        final JSONObject actualOrgNames = contactJson.getJSONObject(jsonKey);
        assertJsonObjectHasProperties(actualOrgNames, orgNames);
    }

    private void assertJsonObjectHasProperties(JSONObject actualOrgNames, Pair<String, String>... orgNames) throws JSONException {
        assertEquals(orgNames.length, actualOrgNames.length());

        for (Pair<String, String> orgName : orgNames) {
            assertEquals(orgName.two(), actualOrgNames.getString(orgName.one()));
        }
    }

    protected void assertEqualJsonObjects(String path, JSONObject inspireModel, JSONObject expectedJson) throws JSONException {
        final Iterator keys = expectedJson.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            final String newPath = path + "." + key;

            assertTrue(path + " -- Missing key: '" + key + "' in\n\n" + inspireModel, inspireModel.has(key));
            Object expectedValue = expectedJson.get(key);
            if (expectedValue instanceof JSONObject) {
                JSONObject value = (JSONObject) expectedValue;
                assertEqualJsonObjects(newPath, inspireModel.getJSONObject(key), value);
            } else if (expectedValue instanceof JSONArray) {
                JSONArray value = (JSONArray) expectedValue;
                assertEqualJsonArrays(newPath, inspireModel.toString(4), inspireModel.getJSONArray(key), value);
            } else {
                assertEquals(newPath + " -- " + inspireModel.toString(4), expectedValue, inspireModel.get(key));
            }
        }
    }

    private void assertEqualJsonArrays(String path, String parentModel, JSONArray inspireModel, JSONArray expectedJSON) throws JSONException {
        for (int i = 0; i < expectedJSON.length(); i++) {
            final String newPath = path + "[" + i + "]";
            Object expectedValue = expectedJSON.get(i);
            assertTrue(path + "-- Missing array element: '" + i + "' in\n\n" + parentModel, inspireModel.length() > i);
            if (expectedValue instanceof JSONObject) {
                JSONObject value = (JSONObject) expectedValue;
                assertEqualJsonObjects(newPath, inspireModel.getJSONObject(i), value);
            } else if (expectedValue instanceof JSONArray) {
                JSONArray value = (JSONArray) expectedValue;
                assertEqualJsonArrays(newPath, inspireModel.toString(4), inspireModel.getJSONArray(i), value);
            } else {
                assertEquals(newPath + " -- " + inspireModel.toString(4), expectedValue, inspireModel.get(i));
            }
        }
    }

    private String loadTestJson() throws URISyntaxException, IOException {
        File file = new File(SaveTest.class.getResource(SaveTest.class.getSimpleName() + ".class").toURI()).getParentFile();
        final String pathToJsonFile = "web-ui/src/main/resources/catalog/components/edit/inspire/EmptyMetadataFactory.js";
        while (!new File(file, pathToJsonFile).exists()) {
            file = file.getParentFile();
        }
        String javascript = Files.toString(new File(file, pathToJsonFile), Charset.forName("UTF-8"));

        final Matcher matcher = Pattern.compile("(?s).*// START TEST DATA.*return (.*)// END TEST DATA.*").matcher(javascript);
        assertTrue(matcher.find());

        return matcher.group(1).trim().replaceAll("guiLanguage", "eng");
    }

    private static class TestGetEditModel extends GetEditModel {
        private final Element testMetadata;

        public TestGetEditModel(Element testMetadata) {
            this.testMetadata = testMetadata;
        }

        @Override
        protected Pair<Element, Boolean> getMetadata(Element params, ServiceContext context, AjaxEditUtils ajaxEditUtils) throws Exception {
            EditLib lib = new EditLib(Mockito.mock(SchemaManager.class));

            lib.removeEditingInfo(testMetadata);
            lib.enumerateTree(testMetadata);

            return Pair.read(testMetadata, true);
        }

        @Override
        protected AjaxEditUtils getAjaxEditUtils(Element params, ServiceContext context) throws Exception {
            return null;
        }

        @Override
        protected IsoLanguagesMapper getIsoLanguagesMapper() {
            return SaveServiceTestImpl.LANGUAGES_MAPPER;
        }

        @Override
        protected void addCodeLists(ServiceContext context, JSONObject metadataJson) throws JDOMException, IOException,
                JSONException {
            // do nothing
        }
    }
}