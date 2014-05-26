package org.fao.geonet.services.metadata.inspire;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.jdom.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import static org.fao.geonet.kernel.search.spatial.Pair.read;
import static org.junit.Assert.assertEquals;

public class GetEditModelTest {

    @Test
    public void testExec() throws Exception {
        final Element testMetadata = Xml.loadFile(GetEditModelTest.class.getResource("inspire-valid-che.xml"));

        GetEditModel service = new GetEditModel() {
            @Override
            protected Element getMetadata(Element params, ServiceContext context, AjaxEditUtils ajaxEditUtils) throws Exception {
                return testMetadata;
            }

            @Override
            protected AjaxEditUtils getAjaxEditUtils(Element params, ServiceContext context) throws Exception {
                return null;
            }

            @Override
            protected IsoLanguagesMapper getIsoLanguagesMapper() {
                return SaveServiceTestImpl.LANGUAGES_MAPPER;
            }
        };

        Element params = new Element("params").addContent(new Element("id").setText("2"));
        ServiceContext context = Mockito.mock(ServiceContext.class);

        final Element result = service.exec(params, context);
        final JSONObject inspireModel = new JSONObject(result.getTextTrim());

        assertEquals("ger", inspireModel.getString(Save.JSON_LANGUAGE));
        assertEquals("utf8", inspireModel.getString(Save.JSON_CHARACTER_SET));
        assertEquals("dataset", inspireModel.getString(Save.JSON_HIERARCHY_LEVEL));
        assertEquals("", inspireModel.getString(Save.JSON_HIERARCHY_LEVEL_NAME));

        assertEquals(1, inspireModel.getJSONArray(Save.JSON_CONTACT).length());
        assertContact(inspireModel.getJSONArray(Save.JSON_CONTACT).getJSONObject(0), "8", "", "",
                "metadata@swisstopo.ch", "pointOfContact", read("ger", "Bundesamt für Landestopografie"),
                read("fre", "Office fédéral de topographie"), read("ita", "Ufficio federale di topografia"),
                read("eng", "Federal Office of Topography"));

        assertJSONArray(inspireModel.getJSONArray(Save.JSON_OTHER_LANGUAGES), "ger", "fre", "eng", "ita");

        JSONObject identification = inspireModel.getJSONObject(Save.JSON_IDENTIFICATION);

        assertEquals("data", identification.getString(Save.JSON_IDENTIFICATION_TYPE));
        assertTranslations(identification, Save.JSON_IDENTIFICATION_TITLE,
                read("ger", "Inspire Test August 2013 v8 mit Vertriebsinfo/Qualität"),
                read("fre", "Test INSPIRE aout 2013 v8 avec Infos distribution et qualité"));
        assertEquals("2012-08-23", identification.getString(Save.JSON_IDENTIFICATION_DATE));
        assertEquals("gco:Date", identification.getString(Save.JSON_IDENTIFICATION_DATE_TAG_NAME));
        assertEquals("creation", identification.getString(Save.JSON_IDENTIFICATION_DATE_TYPE));

        assertTranslations(identification, Save.JSON_IDENTIFICATION_IDENTIFIER, read("ger", "INSPIRE Nr. 123"));

        assertEquals("ger", identification.getString(Save.JSON_LANGUAGE));

        assertTranslations(identification, Save.JSON_IDENTIFICATION_ABSTRACT,
                read("ger", "Zusammenfassung Test INSPIRE"),
                read("fre", "Résumé Test INSPIRE"));

        assertEquals(1, identification.getJSONArray(Save.JSON_IDENTIFICATION_POINT_OF_CONTACT).length());
        assertContact(identification.getJSONArray(Save.JSON_IDENTIFICATION_POINT_OF_CONTACT).getJSONObject(0), "10", "", "",
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
        assertEquals("countries:0", extents.getJSONObject(0).getString("geom"));
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
    }

    private void assertJSONArray(JSONArray jsonArray, String... langs) throws JSONException {
        for (int i = 0; i < langs.length; i++) {
            String lang = langs[i];
            assertEquals(lang, jsonArray.getString(i));
        }
    }

    private void assertContact(JSONObject contactJson, String id, String firstName, String lastName, String email, String role,
                               Pair<String, String>... orgNames) throws JSONException {
        assertEquals(id, contactJson.getString(Save.JSON_CONTACT_ID));
        assertEquals(firstName, contactJson.getString(Save.JSON_CONTACT_FIRST_NAME));
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


}