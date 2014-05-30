package org.fao.geonet.services.metadata.inspire;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GEONET;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;
import static org.fao.geonet.services.metadata.inspire.Save.NS;

/**
 * @author Jesse on 5/17/2014.
 */
public class GetEditModel implements Service {
    private final XmlCacheManager cacheManager = new XmlCacheManager();
    private Function<CodeListEntry, CodeListEntry> topicCategoryGrouper = new Function<CodeListEntry, CodeListEntry>() {
        Map<String, String> topicCategoryGrouping = Maps.newHashMap();

        {
            topicCategoryGrouping.put("A", "A Base Maps, Land Cover, Aerial and Satellite Imagery");
            topicCategoryGrouping.put("E", "E Spatial Planning, Cadastre");
            topicCategoryGrouping.put("F", "F Geology, Soils, Natural Hazards");
            topicCategoryGrouping.put("L", "L Environmental and Nature Protection");
            topicCategoryGrouping.put("Q", "Q Utilities, Supply, Disposal, Communication");
        }
        @Nullable
        @Override
        public CodeListEntry apply(CodeListEntry input) {
            if (topicCategoryGrouping.containsValue(input.title)) {
                return null;
            }
            for (Map.Entry<String, String> entry : topicCategoryGrouping.entrySet()) {
                if (input.title.startsWith(entry.getKey())) {
                    input.group = entry.getValue();
                    return input;
                }
            }
            input.group = input.title;
            return input;
        }
    };
    private Function<CodeListEntry, CodeListEntry> hierarchyLevelGrouper = new Function<CodeListEntry, CodeListEntry>() {
        final List<String> legalOptions = Lists.newArrayList("dataset", "series", "service");
        @Nullable
        @Override
        public CodeListEntry apply(@Nullable CodeListEntry input) {
            if (input != null && legalOptions.contains(input.name)) {
                return input;
            }
            return null;
        }
    };

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        boolean pretty = Util.getParam(params, "pretty", false);
        Element metadataEl = getMetadata(params, context, getAjaxEditUtils(params, context));

        JSONObject metadataJson = new JSONObject();
        addCodeLists(context, metadataJson);
        metadataJson.append("metadataTypeOptions", "data");
        metadataJson.append("metadataTypeOptions", "service");
        processMetadata(metadataEl, metadataJson);
        Element identificationInfo = processIdentificationInfo(metadataEl, metadataJson);
        processConstraints(identificationInfo, metadataJson);
        processConformity(metadataEl, metadataJson);

