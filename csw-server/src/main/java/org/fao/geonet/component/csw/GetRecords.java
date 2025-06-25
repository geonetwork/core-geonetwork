//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.component.csw;

import co.elastic.clients.elasticsearch._types.SortOptions;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.*;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.domain.CustomElementSet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.kernel.csw.services.getrecords.SortByParser;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.CustomElementSetRepository;
import org.fao.geonet.util.xml.NamespaceUtils;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.file.Path;
import java.util.*;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_CSW_ENABLEWHENINDEXING;

/**
 * See OGC 07-006 and OGC 07-045.
 */
@Component(CatalogService.BEAN_PREFIX + GetRecords.NAME)
public class GetRecords extends AbstractOperation implements CatalogService {

    static final String NAME = "GetRecords";

    /**
     * OGC 07-006 10.8.4.4.
     */
    private static final String defaultOutputFormat = "application/xml";

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    @Autowired
    private SearchController _searchController;

    @Autowired
    private CatalogConfiguration _catalogConfig;
    @Autowired
    private FieldMapper _fieldMapper;
    @Autowired
    private SortByParser _sortByParser;

    @Autowired
    private SchemaManager _schemaManager;

    @Autowired
    public GetRecords(ApplicationContext context) {
    }

    /**
     * @return
     */
    public SearchController getSearchController() {
        return _searchController;
    }

    ;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    public String getName() {
        return NAME;
    }

    /**
     *
     * @param request
     * @param context
     * @return
     * @throws CatalogException
     */
    public Element execute(Element request, ServiceContext context) throws CatalogException {
        String timeStamp = new ISODate().toString();

        // Return exception is indexing.
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager settingsManager = gc.getBean(SettingManager.class);
        if (!settingsManager.getValueAsBool(SYSTEM_CSW_ENABLEWHENINDEXING) &&
            BatchOpsMetadataReindexer.isIndexing()) {
            throw new RuntimeException("Catalog is indexing records, retry later.");
        }

        //
        // some validation checks (note: this is not an XSD validation)
        //

        // must be "CSW"
        checkService(request);

        // must be "2.0.2"
        checkVersion(request);

        // GeoNetwork only supports "application/xml"
        checkOutputFormat(request);

        // one of ElementName XOR ElementSetName must be requested
        checkElementNamesXORElementSetName(request);

        // optional integer, value at least 1
        int startPos = getStartPosition(request);

        // optional integer, value at least 1
        int maxRecords = getMaxRecords(request);

        Element query = request.getChild("Query", Csw.NAMESPACE_CSW);

        // one of "hits", "results", "validate".
        ResultType resultType = ResultType.parse(request.getAttributeValue("resultType"));

        // either Record or IsoRecord
        String outSchema = OutputSchema.parse(request.getAttributeValue("outputSchema"), _schemaManager);

        // GeoNetwork-specific parameter defining how to deal with ElementNames. See documentation in
        // SearchController.applyElementNames() about these strategies.
        String elementnameStrategy = getElementNameStrategy(query);

        // value csw:Record or gmd:MD_Metadata
        // heikki: this is actually a List (in OGC 07-006), though OGC 07-045 states:
        // "Because for this application profile it is not possible that a query includes more than one typename, .."
        // So: a single String should be OK. However to increase backwards-compatibility with existing clients sending
        // a comma separated list (such as GN's own CSW Harvesting Client), the check assumes a comma-separated list,
        // and checks whether its values are not other than csw:Record or gmd:MD_Metadata. If both are sent,
        // gmd:MD_Metadata is preferred.
        final SettingManager settingInfo = context.getBean(SettingManager.class);
        String typeName = checkTypenames(query, settingInfo.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE));

        // set of elementnames or null
        Set<String> elemNames = getElementNames(query);

        // If any element names are specified, it's an ad hoc query and overrides the element set name default. In that
        // case, we set setName to FULL instead of SUMMARY so that we can retrieve a CSW:Record and trim out the
        // elements that aren't in the elemNames set.
        ElementSetName setName = ElementSetName.FULL;

