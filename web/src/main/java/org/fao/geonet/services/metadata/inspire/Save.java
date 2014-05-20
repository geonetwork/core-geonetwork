package org.fao.geonet.services.metadata.inspire;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.jdom.Element;
import org.jdom.Namespace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;
import static org.fao.geonet.util.XslUtil.CHE_NAMESPACE;

/**
 * @author Jesse on 5/17/2014.
 */
public class Save implements Service {

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

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        final String data = Util.getParam(params, PARAM_DATA);
        final String id = params.getChildText(Params.ID);
        GeonetContext handlerContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        final SchemaManager schemaManager = handlerContext.getSchemamanager();
        final EditLib editLib = new EditLib(schemaManager);
        final IsoLanguagesMapper instance = getIsoLanguagesMapper();

        final AjaxEditUtils ajaxEditUtils = new AjaxEditUtils(context);
        ajaxEditUtils.preprocessUpdate(params, context);
        final Element metadata = getMetadata(context, id, ajaxEditUtils);
        MetadataSchema metadataSchema = schemaManager.getSchema("iso19139.che");

        final JSONObject jsonObject = new JSONObject(data);

        updateLanguage(editLib, metadata, metadataSchema, jsonObject);
        updateCharset(editLib, metadata, metadataSchema, jsonObject);
        updateHierarchyLevel(editLib, metadata, metadataSchema, jsonObject);
        updateContact(editLib, metadataSchema, instance, metadata, "gmd:contact", jsonObject.getJSONArray("contact"), context);
        updateOtherLanguage(editLib, metadata, metadataSchema, jsonObject);

