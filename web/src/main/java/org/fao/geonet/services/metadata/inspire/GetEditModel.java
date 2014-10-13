package org.fao.geonet.services.metadata.inspire;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.util.Assert;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.apache.jcs.access.exception.CacheException;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.reusable.KeywordsStrategy;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.search.keyword.KeywordSearchType;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
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
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;
import static org.fao.geonet.services.metadata.inspire.Save.NS;

/**
 * @author Jesse on 5/17/2014.
 */
public class GetEditModel implements Service {
    public static final String TRANSFER_OPTION_XPATH = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage";
    private final XmlCacheManager cacheManager = new XmlCacheManager();
    private static final JSONArray CONFORMITY_TITLE_OPTIONS = new JSONArray();
    static {
        try {
            final JSONObject option = new JSONObject();
            option.put("ger", "VERORDNUNG (EG) Nr. 1089/2010 DER KOMMISSION vom 23. November 2010 zur Durchführung " +
                              "der Richtlinie 2007/2/EG des Europäischen Parlaments und des Rates hinsichtlich der Interoperabilität " +
                              "von Geodatensätzen und -diensten");
            option.put("eng", "COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing " +
                              "Directive 2007/2/EC of the European Parliament and of the Council as regards " +
                              "interoperability of spatial data sets and services");
            option.put("fre", "Règlement (UE) n o 1089/2010 de la commission du 23 novembre 2010 portant modalités d'application " +
                              "de la directive 2007/2/ce du Parlement Européen et du conseil en ce qui concerne l'interopérabilité " +
                              "des séries et des services de données géographiques");
            option.put("ita", "REGOLAMENTO (UE) N. 1089/2010 DELLA COMMISSIONE del 23 novembre 2010 recante " +
                              "attuazione della direttiva 2007/2/CE del Parlamento europeo e del Consiglio per quanto riguarda " +
                              "l'interoperabilità dei set di dati territoriali e dei servizi di dati territoriali");
            CONFORMITY_TITLE_OPTIONS.put(option);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private static final JSONArray REF_SYS_OPTIONS = new JSONArray();
    static {
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/4936");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/4937");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/4258");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3035");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3034");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3038");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3039");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3040");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3041");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3042");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3043");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3044");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3045");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3046");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3047");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3048");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3049");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3050");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/3051");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/5730");
            REF_SYS_OPTIONS.put("http://www.opengis.net/def/crs/EPSG/0/7409");
    }
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
    final static Set<String> ALLOWED_SERVICETYPES = Sets.newHashSet("view", "discovery", "download", "transformation", "invoke", "other");
    private Function<CodeListEntry, CodeListEntry> serviceTypeInspire = new Function<CodeListEntry, CodeListEntry>() {
        @Nullable
        @Override
        public CodeListEntry apply(@Nullable CodeListEntry input) {
            if (input != null && ALLOWED_SERVICETYPES.contains(input.name)) {
                return input;
            }
            return null;
        }
    };
    private Function<CodeListEntry, CodeListEntry> inspireRoleFilter = new Function<CodeListEntry, CodeListEntry>() {
        Set<String> illegalRolesForPOC = Sets.newHashSet("editor", "partner");

        @Nullable
        @Override
        public CodeListEntry apply(@Nullable CodeListEntry input) {
            if (input != null && !this.illegalRolesForPOC.contains(input.name.toLowerCase())) {
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
        final Pair<Element, Boolean> pair = getMetadata(params, context, getAjaxEditUtils(params, context));
        Element metadataEl = pair.one();


        return createModel(context, pretty, metadataEl, pair.two());
    }

    protected Element createModel(ServiceContext context, boolean pretty, Element metadataEl, Boolean valid) throws Exception {
        JSONObject metadataJson = new JSONObject();
        metadataJson.put(Save.JSON_VALID_METADATA, valid);
        addCodeLists(context, metadataJson);
        metadataJson.append("metadataTypeOptions", "data");
        metadataJson.append("metadataTypeOptions", "service");

        metadataJson.put("conformityTitleOptions", CONFORMITY_TITLE_OPTIONS);
        metadataJson.put("refSysOptions", REF_SYS_OPTIONS);

        processMetadata(metadataEl, metadataJson);
        Element identificationInfo = processIdentificationInfo(context, metadataEl, metadataJson);

        processConstraints(identificationInfo, metadataJson);
        processReferenceSystems(metadataEl, metadataJson);
        processConformity(metadataEl, metadataJson);
        processTransferOptions(metadataEl, metadataJson);

        final String jsonString;
        if (pretty) {
            jsonString = metadataJson.toString(2);
        } else {
            jsonString = metadataJson.toString();
        }
        return new Element("data").setText(jsonString);
    }

    private void processReferenceSystems(Element metadataEl, JSONObject metadataJson) throws JSONException,
            JDOMException {
        String mainLanguage = metadataJson.getString(Save.JSON_LANGUAGE);
        JSONArray referenceSystemJson = new JSONArray();
        metadataJson.put(Save.JSON_REF_SYS, referenceSystemJson);

        final List<Element> refSysEls = metadataEl.getChildren("referenceSystemInfo", GMD);

        for (Element refSysEl : refSysEls) {
            JSONObject instanceJson = new JSONObject();
            addRef(refSysEl, instanceJson);
            addTranslatedElement(mainLanguage, refSysEl, getIsoLanguagesMapper(), instanceJson,
                    Save.JSON_REF_SYS_CODE, "gmd:MD_ReferenceSystem//gmd:code");

            referenceSystemJson.put(instanceJson);
        }
    }

    private void processTransferOptions(Element metadataEl, JSONObject metadataJson) throws JDOMException, JSONException {
        JSONArray linksJson = new JSONArray();
        metadataJson.put(Save.JSON_LINKS, linksJson);

        final String xpathLinkage = "gmd:distributionInfo/*/gmd:transferOptions//gmd:linkage";
        processLinkages(metadataEl, linksJson, xpathLinkage, "gmd:CI_OnlineResource/gmd:linkage");

        processDistributionFormat(metadataEl, metadataJson);

        String identificationType = metadataJson.getJSONObject(Save.JSON_IDENTIFICATION).getString(Save.JSON_IDENTIFICATION_TYPE);

        if (!Save.JSON_IDENTIFICATION_TYPE_DATA_VALUE.equals(identificationType) && linksJson.length() == 0) {
            JSONObject link = new JSONObject();
            link.put(Save.JSON_LINKS_LOCALIZED_URL, new JSONObject());
            link.put(Save.JSON_LINKS_DESCRIPTION, "");
            link.put(Save.JSON_LINKS_XPATH, TRANSFER_OPTION_XPATH);
            linksJson.put(link);
        }
    }

    @SuppressWarnings("unchecked")
    private void processDistributionFormat(Element metadataEl, JSONObject metadataJson) throws JDOMException, JSONException {
        final String xpathDistributionFormat = "gmd:distributionInfo/*/gmd:distributionFormat/gmd:MD_Format";
        final List<Element> formats = (List<Element>) Xml.selectNodes(metadataEl, xpathDistributionFormat, NS);
        JSONArray formatJson = new JSONArray();
        metadataJson.put(Save.JSON_DISTRIBUTION_FORMAT, formatJson);
        for (Element format : formats) {
            final Element nameEl = format.getChild("name", GMD);
            String name = "";
            if (nameEl != null) {
                name = nameEl.getChildText("CharacterString", GCO);
            }
            final Element versionEl = format.getChild("version", GMD);
            String version = "";
            if (versionEl != null) {
                version = versionEl.getChildText("CharacterString", GCO);
            }
            final String href = format.getParentElement().getAttributeValue("href", XLINK, "");
            final String validated = format.getParentElement().getAttributeValue("role", XLINK, "");
            String id = org.fao.geonet.kernel.reusable.Utils.id(href);

            formatJson.put(createDistributionFormat(name, version, id, !ReusableObjManager.NON_VALID_ROLE.equals(validated)));
        }

        if (formatJson.length() == 0) {
            formatJson.put(createDistributionFormat("", "", "", false));
        }
    }

    private JSONObject createDistributionFormat(String name, String version, String id, boolean validated) throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put(Save.JSON_DISTRIBUTION_FORMAT_NAME, name);
        obj.put(Save.JSON_DISTRIBUTION_FORMAT_VERSION, version);
        obj.put(Save.JSON_DISTRIBUTION_FORMAT_VALIDATED, validated);
        obj.put(Params.ID, id);

        return obj;
    }

    private void processLinkages(Element metadataEl, JSONArray linksJson, String xpath, String jsonXLink) throws JDOMException, JSONException {
        @SuppressWarnings("unchecked")
        final List<Element> linkages = (List<Element>) Xml.selectNodes(metadataEl,
                xpath, NS);

        for (Element linkage : linkages) {
            JSONObject linkJson = new JSONObject();
            Element refElem = linkage;
            int segments = jsonXLink.split("/").length;
            for (int i = 0; i < segments; i++) {
                 refElem = refElem.getParentElement();
            }

            addRef(refElem, linkJson);

            List<?> localisedUrls = Xml.selectNodes(linkage, "*//che:LocalisedURL", NS);
            JSONObject urls = new JSONObject();
            addTranslationElements(getIsoLanguagesMapper(), urls, localisedUrls);
            linkJson.put(Save.JSON_LINKS_LOCALIZED_URL, urls);

            List<?> description = Xml.selectNodes(linkage.getParentElement(), "gmd:description//gmd:LocalisedCharacterString", NS);
            JSONObject descriptionJson = new JSONObject();
            addTranslationElements(getIsoLanguagesMapper(), descriptionJson, description);
            linkJson.put(Save.JSON_LINKS_DESCRIPTION, descriptionJson);
            linkJson.put(Save.JSON_LINKS_XPATH, jsonXLink);
            addValue(linkage.getParentElement(), linkJson, Save.JSON_LINKS_PROTOCOL, "gmd:description");

            linksJson.put(linkJson);
        }
    }

    @SuppressWarnings("unchecked")
    private void processConformity(Element metadataEl, JSONObject metadataJson) throws JSONException, JDOMException {
        List<Element> conformityElements = (List<Element>) Xml.selectNodes(metadataEl,
                "gmd:dataQualityInfo/*/gmd:report//gmd:DQ_ConformanceResult", NS);

        int conformanceResultIndex = -1;
        JSONArray allConformanceResults = new JSONArray();
        int i = 0;
        for (Element conformityElement : conformityElements) {
            JSONObject conformanceJSON = new JSONObject();
            addConformanceProperties(metadataJson, conformityElement, conformanceJSON);

            JSONObject titles = conformanceJSON.getJSONObject(Save.JSON_TITLE);

            final Iterator keys = titles.keys();

            allConformanceResults.put(conformanceJSON);

            while (keys.hasNext()) {
                String key = (String) keys.next();
                String title = titles.getString(key);
                if (isConformityTitle(title)) {
                    conformanceResultIndex = i;
                    break;
                }
            }
            i++;
        }

        if (conformanceResultIndex == -1) {
            final JSONObject newObject = new JSONObject();
            newObject.put(Save.JSON_CONFORMITY_RESULT_REF, "");
            JSONObject title = new JSONObject();
            title.put("eng", "New");
            title.put("fre", "Nouveau");
            title.put("ger", "Neu");
            title.put("ita", "Nuovo");
            newObject.put(Save.JSON_TITLE, title);
            newObject.put(Save.JSON_CONFORMITY_SCOPE_CODE , "");
            newObject.put(Save.JSON_CONFORMITY_LEVEL_DESC, "");
            newObject.put(Save.JSON_CONFORMITY_EXPLANATION, "");
            newObject.put(Save.JSON_CONFORMITY_PASS, "");
            allConformanceResults.put(newObject);
        }

        JSONObject conformityJson = new JSONObject();
        String updateRef = "";
        if (conformanceResultIndex > -1) {
            updateRef = allConformanceResults.getJSONObject(conformanceResultIndex).getString(Save.JSON_CONFORMITY_RESULT_REF);
        }
        conformityJson.put(Save.JSON_CONFORMITY_UPDATE_ELEMENT_REF, updateRef);
        conformityJson.put(Save.JSON_CONFORMITY_ALL_CONFORMANCE_REPORTS, allConformanceResults);
        conformityJson.put(Save.JSON_CONFORMITY_ALL_CONFORMANCE_REPORT_INDEX, conformanceResultIndex);

        JSONObject lineageJson = new JSONObject();
        conformityJson.put(Save.JSON_CONFORMITY_LINEAGE, lineageJson);

        Element lineageEl = Xml.selectElement(metadataEl, "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage", NS);
        String mainLanguage = metadataJson.getString(Save.JSON_LANGUAGE);
        addTranslatedElement(mainLanguage, lineageEl, getIsoLanguagesMapper(), lineageJson, Save.JSON_CONFORMITY_LINEAGE_STATEMENT,
                "gmd:statement");
        addValue(lineageEl, lineageJson, Params.REF, "geonet:element/@ref");

        conformityJson.put(Save.JSON_CONFORMITY_IS_TITLE_SET, false);
        metadataJson.put(Save.JSON_CONFORMITY, conformityJson);
    }

    private JSONObject addConformanceProperties(JSONObject metadataJson, Element conformanceResult,
                                                JSONObject conformityJson)
            throws JSONException, JDOMException {
        String mainLanguage = metadataJson.getString(Save.JSON_LANGUAGE);

        addValue(conformanceResult, conformityJson, Save.JSON_CONFORMITY_RESULT_REF, "geonet:element/@ref");

        final boolean hasTitle = addTranslatedElement(mainLanguage, conformanceResult, getIsoLanguagesMapper(), conformityJson,
                Save.JSON_TITLE, "gmd:specification/gmd:CI_Citation/gmd:title");
        conformityJson.put(Save.JSON_CONFORMITY_IS_TITLE_SET, hasTitle);

        if (hasTitle) {
            addConformityTitleTranslations(conformityJson);
        }

        addConformityDate(conformityJson);

        addValue(conformanceResult, conformityJson, Save.JSON_CONFORMITY_PASS, "gmd:pass/gco:Boolean");
        addValue(conformanceResult, conformityJson, Save.JSON_CONFORMITY_EXPLANATION, "gmd:explanation/gco:CharacterString");
        final String scopeCodeXPath = "gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue";
        addValue(getDataQualityEl(conformanceResult), conformityJson, Save.JSON_CONFORMITY_SCOPE_CODE, scopeCodeXPath, "");
        String levelDescXPath = "gmd:scope/gmd:DQ_Scope/gmd:levelDescription/gmd:MD_ScopeDescription/gmd:other/gco:CharacterString";
        addValue(getDataQualityEl(conformanceResult), conformityJson, Save.JSON_CONFORMITY_LEVEL_DESC, levelDescXPath, "");

        return conformityJson;
    }

    private void addConformityTitleTranslations(JSONObject conformityJson) throws JSONException {
        final JSONObject titles = conformityJson.getJSONObject(Save.JSON_TITLE);

        final Iterator keys = titles.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            final String title = titles.getString(key);
            final int length = CONFORMITY_TITLE_OPTIONS.length();
            for (int i = 0; i < length; i++) {
                final JSONObject titleOption = CONFORMITY_TITLE_OPTIONS.getJSONObject(i);
                final Iterator optionKeys = titleOption.keys();
                while (optionKeys.hasNext()) {
                    String optionKey = (String) optionKeys.next();
                    String translation = titleOption.getString(optionKey);
                    if (translation.equalsIgnoreCase(title)) {
                        conformityJson.put(Save.JSON_TITLE, titleOption);
                        return;
                    }
                }
            }
        }
    }

    static void addConformityDate(JSONObject conformityJson) throws JSONException {
        JSONObject date = new JSONObject();
        date.put(Save.JSON_DATE, "2010-12-08");
        date.put(Save.JSON_DATE_TYPE, "publication");
        date.put(Save.JSON_DATE_TAG_NAME, "gco:Date");
        conformityJson.put(Save.JSON_DATE, date);
    }

    static Element getDataQualityEl(Element conformityElement) {
        if (conformityElement != null && conformityElement.getParentElement() != null
            && conformityElement.getParentElement().getParentElement() != null
            && conformityElement.getParentElement().getParentElement().getParentElement() != null) {
            return conformityElement.getParentElement().getParentElement().getParentElement().getParentElement();
        }
        return null;
    }

    private static boolean isConformityTitle(String title) throws JSONException {
        title = title.toLowerCase();
        for (int i = 0; i < CONFORMITY_TITLE_OPTIONS.length(); i++) {
            JSONObject option = CONFORMITY_TITLE_OPTIONS.getJSONObject(i);
            final Iterator keys = option.keys();
            while (keys.hasNext()) {
                String next = (String) keys.next();
                if (option.getString(next).toLowerCase().equals(title)) {
                    return true;
                }
            }
        }

        return false;
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
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:CI_RoleCode", "roleOptions", this.inspireRoleFilter);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_ScopeCode", "hierarchyLevelOptions", null);

        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_TopicCategoryCode", "topicCategoryOptions",
                topicCategoryGrouper);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_RestrictionCode", "constraintOptions", null);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "gmd:MD_ScopeCode", "scopeCodeOptions", null);
        Set<CodeListEntry> couplingTypeOptions = Sets.newHashSet(
                new CodeListEntry("loose", "loose", "loose"),
                new CodeListEntry("tight", "tight", "tight"),
                new CodeListEntry("mixed", "mixed", "mixed")
        );
        addOptions(metadataJson, "couplingTypeOptions", couplingTypeOptions);
        addCodeListOptions(metadataJson, codelists, cheCodelistExtensions, "srv:DCPList", "dcpListOptions", null);
        addCodeListOptionsFromLabelsHelper(metadataJson, labels, labelExtensions, "srv:serviceType", "serviceTypeOptions", serviceTypeInspire);

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
                                                    String codelistName, String jsonKey, Function<CodeListEntry, CodeListEntry>
            grouperFilter) throws JDOMException, JSONException {
        Set<CodeListEntry> collector = new LinkedHashSet<CodeListEntry>();
        final String xpath = "element[@name = '" + codelistName + "']/helper/option";
        @SuppressWarnings("unchecked")
        List<Element> elems = (List<Element>) Xml.selectNodes(codelists, xpath);
        for (Element elem : elems) {
            final String value = elem.getTextTrim();
            CodeListEntry entry = new CodeListEntry(elem.getAttributeValue("value"), value, value);
            if (grouperFilter != null) {
                entry = grouperFilter.apply(entry);
            }
            if (entry != null) {
                collector.add(entry);
            }
        }

        @SuppressWarnings("unchecked")
        List<Element> cheElems = (List<Element>) Xml.selectNodes(cheCodelistsExtensions, xpath);
        for (Element elem : cheElems) {
            final String value = elem.getTextTrim();
            CodeListEntry entry = new CodeListEntry(elem.getAttributeValue("value"), value, value);
            if (grouperFilter != null) {
                entry = grouperFilter.apply(entry);
            }
            if (entry != null) {
                collector.add(entry);
            }
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

        ConstraintTracker tracker = new ConstraintTracker();
        for (Element legalConstraint : legalConstraints) {
            JSONObject legalJson = new JSONObject();
            processLegalConstraint(mainLanguage, legalConstraint, legalJson, tracker);
            constraintsJson.append(Save.JSON_CONSTRAINTS_LEGAL, legalJson);
        }

        if (!tracker.access) {
            final JSONArray array = constraintsJson.getJSONArray(Save.JSON_CONSTRAINTS_LEGAL);
            if (array.length() > 0) {
                array.getJSONObject(0).getJSONArray(Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS).put("");
            }
        }

        if (!tracker.use) {
            final JSONArray array = constraintsJson.getJSONArray(Save.JSON_CONSTRAINTS_LEGAL);
            if (array.length() > 0) {
                array.getJSONObject(0).getJSONArray(Save.JSON_CONSTRAINTS_USE_CONSTRAINTS).put("");
            }
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
                    Save.JSON_CONSTRAINTS_CLASSIFICATION, codeListJsonEncoder);
            constraintsJson.append(Save.JSON_CONSTRAINTS_SECURITY, json);
        }

    }

    /**
     * Keep track if access constraint and use constraints have been added.
     */
    private static class ConstraintTracker {
        boolean access = false;
        boolean use = false;
    }
    private void processLegalConstraint(String mainLanguage, Element constraint, JSONObject legalJson, ConstraintTracker tracker) throws Exception {
        addRef(constraint, legalJson);
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:useLimitation", Save.JSON_CONSTRAINTS_USE_LIMITATIONS,
                translatedElemEncoder, new JSONArray());
        tracker.access |= addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:accessConstraints/gmd:MD_RestrictionCode",
                Save.JSON_CONSTRAINTS_ACCESS_CONSTRAINTS, noDefaultJsonEncoder);
        tracker.use |= addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:useConstraints/gmd:MD_RestrictionCode",
                Save.JSON_CONSTRAINTS_USE_CONSTRAINTS, noDefaultJsonEncoder);
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson, "gmd:otherConstraints", Save.JSON_CONSTRAINTS_OTHER_CONSTRAINTS,
                translatedElemEncoder, new JSONArray());
        addArray(mainLanguage, constraint, getIsoLanguagesMapper(), legalJson,
                "che:legislationConstraints/che:CHE_MD_Legislation",
                Save.JSON_CONSTRAINTS_LEGISLATION_CONSTRAINTS, legislationConstraintsJsonEncoder, new JSONArray());

    }

    private void addRef(Element constraint, JSONObject json) throws JSONException {
        final Element element = constraint.getChild("element", GEONET);
        if (element != null) {
            String ref = element.getAttributeValue(Params.REF);
            json.put(Params.REF, ref);
        }
    }

    private Element processIdentificationInfo(ServiceContext context, Element metadataEl, JSONObject metadataJson) throws Exception {
        Element identificationInfoEl = Xml.selectElement(metadataEl,
                "gmd:identificationInfo/node()[@gco:isoType = 'gmd:MD_DataIdentification' or local-name() = 'CHE_MD_DataIdentification ' or local-name() = 'MD_DataIdentification ' or " +
                "@gco:isoType = 'srv:SV_ServiceIdentification' or local-name() = 'CHE_SV_ServiceIdentification' or local-name() = 'SV_ServiceIdentification']", NS
        );

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

        addInspireKeywordIfRequired(getIsoLanguagesMapper(), context, mainLanguage, identificationJSON);
        addEmptyKeywordIfRequired(identificationJSON, isDataType);

        addArray(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "gmd:extent|srv:extent",
                Save.JSON_IDENTIFICATION_EXTENTS, extentJsonEncoder);

        addValue(identificationInfoEl, identificationJSON, Save.JSON_IDENTIFICATION_COUPLING_TYPE, "srv:couplingType/srv:SV_CouplingType/@codeListValue");
        addArray(mainLanguage, identificationInfoEl, getIsoLanguagesMapper(), identificationJSON, "srv:containsOperations/srv:SV_OperationMetadata",
                Save.JSON_IDENTIFICATION_CONTAINS_OPERATIONS, containsOperationsEncoder);

        return identificationInfoEl;
    }

    private void addInspireKeywordIfRequired(IsoLanguagesMapper mapper, ServiceContext context, String mainLanguage,
                                             JSONObject identificationJSON)
            throws Exception {
        final List<KeywordBean> inspireKeywords = findINSPIREKeywordBeans(mapper, context);

        if (inspireKeywords.isEmpty()) {
            throw new IllegalStateException("No INSPIRE keyword registered in any of the thesauri");
        } else {
            final JSONArray jsonArray = identificationJSON.getJSONArray(Save.JSON_IDENTIFICATION_KEYWORDS);
            for (KeywordBean keyword : inspireKeywords) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.has(Save.JSON_IDENTIFICATION_KEYWORD_CODE) && keyword.getUriCode().equals(jsonObject.getString(Save.JSON_IDENTIFICATION_KEYWORD_CODE))) {
                        return;
                    }
                }
            }

            final KeywordBean bean = inspireKeywords.get(0);
            String id = String.format("local://che.keyword.get?thesaurus=%s&id=%s&locales=en,it,de,fr",
                    bean.getThesaurusKey(),
                    URLEncoder.encode(bean.getUriCode(), "UTF-8"));
            final Element inspireKeywordEl = new Element("keyword").setAttribute(XLink.HREF, id, XLink.NAMESPACE_XLINK);
            final Object inspireKeyword = keywordJsonEncoder.encode(mainLanguage, inspireKeywordEl, mapper);
            Assert.isTrue(inspireKeyword != null);
            jsonArray.put(inspireKeyword);
        }
    }

    @VisibleForTesting
    protected List<KeywordBean> findINSPIREKeywordBeans(IsoLanguagesMapper mapper, ServiceContext context) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        final GeonetContext handlerContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager thesaurusManager = handlerContext.getThesaurusManager();
        KeywordSearchParamsBuilder paramsBuilder = new KeywordSearchParamsBuilder(mapper).
                keyword("INSPIRE", KeywordSearchType.MATCH, false).
                addLang("eng").addLang("ger").addLang("ita").addLang("fre").addThesaurus(KeywordsStrategy.GEOCAT_THESAURUS_NAME);

        return paramsBuilder.build().search(thesaurusManager);
    }

    private void addEmptyKeywordIfRequired(JSONObject identificationJSON, boolean isDataType) throws JSONException {
        String requiredThesaurus = "external.theme.inspire-theme";
        if (!isDataType) {
            requiredThesaurus = "external.theme.inspire-service-taxonomy";
        }
        boolean needsNewKeyword = true;
        JSONArray keywords = identificationJSON.optJSONArray(Save.JSON_IDENTIFICATION_KEYWORDS);
        if (keywords == null) {
            keywords = new JSONArray();
            identificationJSON.put(Save.JSON_IDENTIFICATION_KEYWORDS, keywords);
        }
        for (int i = 0; i < keywords.length(); i++) {
            JSONObject keyword = keywords.getJSONObject(i);
            String thesaurus = keyword.optString(Save.JSON_IDENTIFICATION_KEYWORD_THESAURUS, "");
            if (requiredThesaurus.equals(thesaurus) || DEFAULT_THESAURUS.equals(thesaurus)) {
                needsNewKeyword = false;
            }
        }

        if (needsNewKeyword) {
            keywords.put(new JSONObject("{"+Save.JSON_IDENTIFICATION_KEYWORD_WORD+":{}}"));
        }
    }

    private void addDateElement(Element identificationInfoEl, JSONObject identificationJSON, String xpathToDate) throws JSONException,
            JDOMException {
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

        final JSONArray otherLanguages = metadataJson.getJSONArray(Save.JSON_OTHER_LANGUAGES);
        Set<String> langs = Sets.newHashSet();
        JSONArray noDupsLanguages = new JSONArray();
        for (int i = 0; i < otherLanguages.length(); i++) {
            final String lang = otherLanguages.getString(i);
            if (!langs.contains(lang)) {
                 langs.add(lang);
                 noDupsLanguages.put(lang);
            }
        }

        metadataJson.put(Save.JSON_OTHER_LANGUAGES, noDupsLanguages);

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

    private static boolean addArray(String mainLanguage, Element metadataEl, IsoLanguagesMapper mapper, JSONObject metadataJson,
                                    String xpath, String jsonKey, JsonEncoder encoder)
            throws Exception {
        return addArray(mainLanguage, metadataEl, mapper, metadataJson, xpath, jsonKey, encoder, null);
    }
    private static boolean addArray(String mainLanguage, Element metadataEl, IsoLanguagesMapper mapper, JSONObject metadataJson,
                                    String xpath, String jsonKey, JsonEncoder encoder, JSONArray defaultVal)
            throws Exception {
        boolean addedElement = false;

        @SuppressWarnings("unchecked")
        final List<Element> nodes = (List<Element>) Xml.selectNodes(metadataEl, xpath, NS);

        for (Element node : nodes) {
            final Object encode = encoder.encode(mainLanguage, node, mapper);
            if (encode != null) {
                metadataJson.append(jsonKey, encode);
                addedElement = true;
            }
        }

        if (nodes.isEmpty()) {
            if (defaultVal == null) {
                Object def = encoder.getDefault();
                if (def != null) {
                    metadataJson.append(jsonKey, def);
                    addedElement = true;
                }
            } else {
                metadataJson.put(jsonKey, defaultVal);
                addedElement = true;
            }
        }

        if (!metadataJson.has(jsonKey)) {
            metadataJson.put(jsonKey, new JSONArray());
        }
        return addedElement;
    }

    private static boolean addTranslatedElement(String mainLanguage, Element metadata, IsoLanguagesMapper mapper, JSONObject json,
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
        return !nodes.isEmpty();
    }

    private static void addTranslationElements(IsoLanguagesMapper mapper, JSONObject obj, List<?>nodes)
            throws JSONException {
        for (Object node : nodes) {
            Element el = (Element) node;

            String langCode = el.getAttributeValue("locale").substring(1).toLowerCase();
            String lang = mapper.iso639_1_to_iso639_2(langCode);
            if(lang == null) {
                lang = langCode;
            }

            obj.put(lang, el.getTextTrim());
        }
    }

    @VisibleForTesting
    protected Pair<Element, Boolean> getMetadata(Element params, ServiceContext context, AjaxEditUtils ajaxEditUtils) throws Exception {
        String id = Utils.getIdentifierFromParameters(params, context);

        Element metadata = (Element) context.getUserSession().getProperty(Geonet.Session.METADATA_EDITING + id);
        if (metadata == null) {
            metadata = ajaxEditUtils.getMetadataEmbedded(context, id, true, false);
            context.getUserSession().setProperty(Geonet.Session.METADATA_EDITING + id, metadata);
        }
        GeonetContext geonetContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager schemaManager = geonetContext.getSchemamanager();
        EditLib lib = new EditLib(schemaManager);

        lib.removeEditingInfo(metadata);
        Processor.processXLink(metadata, context);
        metadata.removeAttribute("schemaLocation", Geonet.Namespaces.XSI);

        Element validationMd = Xml.transform(metadata, context.getAppPath() + "/xsl/add-charstring.xsl");
        final boolean valid = geonetContext.getDataManager().validate(validationMd);
        lib.enumerateTree(metadata);

        return Pair.read(metadata, valid);
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

    @VisibleForTesting
    protected Element resolveXLink(String hRef, ServiceContext context) throws JDOMException, CacheException, IOException {
        return Processor.resolveXLink(hRef, context);
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

            final String hRef = XLink.getHRef(node);
            if (hRef != null) {
                String id = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(hRef), "UTF-8");
                addValue(node, json, Save.JSON_CONTACT_ID, id);
            }
            addValue(node, json, Save.JSON_CONTACT_FIRST_NAME, "che:CHE_CI_ResponsibleParty/che:individualFirstName");
            addValue(node, json, Save.JSON_CONTACT_LAST_NAME, "che:CHE_CI_ResponsibleParty/che:individualLastName");
            addValue(node, json, Save.JSON_CONTACT_ROLE, "che:CHE_CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue");
            addValue(node, json, Save.JSON_CONTACT_EMAIL,
                    "che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/" +
                    "che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString/text()");

            addTranslatedElement(mainLanguage, node, mapper, json, Save.JSON_CONTACT_ORG_NAME, "che:CHE_CI_ResponsibleParty/gmd:organisationName");
            boolean validated = true;
            if (ReusableObjManager.NON_VALID_ROLE.equals(node.getAttributeValue("role", XLINK))) {
                validated = false;
            }

            json.put(Save.JSON_VALIDATED, validated);

            return json;
        }
    };
    private static final Pattern THESAURUS_PATTERN = Pattern.compile("thesaurus=([^&]+)");
    private static final String DEFAULT_THESAURUS = "default";
    private final JsonEncoder keywordJsonEncoder = new JsonEncoder() {
        @Override
        public Object getDefault() throws JSONException {
            JSONObject def = new JSONObject();
            def.put(Save.JSON_IDENTIFICATION_KEYWORD_CODE, "");
            def.put(Save.JSON_IDENTIFICATION_KEYWORD_WORD, new JSONObject());
            def.put(Save.JSON_IDENTIFICATION_KEYWORD_THESAURUS, DEFAULT_THESAURUS);
            return def;
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();

            final String hRef = XLink.getHRef(node);
            if (hRef != null) {
                Element element = GetEditModel.this.resolveXLink(hRef, ServiceContext.get());
                if (element == null) {
                    element = node;
                }
                if (element != null) {
                    String thesaurus = null;
                    String code = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(hRef), "UTF-8");
                    Matcher matcher = THESAURUS_PATTERN.matcher(hRef);
                    if (matcher.find()) {
                        thesaurus = matcher.group(1);
                        json.put(Save.JSON_IDENTIFICATION_KEYWORD_THESAURUS, thesaurus);
                    }
                    json.put(Save.JSON_IDENTIFICATION_KEYWORD_CODE, code);

                    addTranslatedElement(mainLanguage, element, mapper, json,
                            Save.JSON_IDENTIFICATION_KEYWORD_WORD, "gmd:keyword");

                    if (thesaurus != null) {
                        Map<String, String> thesaurusNames = getThesaurusTranslations(mapper, element);
                        final JSONObject translations = json.getJSONObject(Save.JSON_IDENTIFICATION_KEYWORD_WORD);
                        final Iterator keys = translations.keys();

                        while (keys.hasNext()) {
                            String lang = (String) keys.next();
                            String thesaurusName = thesaurusNames.get(lang);
                            if (thesaurusName == null) {
                                thesaurusName = thesaurus;
                            }
                            translations.put(lang, translations.getString(lang) + " (" + thesaurusName + ")");
                        }
                    }

                    return json;
                }
            }

            return null;
        }
    };

    private Map<String, String> getThesaurusTranslations(IsoLanguagesMapper mapper, Element element) throws JDOMException {
        final String xpath = "gmd:thesaurusName/gmd:CI_Citation//gmd:title//gmd:LocalisedCharacterString";
        final List<?> nodes = Xml.selectNodes(element, xpath, NS);


        final HashMap<String, String> translations = Maps.newHashMap();
        for (Object node : nodes) {
            Element el = (Element) node;
            String langCode = el.getAttributeValue("locale");
            if (langCode != null) {
                langCode = langCode.substring(1).toLowerCase();
                translations.put(mapper.iso639_1_to_iso639_2(langCode), el.getTextTrim());
            }
        }
        return translations;
    }

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
    private static final JsonEncoder noDefaultJsonEncoder = new JsonEncoder() {

        @Override
        public Object getDefault() throws JSONException {
            return null;
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

    private final JsonEncoder containsOperationsEncoder = new JsonEncoder() {
        private JSONObject defaultLinkage() throws JSONException {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(Save.JSON_LINKS_XPATH, "gmd:linkage");
            jsonObject.put(Save.JSON_LINKS_LOCALIZED_URL, new JSONObject());
            jsonObject.put(Save.JSON_LINKS_PROTOCOL, "");
            jsonObject.put(Save.JSON_LINKS_DESCRIPTION, new JSONObject());
            jsonObject.put(Params.REF, "");
            return jsonObject;
        }
        @Override
        public Object getDefault() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(Params.REF, "");
            json.put(Save.JSON_IDENTIFICATION_OPERATION_NAME, "");
            json.put(Save.JSON_IDENTIFICATION_OPERATION_NAME, "");
            json.put(Save.JSON_IDENTIFICATION_DCP_LIST, "");
            JSONArray linkages = new JSONArray();
            linkages.put(defaultLinkage());
            json.put(Save.JSON_LINKS, linkages);
            return json;
        }

        @Override
        public Object encode(String mainLanguage, Element node, IsoLanguagesMapper mapper) throws Exception {
            JSONObject json = new JSONObject();

            addRef(node, json);
            addValue(node, json, Save.JSON_IDENTIFICATION_OPERATION_NAME, "srv:operationName/gco:CharacterString");
            addValue(node, json, Save.JSON_IDENTIFICATION_DCP_LIST, "srv:DCP/srv:DCPList/@codeListValue");
            JSONArray linkages = new JSONArray();
            processLinkages(node, linkages, "srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage", "gmd:linkage");
            if (linkages.length() == 0) {
                linkages.put(defaultLinkage());
            }
            json.put(Save.JSON_LINKS, linkages);

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

    final JsonEncoder extentJsonEncoder = new JsonEncoder() {
        final Pattern typenamePattern = Pattern.compile("typename=([^&]+)");
        /**
         * don't forget to update {@link org.fao.geonet.services.metadata.inspire.Save.ExtentHrefBuilder}
         */
        Map<String, String> typenameMapper = Maps.newHashMap();
        {
            typenameMapper.put("gn:countries", "country");
            typenameMapper.put("gn:kantoneBB", "kantone");
            typenameMapper.put("gn:gemeindenBB", "gemeinden");
            typenameMapper.put("gn:xlinks", "xlinks");
            typenameMapper.put("gn:non_validated", "non_validated");
            //
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
            if (href != null) {
                final Element element = GetEditModel.this.resolveXLink(href, ServiceContext.get());
                if (element != null) {
                    String id = URLDecoder.decode(org.fao.geonet.kernel.reusable.Utils.id(href), "UTF-8");
                    final Matcher matcher = typenamePattern.matcher(href);
                    if (!matcher.find()) {
                        throw new AssertionError("Unable to extract the typename in extent href: " + href);
                    }
                    String featureType = typenameMapper.get(matcher.group(1));
                    if (featureType == null) {
                        throw new RuntimeException(matcher.group(1) + " is not a recognized featuretype of geonetwork");
                    }
                    json.put(Save.JSON_IDENTIFICATION_EXTENT_GEOM, featureType + ":" + id);
                }
            }
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
