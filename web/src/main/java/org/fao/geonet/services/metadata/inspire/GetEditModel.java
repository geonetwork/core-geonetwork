package org.fao.geonet.services.metadata.inspire;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GEONET;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;

/**
 * @author Jesse on 5/17/2014.
 */
public class GetEditModel implements Service {
    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {

        Element metadataEl = getMetadata(params, context, getAjaxEditUtils(params, context));

        JSONObject metadataJson = new JSONObject();
        processMetadata(metadataEl, metadataJson);
        Element identificationInfo = processIdentificationInfo(metadataEl, metadataJson);
        processConstraints(identificationInfo, metadataJson);

        return new Element("data").setText(metadataJson.toString());
    }

    private void processConstraints(Element identificationInfo, JSONObject metadataJson) throws Exception {
        JSONObject constraintsJson = new JSONObject();
        metadataJson.accumulate(Save.JSON_CONSTRAINTS, constraintsJson);

        @SuppressWarnings("unchecked")
        List<Element> legalConstraints = (List<Element>) Xml.selectNodes(identificationInfo,
                "gmd:resourceConstraints/*[name() = 'gmd:MD_LegalConstraints' " +
                "or @gco:isoType = 'gmd:MD_LegalConstraints']", Save.NS);

        for (Element legalConstraint : legalConstraints) {
            JSONObject legalJson = new JSONObject();
            processLegalConstraint(legalConstraint, legalJson);
            constraintsJson.append(Save.JSON_CONSTRAINTS_LEGAL, legalJson);
        }
        @SuppressWarnings("unchecked")
        List<Element> genericConstraints = (List<Element>) Xml.selectNodes(identificationInfo,
                "gmd:resourceConstraints/gmd:MD_Constraints", Save.NS);

        for (Element genericConstraint : genericConstraints) {
            JSONObject json = new JSONObject();
            addRef(genericConstraint, json);
            addArray(genericConstraint, getIsoLanguagesMapper(), json, "gmd:useLimitation", Save.JSON_CONSTRAINTS_USE_LIMITATIONS,
                    translatedElemEncoder);
            constraintsJson.append(Save.JSON_CONSTRAINTS_GENERIC, json);
        }
        @SuppressWarnings("unchecked")
        List<Element> securityConstraints = (List<Element>) Xml.selectNodes(identificationInfo,
                "gmd:resourceConstraints/gmd:MD_SecurityConstraints", Save.NS);

        for (Element securityConstraint : securityConstraints) {
            JSONObject json = new JSONObject();
            addRef(securityConstraint, json);
            addArray(securityConstraint, getIsoLanguagesMapper(), json, "gmd:useLimitation", Save.JSON_CONSTRAINTS_USE_LIMITATIONS,
                    translatedElemEncoder);
            addArray(securityConstraint, getIsoLanguagesMapper(), json, "gmd:classification/gmd:MD_ClassificationCode",
                    Save.JSON_CONSTRAINTS_USE_LIMITATIONS, codeListJsonEncoder);
            constraintsJson.append(Save.JSON_CONSTRAINTS_SECURITY, json);
        }

    }

    private void processLegalConstraint(Element constraint, JSONObject legalJson) throws Exception {
        addRef(constraint, legalJson);
        addArray(constraint, getIsoLanguagesMapper(), legalJson, "gmd:useLimitation", Save.JSON_CONSTRAINTS_USE_LIMITATIONS,
                translatedElemEncoder);
        addArray(constraint, getIsoLanguagesMapper(), legalJson, "gmd:accessConstraints/gmd:MD_RestrictionCode",
                Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS, codeListJsonEncoder);
        addArray(constraint, getIsoLanguagesMapper(), legalJson, "gmd:useConstraints/gmd:MD_RestrictionCode",
                Save.JSON_CONSTRAINTS_USE_CONSTRAINTS, codeListJsonEncoder);
        addArray(constraint, getIsoLanguagesMapper(), legalJson, "gmd:otherConstraints", Save.JSON_CONSTRAINTS_OTHER_CONSTRAINTS,
                translatedElemEncoder);
        addArray(constraint, getIsoLanguagesMapper(), legalJson,
                "che:legislationConstraints/che:CHE_MD_Legislation",
                Save.JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS, legislationConstraintsJsonEncoder);

    }

    private void addRef(Element constraint, JSONObject json) throws JSONException {
        String ref = constraint.getChild("element", GEONET).getAttributeValue(Params.REF);
        json.accumulate(Params.REF, ref);
    }