        return new Element("ok");
    }

    private void updateOtherLanguage(EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject jsonObject) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> previousEls = Lists.newArrayList((List<Element>) Xml.selectNodes(metadata, "gmd:locale", metadataSchema.getSchemaNS()));
        String tagName = "gmd:locale";

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

        JSONArray languages = jsonObject.getJSONArray(JSON_OTHER_LANGUAGES);
        for (int i = 0; i < languages.length(); i++) {
            String language = languages.getString(i);
            Element localeEl = new Element("locale", GMD).addContent(
                    new Element("PT_Locale", GMD).setAttribute("id", getIsoLanguagesMapper().iso639_2_to_iso639_1(language).toUpperCase()).
                            addContent(Arrays.asList(
                                    new Element("languageCode", GMD).addContent(
                                            new Element("LanguageCode", GMD).
                                                    setAttribute("codeListValue", language).
                                                    setAttribute("codeList", "#LanguageCode")),
                                    new Element("characterEncoding", GMD).addContent(
                                            new Element("MD_CharacterSetCode", GMD).
                                                    setAttribute("codeListValue", "utf8").
                                                    setAttribute("codeList", "#MD_CharacterSetCode").
                                                    setText("UTF8")
                                    )
                            ))
            );

            metadata.addContent(insertIndex + i, localeEl);
        }
    }

    private void updateLanguage(EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject jsonObject) throws JSONException {
        final String language = jsonObject.getString(JSON_LANGUAGE);
        Element langEl = new Element(EL_CHARACTER_STRING, GCO).setText(language);
        editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, "gmd:language", new AddElemValue(langEl), true);
    }

    private void updateCharset(EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject jsonObject) throws JSONException {
        final String charset = jsonObject.getString("characterSet");
        Element codeElement = new Element("MD_CharacterSetCode", GMD).setAttribute("codeListValue", charset).
                        setAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode");
        editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, "gmd:characterSet", new AddElemValue(codeElement), true);
    }

    private void updateHierarchyLevel(EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject jsonObject) throws JSONException {
        final String hierarchyLevel = jsonObject.getString(JSON_HIERARCHY_LEVEL);
        Element hierarchyLevelEl = new Element("MD_ScopeCode", GMD).setAttribute("codeListValue", hierarchyLevel).
                setAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode");
        editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, "gmd:hierarchyLevel", new AddElemValue(hierarchyLevelEl), true);
    }

    private void updateContact(EditLib editLib, MetadataSchema metadataSchema, IsoLanguagesMapper instance, Element metadata, String xpath,
                               JSONArray contacts, ServiceContext context) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> previousEls = Lists.newArrayList((List<Element>) Xml.selectNodes(metadata, xpath, metadataSchema.getSchemaNS()));
        String tagName = xpath;
        if (xpath.contains("/")) {
            tagName = xpath.substring(xpath.indexOf("/") + 1);
        }

        if (previousEls.isEmpty()) {
            // if there are no contacts then we need to add one to know where to insert all of the new ones.
            Element newContact = editLib.addElement(metadataSchema.getName(), metadata, tagName);
            previousEls = Arrays.asList(newContact);
        }

        final Element parentElement = previousEls.get(0).getParentElement();
        int insertIndex = parentElement.indexOf(previousEls.get(0));

        for (Element previousEl : previousEls) {
            previousEl.detach();
        }

        for (int i = 0; i < contacts.length(); i++) {
            JSONObject contact = contacts.getJSONObject(i);
            String contactId = contact.optString(JSON_CONTACT_ID, null);
            boolean validated = contact.getBoolean(JSON_CONTACT_VALIDATED);
            String role = contact.getString(JSON_CONTACT_ROLE);

            String xlinkHref = "local://xml.user.get?id=" + contactId + "&amp;schema=iso19139.che&amp;role=" + role;

            final Element contactEl;
            if (validated && contactId != null) {
                contactEl = new Element(tagName.split(":", 2)[1], GMD).
                        setAttribute("href", xlinkHref, XLINK).
                        setAttribute("show", "embed", XLINK);
            } else {
                contactEl = new Element(tagName.split(":", 2)[1], GMD);
                if (contactId != null) {
                    contactEl.setAttribute("href", xlinkHref, XLINK).
                            setAttribute("show", "embed", XLINK).
                            setAttribute("role", ReusableObjManager.NON_VALID_ROLE);

                    contactEl.addContent(Processor.resolveXLink(xlinkHref, context));
                }

                JSONObject jsonOrgNames = contact.getJSONObject("organization");
                Element orgNameEl = createTranslatedElement(instance, jsonOrgNames, "organisationName", GMD);

                Element emailEl = new Element("electronicMailAddress", GMD).setText(contact.getString(JSON_CONTACT_EMAIL));

                Element firstNameEl = new Element("individualFirstName", CHE_NAMESPACE).addContent(
                        new Element(EL_CHARACTER_STRING, GCO).setText(contact.getString(JSON_FIRST_NAME)));
                Element lastNameEl = new Element("individualLastName", CHE_NAMESPACE).addContent(
                        new Element(EL_CHARACTER_STRING, GCO).setText(contact.getString(JSON_LAST_NAME)));
                Element roleEl = new Element("role", GMD).addContent(
                        new Element("CI_RoleCode", GCO).
                                setAttribute("codeListValue", role).
                                setAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode"));

                editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema, "che:CHE_CI_ResponsibleParty/gmd:organisationName", new AddElemValue(orgNameEl), true);
                editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema, "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address", new AddElemValue(emailEl), true);
                editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema, "che:CHE_CI_ResponsibleParty/che:individualFirstName", new AddElemValue(firstNameEl), true);
                editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema, "che:CHE_CI_ResponsibleParty/che:individualLastName", new AddElemValue(lastNameEl), true);
                editLib.addElementOrFragmentFromXpath(contactEl, metadataSchema, "che:CHE_CI_ResponsibleParty/gmd:role", new AddElemValue(roleEl), true);
            }

            parentElement.addContent(insertIndex + i, contactEl);

        }
    }

    private Element createTranslatedElement(IsoLanguagesMapper instance, JSONObject translations, String rootElName, Namespace rootElNamespace) throws JSONException {
        List<Element> orgNameTranslations = Lists.newArrayList();
        final Iterator keys = translations.keys();
        while (keys.hasNext()) {
            String orgLang = (String) keys.next();
            String orgName = translations.getString(orgLang);

            orgNameTranslations.add(new Element("textGroup", GMD).addContent(
                    new Element("LocalisedCharacterString", GMD).
                            setAttribute("locale", "#" + instance.iso639_2_to_iso639_1(orgLang).toUpperCase()).
                            setText(orgName)
            ));

        }
        return new Element(rootElName, rootElNamespace).addContent(
                new Element("PT_FreeText", GMD).addContent(orgNameTranslations)
        );
    }

    @VisibleForTesting
    protected Element getMetadata(ServiceContext context, String id, AjaxEditUtils ajaxEditUtils) throws Exception {
        return ajaxEditUtils.getMetadataEmbedded(context, id, false, false);
    }

    @VisibleForTesting
    protected IsoLanguagesMapper getIsoLanguagesMapper() {
        return IsoLanguagesMapper.getInstance();
    }
}
