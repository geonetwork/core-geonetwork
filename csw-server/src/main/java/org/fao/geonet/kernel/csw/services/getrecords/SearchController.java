//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.csw.services.getrecords;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.csw.services.getrecords.es.CswFilter2Es;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.EsFilterBuilder;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SearchController {

    public final static String DEFAULT_ELEMENTNAMES_STRATEGY = "relaxed";

    @Autowired
    NodeInfo node;

    @Autowired
    IMetadataUtils metadataUtils;

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private EsFilterBuilder esFilterBuilder;


    @Autowired
    private SchemaManager schemaManager;

    /**
     * Retrieves metadata from the database. Conversion between metadata record and output schema
     * are defined in xml/csw/schemas/ directory.
     *
     * @param context                        service context
     * @param id                             id of metadata
     * @param setName                        requested ElementSetName
     * @param outSchema                      requested OutputSchema
     * @param elemNames                      requested ElementNames
     * @param typeName                       requested typeName
     * @param resultType                     requested ResultType
     * @param strategy                       ElementNames strategy
     * @param checkMetadataAvailableInPortal Checks if the metadata can be retrieved in the portal.
     *                                       Used in GetRecordById. GetRecords does a query with this check already.
     * @return The XML metadata record if the record could be converted to the required output
     * schema. Null if no conversion available for the schema (eg. fgdc record can not be converted
     * to ISO).
     * @throws CatalogException hmm
     */
    public Element retrieveMetadata(ServiceContext context, String id, ElementSetName setName, String
        outSchema, Set<String> elemNames, String typeName, ResultType resultType, String strategy, String displayLanguage,
                                    boolean checkMetadataAvailableInPortal) throws CatalogException {

        if (checkMetadataAvailableInPortal) {
            // Check if the metadata is available in the portal
            String elasticSearchQuery = "{ \"bool\": {\n" +
                "            \"must\": [\n" +
                "        {" +
                "          \"term\": {" +
                "            \"id\": {" +
                "              \"value\": \"%s\"" +
                "            }" +
                "          }" +
                "        } " +
                "            ]\n" +
                "          ,\"filter\":{\"query_string\":{\"query\":\"%s\"}}}}";

            JsonNode esJsonQuery;

            try {
                String filterQueryString = esFilterBuilder.build(context, "metadata", false, node);
                String jsonQuery = String.format(elasticSearchQuery, id, filterQueryString);

                ObjectMapper objectMapper = new ObjectMapper();
                esJsonQuery = objectMapper.readTree(jsonQuery);

                Set<String> fieldsToRetrieve = new HashSet<>();
                fieldsToRetrieve.add("uuid");
                SearchResponse result = searchManager.query(esJsonQuery, fieldsToRetrieve, 0, 1);

                long numMatches = result.hits().hits().size();
                if (numMatches == 0) {
                    return null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        try {
            //--- get metadata from DB
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            Element res = gc.getBean(DataManager.class).getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
            SchemaManager scm = gc.getBean(SchemaManager.class);
            if (res == null) {
                return null;
            }
            Element info = res.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
            String schema = info.getChildText(Edit.Info.Elem.SCHEMA);

            // Add schemaLocation from schema config if not present in the metadata
            Attribute schemaLocAtt = scm.getSchemaLocation(
                schema, context);

            if (schemaLocAtt != null) {
                if (res.getAttribute(
                    schemaLocAtt.getName(),
                    schemaLocAtt.getNamespace()) == null) {
                    res.setAttribute(schemaLocAtt);
                    // make sure namespace declaration for schemalocation is present -
                    // remove it first (does nothing if not there) then add it
                    res.removeNamespaceDeclaration(schemaLocAtt.getNamespace());
                    res.addNamespaceDeclaration(schemaLocAtt.getNamespace());
                }
            }

            // apply stylesheet according to setName and schema
            //
            // OGC 07-045 :
            // Because for this application profile it is not possible that a query includes more than one
            // typename, any value(s) of the typeNames attribute of the elementSetName element are ignored.
            res = org.fao.geonet.csw.common.util.Xml.applyElementSetName(context, scm, schema, res, outSchema, setName, resultType, id, displayLanguage);

            res = applyElementNames(context, elemNames, typeName, scm, schema, res, resultType, info, strategy);

            if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "SearchController:retrieveMetadata: before applying postprocessing on metadata Element for id " + id);

            res = applyPostProcessing(context, scm, schema, res, outSchema, setName, resultType, id, displayLanguage);

            if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "SearchController:retrieveMetadata: All processing is complete on metadata Element for id " + id);

            if (res != null) {
                if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "SearchController returns\n" + Xml.getString(res));
            } else {
                if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "SearchController returns null");
            }
            return res;
        } catch (InvalidParameterValueEx e) {
            throw e;
        } catch (Exception e) {
            context.error("Error while getting metadata with id : " + id);
            context.error("  (C) StackTrace:\n" + Util.getStackTrace(e));
            throw new NoApplicableCodeEx("Raised exception while getting metadata :" + e);
        }
    }

    /**
     * Applies requested ElementNames and typeNames.
     * <p>
     * For ElementNames, several strategies are implemented. Clients can determine the behaviour by
     * sending attribute "elementname_strategy" with one of the following values:
     * <p>
     * csw202 relaxed context geonetwork26
     * <p>
     * The default is 'relaxed'. The strategies cause the following behaviour:
     * <p>
     * csw202 -- compliant to the CSW2.0.2 specification. In particular this means that complete
     * metadata are returned that match the requested ElementNames, only if they are valid for their
     * XSD. This is because GeoNetwork only supports OutputFormat=application/xml, which mandates
     * that valid documents are returned. Because possibly not many of the catalog's metadata are
     * valid, this is not the default.
     * <p>
     * relaxed -- like csw202, but dropped the requirement to only include valid metadata. So this
     * returns complete metadata that match the requested ElementNames. This is the default
     * strategy.
     * <p>
     * context -- does not return complete metadata but only the elements matching the request, in
     * their context (i.e. all ancestor elements up to the root of the document are retained). This
     * strategy is similar to geonetwork26 but the context allows clients to determine which of the
     * elements returned corresponds to which of the elements requested (in case they have the same
     * name).
     * <p>
     * geonetwork26 -- behaviour as in GeoNetwork 2.6. Just return the requested elements, stripped
     * of any context. This can make it impossible for the client to determine which of the elements
     * returned corresponds to which of the elements requested; for example if the client asks for
     * gmd:title, the response may contain various gmd:title elements taken from different locations
     * in the metadata document.
     * <p>
     * ------------------------------------------------- Relevant sections of specification about
     * typeNames:
     * <p>
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
     * <p>
     * OGC 07-045:
     * <p>
     * 8.2.2.1.1 Request (GetRecords) TypeNames. Must support *one* of “csw:Record” or
     * “gmd:MD_Metadata” in a query. Default value is “csw:Record”.
     * <p>
     * So, in OGC 07-045, exactly one of csw:Record or gmd:MD_Metadata is mandated for typeName.
     * <p>
     * ---------------------------------- Relevant specs about ElementNames:
     * <p>
     * OGC 07-006 10.8.4.9: The ElementName parameter is used to specify one or more metadata record
     * elements, from the output schema specified using the outputSchema parameter, that the query
     * shall present in the response to the a GetRecords operation. Since clause 10.2.5 realizes the
     * core metadata properties using XML schema, the value of the ElementName parameter would be an
     * XPath expression perhaps using qualified names. In the general case, a complete XPath
     * expression may be required to correctly reference an element in the information model of the
     * catalog.
     * <p>
     * However, in the case where the typeNames attribute on the Query element contains a single
     * value, the catalogue can infer the first step in the path expression and it can be omitted.
     * This is usually the case when querying the core metadata properties since the only queryable
     * target is csw:Record.
     * <p>
     * If the metadata record element names are not from the schema specified using the outputSchema
     * parameter, then the service shall raise an exception as described in Subclause 10.3.7.
     * <p>
     * OGC 07-045: Usage of the ELEMENTNAME is not further specified here.
     * <p>
     * ---------------------------------- Relevant specs about outputFormat:
     * <p>
     * OGC 07-006 10.8.4.4 outputFormat parameter: In the case where the output format is
     * application/xml, the CSW shall generate an XML document that validates against a schema
     * document that is specified in the output document via the xsi:schemaLocation attribute
     * defined in XML.
     *
     * @param context       servicecontext everywhere
     * @param elementNames  requested ElementNames
     * @param typeName      requested typeName
     * @param schemaManager schemamanager
     * @param schema        schema
     * @param result        result
     * @param resultType    requested ResultType
     * @param info          ?
     * @param strategy      - which ElementNames strategy to use (see Javadoc)
     * @return results of applying ElementNames filter
     * @throws InvalidParameterValueEx hmm
     */
    private static Element applyElementNames(ServiceContext context, Set<String> elementNames, String typeName,
                                             SchemaManager schemaManager, String schema, Element result,
                                             ResultType resultType, Element info, String strategy) throws InvalidParameterValueEx {
        if (elementNames != null) {

            if (StringUtils.isEmpty(strategy)) {
                strategy = DEFAULT_ELEMENTNAMES_STRATEGY;
            }

            if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "SearchController dealing with # " + elementNames.size() + " elementNames using strategy " + strategy);

            MetadataSchema mds = schemaManager.getSchema(schema);
            List<Namespace> namespaces = mds.getSchemaNS();

            Element matchingMetadata = (Element) result.clone();
            if (strategy.equals("context") || strategy.equals("geonetwork26")) {
                // these strategies do not return complete metadata
                matchingMetadata.removeContent();
            }

            boolean metadataContainsAllRequestedElementNames = true;
            List<Element> nodes = new ArrayList<>();
            for (String elementName : elementNames) {
                if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "SearchController dealing with elementName: " + elementName);
                try {
                    //
                    // OGC 07-006:
                    // In certain cases, such as when the typeNames attribute on the Query element only contains the
                    // name of a single entity, the root path step may be omitted since the catalogue is able to infer
                    // what the first step in the path would be.
                    //
                    // heikki: since in OGC 07-045 only 1 value for typeNames is allowed, the interpreation is as
                    // follows:
                    // case 1: elementname start with / : use it as the xpath as is;
                    // case 2: elementname does not start with / :
                    //      case 2a: elementname starts with one of the supported typeNames (csw:Record or gmd:MD_Metadata) : prepend /
                    //      case 2b: elementname does not start with one of the supported typeNames : prepend with /typeName//
                    //

                    String xpath;
                    // case 1: elementname starts with /
                    if (elementName.startsWith("/")) {
                        // use it as the xpath as is;
                        xpath = elementName;
                        if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH, "elementname start with root: " + elementName);
                    }
                    // case 2: elementname does not start with /
                    else {
                        // case 2a: elementname starts with one of the supported typeNames (csw:Record or gmd:MD_Metadata)
                        // TODO do not hardcode namespace prefixes
                        if (elementName.startsWith("csw:Record") || elementName.startsWith("gmd:MD_Metadata")) {
                            if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                                Log.debug(Geonet.CSW_SEARCH, "elementname starts with one of the supported typeNames : " + elementName);
                            // prepend /
                            xpath = "/" + elementName;
                        }
                        // case 2b: elementname does not start with one of the supported typeNames
                        else {
                            if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                                Log.debug(Geonet.CSW_SEARCH, "elementname does not start with one of the supported typeNames : " + elementName);
                            // prepend with /typeName/
                            xpath = "/" + typeName + "//" + elementName;
                        }
                    }
                    @SuppressWarnings("unchecked")
                    List<Element> elementsMatching = (List<Element>) Xml.selectDocumentNodes(result, xpath, namespaces);

                    if (strategy.equals("context")) {
                        if (Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
                            Log.debug(Geonet.CSW_SEARCH, "strategy is context, constructing context to root");
                        }

                        List<Element> elementsInContextMatching = new ArrayList<>();
                        for (Element match : elementsMatching) {
                            Element parent = match.getParentElement();
                            while (parent != null) {
                                parent.removeContent();
                                parent.addContent((Element) match.clone());
                                match = (Element) parent.clone();
                                parent = parent.getParentElement();
                            }
                            elementsInContextMatching.add(match);
                        }
                        elementsMatching = elementsInContextMatching;
                    }
                    nodes.addAll(elementsMatching);

                    if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "elemName " + elementName + " matched # " + nodes.size() + " nodes");

                    if (nodes.size() == 0) {
                        metadataContainsAllRequestedElementNames = false;
                        break;
                    }
                } catch (Exception x) {
                    Log.error(Geonet.CSW_SEARCH, x.getMessage(), x);
                    throw new InvalidParameterValueEx("elementName has invalid XPath : " + elementName, x.getMessage());
                }
            }

            if (metadataContainsAllRequestedElementNames) {
                if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "metadata containa all requested elementnames: included in response");

                if (strategy.equals("context") || strategy.equals("geonetwork26")) {
                    if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "adding only the matching fragments to result");
                    for (Element node : nodes) {
                        if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH, "adding node:\n" + Xml.getString(node));
                        matchingMetadata.addContent((Content) node.clone());
                    }
                } else {
                    if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "adding the complete metadata to results");
                    if (strategy.equals("csw202")) {
                        GeonetContext geonetContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                        DataManager dataManager = geonetContext.getBean(DataManager.class);
                        boolean valid = dataManager.validate(result);
                        if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH, "strategy csw202: only valid metadata is returned. This one is valid? " + valid);

                        if (!valid) {
                            return null;
                        }
                    }
                    matchingMetadata = result;
                }
                result = matchingMetadata;
            } else {
                if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "metadata does not contain all requested elementnames: not included in response");
                return null;
            }
        } else {
            if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "No ElementNames to apply");
        }
        return result;
    }

    /**
     * TODO improve description of method. Performs the general search tasks.
     *
     * @param context            Service context
     * @param startPos           start position (if paged)
     * @param maxRecords         max records to return
     * @param resultType         requested ResultType
     * @param outSchema          requested OutputSchema
     * @param setName            requested ElementSetName
     * @param filterExpr         requested FilterExpression
     * @param filterVersion      requested Filter version
     * @param sort               requested sorting
     * @param elemNames          requested ElementNames
     * @param typeName           requested typeName
     * @param maxHitsFromSummary ?
     * @param strategy           ElementNames strategy
     * @return result
     * @throws CatalogException hmm
     */
    public Element search(ServiceContext context, int startPos, int maxRecords,
                          ResultType resultType, String outSchema, ElementSetName setName,
                          Element filterExpr, String filterVersion, List<SortOptions> sort,
                          Set<String> elemNames, String typeName, int maxHitsFromSummary,
                          String strategy) throws CatalogException {

        String elasticSearchQuery = convertCswFilterToEsQuery(filterExpr, filterVersion);

        JsonNode esJsonQuery;

        try {
            String filterQueryString = esFilterBuilder.build(context, "metadata", false, node);
            String jsonQuery = String.format(elasticSearchQuery, filterQueryString);

            ObjectMapper objectMapper = new ObjectMapper();
            esJsonQuery = objectMapper.readTree(jsonQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);

        // TODO: Check to get summary or remove custom summary output

        try {
            SearchResponse result = searchManager.query(esJsonQuery, new HashSet<>(), startPos - 1, maxRecords, sort);

            List<Hit> hits = result.hits().hits();

            long numMatches = result.hits().hits().size();

            if (numMatches != 0 && startPos > numMatches) {
                throw new InvalidParameterValueEx("startPosition", String.format(
                    "Start position (%d) can't be greater than number of matching records (%d for current search).",
                    startPos, numMatches
                ));
            }

            int counter = 0;

            ObjectMapper objectMapper = new ObjectMapper();

            for (Hit hit : hits) {
                int mdId = Integer.parseInt((String) objectMapper.convertValue(hit.source(), Map.class).get("id"));

                AbstractMetadata metadata = metadataUtils.findOne(mdId);

                String displayLanguage = context.getLanguage();
                // The query to retrieve GetRecords, filters by portal. No need to re-check again when retrieving each metadata.
                Element resultMD = retrieveMetadata(context, metadata.getId() + "",
                    setName, outSchema, elemNames, typeName, resultType, strategy, displayLanguage, false);

                if (resultMD != null) {
                    if (resultType == ResultType.RESULTS) {
                        results.addContent(resultMD);
                    }

                    counter++;
                }

            }

            results.setAttribute("numberOfRecordsMatched", Long.toString(numMatches));
            results.setAttribute("numberOfRecordsReturned", Long.toString(counter));
            results.setAttribute("elementSet", setName.toString());


            if (numMatches > counter) {
                results.setAttribute("nextRecord", Long.toString(counter + startPos));
            } else {
                results.setAttribute("nextRecord", "0");
            }

            return results;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        //context.getUserSession().setProperty(Geonet.Session.SEARCH_RESULT, searcher);
    }


    /**
     * Applies stylesheet according to ElementSetName and schema.
     *
     * @param context        Service context
     * @param schemaManager  schemamanager
     * @param schema         schema
     * @param result         result
     * @param outputSchema   requested OutputSchema
     * @param elementSetName requested ElementSetName
     * @param resultType     requested ResultTYpe
     * @param id             metadata id
     * @return metadata
     * @throws InvalidParameterValueEx hmm
     */
    public Element applyElementSetName(ServiceContext context, SchemaManager schemaManager, String schema,
                                       Element result, String outputSchema, ElementSetName elementSetName,
                                       ResultType resultType, String id, String displayLanguage) throws InvalidParameterValueEx {
        Path schemaDir = schemaManager.getSchemaCSWPresentDir(schema);
        Path styleSheet = schemaDir.resolve(outputSchema + "-" + elementSetName + ".xsl");

        if (!Files.exists(styleSheet)) {
            context.warning(
                String.format(
                    "OutputSchema '%s' not supported for metadata with '%s' (%s). Corresponding XSL transformation '%s' does not exist. The record will not be returned in response.",
                    outputSchema, id, schema, styleSheet.toString()));
            return null;
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("lang", displayLanguage);

            try {
                result = Xml.transform(result, styleSheet, params);
            } catch (Exception e) {
                context.error("Error while transforming metadata with id : " + id + " using " + styleSheet);
                context.error("  (C) StackTrace:\n" + Util.getStackTrace(e));
                return null;
            }
            return result;
        }
    }

    private String convertCswFilterToEsQuery(Element xml, String filterVersion) {
        return CswFilter2Es.translate(FilterParser.parseFilter(xml, filterVersion), fieldMapper);
    }

    /**
     * Applies postprocessing stylesheet if available.
     * <p>
     * Postprocessing files should be in the present/csw folder of the schema and have this naming:
     * <p>
     * For default CSW service
     * <p>
     * 1) gmd-csw-postprocessing.xsl : Postprocessing xsl applied for CSW service when requesting iso (gmd) output
     * 2) csw-csw-postprocessing.xsl : Postprocessing xsl applied for CSW service when requesting ogc (csw) output
     * <p>
     * For a custom sub-portal named inspire
     * <p>
     * 1) gmd-inspire-postprocessing.xsl : Postprocessing xsl applied for custom inspire sub-portal when requesting iso output
     * 2) csw-inspire-postprocessing.xsl : Postprocessing xsl applied for custom inspire sub-portal when requesting ogc (csw) output
     *
     * @param context         Service context
     * @param schemaManager   schemamanager
     * @param schema          schema
     * @param result          result
     * @param outputSchema    requested OutputSchema
     * @param elementSetName  requested ElementSetName
     * @param resultType      requested ResultTYpe
     * @param id              metadata id
     * @param displayLanguage language to use in response
     * @return metadata
     * @throws InvalidParameterValueEx hmm
     */
    private static Element applyPostProcessing(ServiceContext context, SchemaManager schemaManager, String schema,
                                               Element result, String outputSchema, ElementSetName elementSetName,
                                               ResultType resultType, String id, String displayLanguage) throws InvalidParameterValueEx {
        Path schemaDir = schemaManager.getSchemaCSWPresentDir(schema);
        final NodeInfo nodeInfo = ApplicationContextHolder.get().getBean(NodeInfo.class);


        Path styleSheet = schemaDir.resolve(outputSchema + "-"
            + (context.getService().equals("csw") ? nodeInfo.getId() : context.getService())
            + "-postprocessing.xsl");

        if (Files.exists(styleSheet)) {
            Map<String, Object> params = new HashMap<>();
            params.put("lang", displayLanguage);

            try {
                result = Xml.transform(result, styleSheet, params);
            } catch (Exception e) {
                context.error("Error while transforming metadata with id : " + id + " using " + styleSheet);
                context.error("  (C) StackTrace:\n" + Util.getStackTrace(e));
                return null;
            }
        }

        return result;
    }
}