    private Element processIdentificationInfo(Element metadataEl, JSONObject metadataJson) throws Exception {
        Element identificationInfoEl = Xml.selectElement(metadataEl,
                "gmd:identificationInfo/node()[@gco:isoType = 'gmd:MD_DataIdentification' or " +
                "@gco:isoType = 'srv:SV_ServiceIdentification']", Save.NS);


        if (identificationInfoEl == null) {
            identificationInfoEl = new Element("CHE_MD_DataIdentification", XslUtil.CHE_NAMESPACE);
        }

        JSONObject identificationJSON = new JSONObject();
        metadataJson.accumulate(Save.JSON_IDENTIFICATION, identificationJSON);
        boolean isDataType = identificationInfoEl.getName().equals("gmd:MD_DataIdentification") ||
                             (identificationInfoEl.getAttributeValue("isoType", GCO)!= null &&
                              identificationInfoEl.getAttributeValue("isoType", GCO).equals("gmd:MD_DataIdentification"));
        identificationJSON.accumulate(Save.JSON_IDENTIFICATION_TYPE, isDataType ? "data" : "service");

        addTranslatedElement(identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, Save.JSON_IDENTIFICATION_TITLE,
                "gmd:citation/gmd:CI_Citation/gmd:title");

        addTranslatedElement(identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, Save.JSON_IDENTIFICATION_IDENTIFIER,
                "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code");

        addValue(identificationInfoEl, identificationJSON, Save.JSON_IDENTIFICATION_DATE,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/*");

        final Element dateElement = Xml.selectElement(identificationInfoEl,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/*", Save.NS);
        identificationJSON.accumulate(Save.JSON_IDENTIFICATION_DATE_TAG_NAME, dateElement == null ? "" : dateElement.getQualifiedName());
        addValue(identificationInfoEl, identificationJSON, Save.JSON_IDENTIFICATION_DATE_TYPE,
                "gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue");

        addTranslatedElement(identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, Save.JSON_IDENTIFICATION_ABSTRACT,
                "gmd:abstract");


        addValue(identificationInfoEl, identificationJSON, Save.JSON_LANGUAGE, "gmd:language");
        addArray(identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:topicCategory/gmd:MD_TopicCategoryCode",
                Save.JSON_IDENTIFICATION_TOPIC_CATEGORIES, valueJsonEncoder);

        addArray(identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:pointOfContact",
                Save.JSON_IDENTIFICATION_POINT_OF_CONTACT, contactJsonEncoder);
        addArray(identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:descriptiveKeywords ",
                Save.JSON_IDENTIFICATION_KEYWORDS, keywordJsonEncoder);
        addArray(identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:extent",
                Save.JSON_IDENTIFICATION_EXTENTS, extentJsonEncoder);

        return identificationInfoEl;
    }

    protected void processMetadata(Element metadataEl, JSONObject metadataJson) throws Exception {
        addValue(metadataEl, metadataJson, Save.JSON_LANGUAGE, "gmd:language/gco:CharacterString/text()");
        addValue(metadataEl, metadataJson, Save.JSON_CHARACTER_SET, "gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue");
        addValue(metadataEl, metadataJson, Save.JSON_HIERARCHY_LEVEL, "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
        addValue(metadataEl, metadataJson, Save.JSON_HIERARCHY_LEVEL_NAME, "gmd:hierarchyLevelName/gco:CharacterString/text()");
        addArray(metadataEl, getIsoLanguagesMapper(), metadataJson, "gmd:contact", Save.JSON_CONTACT, contactJsonEncoder);
        addArray(metadataEl, getIsoLanguagesMapper(), metadataJson,
                "gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode",
                Save.JSON_OTHER_LANGUAGES, codeListValueEncoder);
    }

    private static void addValue(Element metadataEl, JSONObject metadataJson, String jsonKey, String xpath) throws JSONException, JDOMException {
        metadataJson.accumulate(jsonKey, Xml.selectString(metadataEl, xpath, Save.NS).trim());
    }

    private static void addArray(Element metadataEl, IsoLanguagesMapper mapper, JSONObject metadataJson, String xpath, String jsonKey, JsonEncoder encoder)
            throws Exception {

        @SuppressWarnings("unchecked")
        final List<Element> nodes = (List<Element>) Xml.selectNodes(metadataEl, xpath);

        for (Element node : nodes) {
            metadataJson.append(jsonKey, encoder.encode(node, mapper));
        }
    }

    private static void addTranslatedElement(Element metadata, IsoLanguagesMapper mapper, JSONObject json,
                                             String jsonKey, String xpath) throws JDOMException,
            JSONException {
        JSONObject obj = new JSONObject();

        List<?> nodes = Xml.selectNodes(metadata, xpath + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", Save.NS);
        addTranslationElements(mapper, obj, nodes);

        json.accumulate(jsonKey, obj);
    }

    private static void addTranslationElements(IsoLanguagesMapper mapper, JSONObject obj, List<?>nodes)
            throws JSONException {
        for (Object node : nodes) {
            Element el = (Element) node;

            String langCode = el.getAttributeValue("locale").substring(1).toLowerCase();
            final String lang = mapper.iso639_1_to_iso639_2(langCode);

            obj.accumulate(lang, el.getTextTrim());
        }
    }

    @VisibleForTesting
    protected Element getMetadata(Element params, ServiceContext context, AjaxEditUtils ajaxEditUtils) throws Exception {
        String id = Utils.getIdentifierFromParameters(params, context);

        Element metadata = (Element) context.getUserSession().getProperty(Geonet.Session.METADATA_EDITING + id);
        if (metadata == null) {
            metadata = ajaxEditUtils.getMetadataEmbedded(context, id, true, false);
        }
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
        public Object encode(Element node, IsoLanguagesMapper mapper) throws JDOMException, JSONException, UnsupportedEncodingException, Exception;
    }

    private static final JsonEncoder codeListValueEncoder = new JsonEncoder() {

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws JDOMException, JSONException {
            return node.getAttributeValue("codeListValue");
        }
    };
    private static final JsonEncoder contactJsonEncoder = new JsonEncoder() {

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws JDOMException, JSONException, UnsupportedEncodingException {
            JSONObject json = new JSONObject();

            String id = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(XLink.getHRef(node)), "UTF-8");
            addValue(node, json, Save.JSON_CONTACT_ID, id);
            addValue(node, json, Save.JSON_CONTACT_FIRST_NAME, "che:CHE_CI_ResponsibleParty/che:individualFirstName");
            addValue(node, json, Save.JSON_CONTACT_LAST_NAME, "che:CHE_CI_ResponsibleParty/che:individualLastName");
            addValue(node, json, Save.JSON_CONTACT_ROLE, "che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue");
            addValue(node, json, Save.JSON_CONTACT_EMAIL,
                    "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/" +
                    "che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString/text()");

            addTranslatedElement(node, mapper, json, Save.JSON_CONTACT_ORG_NAME, "che:CHE_CI_ResponsibleParty/gmd:organisationName");
            boolean validated = false;
            if (node.getAttributeValue("role", XLINK).equals(ReusableObjManager.NON_VALID_ROLE)) {
                validated = true;
            }

            json.append(Save.JSON_VALIDATED, validated);

            return json;
        }
    };
    private static final JsonEncoder keywordJsonEncoder = new JsonEncoder() {

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();

            String code = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(XLink.getHRef(node)), "UTF-8");
            json.accumulate(Save.JSON_IDENTIFICATION_KEYWORD_CODE, code);
            addTranslatedElement(node, mapper, json, Save.JSON_IDENTIFICATION_KEYWORD_WORD, "gmd:MD_Keywords/gmd:keyword");

            return json;
        }
    };
    private static final JsonEncoder valueJsonEncoder = new JsonEncoder() {

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws Exception {
            return node.getTextTrim();
        }
    };
    private static final JsonEncoder codeListJsonEncoder = new JsonEncoder() {

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws Exception {
            return node.getAttributeValue(Save.ATT_CODE_LIST_VALUE);
        }
    };
    private static final JsonEncoder translatedElemEncoder = new JsonEncoder() {

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();
            final List<?> nodes = Xml.selectNodes(node, "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", Save.NS);
            addTranslationElements(mapper, json, nodes);
            return json;
        }
    };

    private static final JsonEncoder legislationConstraintsJsonEncoder = new JsonEncoder() {

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();
            final List<?> nodes = Xml.selectNodes(node,
                    "che:title/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString", Save.NS);
            addTranslationElements(mapper, json, nodes);
            return json;
        }
    };

    private static final JsonEncoder extentJsonEncoder = new JsonEncoder() {
        final Pattern typenamePattern = Pattern.compile("typename=([^&]+)");
        Map<String, String> typenameMapper = Maps.newHashMap();
        {
            typenameMapper.put("gn:countries", "countries");
            typenameMapper.put("gn:kantoneBB", "kantone");
            typenameMapper.put("gn:gemeindenBB", "gemeinden");
        }

        @Override
        public Object encode(Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();
            final String href = XLink.getHRef(node);
            String id = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(href), "UTF-8");
            final Matcher matcher = typenamePattern.matcher(href);
            if (!matcher.find()) {
                throw new AssertionError("Unable to extract the typename in extent href: " + href);
            }
            String featureType = typenameMapper.get(matcher.group(1));
            json.accumulate(Save.JSON_IDENTIFICATION_EXTENT_GEOM, featureType + ":" + id);
            addTranslatedElement(node, mapper, json, Save.JSON_IDENTIFICATION_EXTENT_DESCRIPTION, "gmd:EX_Extent/gmd:description");

            return json;
        }
    };

}
