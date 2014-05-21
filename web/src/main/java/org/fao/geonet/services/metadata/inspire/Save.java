package org.fao.geonet.services.metadata.inspire;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import org.apache.jcs.access.exception.CacheException;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataType;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.constants.Geonet.Namespaces.SRV;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;
import static org.fao.geonet.util.XslUtil.CHE_NAMESPACE;

/**
 * @author Jesse on 5/17/2014.
 */
public class Save implements Service {

    public static final String JSON_IDENTIFICATION_ABSTRACT = "abstract";
    private static final String JSON_CONTACT_ID = "id";
    private static final String PARAM_DATA = "data";
    private static final String JSON_CONTACT = "contact";
    private static final String JSON_OTHER_LANGUAGES = "otherLanguages";
    private static final String JSON_LANGUAGE = "language";
    private static final String JSON_HIERARCHY_LEVEL = "hierarchyLevel";
    private static final String JSON_CONTACT_VALIDATED = "validated";
    private static final String JSON_CONTACT_ROLE = "role";
    public static final String EL_CHARACTER_STRING = "CharacterString";
    private static final String JSON_FIRST_NAME = "name";
    private static final String JSON_LAST_NAME = "surname";
    private static final String JSON_CONTACT_EMAIL = "email";
    private static final String JSON_IDENTIFICATION = "identification";
    public static final String JSON_IDENTIFICATION_TYPE = "type";
    public static final String JSON_CONTACT_ORG_NAME = "organization";
    public static final String ATT_CODE_LIST_VALUE = "codeListValue";
    public static final String ATT_CODE_LIST = "codeList";
    private static final String JSON_IDENTIFICATION_DATE = "date";
    private static final String JSON_IDENTIFICATION_DATE_TYPE = "dateType";
    private static final String JSON_IDENTIFICATION_IDENTIFIER = "citationIdentifier";
    public static final String JSON_IDENTIFICATION_POINT_OF_CONTACT = "pointOfContact";
    public static final String JSON_IDENTIFICATION_TYPE_DATA_VALUE = "data";
    private static final String JSON_TOPIC_CATEGORY = "hierarchyLevelName";
    private static final String JSON_IDENTIFICATION_TOPIC_CATEGORIES = "topicCategory";

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        final String data = Util.getParam(params, PARAM_DATA);
        final String id = params.getChildText(Params.ID);
        GeonetContext handlerContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        final SchemaManager schemaManager = handlerContext.getSchemamanager();
        final DataManager dataManager = handlerContext.getDataManager();
        final EditLib editLib = new EditLib(schemaManager);

        final AjaxEditUtils ajaxEditUtils = getAjaxEditUtils(params, context);
        final Element metadata = getMetadata(context, id, ajaxEditUtils);
        MetadataSchema metadataSchema = schemaManager.getSchema("iso19139.che");

        final JSONObject jsonObject = new JSONObject(data);

        updateMetadata(context, editLib, metadata, metadataSchema, jsonObject);

        updateIdentificationInfo(null, editLib, metadata, metadataSchema, jsonObject);

        saveMetadata(context, id, dataManager, metadata);

