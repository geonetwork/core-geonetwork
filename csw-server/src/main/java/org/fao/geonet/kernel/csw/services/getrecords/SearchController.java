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

package org.fao.geonet.kernel.csw.services.getrecords;

import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Sort;
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
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.gml2.GMLConfiguration;
import org.jdom.*;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO javadoc.
 */
public class SearchController {

    public final static String DEFAULT_ELEMENTNAMES_STRATEGY = "relaxed";
    private final Set<String> _selector;
    private final Set<String> _uuidselector;
    private GMLConfiguration _gmlConfig;
    private ApplicationContext _applicationContext;

    //---------------------------------------------------------------------------
    //---
    //--- Single public method to perform the general search tasks
    //---
    //---------------------------------------------------------------------------

    public SearchController(ApplicationContext applicationContext) {
        _selector = Collections.singleton("_id");
        _uuidselector = Collections.singleton("_uuid");
        _gmlConfig = new GMLConfiguration();
        this._applicationContext = applicationContext;
    }

    /**
     * Retrieves metadata from the database. Conversion between metadata record and output schema
     * are defined in xml/csw/schemas/ directory.
     *
     * @param context    service context
     * @param id         id of metadata
     * @param setName    requested ElementSetName
     * @param outSchema  requested OutputSchema
     * @param elemNames  requested ElementNames
     * @param typeName   requested typeName
     * @param resultType requested ResultType
     * @param strategy   ElementNames strategy
     * @throws CatalogException hmm
     * @return The XML metadata record if the record could be converted to the required output
     * schema. Null if no conversion available for the schema (eg. fgdc record can not be converted
     * to ISO).
     */
    public static Element retrieveMetadata(ServiceContext context, String id, ElementSetName setName, String
        outSchema, Set<String> elemNames, String typeName, ResultType resultType, String strategy, String displayLanguage) throws CatalogException {

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

            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "SearchController:retrieveMetadata: before applying postprocessing on metadata Element for id " + id);

            res = applyPostProcessing(context, scm, schema, res, outSchema, setName, resultType, id, displayLanguage);

            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
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
     *
     * For ElementNames, several strategies are implemented. Clients can determine the behaviour by
     * sending attribute "elementname_strategy" with one of the following values:
     *
     * csw202 relaxed context geonetwork26
     *
     * The default is 'relaxed'. The strategies cause the following behaviour:
     *
     * csw202 -- compliant to the CSW2.0.2 specification. In particular this means that complete
     * metadata are returned that match the requested ElementNames, only if they are valid for their
     * XSD. This is because GeoNetwork only supports OutputFormat=application/xml, which mandates
     * that valid documents are returned. Because possibly not many of the catalog's metadata are
     * valid, this is not the default.
     *
     * relaxed -- like csw202, but dropped the requirement to only include valid metadata. So this
     * returns complete metadata that match the requested ElementNames. This is the default
     * strategy.
     *
     * context -- does not return complete metadata but only the elements matching the request, in
     * their context (i.e. all ancestor elements up to the root of the document are retained). This
     * strategy is similar to geonetwork26 but the context allows clients to determine which of the
     * elements returned corresponds to which of the elements requested (in case they have the same
     * name).
     *
     * geonetwork26 -- behaviour as in GeoNetwork 2.6. Just return the requested elements, stripped
     * of any context. This can make it impossible for the client to determine which of the elements
     * returned corresponds to which of the elements requested; for example if the client asks for
     * gmd:title, the response may contain various gmd:title elements taken from different locations
     * in the metadata document.
     *
     * ------------------------------------------------- Relevant sections of specification about
     * typeNames:
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
     * OGC 07-045:
     *
     * 8.2.2.1.1 Request (GetRecords) TypeNames. Must support *one* of “csw:Record” or
     * “gmd:MD_Metadata” in a query. Default value is “csw:Record”.
     *
     * So, in OGC 07-045, exactly one of csw:Record or gmd:MD_Metadata is mandated for typeName.
     *
     * ---------------------------------- Relevant specs about ElementNames:
     *
     * OGC 07-006 10.8.4.9: The ElementName parameter is used to specify one or more metadata record
     * elements, from the output schema specified using the outputSchema parameter, that the query
     * shall present in the response to the a GetRecords operation. Since clause 10.2.5 realizes the
     * core metadata properties using XML schema, the value of the ElementName parameter would be an
     * XPath expression perhaps using qualified names. In the general case, a complete XPath
     * expression may be required to correctly reference an element in the information model of the
     * catalog.
     *
     * However, in the case where the typeNames attribute on the Query element contains a single
     * value, the catalogue can infer the first step in the path expression and it can be omitted.
     * This is usually the case when querying the core metadata properties since the only queryable
     * target is csw:Record.
     *
     * If the metadata record element names are not from the schema specified using the outputSchema
     * parameter, then the service shall raise an exception as described in Subclause 10.3.7.
     *
     * OGC 07-045: Usage of the ELEMENTNAME is not further specified here.
     *
     * ---------------------------------- Relevant specs about outputFormat:
     *
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
            List<Element> nodes = new ArrayList<Element>();
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