        //
        // no ElementNames requested: use ElementSetName
        //
        if ((elemNames == null)) {
            setName = getElementSetName(query, ElementSetName.SUMMARY);
            // elementsetname is FULL: use customized elementset if defined
            if (setName.equals(ElementSetName.FULL)) {
                final List<CustomElementSet> customElementSets = context.getBean(CustomElementSetRepository.class).findAll();
                // custom elementset defined
                if (!CollectionUtils.isEmpty(customElementSets)) {
                    elemNames = new HashSet<String>();
                    for (CustomElementSet customElementSet : customElementSets) {
                        elemNames.add(customElementSet.getXpath());
                    }
                }
            }
        }


        // "Constraint Optional" & "Must be specified with QUERYCONSTRAINT parameter"
        Element constr = query.getChild("Constraint", Csw.NAMESPACE_CSW);
        Element filterExpr = getFilterExpression(constr);
        String filterVersion = getFilterVersion(constr);

        // Get max hits to be used for summary - CSW GeoNetwork extension
        int maxHitsInSummary = 1000;
        String sMaxRecordsInKeywordSummary = query.getAttributeValue("maxHitsInSummary");
        if (sMaxRecordsInKeywordSummary != null) {
            // TODO : it could be better to use service config parameter instead
            // sMaxRecordsInKeywordSummary = config.getValue("maxHitsInSummary", "1000");
            maxHitsInSummary = Integer.parseInt(sMaxRecordsInKeywordSummary);
        }