        return new Element("ok");
    }

    protected void updateMetadata(ServiceContext context, EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject
            jsonObject) throws Exception {
        updateCharString(editLib, metadata, metadataSchema, "gmd:language", jsonObject.getString(JSON_LANGUAGE));
        updateCharString(editLib, metadata, metadataSchema, "gmd:hierarchyLevelName", jsonObject.getString(JSON_TOPIC_CATEGORY));

        updateCharset(editLib, metadata, metadataSchema, jsonObject);
        updateHierarchyLevel(editLib, metadata, metadataSchema, jsonObject);
        updateContact(editLib, metadataSchema, metadata, "gmd:contact", jsonObject.getJSONArray("contact"), context);
        assert editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, "gmd:dateStamp/gco:DateTime", new AddElemValue("2014-05-20T07:35:05"), true);
        updateOtherLanguage(editLib, metadata, metadataSchema, jsonObject);
    }

    protected void updateIdentificationInfo(ServiceContext context, EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject jsonObject) throws Exception {
        JSONObject identificationJson = jsonObject.getJSONObject(JSON_IDENTIFICATION);
        Element identification = getIdentification(editLib, metadata, metadataSchema, identificationJson);

        updateTranslatedInstance(editLib, metadataSchema, identification, identificationJson.getJSONObject("title"),
                "gmd:citation/gmd:CI_Citation/gmd:title", "title", GMD);

        updateDate(editLib, metadataSchema, identification, identificationJson);
        final String citationIdentifier = identificationJson.getString(JSON_IDENTIFICATION_IDENTIFIER);

        updateCharString(editLib, identification, metadataSchema, "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString", citationIdentifier);
        updateTranslatedInstance(editLib, metadataSchema, identification, identificationJson.getJSONObject(JSON_IDENTIFICATION_ABSTRACT),
                "gmd:abstract", "abstract", GMD);

        updateContact(editLib, metadataSchema, identification, "gmd:pointOfContact", identificationJson.getJSONArray
                (JSON_IDENTIFICATION_POINT_OF_CONTACT), context);

        updateCharString(editLib, identification, metadataSchema, "gmd:language", identificationJson.getString(JSON_LANGUAGE));

        updateKeywords(context, editLib, metadataSchema, identification, identificationJson);
        updateTopicCategory(editLib, metadataSchema, identification, identificationJson);
        updateExtent(context, editLib, metadataSchema, identification, identificationJson);
    }

    private void updateTopicCategory(EditLib editLib, MetadataSchema metadataSchema, Element identification, JSONObject identificationJson) throws Exception {


        int insertIndex = findIndexToAddElements(editLib, identification, metadataSchema, "gmd:topicCategory").one();

        JSONArray categories = identificationJson.getJSONArray(JSON_IDENTIFICATION_TOPIC_CATEGORIES);

        for (int i = 0; i < categories.length(); i++) {
            String category = categories.getString(i);
            final Element element = new Element("topicCategory", GMD).addContent(new Element("MD_TopicCategoryCode",
                    GMD).setText(category));

            identification.addContent(insertIndex + i, element);
        }
    }

    private void updateKeywords(ServiceContext context, EditLib editLib, MetadataSchema metadataSchema, Element identification,
                                JSONObject identificationJson) throws Exception {
        HrefBuilder hrefBuilder = new ExtentHrefBuilder();

        updateSharedObject(context, editLib, metadataSchema, identification, identificationJson, "descriptiveKeywords",
                "descriptiveKeywords", GMD, hrefBuilder);
    }
    private void updateExtent(ServiceContext context, EditLib editLib, MetadataSchema metadataSchema, Element identification,
                              JSONObject identificationJson) throws Exception {
        HrefBuilder hrefBuilder = new DescriptiveKeywordsHrefBuilder();

        Namespace namespace;
        if (identificationJson.getString(JSON_IDENTIFICATION_TYPE).equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
            namespace = GMD;
        } else {
            namespace = SRV;
        }
        updateSharedObject(context, editLib, metadataSchema, identification, identificationJson, "extents",
                "extent", namespace, hrefBuilder);
    }

    private void updateSharedObject(ServiceContext context, EditLib editLib, MetadataSchema metadataSchema, Element identification,
                                    JSONObject identificationJson, String jsonKey, String tagName, Namespace namespace, HrefBuilder hrefBuilder) throws Exception {
        final JSONArray jsonObjects = identificationJson.getJSONArray(jsonKey);

        Set<String> hrefsToKeep = Sets.newHashSet();
        Map<String, Element> hrefsToAdd = Maps.newIdentityHashMap();

        for (int i = 0; i < jsonObjects.length(); i++) {
            JSONObject jsonObject = jsonObjects.getJSONObject(i);

            final String href = hrefBuilder.createHref(jsonObject, identification, identificationJson.getString(JSON_IDENTIFICATION_TYPE));
            hrefsToKeep.add(href);
            hrefsToAdd.put(href, null);
        }

        @SuppressWarnings("unchecked")
        final List<Element> elementList = Lists.newArrayList(identification.getChildren(tagName, namespace));

        int addIndex;
        if (elementList.isEmpty()) {
            final Element element = new Element(tagName, namespace);
            assert editLib.addElementOrFragmentFromXpath(identification, metadataSchema, namespace.getPrefix() + ":" + tagName,
                    new AddElemValue(element), true);
            addIndex = identification.indexOf(element);
            element.detach();
        } else {
            addIndex = identification.indexOf(elementList.get(0));
            for (Element sharedObjectEl : elementList) {
                String href = sharedObjectEl.getAttributeValue("href", XLINK);

                if (hrefsToKeep.contains(href)) {
                    hrefsToAdd.put(href, sharedObjectEl);
                } else {
                    elementList.remove(sharedObjectEl);
                }

                sharedObjectEl.detach();
            }
        }

        for (Map.Entry<String, Element> entry : hrefsToAdd.entrySet()) {
            String href = entry.getKey();
            final Element element;
            if (entry.getValue() == null) {
                element = new Element(tagName, namespace).
                        setAttribute("href", href, XLINK).
                        addContent(resolveXlink(context, href));
                entry.setValue(element);
            }
        }

        identification.addContent(addIndex, hrefsToAdd.values());
    }

    private void updateDate(EditLib editLib, MetadataSchema metadataSchema, Element identification, JSONObject identificationJson) throws JSONException {
        final String jsonDate = identificationJson.getString(JSON_IDENTIFICATION_DATE);
        final String jsonDateType = identificationJson.getString(JSON_IDENTIFICATION_DATE_TYPE);

        final Element dateEl;
        if (jsonDate.contains("T")) {
            dateEl = new Element("DateTime", GCO);
        } else {
            dateEl = new Element("Date", GCO);
        }
        dateEl.setText(jsonDate);

        final Element date = new Element("date", GMD).addContent(
                        new Element("CI_Date", GMD).addContent(Arrays.asList(
                                new Element("date", GMD).addContent(dateEl),
                                new Element("dateType", GMD).addContent(
                                        new Element("CI_DateTypeCode", GMD).
                                                setAttribute(ATT_CODE_LIST_VALUE, jsonDateType).
                                                setAttribute(ATT_CODE_LIST, "http://www.isotc211.org/2005/resources/codeList" +
                                                                            ".xml#CI_DateTypeCode")
                                )
                        )));
        assert editLib.addElementOrFragmentFromXpath(identification, metadataSchema, "gmd:citation/gmd:CI_Citation/gmd:date",
                new AddElemValue(date), true);
    }

    private Element getIdentification(EditLib editLib, Element metadata, MetadataSchema metadataSchema,
                                      JSONObject identificationJson) throws Exception {
        Element identificationInfo = metadata.getChild("gmd:identificationInfo", Geonet.Namespaces.GMD);
        if (identificationInfo == null) {
            identificationInfo = editLib.addElement(metadataSchema.getName(), metadata, "gmd:identificationInfo");
        }

        final String requiredInfoTagName;
        final String jsonType = identificationJson.getString(JSON_IDENTIFICATION_TYPE);
        if (jsonType.equalsIgnoreCase("data")) {
            requiredInfoTagName = "CHE_MD_DataIdentification";
        } else {
            requiredInfoTagName = "CHE_SV_ServiceIdentification";
        }

        Element finalInfo;
        if (!identificationInfo.getChildren().isEmpty()) {
            finalInfo = (Element) identificationInfo.getChildren().get(0);
        } else {
            finalInfo = new Element(requiredInfoTagName, CHE_NAMESPACE);
            identificationInfo.addContent(finalInfo);
        }

        if (!finalInfo.getName().equals(requiredInfoTagName)) {
            finalInfo.detach();

            Element newInfo = new Element(requiredInfoTagName, CHE_NAMESPACE);
            identificationInfo.addContent(newInfo);

            final MetadataType elementType = metadataSchema.getTypeInfo("che:" + requiredInfoTagName);
            final List<String> elementList = elementType.getElementList();
            for (Object o : finalInfo.getChildren()) {
                Element child = (Element) o;
                String name = child.getNamespacePrefix() + ":" + child.getName();
                if (elementList.contains(name)) {
                    finalInfo.addContent(child);
                }
            }
        }

        return finalInfo;
    }

    private void updateOtherLanguage(EditLib editLib, Element metadata, MetadataSchema metadataSchema,
                                     JSONObject jsonObject) throws Exception {
        String tagName = "gmd:locale";
        int insertIndex = findIndexToAddElements(editLib, metadata, metadataSchema, tagName).one();

        JSONArray languages = jsonObject.getJSONArray(JSON_OTHER_LANGUAGES);
        for (int i = 0; i < languages.length(); i++) {
            String language = languages.getString(i);
            Element localeEl = new Element("locale", GMD).addContent(
                    new Element("PT_Locale", GMD).setAttribute("id", getIsoLanguagesMapper().iso639_2_to_iso639_1(language).toUpperCase
                            ()).
                            addContent(Arrays.asList(
                                    new Element("languageCode", GMD).addContent(
                                            new Element("LanguageCode", GMD).
                                                    setAttribute(ATT_CODE_LIST_VALUE, language).
                                                    setAttribute(ATT_CODE_LIST, "#LanguageCode")
                                    ),
                                    new Element("characterEncoding", GMD).addContent(
                                            new Element("MD_CharacterSetCode", GMD).
                                                    setAttribute(ATT_CODE_LIST_VALUE, "utf8").
                                                    setAttribute(ATT_CODE_LIST, "#MD_CharacterSetCode").
                                                    setText("UTF8")
                                    )
                            ))
            );

            metadata.addContent(insertIndex + i, localeEl);
        }
    }

    private Pair<Integer, Element> findIndexToAddElements(EditLib editLib, Element metadata, MetadataSchema metadataSchema, String tagName) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> previousEls = Lists.newArrayList((List<Element>) Xml.selectNodes(metadata, tagName,
                metadataSchema.getSchemaNS()));

        if (previousEls.isEmpty()) {
            // if there are no contacts then we need to add one to know where to insert all of the new ones.
            Element newEl = editLib.addElement(metadataSchema.getName(), metadata, tagName);
            previousEls = Arrays.asList(newEl);
        }

        final Element parentElement = previousEls.get(0).getParentElement();
        int insertIndex = parentElement.indexOf(previousEls.get(0));

        for (Element previousEl : previousEls) {
            previousEl.detach();
        }
        return Pair.read(insertIndex, parentElement);
    }

    private void updateCharString(EditLib editLib, Element metadata, MetadataSchema metadataSchema, String xpath, String newValue) {
        Element langEl = new Element(EL_CHARACTER_STRING, GCO).setText(newValue);
        assert editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, xpath, new AddElemValue(langEl), true);
    }

    private void updateCharset(EditLib editLib, Element metadata, MetadataSchema metadataSchema,
                               JSONObject jsonObject) throws JSONException {
        final String charset = jsonObject.getString("characterSet");
        Element codeElement = new Element("MD_CharacterSetCode", GMD).setAttribute(ATT_CODE_LIST_VALUE, charset).
                setAttribute(ATT_CODE_LIST, "http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode");
        assert editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, "gmd:characterSet", new AddElemValue(codeElement), true);
    }

    private void updateHierarchyLevel(EditLib editLib, Element metadata, MetadataSchema metadataSchema,
                                      JSONObject jsonObject) throws JSONException {
        final String hierarchyLevel = jsonObject.getString(JSON_HIERARCHY_LEVEL);
        Element hierarchyLevelEl = new Element("MD_ScopeCode", GMD).setAttribute(ATT_CODE_LIST_VALUE, hierarchyLevel).
                setAttribute(ATT_CODE_LIST, "http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode");
        assert editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, "gmd:hierarchyLevel", new AddElemValue(hierarchyLevelEl), true);
    }

    private void updateContact(EditLib editLib, MetadataSchema metadataSchema, Element metadata, String xpath,
                               JSONArray contacts, ServiceContext context) throws Exception {
        String tagName = xpath;
        if (xpath.contains("/")) {
            tagName = xpath.substring(xpath.indexOf("/") + 1);
        }
        Pair<Integer, Element> result = findIndexToAddElements(editLib, metadata, metadataSchema, tagName);

        int insertIndex = result.one();

        for (int i = 0; i < contacts.length(); i++) {
            JSONObject contact = contacts.getJSONObject(i);
            String contactId = contact.optString(JSON_CONTACT_ID, null);
            boolean validated = contact.getBoolean(JSON_CONTACT_VALIDATED);
            String role = contact.getString(JSON_CONTACT_ROLE);

            String xlinkHref = "local://xml.user.get?id=" + contactId + "&amp;schema=iso19139.che&amp;role=" + role;

            final Element contactEl;
            if (validated && !Strings.isNullOrEmpty(contactId)) {
                contactEl = new Element(tagName.split(":", 2)[1], GMD).
                        setAttribute("href", xlinkHref, XLINK).
                        setAttribute("show", "embed", XLINK);

                contactEl.addContent(resolveXlink(context, xlinkHref));
            } else {
                contactEl = new Element(tagName.split(":", 2)[1], GMD);
                if (!Strings.isNullOrEmpty(contactId)) {
                    contactEl.setAttribute("href", xlinkHref, XLINK).
                            setAttribute("show", "embed", XLINK).
                            setAttribute("role", ReusableObjManager.NON_VALID_ROLE, XLINK);

                    contactEl.addContent(resolveXlink(context, xlinkHref));
                }

                Element emailEl = new Element("electronicMailAddress", GMD).setText(contact.getString(JSON_CONTACT_EMAIL));

                Element firstNameEl = new Element("individualFirstName", CHE_NAMESPACE).addContent(
                        new Element(EL_CHARACTER_STRING, GCO).setText(contact.getString(JSON_FIRST_NAME)));
                Element lastNameEl = new Element("individualLastName", CHE_NAMESPACE).addContent(
                        new Element(EL_CHARACTER_STRING, GCO).setText(contact.getString(JSON_LAST_NAME)));
                Element roleEl = new Element("role", GMD).addContent(
                        new Element("CI_RoleCode", GMD).
                                setAttribute(ATT_CODE_LIST_VALUE, role).
                                setAttribute(ATT_CODE_LIST, "http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode")
                );

                updateTranslatedInstance(editLib, metadataSchema, contactEl, contact.getJSONObject(JSON_CONTACT_ORG_NAME),
                        "che:CHE_CI_ResponsibleParty/gmd:organisationName", "organisationName", GMD);
                assert editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema,
                        "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address",
                        new AddElemValue(emailEl),
                        true);
                assert editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema,
                        "che:CHE_CI_ResponsibleParty/che:individualFirstName",
                        new AddElemValue(firstNameEl),
                        true);
                assert editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema,
                        "che:CHE_CI_ResponsibleParty/che:individualLastName",
                        new AddElemValue(lastNameEl),
                        true);
                assert editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema,
                        "che:CHE_CI_ResponsibleParty/gmd:role",
                        new AddElemValue(roleEl),
                        true);
            }

            result.two().addContent(insertIndex + i, contactEl);
        }
    }

    private void updateTranslatedInstance(EditLib editLib, MetadataSchema metadataSchema, Element metadata, JSONObject jsonOrgNames,
                                          String xpath, String tagName, Namespace namespace) throws JSONException {
        IsoLanguagesMapper instance = getIsoLanguagesMapper();
        List<Element> translations = Lists.newArrayList();
        final Iterator keys = jsonOrgNames.keys();
        while (keys.hasNext()) {
            String threeLetterLangCode = (String) keys.next();
            String translation = jsonOrgNames.getString(threeLetterLangCode);

            translations.add(new Element("textGroup", GMD).addContent(
                    new Element("LocalisedCharacterString", GMD).
                            setAttribute("locale", "#" + instance.iso639_2_to_iso639_1(threeLetterLangCode).toUpperCase()).
                            setText(translation)
            ));

        }
        Element translatedEl = new Element(tagName, namespace).addContent(
                new Element("PT_FreeText", GMD).addContent(translations)
        );
        assert editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, xpath, new AddElemValue(translatedEl), true);
    }

    @VisibleForTesting
    protected void saveMetadata(ServiceContext context, String id, DataManager dataManager, Element metadata) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        dataManager.updateMetadata(context, dbms, id, metadata, false, true, true, context.getLanguage(), null,
                true, true);
    }

    @VisibleForTesting
    protected Element getMetadata(ServiceContext context, String id, AjaxEditUtils ajaxEditUtils) throws Exception {
        return ajaxEditUtils.getMetadataEmbedded(context, id, false, false);
    }

    @VisibleForTesting
    protected IsoLanguagesMapper getIsoLanguagesMapper() {
        return IsoLanguagesMapper.getInstance();
    }

    @VisibleForTesting
    protected Element resolveXlink(ServiceContext context, String xlinkHref) throws IOException, JDOMException, CacheException {
        return Processor.resolveXLink(xlinkHref, context);
    }

    @VisibleForTesting
    protected AjaxEditUtils getAjaxEditUtils(Element params, ServiceContext context) throws Exception {
        final AjaxEditUtils ajaxEditUtils = new AjaxEditUtils(context);
        ajaxEditUtils.preprocessUpdate(params, context);
        return ajaxEditUtils;
    }

    private interface HrefBuilder {
        public String createHref(JSONObject jsonObject, Element elem, String identType) throws Exception;
    }

    private static class ExtentHrefBuilder implements HrefBuilder {
        @Override
        public String createHref(JSONObject jsonObject, Element elem, String identType) throws Exception {

            String hrefTemplate = "local://che.keyword.get?thesaurus=%s&amp;id=%s&amp;locales=fr,en,de,it";

            String thesaurus;
            if (identType.equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
                thesaurus = "external.theme.inspire-theme";
            } else {
                thesaurus = "external.theme.inspire-service-taxonomy";
            }

            final String code = URLEncoder.encode(jsonObject.getString("code"), "UTF-8");
            return String.format(hrefTemplate, thesaurus, code);
        }
    }

    private static class DescriptiveKeywordsHrefBuilder implements HrefBuilder {
        Map<String, String> typenameMapping = Maps.newHashMap();

        {
            typenameMapping.put("kantone", "kantoneBB");
            typenameMapping.put("gemeinden", "gemeindenBB");
            typenameMapping.put("countries", "countries");
        }

        @Override
        public String createHref(JSONObject jsonObject, Element elem, String identType) throws Exception {

            String hrefTemplate = "local://xml.extent.get?id=%s&amp;wfs=default&amp;typename=gn:%s&amp;format=gmd_complete&amp;extentTypeCode=true";

            final String[] geom = jsonObject.getString("geom").split(":");
            String id = geom[1];
            String typename = typenameMapping.get(geom[0]);

            return String.format(hrefTemplate, id, typename);
        }
    }
}