                        List<Element> elementsInContextMatching = new ArrayList<Element>();
                        for (Element match : elementsInContextMatching) {
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

            if (metadataContainsAllRequestedElementNames == true) {
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

                if (resultType == ResultType.RESULTS_WITH_SUMMARY) {
                    matchingMetadata.addContent((Content) info.clone());
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
     * @param context                     Service context
     * @param startPos                    start position (if paged)
     * @param maxRecords                  max records to return
     * @param resultType                  requested ResultType
     * @param outSchema                   requested OutputSchema
     * @param setName                     requested ElementSetName
     * @param filterExpr                  requested FilterExpression
     * @param filterVersion               requested Filter version
     * @param sort                        requested sorting
     * @param elemNames                   requested ElementNames
     * @param typeName                    requested typeName
     * @param maxHitsFromSummary          ?
     * @param cswServiceSpecificContraint specific contraint for specialized CSW services
     * @param strategy                    ElementNames strategy
     * @return result
     * @throws CatalogException hmm
     */
    public Pair<Element, Element> search(ServiceContext context, int startPos, int maxRecords,
                                         ResultType resultType, String outSchema, ElementSetName setName,
                                         Element filterExpr, String filterVersion, Sort sort,
                                         Set<String> elemNames, String typeName, int maxHitsFromSummary,
                                         String cswServiceSpecificContraint, String strategy) throws CatalogException {

        Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);

        CatalogSearcher searcher = new CatalogSearcher(_gmlConfig, _selector, _uuidselector, _applicationContext);

        context.getUserSession().setProperty(Geonet.Session.SEARCH_RESULT, searcher);

        // search for results, filtered and sorted
        Pair<Element, List<ResultItem>> summaryAndSearchResults = searcher.search(context, filterExpr, filterVersion,
            typeName, sort, resultType, startPos, maxRecords, maxHitsFromSummary, cswServiceSpecificContraint);

        Element summary = summaryAndSearchResults.one();
        int numMatches = Integer.parseInt(summary.getAttributeValue("count"));
        if (numMatches != 0 && startPos > numMatches) {
            throw new InvalidParameterValueEx("startPosition", String.format(
                "Start position (%d) can't be greater than number of matching records (%d for current search).",
                startPos, numMatches
            ));
        }


        final SettingInfo settingInfo = context.getBean(SearchManager.class).getSettingInfo();
        String displayLanguage = LuceneSearcher.determineLanguage(context, filterExpr, settingInfo).presentationLanguage;
        // retrieve actual metadata for results
        int counter = retrieveMetadataMatchingResults(context, results, summaryAndSearchResults, maxRecords, setName,
            outSchema, elemNames, typeName, resultType, strategy, displayLanguage);

        //
        // properties of search result
        //
        results.setAttribute("numberOfRecordsMatched", numMatches + "");
        results.setAttribute("numberOfRecordsReturned", counter + "");
        results.setAttribute("elementSet", setName.toString());

        int nextRecord = counter + startPos;
        if (nextRecord > numMatches) {
            //  "number of records returned to client nextRecord -
            // position of next record in the result set
            // (0 if no records remain)"
            // Cf. http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
            results.setAttribute("nextRecord", "0");
        } else {
            results.setAttribute("nextRecord", nextRecord + "");
        }

        return Pair.read(summary, results);
    }

    /**
     * Retrieve actual metadata matching the results. Adds elements to results parameter as a side
     * effect.
     *
     * @param context                 Service context
     * @param results                 retrieved results
     * @param summaryAndSearchResults results from search
     * @param maxRecords              equested max records to return
     * @param elementSetName          requested ElementSetName
     * @param outputSchema            requested OutputSchema
     * @param elementNames            requested ElementNames
     * @param typeName                requested typeName
     * @param resultType              requested ResultType
     * @param strategy                ElementNames strategy
     * @return number of results from search that could be retrieved
     * @throws CatalogException hmm
     */
    private int retrieveMetadataMatchingResults(ServiceContext context,
                                                Element results,
                                                Pair<Element, List<ResultItem>> summaryAndSearchResults,
                                                int maxRecords, ElementSetName elementSetName,
                                                String outputSchema, Set<String> elementNames,
                                                String typeName, ResultType resultType, String strategy, String displayLanguage)
        throws CatalogException {

        List<ResultItem> resultsList = summaryAndSearchResults.two();
        int counter = 0;
        for (int i = 0; (i < maxRecords) && (i < resultsList.size()); i++) {
            ResultItem resultItem = resultsList.get(i);
            String id = resultItem.getID();
            Element md = null;

            try {
                md = retrieveMetadata(context, id, elementSetName, outputSchema, elementNames, typeName, resultType, strategy, displayLanguage);
                // metadata cannot be retrieved
                if (md == null) {
                    results.addContent(new Comment(String.format("Metadata with id '%s' returned null.", id)));
                    context.warning("SearchController : Metadata not found or invalid schema : " + id);
                }
                // metadata can be retrieved
                else {
                    // metadata must be included in response
                    if ((resultType == ResultType.RESULTS || resultType == ResultType.RESULTS_WITH_SUMMARY)) {
                        results.addContent(md);
                    }
                }
            } catch (InvalidParameterValueEx e) {
                results.addContent(new Comment(e.getMessage()));
            }
            counter++;
        }
        return counter;
    }

    /**
     * Applies postprocessing stylesheet if available.
     *
     * Postprocessing files should be in the present/csw folder of the schema and have this naming:
     *
     * For default CSW service
     *
     * 1) gmd-csw-postprocessing.xsl : Postprocessing xsl applied for CSW service when requesting iso (gmd) output
     * 2) csw-csw-postprocessing.xsl : Postprocessing xsl applied for CSW service when requesting ogc (csw) output
     *
     * For a custom CSW service named csw-inspire
     *
     * 1) gmd-csw-inspire-postprocessing.xsl : Postprocessing xsl applied for custom CSW csw-inspire service when requesting iso output
     * 2) csw-csw-inspire-postprocessing.xsl : Postprocessing xsl applied for custom CSW csw-inspire service when requesting ogc (csw) output
     *
     * @param context Service context
     * @param schemaManager schemamanager
     * @param schema schema
     * @param result result
     * @param outputSchema requested OutputSchema
     * @param elementSetName requested ElementSetName
     * @param resultType requested ResultTYpe
     * @param id metadata id
     * @param displayLanguage language to use in response
     * @return metadata
     * @throws InvalidParameterValueEx hmm
     */
    private static Element applyPostProcessing(ServiceContext context, SchemaManager schemaManager, String schema,
                                               Element result, String outputSchema, ElementSetName elementSetName,
                                               ResultType resultType, String id, String displayLanguage) throws InvalidParameterValueEx {
        Path schemaDir  = schemaManager.getSchemaCSWPresentDir(schema);
        final NodeInfo nodeInfo = ApplicationContextHolder.get().getBean(NodeInfo.class);


        Path styleSheet = schemaDir.resolve(outputSchema + "-"
            + (context.getService().equals("csw") ? nodeInfo.getId() : context.getService())
            + "-postprocessing.xsl");

        if (Files.exists(styleSheet)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("lang", displayLanguage);
            params.put("displayInfo", resultType == ResultType.RESULTS_WITH_SUMMARY ? "true" : "false");

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