        Element response;
        if (resultType == ResultType.VALIDATE) {
            //String schema = context.getAppPath() + Geonet.Path.VALIDATION + "csw/2.0.2/csw-2.0.2.xsd";
            Path schema = context.getAppPath().resolve(Geonet.Path.VALIDATION).resolve("csw202_apiso100/csw/2.0.2/CSW-discovery.xsd");

            if (Log.isDebugEnabled(Geonet.CSW))
                Log.debug(Geonet.CSW, "Validating request against " + schema);
            try {
                Xml.validate(schema, request);
            } catch (Exception e) {
                throw new NoApplicableCodeEx("Request failed validation:" + e.toString());
            }

            response = new Element("Acknowledgement", Csw.NAMESPACE_CSW);
            response.setAttribute("timeStamp", timeStamp);

            Element echoedRequest = new Element("EchoedRequest", Csw.NAMESPACE_CSW);
            echoedRequest.addContent(request);

            response.addContent(echoedRequest);
        } else {
            List<SortOptions> sort = _sortByParser.parseSortBy(request);

            response = new Element(getName() + "Response", Csw.NAMESPACE_CSW);

            Attribute schemaLocation = new Attribute("schemaLocation", "http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd", Csw.NAMESPACE_XSI);
            response.setAttribute(schemaLocation);

            Element status = new Element("SearchStatus", Csw.NAMESPACE_CSW);
            status.setAttribute("timestamp", timeStamp);

            response.addContent(status);

            Element search = _searchController.search(context, startPos, maxRecords, resultType, outSchema,
                setName, filterExpr, filterVersion, sort, elemNames, typeName, maxHitsInSummary, elementnameStrategy);

            response.addContent(search);
        }
        return response;
    }

    /**
     * TODO javadoc.
     */
    public Element adaptGetRequest(Map<String, String> params) throws CatalogException {
        String service = params.get("service");
        String version = params.get("version");
        String resultType = params.get("resulttype");
        String outputFormat = params.get("outputformat");
        String outputSchema = params.get("outputschema");
        String startPosition = params.get("startposition");
        String maxRecords = params.get("maxrecords");
        String hopCount = params.get("hopcount");
        String distribSearch = params.get("distributedsearch");
        String typeNames = params.get("typenames");
        String elemSetName = params.get("elementsetname");
        String elemName = params.get("elementname");
        String constraint = params.get("constraint");
        String constrLang = params.get("constraintlanguage");
        String constrLangVer = params.get("constraint_language_version");
        String sortby = params.get("sortby");
        String elementnameStrategy = params.get("elementnamestrategy");

        //--- build POST request

        Element request = new Element(getName(), Csw.NAMESPACE_CSW);

        setAttrib(request, "service", service);
        setAttrib(request, "version", version);
        setAttrib(request, "resultType", resultType);
        setAttrib(request, "outputFormat", outputFormat);
        setAttrib(request, "outputSchema", outputSchema);
        setAttrib(request, "startPosition", startPosition);
        setAttrib(request, "maxRecords", maxRecords);

        if (distribSearch != null && distribSearch.equals("true")) {
            Element ds = new Element("DistributedSearch", Csw.NAMESPACE_CSW);
            ds.setText("TRUE");

            if (hopCount != null) {
                ds.setAttribute("hopCount", hopCount);
            }
            request.addContent(ds);
        }

        //--- build query element

        Element query = new Element("Query", Csw.NAMESPACE_CSW);
        request.addContent(query);

        if (elementnameStrategy != null) {
            setAttrib(query, "elementnamestrategy", elementnameStrategy);
        }

        if (typeNames != null) {
            setAttrib(query, "typeNames", typeNames.replace(',', ' '));
        }
        //--- these 2 are in mutual exclusion

        addElement(query, "ElementSetName", elemSetName);
        fill(query, "ElementName", elemName);

        //--- handle constraint

        if (constraint != null) {
            ConstraintLanguage language = ConstraintLanguage.parse(constrLang);
            Element constr = new Element("Constraint", Csw.NAMESPACE_CSW);
            query.addContent(constr);

            if (language == ConstraintLanguage.CQL) {
                addElement(constr, "CqlText", constraint);
            } else {
                try {
                    constr.addContent(Xml.loadString(constraint, false));
                } catch (Exception e) {
                    Log.error(Geonet.CSW_SEARCH, "Constraint is not a valid xml, error:" + e.getMessage(), e);
                    throw new NoApplicableCodeEx("Constraint is not a valid xml");
                }
            }
            setAttrib(constr, "version", constrLangVer);
        }

        //--- handle sortby

        if (sortby != null) {
            Element sortBy = new Element("SortBy", Csw.NAMESPACE_OGC);
            query.addContent(sortBy);

            StringTokenizer st = new StringTokenizer(sortby, ",");
            while (st.hasMoreTokens()) {
                String sortInfo = st.nextToken();
                String field = sortInfo.substring(0, sortInfo.length() - 2);
                boolean ascen = sortInfo.endsWith(":A");

                Element sortProp = new Element("SortProperty", Csw.NAMESPACE_OGC);
                sortBy.addContent(sortProp);

                Element propName = new Element("PropertyName", Csw.NAMESPACE_OGC)
                    .setText(field);
                Element sortOrder = new Element("SortOrder", Csw.NAMESPACE_OGC)
                    .setText(ascen ? "ASC" : "DESC");

                sortProp.addContent(propName);
                sortProp.addContent(sortOrder);
            }
        }

        return request;
    }

    /**
     * TODO javadoc.
     */
    public Element retrieveValues(String parameterName) throws CatalogException {

        Element listOfValues = null;
        if (parameterName.equalsIgnoreCase("resultType")
            || parameterName.equalsIgnoreCase("outputFormat")
            || parameterName.equalsIgnoreCase("elementSetName")
            || parameterName.equalsIgnoreCase("outputSchema")
            || parameterName.equalsIgnoreCase("typenames"))
            listOfValues = new Element("ListOfValues", Csw.NAMESPACE_CSW);

        // Handle resultType parameter
        if (parameterName.equalsIgnoreCase("resultType")) {
            List<Element> values = new ArrayList<Element>();
            ResultType[] resultType = ResultType.values();
            for (ResultType aResultType : resultType) {
                String value = aResultType.toString();
                values.add(new Element("Value", Csw.NAMESPACE_CSW).setText(value));
            }
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        // Handle elementSetName parameter
        if (parameterName.equalsIgnoreCase("elementSetName")) {
            List<Element> values = new ArrayList<Element>();
            ElementSetName[] esn = ElementSetName.values();
            for (ElementSetName anEsn : esn) {
                String value = anEsn.toString();
                values.add(new Element("Value", Csw.NAMESPACE_CSW).setText(value));
            }
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        // Handle outputFormat parameter
        if (parameterName.equalsIgnoreCase("outputformat")) {
            Set<String> formats = _catalogConfig
                .getGetRecordsOutputFormat();
            List<Element> values = createValuesElement(formats);
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        // Handle outputSchema parameter
        if (parameterName.equalsIgnoreCase("outputSchema")) {
            Set<String> namespacesUri = _catalogConfig
                .getGetRecordsOutputSchema();
            List<Element> values = createValuesElement(namespacesUri);
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        // Handle typenames parameter
        if (parameterName.equalsIgnoreCase("typenames")) {
            Set<String> typenames = _catalogConfig
                .getGetRecordsTypenames();
            List<Element> values = createValuesElement(typenames);
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        return listOfValues;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    /**
     * GeoNetwork only supports default value for outputFormat.
     *
     * OGC 07-006: The only value that is required to be supported is application/xml. Other
     * supported values may include text/html and text/plain. Cardinality: Zero or one (Optional).
     * Default value is application/xml.
     *
     * In the case where the output format is application/xml, the CSW shall generate an XML
     * document that validates against a schema document that is specified in the output document
     * via the xsi:schemaLocation attribute defined in XML.
     *
     * @param request GetRecords request
     * @throws InvalidParameterValueEx hmm
     */
    private String checkOutputFormat(Element request) throws InvalidParameterValueEx {
        String format = request.getAttributeValue("outputFormat");
        if (format != null && !format.equals(defaultOutputFormat)) {
            throw new InvalidParameterValueEx("outputFormat", format);
        } else {
            return defaultOutputFormat;
        }
    }

    /**
     * If the request contains a Query element, it must have attribute typeNames.
     *
     * The OGC 07-045 spec is more restrictive than OGC 07-006.
     *
     * OGC 07-006 10.8.4.8: The typeNames parameter is a list of one or more names of queryable
     * entities in the catalogue's information model that may be constrained in the predicate of the
     * query. In the case of XML realization of the OGC core metadata properties (Subclause 10.2.5),
     * the element csw:Record is the only queryable entity. Other information models may include
     * more than one queryable component. For example, queryable components for the XML realization
     * of the ebRIM include rim:Service, rim:ExtrinsicObject and rim:Association. In such cases the
     * application profile shall describe how multiple typeNames values should be processed. In
     * addition, all or some of the these queryable entity names may be specified in the query to
     * define which metadata record elements the query should present in the response to the
     * GetRecords operation.
     *
     * OGC 07-045 8.2.2.1.1: Mandatory: Must support *one* of â€œcsw:Recordâ€� or
     * â€œgmd:MD_Metadataâ€� in a query. Default value is â€œcsw:Recordâ€�.
     *
     * (note how OGC 07-045 mixes up a mandatory parameter that has a default value !!)
     *
     * We'll go for the default value option rather than the mandatory-ness. So: if typeNames is not
     * present or empty, "csw:Record" is used.
     *
     * If the request does not contain exactly one (or comma-separated, both) of the values
     * specified in OGC 07-045, an exception is thrown. If both are present "gmd:MD_Metadata" is
     * preferred.
     *
     * @param query    query element
     * @param isStrict enable strict error message to comply with GDI-DE Testsuite test
     *                 csw:InterfaceBindings.GetRecords-InvalidRequest
     * @return typeName
     * @throws MissingParameterValueEx if typeNames is missing
     * @throws InvalidParameterValueEx if typeNames does not have one of the mandated values
     */
    private String checkTypenames(Element query, boolean isStrict) throws MissingParameterValueEx, InvalidParameterValueEx {
        if (Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
            Log.debug(Geonet.CSW_SEARCH, "checking typenames in query:\n" + Xml.getString(query));
        }
        //
        // get the prefix used for CSW namespace used in this input document
        //
        String cswPrefix = getPrefixForNamespace(query, Csw.NAMESPACE_CSW);
        if (cswPrefix == null) {
            if (Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
                Log.debug(Geonet.CSW_SEARCH, "checktypenames: csw prefix not found, using " + Csw.NAMESPACE_CSW.getPrefix());
            }
            cswPrefix = Csw.NAMESPACE_CSW.getPrefix();
        }
        //
        // get the prefix used for GMD namespace used in this input document
        //
        String gmdPrefix = getPrefixForNamespace(query, Csw.NAMESPACE_GMD);
        if (gmdPrefix == null) {
            if (Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
                Log.debug(Geonet.CSW_SEARCH, "checktypenames: gmd prefix not found, using " + Csw.NAMESPACE_GMD.getPrefix());
            }
            gmdPrefix = Csw.NAMESPACE_GMD.getPrefix();
        }
        if (Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
            Log.debug(Geonet.CSW_SEARCH, "checktypenames: csw prefix set to " + cswPrefix + ", gmd prefix set to " + gmdPrefix);
        }

        Attribute typeNames = query.getAttribute("typeNames", query.getNamespace());
        typeNames = query.getAttribute("typeNames");
        if (typeNames != null) {
            String typeNamesValue = typeNames.getValue();
            // empty typenames element
            if (StringUtils.isEmpty(typeNamesValue)) {
                return cswPrefix + ":Record";
            }
            // not empty: scan space-separated string
            @SuppressWarnings("resource")
            Scanner spaceScanner = new Scanner(typeNamesValue);
            spaceScanner.useDelimiter(" ");
            String result = cswPrefix + ":Record";
            while (spaceScanner.hasNext()) {
                String typeName = spaceScanner.next();
                typeName = typeName.trim();
                if (Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
                    Log.debug(Geonet.CSW_SEARCH, "checking typename in query:" + typeName);
                }

                if (!_schemaManager.getListOfTypeNames().contains(typeName)) {
                    throw new InvalidParameterValueEx("typeNames",
                        String.format("'%s' typename is not valid. Supported values are: %s", typeName, _schemaManager.getListOfTypeNames()));
                }
                if (typeName.equals(gmdPrefix + ":MD_Metadata")) {
                    return typeName;
                }
            }
            return result;
        }
        // missing typeNames element
        else {
            if (isStrict) {
                //Mandatory check if strict.
                throw new MissingParameterValueEx("typeNames",
                    String.format("Attribute 'typeNames' is missing. Supported values are: %s. Default is csw:Record according to OGC 07-045.",
                        _schemaManager.getListOfTypeNames()));
            } else {
                //Return default value according to OGC 07-045.
                return cswPrefix + ":Record";
            }
        }
    }

    /**
     * Returns the prefix used in the scope of an element for a particular namespace, or null if the
     * namespace is not in scope.
     */
    private String getPrefixForNamespace(Element element, Namespace namespace) {
        List<Namespace> namespacesInScope = NamespaceUtils.getNamespacesInScope(element);
        for (Namespace ns : namespacesInScope) {
            if (ns.getURI().equals(namespace.getURI())) {
                return ns.getPrefix();
            }
        }
        return null;
    }

    /**
     * GeoNetwork-specific parameter to control the behaviour when dealing with ElementNames.
     * Supported values are 'csw202', 'relaxed' , 'context'  and 'geonetwork26'. If the parameter is
     * missing or does not have one of these values, the default value 'relaxed' is used. See
     * documentation in SearchController.applyElementNames() about these strategies.
     *
     * @param query query element
     * @return elementnames strategy
     */
    private String getElementNameStrategy(Element query) {
        if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "getting elementnameStrategy from query:\n" + Xml.getString(query));
        Attribute elementNameStrategyA = query.getAttribute("elementnameStrategy");
        // default
        String elementNameStrategy = "relaxed";
        if (elementNameStrategyA != null) {
            elementNameStrategy = elementNameStrategyA.getValue();
        }
        // empty or not one of the supported values
        if (StringUtils.isNotEmpty(elementNameStrategy) &&
            !(elementNameStrategy.equals("csw202") ||
                elementNameStrategy.equals("relaxed") ||
                elementNameStrategy.equals("context") ||
                elementNameStrategy.equals("geonetwork26"))) {
            // use default
            elementNameStrategy = "relaxed";
        }
        if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "elementNameStrategy: " + elementNameStrategy);
        return elementNameStrategy;
    }

    /**
     * Checks that not ElementName and ElementSetName are both present in the query, see OGC 07-006
     * section 10 8 4 9.
     */
    private void checkElementNamesXORElementSetName(Element request) throws InvalidParameterValueEx {
        Element query = request.getChild("Query", Csw.NAMESPACE_CSW);
        if (query != null) {
            boolean elementNamePresent = !CollectionUtils.isEmpty(query.getChildren("ElementName", query.getNamespace()));
            boolean elementSetNamePresent = !CollectionUtils.isEmpty(query.getChildren("ElementSetName", query.getNamespace()));
            if (elementNamePresent && elementSetNamePresent) {
                throw new InvalidParameterValueEx("ElementName and ElementSetName", "mutually exclusive");
            }
        }
    }

    /**
     * OGC 07-006 and OGC 07-045: Non-zero, positive Integer. Optional. The default value is 1.
     *
     * @param request the request
     * @return startPosition
     * @throws InvalidParameterValueEx hmm
     */
    private int getStartPosition(Element request) throws InvalidParameterValueEx {
        String start = request.getAttributeValue("startPosition");
        if (start == null) {
            return 1;
        }
        try {
            int value = Integer.parseInt(start);
            if (value >= 1) {
                return value;
            } else {
                throw new InvalidParameterValueEx("startPosition", start);
            }
        } catch (NumberFormatException x) {
            throw new InvalidParameterValueEx("startPosition", start);
        }
    }

    /**
     * OGC 07-006 and OGC 07-045: PositiveInteger. Optional. The default value is 10.
     *
     * @param request the request
     * @return maxRecords
     * @throws InvalidParameterValueEx hmm
     */
    private int getMaxRecords(Element request) throws InvalidParameterValueEx {
        String max = request.getAttributeValue("maxRecords");
        if (max == null) {
            return 10;
        }
        try {
            int value = Integer.parseInt(max);
            if (value >= 1) {
                return value;
            } else {
                throw new InvalidParameterValueEx("maxRecords", max);
            }
        } catch (NumberFormatException x) {
            throw new InvalidParameterValueEx("maxRecords", max);
        }
    }

    /**
     * Returns the values of ElementNames in the query, or null if there are none.
     *
     * @param query the query
     * @return set of elementname values
     */
    private Set<String> getElementNames(Element query) {
        if (Log.isDebugEnabled(Geonet.CSW))
            Log.debug(Geonet.CSW, "GetRecords getElementNames");
        Set<String> elementNames = null;
        if (query != null) {
            @SuppressWarnings("unchecked")
            List<Element> elementList = query.getChildren("ElementName", query.getNamespace());
            for (Element element : elementList) {
                if (elementNames == null) {
                    elementNames = new HashSet<String>();
                }
                elementNames.add(element.getTextNormalize());
            }
        }
        // TODO in if(isDebugEnabled) condition. Jeeves LOG doesn't provide that useful function though.
        if (elementNames != null) {
            for (String elementName : elementNames) {
                if (Log.isDebugEnabled(Geonet.CSW))
                    Log.debug(Geonet.CSW, "ElementName: " + elementName);
            }
        } else {
            if (Log.isDebugEnabled(Geonet.CSW))
                Log.debug(Geonet.CSW, "No ElementNames found in request");
        }
        // TODO end if(isDebugEnabled)
        return elementNames;
    }


    /**
     * Retrieves the values of attribute typeNames. If typeNames contains csw:BriefRecord or csw:SummaryRecord, an
     * exception is thrown.
     *
     * @param query the query
     * @return list of typenames, or null if not found
     * @throws InvalidParameterValueEx if a typename is illegal
     */
//    private Set<String> getTypeNames(Element query) throws InvalidParameterValueEx {
//        Set<String> typeNames = null;
//        String typeNames$ = query.getAttributeValue("typeNames");
//        if(typeNames$ != null) {
//            Scanner commaSeparatedScanner = new Scanner(typeNames$).useDelimiter(",");
//            while(commaSeparatedScanner.hasNext()) {
//                String typeName = commaSeparatedScanner.next().trim();
//                // These two are explicitly not allowed as search targets in CSW 2.0.2, so we throw an exception if the
//                // client asks for them
//                if (typeName.equals("csw:BriefRecord") || typeName.equals("csw:SummaryRecord")) {
//                    throw new InvalidParameterValueEx("typeName", typeName);
//                }
//                if(typeNames == null) {
//                    typeNames = new HashSet<String>();
//                }
//                typeNames.add(typeName);
//            }
//        }
//        // TODO in if(isDebugEnabled) condition. Jeeves LOG doesn't provide that useful function though.
//        if(typeNames != null) {
//            for(String typeName : typeNames) {
//                if(Log.isDebugEnabled(Geonet.CSW))
//                    Log.debug(Geonet.CSW, "TypeName: " + typeName);
//            }
//        }
//        else {
//            if(Log.isDebugEnabled(Geonet.CSW))
//                Log.debug(Geonet.CSW, "No TypeNames found in request");
//        }
//        // TODO end if(isDebugEnabled)
//        return typeNames;
//    }

}
