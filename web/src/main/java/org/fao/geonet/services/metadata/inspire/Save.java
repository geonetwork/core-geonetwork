package org.fao.geonet.services.metadata.inspire;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
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
import org.fao.geonet.util.ISODate;
import org.fao.geonet.util.XslUtil;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GEONET;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.constants.Geonet.Namespaces.SRV;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;
import static org.fao.geonet.constants.Geonet.Namespaces.XSI;
import static org.fao.geonet.util.XslUtil.CHE_NAMESPACE;

/**
 * @author Jesse on 5/17/2014.
 */
public class Save implements Service {
    static final List<Namespace> NS = Arrays.asList(
            GCO,
            GMD,
            SRV,
            GEONET,
            Geonet.Namespaces.XLINK,
            XslUtil.CHE_NAMESPACE);

    private static final String PARAM_DATA = "data";
    private static final String PARAM_FINISH = "finish";
    private static final String PARAM_VALIDATE = "validate";

    public static final String EL_CHARACTER_STRING = "CharacterString";
    public static final String SERVICE_TYPE_TAG_NAME = "serviceType";
    public static final String ATT_CODE_LIST_VALUE = "codeListValue";
    public static final String ATT_CODE_LIST = "codeList";

    static final String JSON_IDENTIFICATION_ABSTRACT = "abstract";
    static final String JSON_CONTACT_ID = "id";
    static final String JSON_CONTACT = "contact";
    static final String JSON_OTHER_LANGUAGES = "otherLanguages";
    static final String JSON_LANGUAGE = "language";
    static final String JSON_HIERARCHY_LEVEL = "hierarchyLevel";
    static final String JSON_VALIDATED = "validated";
    static final String JSON_CONTACT_ROLE = "role";
    static final String JSON_CONTACT_FIRST_NAME = "name";
    static final String JSON_CONTACT_LAST_NAME = "surname";
    static final String JSON_CONTACT_EMAIL = "email";
    static final String JSON_IDENTIFICATION = "identification";
    static final String JSON_IDENTIFICATION_TYPE = "type";
    static final String JSON_CONTACT_ORG_NAME = "organization";
    static final String JSON_DATE = "date";
    static final String JSON_DATE_TYPE = "dateType";
    static final String JSON_IDENTIFICATION_IDENTIFIER = "citationIdentifier";
    static final String JSON_IDENTIFICATION_POINT_OF_CONTACT = "pointOfContact";
    static final String JSON_IDENTIFICATION_TYPE_DATA_VALUE = "data";
    static final String JSON_HIERARCHY_LEVEL_NAME = "hierarchyLevelName";
    static final String JSON_IDENTIFICATION_TOPIC_CATEGORIES = "topicCategory";
    static final String JSON_CONSTRAINTS = "constraints";
    static final String JSON_CONSTRAINTS_LEGAL = "legal";
    static final String JSON_CONSTRAINTS_GENERIC = "generic";
    static final String JSON_CONSTRAINTS_SECURITY = "security";
    public static final String JSON_CONSTRAINTS_CLASSIFICATION = "classification";
    static final String JSON_CHARACTER_SET = "characterSet";
    public static final String JSON_TITLE = "title";
    public static final String JSON_DATE_TAG_NAME = "dateTagName";
    public static final String JSON_IDENTIFICATION_KEYWORDS = "descriptiveKeywords";
    public static final String JSON_IDENTIFICATION_KEYWORD_CODE = "code";
    public static final String JSON_IDENTIFICATION_KEYWORD_WORD = "words";
    public static final String JSON_IDENTIFICATION_EXTENTS = "extents";
    public static final String JSON_CONSTRAINTS_ACCESS_CONSTRAINTS = "accessConstraints";
    public static final String JSON_CONSTRAINTS_USE_CONSTRAINTS = "useConstraints";
    public static final String JSON_CONSTRAINTS_OTHER_CONSTRAINTS = "otherConstraints";
    public static final String JSON_CONSTRAINTS_USE_LIMITATIONS = "useLimitations";
    public static final String JSON_IDENTIFICATION_EXTENT_GEOM = "geom";
    public static final String JSON_IDENTIFICATION_EXTENT_DESCRIPTION = "description";
    static final String JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS = "legislationConstraints";
    static final String JSON_IDENTIFICATION_SERVICETYPE = "serviceType";
    static final String JSON_CONFORMITY = "conformity";
    static final String JSON_CONFORMITY_PASS = "pass";
    static final String JSON_CONFORMITY_RESULT_REF = "conformanceResultRef";
    public static final String JSON_CONFORMITY_EXPLANATION = "explanation";
    public static final String JSON_CONFORMITY_LINEAGE = "lineage";
    public static final String JSON_CONFORMITY_LINEAGE_STATEMENT = "statement";
    public static final String JSON_CONFORMITY_ALL_CONFORMANCE_REPORTS = "allConformanceReports";
    public static final String JSON_CONFORMITY_SCOPE_CODE = "scopeCode";
    public static final String JSON_CONFORMITY_LEVEL_DESC = "levelDescription";
    public static final String JSON_CONFORMITY_ALL_CONFORMANCE_REPORT_INDEX = "reportIndex";
    public static final String JSON_CONFORMITY_IS_TITLE_SET = "isTitleSet";
    public static final String JSON_VALID_METADATA = "metadataIsXsdValid";
    public static final String JSON_CONFORMITY_UPDATE_ELEMENT_REF = "updateResultRef";
    public static final String JSON_LINKS = "links";
    public static final String JSON_LINKS_LOCALIZED_URL = "localizedURL";
    public static final String JSON_LINKS_DESCRIPTION = "description";
    public static final String JSON_LINKS_PROTOCOL = "protocol";
    public static final String JSON_LINKS_XPATH = "xpath";
    public static final String JSON_IDENTIFICATION_KEYWORD_THESAURUS = "thesaurus";
    public static final String JSON_IDENTIFICATION_COUPLING_TYPE = "couplingType";
    public static final String JSON_IDENTIFICATION_CONTAINS_OPERATIONS = "containsOperations";
    public static final String JSON_IDENTIFICATION_OPERATION_NAME = "operationName";
    public static final String JSON_IDENTIFICATION_DCP_LIST = "dcpList";
    public static final String JSON_DISTRIBUTION_FORMAT = "distributionFormats";
    public static final String JSON_DISTRIBUTION_FORMAT_NAME = "name";
    public static final String JSON_DISTRIBUTION_FORMAT_VERSION = "version";
    public static final String JSON_DISTRIBUTION_FORMAT_VALIDATED = "validated";
    public static final String JSON_REF_SYS = "refSys";
    public static final String JSON_REF_SYS_CODE = "code";

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        try {
            final String data = Util.getParam(params, PARAM_DATA);
            final String id = params.getChildText(Params.ID);
            final boolean finish = Util.getParam(params, Params.FINISHED, false);
            final boolean commit = Util.getParam(params, Params.START_EDITING_SESSION, false);

            GeonetContext handlerContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            final SchemaManager schemaManager = handlerContext.getSchemamanager();
            final DataManager dataManager = handlerContext.getDataManager();
            final EditLib editLib = new EditLib(schemaManager);

            final AjaxEditUtils ajaxEditUtils = getAjaxEditUtils(params, context);
            MetadataSchema metadataSchema = schemaManager.getSchema("iso19139.che");
            final Element metadata = getMetadata(context, editLib, id, ajaxEditUtils);

            final JSONObject jsonObject = new JSONObject(data);

            String mainLang = updateMetadata(context, editLib, metadata, metadataSchema, jsonObject);

            final Element identificationInfo = updateIdentificationInfo(mainLang, context, editLib, metadata, metadataSchema, jsonObject);
            updateConstraints(editLib, metadataSchema, identificationInfo, jsonObject, mainLang);
            updateSysRef(editLib, metadataSchema, metadata, jsonObject, mainLang);

            updateConformity(editLib, metadata, metadataSchema, jsonObject, mainLang);

            updateLinks(editLib, metadataSchema, metadata, jsonObject);
            updateFormats(editLib, metadataSchema, metadata, jsonObject);

            editLib.removeEditingInfo(metadata);
            Element metadataToSave = (Element) metadata.clone();
            saveMetadata(context, ajaxEditUtils, id, dataManager, metadataToSave, finish, commit);

            return getResponse(params, context, id, editLib, metadata);
        } catch (Throwable t) {

            Log.error(Geonet.EDITOR, "Error in Save", t);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream s = new PrintStream(out);
            t.printStackTrace(s);
            return new Element("pre").addContent(
                    new Element("code").addContent(out.toString()));
        }
    }

    private void updateSysRef(EditLib editLib, MetadataSchema metadataSchema, Element metadata, JSONObject jsonObject, String mainLang) throws JSONException, JDOMException {
        @SuppressWarnings("unchecked")
        final List<Element> sysRefsEls = Lists.newArrayList(metadata.getChildren("referenceSystemInfo", GMD));
        final JSONArray sysRefJson = jsonObject.optJSONArray(JSON_REF_SYS);
        if (sysRefJson == null) {
            return ;
        }
        Map<String, JSONObject> refs = Maps.newHashMap();

        List<Element> newEls = Lists.newArrayList();

        for (int i = 0; i < sysRefJson.length(); i++) {
            final JSONObject obj = sysRefJson.getJSONObject(i);
            String ref = obj.optString(Params.REF, "").trim();
            if (!ref.trim().isEmpty()) {
                refs.put(ref, obj);
            } else {
                newEls.add(createNewSysRef(obj, mainLang));
            }
        }

        for (Element sysRef : sysRefsEls) {
            final String ref = sysRef.getChild("element", GEONET).getAttributeValue("ref");
            if (refs.containsKey(ref)) {
                final JSONObject translationJson = refs.get(ref);
                Element code = createTranslatedInstance(mainLang, translationJson.getJSONObject(Save.JSON_REF_SYS_CODE), "code", GMD);
                addElementFromXPath(editLib, metadataSchema, sysRef, "gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code", code);
            } else {
                sysRef.detach();
            }
        }

        int addIndex = -1;
        final List infos = metadata.getChildren("referenceSystemInfo", GMD);
        if (!infos.isEmpty()) {
            addIndex = metadata.indexOf((Content) infos.get(infos.size() - 1)) + 1;
        }
        for (Element newEl : newEls) {
            if (addIndex == -1) {
                addElementFromXPath(editLib, metadataSchema, metadata, "gmd:referenceSystemInfo", newEl);
                addIndex = metadata.indexOf(newEl);
            } else {
                metadata.addContent(addIndex, newEl);
            }
            addIndex++;
        }
    }

    private Element createNewSysRef(JSONObject obj, String mainLang) throws JSONException {
        final Element code = createTranslatedInstance(mainLang, obj.getJSONObject(JSON_REF_SYS_CODE), "code", GMD);
        return new Element("referenceSystemInfo", GMD).addContent(
                new Element("MD_ReferenceSystem", GMD).addContent(
                        new Element("referenceSystemIdentifier", GMD).addContent(
                                new Element("RS_Identifier", GMD).addContent(code)
                        )
                )
        );
    }

    private void updateFormats(EditLib editLib, MetadataSchema metadataSchema, Element metadata, JSONObject jsonObject)
            throws JSONException, JDOMException {
        Element distributionInfo = Xml.selectElement(metadata, "gmd:distributionInfo/gmd:MD_Distribution", NS);
        if (distributionInfo != null) {
            @SuppressWarnings("unchecked")
            final List<Element> formats = Lists.newArrayList((List<Element>) distributionInfo.getChildren("distributionFormat", GMD));
            for (Element format : formats) {
                format.detach();
            }
        }

        JSONArray formatJson = jsonObject.optJSONArray(JSON_DISTRIBUTION_FORMAT);
        if (formatJson != null) {
            for (int i = 0; i < formatJson.length(); i++) {
                JSONObject format = formatJson.getJSONObject(i);
                String name = format.getString(JSON_DISTRIBUTION_FORMAT_NAME);
                String version = format.getString(JSON_DISTRIBUTION_FORMAT_VERSION);
                String id = format.optString(Params.ID, "");
                if (!id.trim().isEmpty()) {
                    boolean validated = format.getBoolean(JSON_DISTRIBUTION_FORMAT_VALIDATED);
                    Element formatEl = new Element("distributionFormat", GMD).addContent(
                            new Element("MD_Format", GMD).addContent(Arrays.asList(
                                    new Element("name", GMD).addContent(new Element("CharacterString", GCO).setText(name)),
                                    new Element("version", GMD).addContent(new Element("CharacterString", GCO).setText(version))
                            ))
                    );
                    formatEl.setAttribute("href", "local://xml.format.get?id=" + id, XLINK);
                    if (!validated) {
                        formatEl.setAttribute("role", ReusableObjManager.NON_VALID_ROLE, XLINK);
                    }

                    if (distributionInfo != null) {
                        distributionInfo.addContent(i, formatEl);
                    } else {
                        addElementFromXPath(editLib, metadataSchema, metadata,
                                "gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat", formatEl);

                        distributionInfo = formatEl.getParentElement();
                    }
                }
            }
        }
    }

    private void updateLinks(EditLib editLib, MetadataSchema metadataSchema, Element metadata, JSONObject jsonObject)
            throws JSONException, JDOMException {
        final JSONArray jsonArray = jsonObject.optJSONArray(Save.JSON_LINKS);
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject linkJson = jsonArray.getJSONObject(i);
                String ref = linkJson.optString(Params.REF);
                if (Strings.isNullOrEmpty(ref)) {
                    if (linkJson.has(Save.JSON_LINKS_LOCALIZED_URL)) {
                        addLink(editLib, metadataSchema, linkJson, metadata, false);
                    }
                } else {
                    boolean delete = !linkJson.has(Save.JSON_LINKS_LOCALIZED_URL);
                    final Element element = Xml.selectElement(metadata, "*//*[geonet:element/@ref = '" + ref + "']", NS);
                    if (delete) {
                        element.detach();
                    } else {
                        addLink(editLib, metadataSchema, linkJson, element, true);
                    }
                }
            }
        }
    }

    private void addLink(EditLib editLib, MetadataSchema metadataSchema, JSONObject linkJson, Element element, boolean replace) throws JSONException {
        final JSONObject localizedURL = linkJson.optJSONObject(JSON_LINKS_LOCALIZED_URL);
        JSONObject description = linkJson.optJSONObject(JSON_LINKS_DESCRIPTION);
        if (localizedURL != null) {
            final String xpath = linkJson.getString(JSON_LINKS_XPATH);
            final String specialTag = replace ? EditLib.SpecialUpdateTags.REPLACE : EditLib.SpecialUpdateTags.ADD;
            final Element buildLocalizedElem = buildLocalizedURLElem(localizedURL);
            AddElemValue value = new AddElemValue(new Element(specialTag).addContent(buildLocalizedElem));

            final boolean result = editLib.addElementOrFragmentFromXpath(element, metadataSchema, xpath, value, true);
            assert result;
            final Element parentElement = buildLocalizedElem.getParentElement();
            parentElement.setAttribute("type", "che:PT_FreeURL_PropertyType", XSI);
            final Element onlineResource = parentElement.getParentElement();
            final Element descriptionEl = onlineResource.getChild("description", GMD);
            if (description == null || description.length() == 0) {
                if (descriptionEl != null) {
                    descriptionEl.detach();
                }
            } else {
                if (descriptionEl != null) {
                    final Element freeText = buildLocalizedCharStringElem(description);
                    descriptionEl.setContent(freeText);
                }
            }
        }
    }

    private Element buildLocalizedURLElem(JSONObject localizedURL) throws JSONException {
        final Iterator keys = localizedURL.keys();
        final Element freeUrl = new Element("PT_FreeURL", CHE_NAMESPACE);
        ArrayList<Element> translations = Lists.newArrayList();
        while (keys.hasNext()) {
            String lang = (String) keys.next();
            final String url = localizedURL.getString(lang);
            String code = "#" + getIsoLanguagesMapper().iso639_2_to_iso639_1(lang.toLowerCase()).toUpperCase();
            translations.add(new Element("URLGroup", CHE_NAMESPACE).addContent(
                    new Element("LocalisedURL", CHE_NAMESPACE).setAttribute("locale", code).setText(url)
            ));
        }
        freeUrl.addContent(translations);
        return freeUrl;
    }

    private Element buildLocalizedCharStringElem(JSONObject description) throws JSONException {
        final Iterator keys = description.keys();
        final Element freeUrl = new Element("PT_FreeText", GMD);
        ArrayList<Element> translations = Lists.newArrayList();
        while (keys.hasNext()) {
            String lang = (String) keys.next();
            final String text = description.getString(lang);
            String code = "#" + getIsoLanguagesMapper().iso639_2_to_iso639_1(lang.toLowerCase()).toUpperCase();
            translations.add(new Element("textGroup", GMD).addContent(
                    new Element("LocalisedCharacterString", GMD).setAttribute("locale", code).setText(text)
            ));
        }
        freeUrl.addContent(translations);
        return freeUrl;
    }

    private void updateConstraints(EditLib editLib, MetadataSchema metadataSchema, Element identificationInfo,
                                   JSONObject jsonObject, String mainLang) throws JSONException, JDOMException {
        JSONObject constraintsJson = jsonObject.optJSONObject(JSON_CONSTRAINTS);
        if (constraintsJson == null) {
            return;
        }

        List<String> refsOfConstraintsToKeep = Lists.newArrayList();

        final JSONArray legalConstraints;
        if (constraintsJson.has(JSON_CONSTRAINTS_LEGAL)) {
            legalConstraints = constraintsJson.optJSONArray(JSON_CONSTRAINTS_LEGAL);
        } else {
            legalConstraints = new JSONArray();
        }
        addReferences(legalConstraints, refsOfConstraintsToKeep);
        addReferences(constraintsJson.optJSONArray(JSON_CONSTRAINTS_GENERIC), refsOfConstraintsToKeep);
        addReferences(constraintsJson.optJSONArray(JSON_CONSTRAINTS_SECURITY), refsOfConstraintsToKeep);

        ArrayList<Object> allConstraints = Lists.newArrayList(Xml.selectNodes(identificationInfo,
                "gmd:resourceConstraints/node()[@gco:isoType = 'gmd:MD_LegalConstraints' or name() = 'gmd:MD_LegalConstraints' " +
                "or name() = 'che:CHE_MD_LegalConstraints' or name() = 'gmd:MD_Constraints' or @gco:isoType = 'gmd:MD_Constraints'" +
                "or name() = 'gmd:MD_SecurityConstraints' or @gco:isoType = 'gmd:MD_SecurityConstraints']", NS));

        for (Object o : allConstraints) {
            Element el = (Element) o;
            String ref = el.getChild("element", GEONET).getAttributeValue(Params.REF);
            if (!refsOfConstraintsToKeep.contains(ref)) {
                el.getParentElement().detach();
            }
        }
        int addIndex = -1;
        Element resourceConstraints = identificationInfo.getChild("resourceConstraints", GMD);
        if (resourceConstraints != null) {
            addIndex = identificationInfo.indexOf(resourceConstraints);
        }

        for (int i = 0; i < legalConstraints.length(); i++) {
            final JSONObject constraint = legalConstraints.getJSONObject(i);

            String ref = constraint.optString(Params.REF, "");
            Element legalConstraintEl = null;

            if (!ref.trim().isEmpty()) {
                legalConstraintEl = Xml.selectElement(identificationInfo,
                        "gmd:resourceConstraints/node()[geonet:element/@ref = '" + ref + "']", NS);
                if (legalConstraintEl != null && legalConstraintEl.getParentElement() != null) {
                    legalConstraintEl.getParentElement().detach();
                }
            }

            if (legalConstraintEl == null) {
                legalConstraintEl = new Element("CHE_MD_LegalConstraints", CHE_NAMESPACE).setAttribute("isoType", "gmd:MD_LegalConstraints", GCO);
            }


            updateMultipleTranslatedInstances(mainLang, editLib, metadataSchema, legalConstraintEl, "gmd:useLimitation", constraint,
                    "useLimitation", JSON_CONSTRAINTS_USE_LIMITATIONS);

            updateConstraintCodeList(editLib, metadataSchema, legalConstraintEl, "gmd:accessConstraints", constraint,
                    JSON_CONSTRAINTS_ACCESS_CONSTRAINTS);
            updateConstraintCodeList(editLib, metadataSchema, legalConstraintEl, "gmd:useConstraints", constraint,
                    JSON_CONSTRAINTS_USE_CONSTRAINTS);

            updateMultipleTranslatedInstances(mainLang, editLib, metadataSchema, legalConstraintEl, "gmd:otherConstraints", constraint,
                    JSON_CONSTRAINTS_OTHER_CONSTRAINTS, JSON_CONSTRAINTS_OTHER_CONSTRAINTS);
            updateMultipleTranslatedInstances(mainLang, editLib, metadataSchema, legalConstraintEl,
                    "che:legislationConstraints/che:CHE_MD_Legislation/che:title/gmd:CI_Citation/gmd:title", constraint,
                    "title", JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS);

            resourceConstraints = legalConstraintEl.getParentElement();
            if (resourceConstraints == null) {
                resourceConstraints = new Element("resourceConstraints", GMD).addContent(legalConstraintEl);
            }

            if (addIndex == -1) {
                addElementFromXPath(editLib, metadataSchema, identificationInfo, "gmd:resourceConstraints", resourceConstraints);
                addIndex = identificationInfo.indexOf(resourceConstraints);
            } else {
                identificationInfo.addContent(addIndex, resourceConstraints);
            }

            addIndex++;

        }

    }

    private void updateMultipleTranslatedInstances(String mainLang, EditLib editLib, MetadataSchema metadataSchema, Element metadata, String xpath,
                                                   JSONObject constraint, String tagName, String jsonKey)
            throws JSONException, JDOMException {
        final JSONArray useLimitations = constraint.optJSONArray(jsonKey);
        if (useLimitations == null) {
            return;
        }

        Pair<Integer, Element> result = removeOldElements(metadata, xpath);
        int addIndex = result.one();
        Element parentElement = result.two();

        for (int j = 0; j < useLimitations.length(); j++) {
            Element translation = createTranslatedInstance(mainLang, useLimitations.getJSONObject(j), tagName, GMD);
            if (addIndex == -1) {
                addElementFromXPath(editLib, metadataSchema, metadata, xpath, translation);
                addIndex = translation.getParentElement().indexOf(translation);
                parentElement = translation.getParentElement();
            } else {
                parentElement.addContent(addIndex, translation);
            }
            addIndex++;
        }
    }

    private void updateConstraintCodeList(EditLib editLib, MetadataSchema metadataSchema, Element metadata, String xpath, JSONObject
            constraint, String tagName) throws JSONException, JDOMException {
        final JSONArray codeListValues = constraint.optJSONArray(tagName);

        Pair<Integer, Element> result = removeOldElements(metadata, xpath);
        int addIndex = result.one();
        Element parentElement = result.two();

        for (int j = 0; j < codeListValues.length(); j++) {
            String value = codeListValues.getString(j);
            Element element = new Element(tagName, GMD).addContent(
                    createCodeListEl("MD_RestrictionCode", GMD,
                            "http://www.isotc211.org/2005/resources/codeList" +
                            ".xml#MD_RestrictionCode", value));
            if (addIndex == -1) {
                addElementFromXPath(editLib, metadataSchema, metadata, xpath, element);
                addIndex = element.getParentElement().indexOf(element);
                parentElement = element.getParentElement();
            } else {
                parentElement.addContent(addIndex, element);
            }
            addIndex++;
        }
    }

    private void addElementFromXPath(EditLib editLib, MetadataSchema metadataSchema, Element metadata, String xpath, Element element) {
        if (element != null) {
            boolean addSucceeded = editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, xpath,
                    new AddElemValue(element), true);

            if (!addSucceeded) {
                throw new AssertionError("Unable to add " + element.getQualifiedName() + " to " + metadata.getQualifiedName() + " at '"
                                         + xpath + "'");
            }
        }
    }

    private Pair<Integer, Element> removeOldElements(Element metadata, String xpath) throws JDOMException {

        final List<?> elementsToReplace = Lists.newArrayList(Xml.selectNodes(metadata, xpath, NS));

        int addIndex = -1;
        Element parentElement = null;

        if (!elementsToReplace.isEmpty()) {
            Element elem = (Element) elementsToReplace.get(0);
            parentElement = elem.getParentElement();
            addIndex = parentElement.indexOf(elem);
            for (Object o : elementsToReplace) {
                elem = (Element) o;
                elem.detach();
            }
        }

        return Pair.read(addIndex, parentElement);
    }

    private Element createCodeListEl(String tagName, Namespace namespace, String codeList, String codeListValue) {
        return new Element(tagName, namespace).
                setAttribute(ATT_CODE_LIST, codeList).
                setAttribute(ATT_CODE_LIST_VALUE, codeListValue);
    }

    private void addReferences(JSONArray jsonArray, List<String> refsOfConstraintsToKeep) throws JSONException {
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject constraint = jsonArray.getJSONObject(i);
                if (!constraint.optString(Params.REF, "").trim().isEmpty()) {
                    refsOfConstraintsToKeep.add(constraint.getString(Params.REF));
                }
            }
        }
    }

    protected String updateMetadata(ServiceContext context, EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject
            jsonObject) throws Exception {
        final String mainLang = jsonObject.optString(JSON_LANGUAGE);
        updateCharString(editLib, metadata, metadataSchema, "gmd:language", mainLang);
        updateCharString(editLib, metadata, metadataSchema, "gmd:hierarchyLevelName", jsonObject.optString(JSON_HIERARCHY_LEVEL_NAME));

        updateDateStamp(editLib, metadata, metadataSchema);
        updateCharset(editLib, metadata, metadataSchema, jsonObject);
        updateHierarchyLevel(editLib, metadata, metadataSchema, jsonObject);
        updateContact(mainLang, editLib, metadataSchema, metadata, "gmd:contact", jsonObject.optJSONArray("contact"), context);

        assert editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, "gmd:dateStamp/gco:DateTime", new AddElemValue("2014-05-20T07:35:05"), true);
        updateOtherLanguage(editLib, metadata, metadataSchema, jsonObject);

        return mainLang;
    }

    private void updateDateStamp(EditLib editLib, Element metadata, MetadataSchema metadataSchema) {
        Element dateStamp = new Element("dateStamp", GMD).addContent(
                new Element("DateTime", GCO).setText(new ISODate().toString())
        );
        addElementFromXPath(editLib, metadataSchema, metadata, "gmd:dateStamp", dateStamp);
    }

    private void updateConformity(EditLib editLib, Element metadata, MetadataSchema metadataSchema, JSONObject jsonObject,
                                  String mainLang) throws Exception {
        JSONObject conformityJson = jsonObject.optJSONObject(JSON_CONFORMITY);
        if (conformityJson == null) {
            return;
        }

        String conformanceResultRef = conformityJson.optString(JSON_CONFORMITY_RESULT_REF);
        if (Strings.isNullOrEmpty(conformanceResultRef)) {
            conformanceResultRef = conformityJson.optString(JSON_CONFORMITY_UPDATE_ELEMENT_REF);
        }

        Element conformanceResult = null;
        if (!Strings.isNullOrEmpty(conformanceResultRef)) {
            conformanceResult = Xml.selectElement(metadata,
                    "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report//gmd:DQ_ConformanceResult[geonet:element/@ref = '" +
                    conformanceResultRef + "']", NS);

            if (conformanceResult == null) {
                Element elem = Xml.selectElement(metadata, "*//*[geonet:element/@ref = '" + conformanceResultRef + "']", NS);
                if (elem != null) {
                    throw new IllegalArgumentException("Reference ID: '" + conformanceResultRef + "' does not identify a " +
                                                       "gmd:DQ_ConformanceResult element.  Instead it identifies: \n" + Xml.getString(elem));

                } else {
                    throw new IllegalArgumentException("Reference ID: '" + conformanceResultRef + "' does not identify a " +
                                                       "gmd:DQ_ConformanceResult element.");
                }
            }
        }

        if ((!conformityJson.has(JSON_CONFORMITY_IS_TITLE_SET) || !conformityJson.getBoolean(JSON_CONFORMITY_IS_TITLE_SET)) &&
            conformanceResult == null) {
            return;
        }

        if (conformityJson.has(JSON_CONFORMITY_IS_TITLE_SET) && conformityJson.getBoolean(JSON_CONFORMITY_IS_TITLE_SET) && conformanceResult == null) {
            conformanceResult = new Element("DQ_ConformanceResult", GMD);

            // iso19139 allows multiple gmd:results but inspire doesn't so check that we don't add an extra unintentionally
            final String xpathToResult = "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result";
            final Element resultEl = Xml.selectElement(metadata, xpathToResult, NS);
            if (resultEl == null) {
                addElementFromXPath(editLib, metadataSchema, metadata, xpathToResult,
                        new Element(EditLib.SpecialUpdateTags.ADD, GEONET).addContent(conformanceResult));
            } else {
                final Element gmdDQ_DataQuality = resultEl.getParentElement().getParentElement().getParentElement();
                final Element gmdReport = editLib.addElement(metadataSchema.getName(), gmdDQ_DataQuality, "gmd:report");
                addElementFromXPath(editLib, metadataSchema, gmdReport, "gmd:DQ_DomainConsistency/gmd:result",
                        new Element(EditLib.SpecialUpdateTags.ADD, GEONET).addContent(conformanceResult));
            }

            final Element gmdDQ_DataQuality = conformanceResult.getParentElement().getParentElement().getParentElement().getParentElement();

            @SuppressWarnings("unchecked")
            final List<Element> dqScopes = new ArrayList<Element>((java.util.Collection<? extends Element>) Xml.selectNodes(gmdDQ_DataQuality, "gmd:scope/gmd:DQ_Scope"));
            for (Element scope : dqScopes) {
                if (scope.getChildren().isEmpty()) {
                    scope.detach();
                }
            }
        }

        final Element title = createTranslatedInstance(mainLang, conformityJson.optJSONObject(JSON_TITLE), JSON_TITLE, GMD);
        addElementFromXPath(editLib, metadataSchema, conformanceResult,
                "gmd:specification/gmd:CI_Citation/gmd:title", title);

        Element citationEl = title.getParentElement();
        if (!conformityJson.has(JSON_DATE)) {
            GetEditModel.addConformityDate(conformityJson);
        }
        updateDate(editLib, metadataSchema, citationEl, "gmd:date", conformityJson);

        Element pass = new Element(JSON_CONFORMITY_PASS, GMD).addContent(
                new Element("Boolean", GCO).setText(conformityJson.optString(JSON_CONFORMITY_PASS, ""))
        );
        addElementFromXPath(editLib, metadataSchema, conformanceResult,
                "gmd:pass", pass);

        Element explanation = new Element("explanation", GMD).addContent(
                new Element("CharacterString", GCO).setText(conformityJson.optString(JSON_CONFORMITY_EXPLANATION, ""))
        );
        addElementFromXPath(editLib, metadataSchema, conformanceResult,
                "gmd:explanation", explanation);

        final Element dataQualityEl = GetEditModel.getDataQualityEl(conformanceResult);
        updateCodeList(editLib,
                dataQualityEl,
                metadataSchema,
                new Element("MD_ScopeCode", GMD),
                conformityJson.getString(JSON_CONFORMITY_SCOPE_CODE),
                "gmd:scope/gmd:DQ_Scope/gmd:level",
                "http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode",
                true
        );

        if (conformityJson.has(JSON_CONFORMITY_LEVEL_DESC)) {
            updateCharString(editLib, dataQualityEl, metadataSchema,
                    "gmd:scope/gmd:DQ_Scope/gmd:levelDescription/gmd:MD_ScopeDescription/gmd:other",
                    conformityJson.getString(JSON_CONFORMITY_LEVEL_DESC));
        }
        JSONObject lineageJSON = conformityJson.getJSONObject(JSON_CONFORMITY_LINEAGE);
        String lineageRef = lineageJSON.optString(Params.REF);

        Element lineageEl = Xml.selectElement(metadata,
                "gmd:dataQualityInfo//gmd:lineage/gmd:LI_Lineage[geonet:element/@ref = '" + lineageRef + "']", Save.NS);

        if (lineageEl == null) {
            lineageEl = new Element("LI_Lineage", GMD);
            addElementFromXPath(editLib, metadataSchema, metadata,
                    "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage", lineageEl);
        }

        updateTranslatedInstance(mainLang, editLib, metadataSchema, lineageEl, lineageJSON.getJSONObject(JSON_CONFORMITY_LINEAGE_STATEMENT),
                "gmd:statement", "statement", GMD);

    }

    protected Element updateIdentificationInfo(String mainLang, ServiceContext context, EditLib editLib, Element metadata,
                                               MetadataSchema metadataSchema, JSONObject jsonObject) throws Exception {
        JSONObject identificationJson = jsonObject.optJSONObject(JSON_IDENTIFICATION);
        if (identificationJson == null) {
            identificationJson = new JSONObject();
        }
        Element identification = getIdentification(editLib, metadata, metadataSchema, identificationJson);

        updateTranslatedInstance(mainLang, editLib, metadataSchema, identification, identificationJson.optJSONObject("title"),
                "gmd:citation/gmd:CI_Citation/gmd:title", JSON_TITLE, GMD);

        updateDate(editLib, metadataSchema, identification, "gmd:citation/gmd:CI_Citation/gmd:date", identificationJson);

        final String identificationType = identificationJson.optString(JSON_IDENTIFICATION_TYPE, JSON_IDENTIFICATION_TYPE_DATA_VALUE);
        if (identificationType.equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
            String citationIdentifier = identificationJson.optString(JSON_IDENTIFICATION_IDENTIFIER, "");
            updateCharString(editLib, identification, metadataSchema,
                    "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code", citationIdentifier);
        }
        updateTranslatedInstance(mainLang, editLib, metadataSchema, identification, identificationJson.optJSONObject(JSON_IDENTIFICATION_ABSTRACT),
                "gmd:abstract", "abstract", GMD);

        updateContact(mainLang, editLib, metadataSchema, identification, "gmd:pointOfContact", identificationJson.optJSONArray
                (JSON_IDENTIFICATION_POINT_OF_CONTACT), context);

        updateCharString(editLib, identification, metadataSchema, "gmd:language", identificationJson.optString(JSON_LANGUAGE));

        updateKeywords(context, editLib, metadataSchema, identification, identificationJson);
        if (identificationType.equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
            updateTopicCategory(editLib, metadataSchema, identification, identificationJson);
        }
        updateExtent(context, editLib, metadataSchema, identification, identificationJson);
        if (!identificationType.equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
            updateServiceType(editLib, metadataSchema, identification, identificationJson);
            updateCodeList(editLib,
                    identification,
                    metadataSchema,
                    new Element("SV_CouplingType", SRV),
                    identificationJson.optString(Save.JSON_IDENTIFICATION_COUPLING_TYPE),
                    "srv:couplingType",
                    "http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#SV_CouplingType",
                    true);
            updateContainsOperation(editLib, identification, metadataSchema, identificationJson);
        }
        return identification;
    }

    private void updateCodeList(EditLib editLib, Element metadata, MetadataSchema metadataSchema, Element template,
                                String value, String xpath, String codelist, boolean replace) {
        if (value != null) {
           template.setAttribute(ATT_CODE_LIST_VALUE, value).
                    setAttribute(ATT_CODE_LIST, codelist);
            final String tag = replace ? EditLib.SpecialUpdateTags.REPLACE : EditLib.SpecialUpdateTags.ADD;
            addElementFromXPath(editLib, metadataSchema, metadata, xpath, new Element(tag).addContent(template));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateContainsOperation(EditLib editLib, Element identification, MetadataSchema metadataSchema, JSONObject
            identificationJson) throws JSONException, JDOMException {
        final JSONArray jsonArray = identificationJson.optJSONArray(Save.JSON_IDENTIFICATION_CONTAINS_OPERATIONS);
        if (jsonArray == null) {
            return;
        }
        final List<Element> allOperations = Lists.newArrayList((
                Iterable<? extends Element>) Xml.selectNodes(identification, "srv:containsOperations", NS));

        HashSet<Object> refsToKeep = Sets.newHashSet();
        for (int i = 0; i < jsonArray.length(); i++) {
            String ref = jsonArray.getJSONObject(i).getString(Params.REF);
            if (!Strings.isNullOrEmpty(ref)) {
                refsToKeep.add(ref);
            }
        }

        for (Element operationEl : allOperations) {
            final Element opMetadataEl = operationEl.getChild("SV_OperationMetadata", SRV);
            if (opMetadataEl == null) {
                operationEl.detach();
            } else {
                String ref = opMetadataEl.getChild("element", GEONET).getAttributeValue(Params.REF);
                if (!refsToKeep.contains(ref)) {
                    operationEl.detach();
                }
            }
        }


        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject op = jsonArray.getJSONObject(i);
            Element opEl = null;

            String ref = op.getString(Params.REF);
            if (!Strings.isNullOrEmpty(ref)) {
                opEl = Xml.selectElement(identification, "*//srv:SV_OperationMetadata[geonet:element/@ref = '" + ref + "']", NS);
            }

            if (opEl == null) {
                opEl = new Element("SV_OperationMetadata", SRV);
            }
            updateCharString(editLib, opEl, metadataSchema, "srv:operationName",
                    op.optString(Save.JSON_IDENTIFICATION_OPERATION_NAME));
            updateCodeList(editLib,
                    opEl,
                    metadataSchema,
                    new Element("DCPList", SRV),
                    op.optString(Save.JSON_IDENTIFICATION_DCP_LIST),
                    "srv:DCP",
                    "http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#DCPList", true);
            updateLinks(editLib, metadataSchema, opEl, op);

            if (opEl.getParentElement() == null) {
                addElementFromXPath(editLib, metadataSchema, identification, "",
                        new Element(EditLib.SpecialUpdateTags.ADD).
                                addContent(new Element("containsOperations", SRV).addContent(opEl)));
            }
        }
    }

    private void updateServiceType(EditLib editLib, MetadataSchema metadataSchema, Element identification, JSONObject identificationJson) {

        final String serviceType = identificationJson.optString(JSON_IDENTIFICATION_SERVICETYPE);
        if (!Strings.isNullOrEmpty(serviceType)) {

            @SuppressWarnings("unchecked")
            final Element serviceTypeEl = identification.getChild(SERVICE_TYPE_TAG_NAME, SRV);

            if (serviceTypeEl != null) {
                final Element name = serviceTypeEl.getChild("LocalName", GCO);
                name.setText(serviceType);
            } else {
                final Element element = new Element(SERVICE_TYPE_TAG_NAME, SRV).addContent(new Element("LocalName", GCO).setText(serviceType));
                AddElemValue value = new AddElemValue(new Element(EditLib.SpecialUpdateTags.REPLACE).addContent(element));
                editLib.addElementOrFragmentFromXpath(identification.getParentElement(), metadataSchema, identification.getQualifiedName(), value, true);
            }
        }
    }

    private void updateTopicCategory(EditLib editLib, MetadataSchema metadataSchema, Element identification, JSONObject identificationJson) throws Exception {

        boolean addTopicCategory = false;
        JSONArray categories = identificationJson.optJSONArray(JSON_IDENTIFICATION_TOPIC_CATEGORIES);
        if (categories == null) {
            categories = new JSONArray();
        }
        for (int i = 0; !addTopicCategory && i < categories.length(); i++) {
            String category = categories.getString(i);
            if (!category.trim().isEmpty()) {
                addTopicCategory = true;
            }
        }

        if (!addTopicCategory) {
            return;
        }

        int insertIndex = findIndexToAddElements(editLib, identification, metadataSchema, "gmd:topicCategory").one();

        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                String category = categories.getString(i);
                final Element element = new Element("topicCategory", GMD).addContent(new Element("MD_TopicCategoryCode",
                        GMD).setText(category));

                identification.addContent(insertIndex + i, element);
            }
        }
    }

    private void updateKeywords(ServiceContext context, EditLib editLib, MetadataSchema metadataSchema, Element identification,
                                JSONObject identificationJson) throws Exception {
        HrefBuilder hrefBuilder = new KeywordHrefBuilder();

        updateSharedObject(context, editLib, metadataSchema, identification, identificationJson, JSON_IDENTIFICATION_KEYWORDS,
                JSON_IDENTIFICATION_KEYWORDS, GMD, hrefBuilder);
    }
    private void updateExtent(ServiceContext context, EditLib editLib, MetadataSchema metadataSchema, Element identification,
                              JSONObject identificationJson) throws Exception {
        HrefBuilder hrefBuilder = new ExtentHrefBuilder();

        Namespace namespace;
        if (identificationJson.optString(JSON_IDENTIFICATION_TYPE, "data").equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
            namespace = GMD;
        } else {
            namespace = SRV;
        }
        updateSharedObject(context, editLib, metadataSchema, identification, identificationJson, JSON_IDENTIFICATION_EXTENTS,
                "extent", namespace, hrefBuilder);
    }

    private void updateSharedObject(ServiceContext context, EditLib editLib, MetadataSchema metadataSchema, Element identification,
                                    JSONObject identificationJson, String jsonKey, String tagName, Namespace namespace, HrefBuilder hrefBuilder) throws Exception {
        final JSONArray jsonObjects = identificationJson.optJSONArray(jsonKey);

        if (jsonObjects == null) {
            return;
        }

        Set<String> hrefsToKeep = Sets.newHashSet();
        Map<String, Element> hrefsToAdd = Maps.newLinkedHashMap();

        final String identType = identificationJson.getString(JSON_IDENTIFICATION_TYPE);
        for (int i = 0; i < jsonObjects.length(); i++) {
            JSONObject jsonObject = jsonObjects.getJSONObject(i);

            final String href = hrefBuilder.createHref(jsonObject, identification, identType);
            if (href != null) {
                hrefsToKeep.add(href);
                hrefsToAdd.put(href, null);
            }
        }

        @SuppressWarnings("unchecked")
        final List<Element> elementList = Lists.newArrayList(identification.getChildren(tagName, namespace));

        int addIndex;
        if (elementList.isEmpty()) {
            final Element element = new Element(tagName, namespace);
            addElementFromXPath(editLib, metadataSchema, identification,  namespace.getPrefix() + ":" + tagName, element);
            addIndex = identification.indexOf(element);
            element.detach();
        } else {
            addIndex = identification.indexOf(elementList.get(0));
            Iterator<Element> iter = elementList.iterator();
            while (iter.hasNext()) {
                Element sharedObjectEl = iter.next();
                String href = sharedObjectEl.getAttributeValue("href", XLINK);

                if (hrefsToKeep.contains(href)) {
                    hrefsToAdd.put(href, sharedObjectEl);
                }

                sharedObjectEl.detach();
            }
        }

        for (Map.Entry<String, Element> entry : hrefsToAdd.entrySet()) {
            String href = entry.getKey();
            final Element element;
            if (entry.getValue() == null) {
                element = new Element(tagName, namespace).
                        setAttribute("href", href, XLINK);
                try {
                    final Element child = resolveXlink(context, href);
                    element.addContent(child);
                } catch (Throwable t) {
                    Log.error(Geonet.EDITOR, "Error resolving xlink: " + href);
                }
                entry.setValue(element);
            }
        }

        identification.addContent(addIndex, hrefsToAdd.values());
    }

    private void updateDate(EditLib editLib, MetadataSchema metadataSchema, Element metadata, String xpath, JSONObject identificationJson) throws JSONException {
        final JSONObject dateJSONObj = identificationJson.optJSONObject(JSON_DATE);
        if (dateJSONObj == null) {
            return;
        }

        final String jsonDate = dateJSONObj.optString(JSON_DATE);
        final String jsonDateType = dateJSONObj.optString(JSON_DATE_TYPE, "creation");

        if (jsonDate != null) {
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
                                    createCodeListEl("CI_DateTypeCode", GMD,
                                            "http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode", jsonDateType)
                            )
                    ))
            );
            addElementFromXPath(editLib, metadataSchema, metadata, xpath, date);
        }
    }

    @VisibleForTesting
    Element getIdentification(EditLib editLib, Element metadata, MetadataSchema metadataSchema,
                                      JSONObject identificationJson) throws Exception {
        Element identificationInfo = metadata.getChild("identificationInfo", Geonet.Namespaces.GMD);
        if (identificationInfo == null) {
            identificationInfo = editLib.addElement(metadataSchema.getName(), metadata, "gmd:identificationInfo");
        }

        final String requiredInfoTagName;
        final String isoType;
        final String jsonType = identificationJson.optString(JSON_IDENTIFICATION_TYPE, JSON_IDENTIFICATION_TYPE_DATA_VALUE);
        if (jsonType.equalsIgnoreCase(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
            requiredInfoTagName = "CHE_MD_DataIdentification";
            isoType = "gmd:MD_DataIdentification";
        } else {
            requiredInfoTagName = "CHE_SV_ServiceIdentification";
            isoType = "srv:SV_ServiceIdentification";
        }

        Element finalInfo;
        if (!identificationInfo.getChildren().isEmpty()) {
            finalInfo = (Element) identificationInfo.getChildren().get(0);
        } else {
            finalInfo = new Element(requiredInfoTagName, CHE_NAMESPACE).setAttribute("isoType", isoType, GCO);
            identificationInfo.addContent(finalInfo);
        }

        if (!finalInfo.getName().equals(requiredInfoTagName)) {
            finalInfo.detach();
            Element oldInfo = finalInfo;
            finalInfo = new Element(requiredInfoTagName, CHE_NAMESPACE);
            identificationInfo.addContent(finalInfo);

            final MetadataType elementType = metadataSchema.getTypeInfo("che:" + requiredInfoTagName + "_Type");
            final List<String> elementList = elementType.getElementList();

            if (elementList.isEmpty()) {
                throw new Error("A bug was found when looking up the identification info type");
            }
            final List elementsToCopy = Lists.newArrayList(oldInfo.getChildren());

            for (Object o : elementsToCopy) {
                Element child = (Element) o;

                if (child.getName().equals("extent")) {
                    if (jsonType.equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
                        child.setNamespace(GMD);
                    } else {
                        child.setNamespace(SRV);
                    }
                }

                if (elementList.contains(child.getQualifiedName())) {
                    child.detach();
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

        JSONArray languages = jsonObject.optJSONArray(JSON_OTHER_LANGUAGES);
        if (languages != null) {
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
        if (newValue != null) {
            Element langEl = new Element(EL_CHARACTER_STRING, GCO).setText(newValue);
            addElementFromXPath(editLib, metadataSchema, metadata, xpath, langEl);
        }
    }

    private void updateCharset(EditLib editLib, Element metadata, MetadataSchema metadataSchema,
                               JSONObject jsonObject) throws JSONException {
        final String charset = jsonObject.optString(JSON_CHARACTER_SET);
        if (!Strings.isNullOrEmpty(charset)) {
            Element codeElement = new Element("MD_CharacterSetCode", GMD).setAttribute(ATT_CODE_LIST_VALUE, charset).
                    setAttribute(ATT_CODE_LIST, "http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode");
            addElementFromXPath(editLib, metadataSchema, metadata, "gmd:characterSet", codeElement);
        }
    }

    private void updateHierarchyLevel(EditLib editLib, Element metadata, MetadataSchema metadataSchema,
                                      JSONObject jsonObject) throws JSONException {
        final String hierarchyLevel = jsonObject.optString(JSON_HIERARCHY_LEVEL);
        if (!Strings.isNullOrEmpty(hierarchyLevel)) {
            Element hierarchyLevelEl = new Element("MD_ScopeCode", GMD).setAttribute(ATT_CODE_LIST_VALUE, hierarchyLevel).
                    setAttribute(ATT_CODE_LIST, "http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode");
            addElementFromXPath(editLib, metadataSchema, metadata, "gmd:hierarchyLevel", hierarchyLevelEl);

        }
    }

    private void updateContact(String mainLang, EditLib editLib, MetadataSchema metadataSchema, Element metadata, String xpath,
                               JSONArray contacts, ServiceContext context) throws Exception {
        if (contacts == null) {
            return;
        }

        JSONArray nonEmptyContacts = new JSONArray();
        for (int i = 0; i < contacts.length(); i++) {
            JSONObject contact = contacts.getJSONObject(i);
            final Iterator keys = contact.keys();
            while (keys.hasNext()) {
                String next = (String) keys.next();
                if (!next.equals(Save.JSON_VALIDATED)) {
                    final Object property = contact.get(next);
                    if (property instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) property;
                        if (jsonObject.length() > 0) {
                            nonEmptyContacts.put(contact);
                            break;
                        }
                    } else if (property instanceof String) {
                        String s = (String) property;
                        if (!s.trim().isEmpty()) {
                            nonEmptyContacts.put(contact);
                            break;
                        }

                    } else if (property != null) {
                        nonEmptyContacts.put(contact);
                        break;
                    }
                }
            }
        }

        if (nonEmptyContacts.length() == 0) {
            return;
        }

        String tagName = xpath;
        if (xpath.contains("/")) {
            tagName = xpath.substring(xpath.indexOf("/") + 1);
        }
        Pair<Integer, Element> result = findIndexToAddElements(editLib, metadata, metadataSchema, tagName);

        int insertIndex = result.one();

        for (int i = 0; i < nonEmptyContacts.length(); i++) {
            JSONObject contact = nonEmptyContacts.getJSONObject(i);
            String contactId = contact.optString(JSON_CONTACT_ID, null);
            boolean validated = contact.getBoolean(JSON_VALIDATED);
            String role = contact.optString(JSON_CONTACT_ROLE, "pointOfContact");

            String xlinkHref = "local://xml.user.get?id=" + contactId + "&amp;schema=iso19139.che&amp;role=" + role;

            final Element contactEl;
            if (validated && !Strings.isNullOrEmpty(contactId)) {
                contactEl = new Element(tagName.split(":", 2)[1], GMD).
                        setAttribute("href", xlinkHref, XLINK).
                        setAttribute("show", "embed", XLINK);

                final Element sharedObject = resolveXlink(context, xlinkHref);
                contactEl.addContent(sharedObject);
            } else {
                contactEl = new Element(tagName.split(":", 2)[1], GMD);
                if (!Strings.isNullOrEmpty(contactId)) {
                    contactEl.setAttribute("href", xlinkHref, XLINK).
                            setAttribute("show", "embed", XLINK).
                            setAttribute("role", ReusableObjManager.NON_VALID_ROLE, XLINK);

                    Element sharedObject = resolveXlink(context, xlinkHref);
                    if (sharedObject == null) {
                        contactEl.removeAttribute("href", XLINK);
                        contactEl.removeAttribute("show", XLINK);
                        contactEl.removeAttribute("role", XLINK);
                        sharedObject = Xml.selectElement(metadata, "*[@xlink:href = '" + xlinkHref + "']", NS);
                    }
                    if (sharedObject != null) {
                        contactEl.addContent(sharedObject);
                    }
                }

                Element emailEl = new Element("electronicMailAddress", GMD).addContent(
                        new Element("CharacterString", GCO).setText(contact.getString(JSON_CONTACT_EMAIL))
                );

                Element firstNameEl = new Element("individualFirstName", CHE_NAMESPACE).addContent(
                        new Element(EL_CHARACTER_STRING, GCO).setText(contact.getString(JSON_CONTACT_FIRST_NAME)));
                Element lastNameEl = new Element("individualLastName", CHE_NAMESPACE).addContent(
                        new Element(EL_CHARACTER_STRING, GCO).setText(contact.getString(JSON_CONTACT_LAST_NAME)));
                Element roleEl = new Element("role", GMD).addContent(
                        new Element("CI_RoleCode", GMD).
                                setAttribute(ATT_CODE_LIST_VALUE, role).
                                setAttribute(ATT_CODE_LIST, "http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode")
                );

                updateTranslatedInstance(mainLang, editLib, metadataSchema, contactEl, contact.getJSONObject(JSON_CONTACT_ORG_NAME),
                        "che:CHE_CI_ResponsibleParty/gmd:organisationName", "organisationName", GMD);
                addElementFromXPath(editLib, metadataSchema, contactEl, "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address", emailEl);

                addElementFromXPath(editLib, metadataSchema, contactEl, "che:CHE_CI_ResponsibleParty/che:individualFirstName", firstNameEl);

                addElementFromXPath(editLib, metadataSchema, contactEl, "che:CHE_CI_ResponsibleParty/che:individualLastName", lastNameEl);
                addElementFromXPath(editLib, metadataSchema, contactEl, "che:CHE_CI_ResponsibleParty/gmd:role", roleEl);
            }

            result.two().addContent(insertIndex + i, contactEl);
        }
    }

    private void updateTranslatedInstance(String mainLang, EditLib editLib, MetadataSchema metadataSchema, Element metadata, JSONObject translationJson,
                                          String xpath, String tagName, Namespace namespace) throws JSONException {
        if (translationJson != null) {
            Element translatedEl = createTranslatedInstance(mainLang, translationJson, tagName, namespace);
            addElementFromXPath(editLib, metadataSchema, metadata, xpath, translatedEl);
        }
    }
    private Element createTranslatedInstance(String mainLang, JSONObject translationJson,
                                             String tagName, Namespace namespace) throws JSONException {
        IsoLanguagesMapper instance = getIsoLanguagesMapper();
        List<Element> translations = Lists.newArrayList();
        final Iterator keys = translationJson != null ? translationJson.keys() : Iterators.emptyIterator();
        String mainTranslation = null;
        while (keys.hasNext()) {
            String threeLetterLangCode = (String) keys.next();
            if (threeLetterLangCode.equals("undefined")) {
                // the language was deleted
                continue;
            }
            String translation = translationJson.getString(threeLetterLangCode);
            if (threeLetterLangCode.equals(mainLang)) {
                mainTranslation = translation;
            }
            translations.add(new Element("textGroup", GMD).
                    addContent(
                            new Element("LocalisedCharacterString", GMD).
                                    setAttribute("locale", "#" + instance.iso639_2_to_iso639_1(threeLetterLangCode).toUpperCase()).
                                    setText(translation)
                    ));

        }
        final Element element = new Element(tagName, namespace).addContent(
                new Element("PT_FreeText", GMD).addContent(translations)
        );
        if (mainLang != null) {
            element.addContent(0,
                    new Element("CharacterString", GCO).setText(mainTranslation)
            );
        }
        return element;
    }

    @VisibleForTesting
    protected void saveMetadata(ServiceContext context, AjaxEditUtils ajaxEditUtils, String id, DataManager dataManager, Element metadata,
                                boolean finished, boolean commitChange) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        dataManager.updateMetadata(context, dbms, id, metadata, false, true, true, context.getLanguage(), null, true, true);
        context.getUserSession().setProperty(Geonet.Session.METADATA_EDITING + id, metadata);

        if (finished) {
            final UserSession session = context.getUserSession();
            ajaxEditUtils.removeMetadataEmbedded(session, id);

            dataManager.endEditingSession(id, session);
        } else if (commitChange) {
            dataManager.startEditingSession(context, id);
        }

    }

    @VisibleForTesting
    protected Element getMetadata(ServiceContext context, EditLib lib, String id, AjaxEditUtils ajaxEditUtils) throws Exception {
        Element metadata = (Element) context.getUserSession().getProperty(Geonet.Session.METADATA_EDITING + id);
        if (metadata == null) {
            metadata = ajaxEditUtils.getMetadataEmbedded(context, id, true, false);
        }

        lib.removeEditingInfo(metadata);
        lib.enumerateTree(metadata);
        return metadata;
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

    @VisibleForTesting
    protected Element getResponse(Element params, ServiceContext context, String id, EditLib editLib, Element metadata) throws Exception {
        final boolean finished = Util.getParam(params, PARAM_FINISH, false);
        final boolean validate = Util.getParam(params, PARAM_VALIDATE, true);
        if (finished || !validate) {
            if (finished) {
                context.getUserSession().removeProperty(Geonet.Session.METADATA_EDITING + id);
            }
            return new Element("ok").setText("ok");
        } else {
            return new GetEditModel().exec(params, context);
        }
    }

    private interface HrefBuilder {
        @Nullable
        public String createHref(JSONObject jsonObject, Element elem, String identType) throws Exception;
    }

    private static class KeywordHrefBuilder implements HrefBuilder {
        @Override
        public String createHref(JSONObject jsonObject, Element elem, String identType) throws Exception {

            String hrefTemplate = "local://che.keyword.get?thesaurus=%s&amp;id=%s&amp;locales=fr,en,de,it";

            String thesaurus = jsonObject.optString(JSON_IDENTIFICATION_KEYWORD_THESAURUS);

            if (thesaurus == null) {
                if (identType.equals(JSON_IDENTIFICATION_TYPE_DATA_VALUE)) {
                    thesaurus = "external.theme.inspire-theme";
                } else {
                    thesaurus = "external.theme.inspire-service-taxonomy";
                }
            }

            final String codeString = jsonObject.optString("code");
            if (Strings.isNullOrEmpty(codeString)) {
                return null;
            }

            final String code = URLEncoder.encode(codeString, "UTF-8");
            return String.format(hrefTemplate, thesaurus, code);
        }
    }

    static class ExtentHrefBuilder implements HrefBuilder {
        /**
         * Don't forget to update {@link org.fao.geonet.services.metadata.inspire.GetEditModel#extentJsonEncoder}
         */
        Map<String, String> typenameMapping = Maps.newHashMap();

        {
            typenameMapping.put("kantone", "kantoneBB");
            typenameMapping.put("gemeinden", "gemeindenBB");
            typenameMapping.put("country", "countries");
            typenameMapping.put("xlinks", "xlinks");
            typenameMapping.put("non_validated", "non_validated");
        }

        @Override
        public String createHref(JSONObject jsonObject, Element elem, String identType) throws Exception {

            String hrefTemplate = "local://xml.extent.get?id=%s&amp;wfs=default&amp;typename=gn:%s&amp;format=gmd_complete&amp;extentTypeCode=true";


            final String geomString = jsonObject.optString("geom");
            if (Strings.isNullOrEmpty(geomString)) {
                return null;
            }
            final String[] geom = geomString.split(":");
            String id = geom[1];
            String typename = typenameMapping.get(geom[0]);

            return String.format(hrefTemplate, id, typename);
        }
    }
}