        final String jsonString;
        if (pretty) {
            jsonString = metadataJson.toString(2);
        } else {
            jsonString = metadataJson.toString();
        }
        return new Element("data").setText(jsonString);
    }

    private void processConformity(Element metadataEl, JSONObject metadataJson) throws JSONException, JDOMException {

        JSONObject conformityJson = new JSONObject();
        String mainLanguage = metadataJson.getString(Save.JSON_LANGUAGE);

        Element conformanceResult = Xml.selectElement(metadataEl, "gmd:dataQualityInfo/*/gmd:report//gmd:DQ_ConformanceResult" , NS);
        addValue(conformanceResult, conformityJson, Save.JSON_CONFORMITY_RESULT_REF, "geonet:element/@ref");

        addTranslatedElement(mainLanguage, conformanceResult, getIsoLanguagesMapper(), conformityJson, Save.JSON_TITLE,
                "gmd:specification/gmd:CI_Citation/gmd:title");

        JSONObject date = new JSONObject();
        date.put(Save.JSON_DATE, "2010-12-08");
        date.put(Save.JSON_DATE_TYPE, "publication");
        date.put(Save.JSON_DATE_TAG_NAME, "gco:Date");
        conformityJson.put(Save.JSON_DATE, date);

        addValue(conformanceResult, conformityJson, Save.JSON_CONFORMITY_PASS, "gmd:pass/gco:Boolean");
        addValue(conformanceResult, conformityJson, Save.JSON_CONFORMITY_EXPLANATION, "gmd:explanation/gco:CharacterString");

        JSONObject lineageJson = new JSONObject();
        conformityJson.put(Save.JSON_CONFORMITY_LINEAGE, lineageJson);

        Element lineageEl = Xml.selectElement(metadataEl, "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage", NS);
        addTranslatedElement(mainLanguage, lineageEl, getIsoLanguagesMapper(), lineageJson, Save.JSON_CONFORMITY_LINEAGE_STATEMENT, "gmd:statement");
        addValue(lineageEl, lineageJson, Params.REF, "geonet:element/@ref");

        metadataJson.put(Save.JSON_CONFORMITY, conformityJson);

    }

    @VisibleForTesting
    protected void addCodeLists(ServiceContext context, JSONObject metadataJson) throws JDOMException, IOException, JSONException {
        GeonetContext geonetContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        final MetadataSchema iso19139CHESchema = geonetContext.getSchemamanager().getSchema("iso19139.che");
        final MetadataSchema iso19139Schema = geonetContext.getSchemamanager().getSchema("iso19139");
        final Element codelists = cacheManager.get(context, true, iso19139Schema.getSchemaDir() + "/loc", "codelists.xml",
                context.getLanguage(), "ger", true);
        final Element cheCodelistExtensions = cacheManager.get(context, true, iso19139CHESchema.getSchemaDir() + "/loc", "codelists.xml",
                context.getLanguage(), "ger", true);
        final Element labels = cacheManager.get(context, true, iso19139Schema.getSchemaDir() + "/loc", "labels.xml",
                context.getLanguage(), "ger", true);
        final Element labelExtensions = cacheManager.get(context, true, iso19139CHESchema.getSchemaDir() + "/loc", "labels.xml",
                context.getLanguage(), "ger", true);


        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:CI_DateTypeCode", "dateTypeOptions", null);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:CI_RoleCode", "roleOptions", null);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_ScopeCode", "hierarchyLevelOptions",
                hierarchyLevelGrouper);

        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_TopicCategoryCode", "topicCategoryOptions",
                topicCategoryGrouper);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_RestrictionCode", "constraintOptions", null);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_ScopeCode", "scopeCodeOptions", null);
        addCodeListOptionsFromLabelsHelper(metadataJson, labels, labelExtensions, "srv:serviceType", "serviceTypeOptions");

    }

    private void addCodeListOptions(JSONObject metadataJson, Element codelists, Element cheCodelistsExtensions,
                                    String codelistName, String jsonKey, Function<CodeListEntry, CodeListEntry> grouper) throws JDOMException, JSONException {

        Set<CodeListEntry> collector = new TreeSet<CodeListEntry>();
        final String xpath = "codelist[@name = '" + codelistName + "']/entry";
        @SuppressWarnings("unchecked")
        List<Element> elems = (List<Element>) Xml.selectNodes(codelists, xpath);
        for (Element elem : elems) {
            CodeListEntry entry = new CodeListEntry(elem.getChildTextTrim("code"), elem.getChildTextTrim("label"),
                    elem.getChildTextTrim("description"));
            if (grouper != null) {
                entry = grouper.apply(entry);
            }
            if (entry != null) {
                collector.add(entry);
            }
        }

        @SuppressWarnings("unchecked")
        List<Element> cheElems = (List<Element>) Xml.selectNodes(cheCodelistsExtensions, xpath);
        for (Element elem : cheElems) {
            CodeListEntry entry = new CodeListEntry(elem.getChildTextTrim("code"), elem.getChildTextTrim("label"),
                    elem.getChildTextTrim("description"));

            if (grouper != null) {
                entry = grouper.apply(entry);
            }
            if (entry != null) {
                collector.add(entry);
            }
        }

        addOptions(metadataJson, jsonKey, collector);
    }

    private void addCodeListOptionsFromLabelsHelper(JSONObject metadataJson, Element codelists, Element cheCodelistsExtensions,
                                    String codelistName, String jsonKey) throws JDOMException, JSONException {
        Set<CodeListEntry> collector = new LinkedHashSet<CodeListEntry>();
        final String xpath = "element[@name = '" + codelistName + "']/helper/option";
        @SuppressWarnings("unchecked")
        List<Element> elems = (List<Element>) Xml.selectNodes(codelists, xpath);
        for (Element elem : elems) {
            final String value = elem.getTextTrim();
            final CodeListEntry entry = new CodeListEntry(elem.getAttributeValue("value"), value, value);
            collector.add(entry);
        }

        @SuppressWarnings("unchecked")
        List<Element> cheElems = (List<Element>) Xml.selectNodes(cheCodelistsExtensions, xpath);
        for (Element elem : cheElems) {
            final String value = elem.getTextTrim();
            final CodeListEntry entry = new CodeListEntry(elem.getAttributeValue("value"), value, value);
            collector.add(entry);
        }

        addOptions(metadataJson, jsonKey, collector);
    }

    private void addOptions(JSONObject metadataJson, String jsonKey, Set<CodeListEntry> dateTypeOptions) throws JSONException {
        for (CodeListEntry entry : dateTypeOptions) {
            JSONObject obj = new JSONObject();
            obj.put("name", entry.name);
            obj.put("title", entry.title);
            obj.put("desc", entry.description);
            if (entry.group != null) {
                obj.put("group", entry.group);
            }
            metadataJson.append(jsonKey, obj);
        }
    }

    private void processConstraints(Element identificationInfo, JSONObject metadataJson) throws Exception {
        String mainLanguage = metadataJson.getString(Save.JSON_LANGUAGE);

        JSONObject constraintsJson = new JSONObject();
        metadataJson.put(Save.JSON_CONSTRAINTS, constraintsJson);

        @SuppressWarnings("unchecked")
        List<Element> legalConstraints = (List<Element>) Xml.selectNodes(identificationInfo,
                "gmd:resourceConstraints/*[name() = 'gmd:MD_LegalConstraints' " +
                "or @gco:isoType = 'gmd:MD_LegalConstraints']", NS);

        if (legalConstraints.isEmpty()) {
            constraintsJson.put(Save.JSON_CONSTRAINTS_LEGAL, new JSONArray());
        }

        for (Element legalConstraint : legalConstraints) {
            JSONObject legalJson = new JSONObject();
            processLegalConstraint(mainLanguage, legalConstraint, legalJson);
            constraintsJson.append(Save.JSON_CONSTRAINTS_LEGAL, legalJson);
        }
        @SuppressWarnings("unchecked")
        List<Element> genericConstraints = (List<Element>) Xml.selectNodes(identificationInfo,
                "gmd:resourceConstraints/gmd:MD_Constraints", NS);

        if (genericConstraints.isEmpty()) {
            constraintsJson.put(Save.JSON_CONSTRAINTS_GENERIC, new JSONArray());
        }
        for (Element genericConstraint : genericConstraints) {
            JSONObject json = new JSONObject();
            addRef(genericConstraint, json);
            addArray(mainLanguage, genericConstraint, getIsoLanguagesMapper(), json, "gmd:useLimitation", Save.JSON_CONSTRAINTS_USE_LIMITATIONS,
                    translatedElemEncoder);
            constraintsJson.append(Save.JSON_CONSTRAINTS_GENERIC, json);
        }
        @SuppressWarnings("unchecked")
        List<Element> securityConstraints = (List<Element>) Xml.selectNodes(identificationInfo,
                "gmd:resourceConstraints/gmd:MD_SecurityConstraints", NS);
        if (securityConstraints.isEmpty()) {
            constraintsJson.put(Save.JSON_CONSTRAINTS_SECURITY, new JSONArray());
        }

        for (Element securityConstraint : securityConstraints) {
            JSONObject json = new JSONObject();
            addRef(securityConstraint, json);
            addArray(mainLanguage, securityConstraint, getIsoLanguagesMapper(), json, "gmd:useLimitation", Save.JSON_CONSTRAINTS_USE_LIMITATIONS,
                    translatedElemEncoder);
            addArray(mainLanguage, securityConstraint, getIsoLanguagesMapper(), json, "gmd:classification/gmd:MD_ClassificationCode",
                    Save.JSON_CONSTRAINTS_USE_LIMITATIONS, codeListJsonEncoder);
            constraintsJson.append(Save.JSON_CONSTRAINTS_SECURITY, json);
        }

    }

    private void processLegalConstraint(String mainLanguage, Element constraint, JSONObject legalJson) throws Exception {
        addRef(constraint, legalJson);
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:useLimitation", Save.JSON_CONSTRAINTS_USE_LIMITATIONS,
                translatedElemEncoder, new JSONArray());
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:accessConstraints/gmd:MD_RestrictionCode",
                Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS, codeListJsonEncoder);
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:useConstraints/gmd:MD_RestrictionCode",
                Save.JSON_CONSTRAINTS_USE_CONSTRAINTS, codeListJsonEncoder);
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:otherConstraints", Save.JSON_CONSTRAINTS_OTHER_CONSTRAINTS,
                translatedElemEncoder, new JSONArray());
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson,
                "che:legislationConstraints/che:CHE_MD_Legislation",
                Save.JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS, legislationConstraintsJsonEncoder, new JSONArray());

    }

    private void addRef(Element constraint, JSONObject json) throws JSONException {
        String ref = constraint.getChild("element", GEONET).getAttributeValue(Params.REF);
        json.put(Params.REF, ref);
    }

    private Element processIdentificationInfo(Element metadataEl, JSONObject metadataJson) throws Exception {
        Element identificationInfoEl = Xml.selectElement(metadataEl,
                "gmd:identificationInfo/node()[@gco:isoType = 'gmd:MD_DataIdentification' or " +
                "@gco:isoType = 'srv:SV_ServiceIdentification']", NS);

        if (identificationInfoEl == null) {
            identificationInfoEl = new Element("CHE_MD_DataIdentification", XslUtil.CHE_NAMESPACE).setAttribute("isoType", "gmd:MD_DataIdentification", GCO);
        }

        JSONObject identificationJSON = new JSONObject();
        metadataJson.put(Save.JSON_IDENTIFICATION, identificationJSON);
        boolean isDataType = identificationInfoEl.getName().equals("gmd:MD_DataIdentification") ||
                             (identificationInfoEl.getAttributeValue("isoType", GCO)!= null &&
                              identificationInfoEl.getAttributeValue("isoType", GCO).equals("gmd:MD_DataIdentification"));
        identificationJSON.put(Save.JSON_IDENTIFICATION_TYPE, isDataType ? "data" : "service");

        String mainLanguage = metadataJson.getString(Save.JSON_LANGUAGE);
        addTranslatedElement(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, Save.JSON_TITLE,
                "gmd:citation/gmd:CI_Citation/gmd:title");

        addValue(identificationInfoEl, identificationJSON, Save.JSON_IDENTIFICATION_IDENTIFIER,
                "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");

        addDateElement(identificationInfoEl, identificationJSON, "gmd:citation/gmd:CI_Citation/gmd:date");

        addTranslatedElement(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, Save.JSON_IDENTIFICATION_ABSTRACT,
                "gmd:abstract");

        addValue(identificationInfoEl, identificationJSON, Save.JSON_LANGUAGE, "gmd:language");
        addValue(identificationInfoEl, identificationJSON, Save.JSON_IDENTIFICATION_SERVICETYPE, "srv:serviceType");
        addArray(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:topicCategory/gmd:MD_TopicCategoryCode",
                Save.JSON_IDENTIFICATION_TOPIC_CATEGORIES, valueJsonEncoder);

        addArray(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:pointOfContact",
                Save.JSON_IDENTIFICATION_POINT_OF_CONTACT, contactJsonEncoder);
        addArray(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:descriptiveKeywords ",
                Save.JSON_IDENTIFICATION_KEYWORDS, keywordJsonEncoder);
        addArray(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:extent|srv:extent",
                Save.JSON_IDENTIFICATION_EXTENTS, extentJsonEncoder);

        return identificationInfoEl;
    }

    private void addDateElement(Element identificationInfoEl, JSONObject identificationJSON, String xpathToDate) throws JSONException, JDOMException {
        JSONObject dateObject = new JSONObject();
        addValue(identificationInfoEl, dateObject, Save.JSON_DATE,
                xpathToDate + "/gmd:CI_Date/gmd:date/*");

        final Element dateElement = Xml.selectElement(identificationInfoEl,
                xpathToDate + "/gmd:CI_Date/gmd:date/*", NS);
        dateObject.put(Save.JSON_DATE_TAG_NAME, dateElement == null ? "" : dateElement.getQualifiedName());
        addValue(identificationInfoEl, dateObject, Save.JSON_DATE_TYPE,
                xpathToDate + "/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue");
        identificationJSON.put(Save.JSON_DATE, dateObject);
    }

    protected void processMetadata(Element metadataEl, JSONObject metadataJson) throws Exception {
        addValue(metadataEl, metadataJson, Save.JSON_LANGUAGE, "gmd:language/gco:CharacterString/text()");

        String mainLanguage = metadataJson.getString(Save.JSON_LANGUAGE);

        addValue(metadataEl, metadataJson, Save.JSON_CHARACTER_SET, "gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue", "utf-8");
        addValue(metadataEl, metadataJson, Save.JSON_HIERARCHY_LEVEL, "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
        addValue(metadataEl, metadataJson, Save.JSON_HIERARCHY_LEVEL_NAME, "gmd:hierarchyLevelName/gco:CharacterString/text()");
        addArray(mainLanguage, metadataEl, getIsoLanguagesMapper(), metadataJson, "gmd:contact", Save.JSON_CONTACT, contactJsonEncoder);
        JSONArray def = new JSONArray();
        def.put("ger").put("fre").put("ita").put("eng").put("roh");
        addArray(mainLanguage, metadataEl, getIsoLanguagesMapper(), metadataJson,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode",
                Save.JSON_OTHER_LANGUAGES, codeListJsonEncoder, def);
    }

    private static void addValue(Element metadataEl, JSONObject metadataJson, String jsonKey, String xpath) throws JSONException, JDOMException {
        addValue(metadataEl, metadataJson, jsonKey, xpath, null);
    }
    private static void addValue(Element metadataEl, JSONObject metadataJson, String jsonKey, String xpath, String defaultVal) throws JSONException, JDOMException {

        String value = Xml.selectString(metadataEl, xpath, NS).trim();
        if (value.isEmpty() && defaultVal != null) {
            value = defaultVal;
        }
        metadataJson.put(jsonKey, value.trim());
    }

    private static void addArray(String mainLanguage, Element metadataEl, IsoLanguagesMapper mapper, JSONObject metadataJson, String xpath, String jsonKey, JsonEncoder encoder)
            throws Exception {
        addArray(mainLanguage, metadataEl, mapper, metadataJson, xpath, jsonKey, encoder, null);
    }
    private static void addArray(String mainLanguage, Element metadataEl, IsoLanguagesMapper mapper, JSONObject metadataJson, String xpath, String jsonKey, JsonEncoder encoder, JSONArray defaultVal)
            throws Exception {

        @SuppressWarnings("unchecked")
        final List<Element> nodes = (List<Element>) Xml.selectNodes(metadataEl, xpath, NS);

        for (Element node : nodes) {
            metadataJson.append(jsonKey, encoder.encode(mainLanguage, node, mapper));
        }

        if (nodes.isEmpty()) {
            if (defaultVal == null) {
                Object def = encoder.getDefault();
                if (def != null) {
                    metadataJson.append(jsonKey, def);
                }
            } else {
                metadataJson.put(jsonKey, defaultVal);
            }
        }
    }

    private static void addTranslatedElement(String mainLanguage, Element metadata, IsoLanguagesMapper mapper, JSONObject json,
                                             String jsonKey, String xpath) throws JDOMException,
            JSONException {
        JSONObject obj = new JSONObject();

        List<?> nodes = Xml.selectNodes(metadata, xpath + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", NS);
        if (nodes.isEmpty()) {
            final String charString = Xml.selectString(metadata, xpath + "/gco:CharacterString", NS);
            if (!Strings.isNullOrEmpty(charString)) {
                obj.put(mainLanguage, charString.trim());
            }
        } else {
            addTranslationElements(mapper, obj, nodes);
        }

        json.put(jsonKey, obj);
    }

    private static void addTranslationElements(IsoLanguagesMapper mapper, JSONObject obj, List<?>nodes)
            throws JSONException {
        for (Object node : nodes) {
            Element el = (Element) node;

            String langCode = el.getAttributeValue("locale").substring(1).toLowerCase();
            final String lang = mapper.iso639_1_to_iso639_2(langCode);

            obj.put(lang, el.getTextTrim());
        }
    }

    @VisibleForTesting
    protected Element getMetadata(Element params, ServiceContext context, AjaxEditUtils ajaxEditUtils) throws Exception {
        String id = Utils.getIdentifierFromParameters(params, context);

        Element metadata = (Element) context.getUserSession().getProperty(Geonet.Session.METADATA_EDITING + id);
        if (metadata == null) {
            metadata = ajaxEditUtils.getMetadataEmbedded(context, id, true, false);
        }
        GeonetContext geonetContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager schemaManager = geonetContext.getSchemamanager();
        EditLib lib = new EditLib(schemaManager);

        lib.removeEditingInfo(metadata);
        lib.enumerateTree(metadata);

        return metadata;
    }

    @VisibleForTesting
    protected IsoLanguagesMapper getIsoLanguagesMapper() {
        return IsoLanguagesMapper.getInstance();
    }

    @VisibleForTesting
    protected AjaxEditUtils getAjaxEditUtils(Element params, ServiceContext context) throws Exception {
        final AjaxEditUtils ajaxEditUtils = new AjaxEditUtils(context);
        ajaxEditUtils.preprocessUpdate(params, context);
        return ajaxEditUtils;
    }

    interface JsonEncoder {
        public Object getDefault() throws JSONException;
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception;
    }

    private static final JsonEncoder contactJsonEncoder = new JsonEncoder() {

        @Override
        public Object getDefault() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(Save.JSON_CONTACT_ID, "");
            json.put(Save.JSON_CONTACT_FIRST_NAME, "");
            json.put(Save.JSON_CONTACT_LAST_NAME, "");
            json.put(Save.JSON_CONTACT_ROLE, "");
            json.put(Save.JSON_CONTACT_EMAIL, "");
            json.put(Save.JSON_VALIDATED, false);
            json.put(Save.JSON_CONTACT_ORG_NAME, new JSONObject());
            return json;
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws JDOMException, JSONException, UnsupportedEncodingException {
            JSONObject json = new JSONObject();

            String id = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(XLink.getHRef(node)), "UTF-8");
            addValue(node, json, Save.JSON_CONTACT_ID, id);
            addValue(node, json, Save.JSON_CONTACT_FIRST_NAME, "che:CHE_CI_ResponsibleParty/che:individualFirstName");
            addValue(node, json, Save.JSON_CONTACT_LAST_NAME, "che:CHE_CI_ResponsibleParty/che:individualLastName");
            addValue(node, json, Save.JSON_CONTACT_ROLE, "che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue");
            addValue(node, json, Save.JSON_CONTACT_EMAIL,
                    "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/" +
                    "che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString/text()");

            addTranslatedElement(mainLanguage, node, mapper, json, Save.JSON_CONTACT_ORG_NAME, "che:CHE_CI_ResponsibleParty/gmd:organisationName");
            boolean validated = true;
            if (node.getAttributeValue("role", XLINK).equals(ReusableObjManager.NON_VALID_ROLE)) {
                validated = false;
            }

            json.put(Save.JSON_VALIDATED, validated);

            return json;
        }
    };
    private static final JsonEncoder keywordJsonEncoder = new JsonEncoder() {

        @Override
        public Object getDefault() throws JSONException {
            JSONObject def = new JSONObject();
            def.put(Save.JSON_IDENTIFICATION_KEYWORD_CODE, "");
            def.put(Save.JSON_IDENTIFICATION_KEYWORD_WORD, new JSONObject());
            return def;
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();

            String code = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(XLink.getHRef(node)), "UTF-8");
            json.put(Save.JSON_IDENTIFICATION_KEYWORD_CODE, code);
            addTranslatedElement(mainLanguage, node, mapper, json, Save.JSON_IDENTIFICATION_KEYWORD_WORD, "gmd:MD_Keywords/gmd:keyword");

            return json;
        }
    };
    private static final JsonEncoder valueJsonEncoder = new JsonEncoder() {

        @Override
        public Object getDefault() throws JSONException {
            return "";
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            return node.getTextTrim();
        }
    };
    private static final JsonEncoder codeListJsonEncoder = new JsonEncoder() {

        @Override
        public Object getDefault() throws JSONException {
            return "";
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            return node.getAttributeValue(Save.ATT_CODE_LIST_VALUE);
        }
    };
    private static final JsonEncoder translatedElemEncoder = new JsonEncoder() {

        @Override
        public Object getDefault() throws JSONException {
            return new JSONObject();
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();
            final List<?> nodes = Xml.selectNodes(node, "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", NS);
            addTranslationElements(mapper, json, nodes);
            return json;
        }
    };

    private static final JsonEncoder legislationConstraintsJsonEncoder = new JsonEncoder() {

        @Override
        public Object getDefault() throws JSONException {
            return null;
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();
            final List<?> nodes = Xml.selectNodes(node,
                    "che:title/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", NS);
            addTranslationElements(mapper, json, nodes);
            return json;
        }
    };

    private static final JsonEncoder extentJsonEncoder = new JsonEncoder() {
        final Pattern typenamePattern = Pattern.compile("typename=([^&]+)");
        Map<String, String> typenameMapper = Maps.newHashMap();
        {
            typenameMapper.put("gn:countries", "country");
            typenameMapper.put("gn:kantoneBB", "kantone");
            typenameMapper.put("gn:gemeindenBB", "gemeinden");
        }

        @Override
        public Object getDefault() throws JSONException {
            JSONObject def = new JSONObject();
            def.put(Save.JSON_IDENTIFICATION_EXTENT_GEOM, "");
            def.put(Save.JSON_IDENTIFICATION_EXTENT_DESCRIPTION, new JSONObject());
            return def;
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();
            final String href = XLink.getHRef(node);
            String id = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(href), "UTF-8");
            final Matcher matcher = typenamePattern.matcher(href);
            if (!matcher.find()) {
                throw new AssertionError("Unable to extract the typename in extent href: " + href);
            }
            String featureType = typenameMapper.get(matcher.group(1));
            json.put(Save.JSON_IDENTIFICATION_EXTENT_GEOM, featureType + ":" + id);
            addTranslatedElement(mainLanguage, node, mapper, json, Save.JSON_IDENTIFICATION_EXTENT_DESCRIPTION, "gmd:EX_Extent/gmd:description");

            return json;
        }
    };

    private static class CodeListEntry implements Comparable<CodeListEntry> {
        final String name;
        final String title;
        final String description;
        public String group;

        private CodeListEntry(String name, String title, String description) {
            this.name = name;
            this.title = title;
            this.description = description;
        }

        @Override
        public int compareTo(CodeListEntry o) {
            return title.compareTo(o.title);
        }
    }

}
