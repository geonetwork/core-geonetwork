/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.csw.services.getrecords;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.csw.services.getrecords.solr.CswFilter2Solr;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.SolrAuth;
import org.fao.geonet.kernel.search.SolrSearchManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import jeeves.server.context.ServiceContext;

public class SolrSearchController implements ISearchController {
    @Autowired
    private SolrSearchManager searchManager;

    @Autowired
    private FieldMapper fieldMapper;

    @Override
    public Pair<Element, Element> search(ServiceContext context, int startPos, int maxRecords,
                                         ResultType resultType, String outSchema, ElementSetName setName,
                                         Element filterExpr, String filterVersion, Element request,
                                         Set<String> elemNames, String typeName, int maxHitsFromSummary,
                                         String cswServiceSpecificContraint, String strategy) throws CatalogException {

        final SolrClient client = searchManager.getClient();
        final SolrQuery params = new SolrQuery("*:*");
        addFilter(params, convertCswFilter(filterExpr, filterVersion));
        addFilter(params, cswServiceSpecificContraint);
        try {
            addFilter(params, SolrAuth.getPermissions());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        params.setStart(startPos - 1);
        params.setRows(maxRecords);
        params.setFields(SolrSearchManager.ID);
        try {

            final MyStreamingResponseCallback callback = new MyStreamingResponseCallback(context, outSchema, setName, resultType, elemNames, typeName, strategy);
            client.queryAndStreamResponse(params, callback);
            final Element results = callback.results;
            results.setAttribute("numberOfRecordsMatched", Long.toString(callback.numMatches));
            results.setAttribute("numberOfRecordsReturned", Long.toString(callback.counter));
            results.setAttribute("elementSet", setName.toString());

            if (callback.numMatches > callback.counter) {
                results.setAttribute("nextRecord", Long.toString(callback.counter + startPos));
            } else {
                results.setAttribute("nextRecord", "0");
            }
            return Pair.read(null, results);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFilter(SolrQuery query, String filter) {
        if (filter == null || filter.isEmpty()) {
            return;
        }
        query.addFilterQuery(filter);
    }


    private static final Configuration FILTER_1_0_0 = new org.geotools.filter.v1_0.OGCConfiguration();
    private static final Configuration FILTER_1_1_0 = new org.geotools.filter.v1_1.OGCConfiguration();
    private static final Configuration FILTER_2_0_0 = new org.geotools.filter.v2_0.FESConfiguration();
    public static Parser createFilterParser(String filterVersion) {
        Configuration config;
        if (filterVersion.equals(FilterCapabilities.VERSION_100)) {
            config = FILTER_1_0_0;
        } else if (filterVersion.equals(FilterCapabilities.VERSION_200)) {
            config = FILTER_2_0_0;
        } else if (filterVersion.equals(FilterCapabilities.VERSION_110)) {
            config = FILTER_1_1_0;
        } else {
            throw new IllegalArgumentException("UnsupportFilterVersion: "+filterVersion);
        }
        return new Parser(config);
    }
    private Filter parseFilter(Element xml, String filterVersion) {
        final Parser parser = createFilterParser(filterVersion);
        parser.setValidating(true);
        parser.setFailOnValidationError(true);
        String string = Xml.getString(xml);
        try {
            final Object parseResult = parser.parse(new StringReader(string));
            if (parseResult instanceof Filter) {
                return (Filter) parseResult;
            } else {
                return null;
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            Log.error(Geonet.CSW_SEARCH, "Errors occurred when trying to parse a filter", e);
            return null;
        }
    }

    private String convertCswFilter(Element xml, String filterVersion) {
        if (xml == null) {
            return null;
        }
        String result = CswFilter2Solr.translate(parseFilter(xml, filterVersion), fieldMapper);
        if (result != null && !result.contains("_isTemplate:")) {
            result += " AND (_isTemplate:n)";
        }
        return result;
    }

    private static class MyStreamingResponseCallback extends StreamingResponseCallback {
        private final Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);
        private final ServiceContext context;
        private final String outSchema;
        private final ElementSetName setName;
        private final ResultType resultType;
        private final Set<String> elemNames;
        private final String typeName;
        private final String strategy;
        private final MetadataRepository metadataRepository;
        private final SchemaManager scm;
        private long numMatches = 0;
        private long counter = 0;

        public MyStreamingResponseCallback(ServiceContext context, String outSchema, ElementSetName setName, ResultType resultType, Set<String> elemNames, String typeName, String strategy) {
            this.context = context;
            this.outSchema = outSchema;
            this.setName = setName;
            this.resultType = resultType;
            this.elemNames = elemNames;
            this.typeName = typeName;
            this.strategy = strategy;
            metadataRepository = context.getBean(MetadataRepository.class);
            scm = context.getBean(SchemaManager.class);
        }

        @Override
        public void streamSolrDocument(SolrDocument doc) {
            final Element searchResult;
            try {
                searchResult = createSearchResult(doc);
            } catch (IOException | JDOMException | InvalidParameterValueEx e) {
                throw new RuntimeException(e);
            }
            if (searchResult != null) {
                results.addContent(searchResult);
                counter++;
            }
        }

        @Override
        public void streamDocListInfo(long numFound, long start, Float maxScore) {
            this.numMatches = numFound;
        }

        private Element createSearchResult(SolrDocument doc) throws IOException, JDOMException, InvalidParameterValueEx {
            final int id = Integer.valueOf(doc.getFieldValue(SolrSearchManager.ID).toString());
            final Metadata metadata = getMetaData(id);
            if (metadata == null) {
                return null;
            }
            final String schema = metadata.getDataInfo().getSchemaId();
            String displayLanguage = context.getLanguage();
            Element result = applyElementSetName(context, scm, schema,
                metadata.getXmlData(false), outSchema, setName, resultType, Integer.toString(id), displayLanguage);
            return applyElementNames(context, elemNames, typeName, scm, schema, result, resultType, null, strategy);
        }

        private Metadata getMetaData(int id) {
            Metadata md = metadataRepository.findOne(id);
            if (md == null) {
                return null;
            }
            return md;
        }
    }
    public final static String DEFAULT_ELEMENTNAMES_STRATEGY = "relaxed";

    /**
     * Applies requested ElementNames and typeNames.
     *
     * For ElementNames, several strategies are implemented. Clients can determine the behaviour by sending attribute
     * "elementname_strategy" with one of the following values:
     *
     * csw202
     * relaxed
     * context
     * geonetwork26
     *
     * The default is 'relaxed'. The strategies cause the following behaviour:
     *
     * csw202 -- compliant to the CSW2.0.2 specification. In particular this means that complete metadata are returned
     *           that match the requested ElementNames, only if they are valid for their XSD. This is because
     *           GeoNetwork only supports OutputFormat=application/xml, which mandates that valid documents are
     *           returned. Because possibly not many of the catalog's metadata are valid, this is not the default.
     *
     * relaxed -- like csw202, but dropped the requirement to only include valid metadata. So this returns complete
     *            metadata that match the requested ElementNames. This is the default strategy.
     *
     * context -- does not return complete metadata but only the elements matching the request, in their context (i.e.
     *            all ancestor elements up to the root of the document are retained). This strategy is similar to
     *            geonetwork26 but the context allows clients to determine which of the elements returned corresponds to
     *            which of the elements requested (in case they have the same name).
     *
     * geonetwork26 -- behaviour as in GeoNetwork 2.6. Just return the requested elements, stripped of any context. This
     *                 can make it impossible for the client to determine which of the elements returned corresponds to
     *                 which of the elements requested; for example if the client asks for gmd:title, the response may
     *                 contain various gmd:title elements taken from different locations in the metadata document.
     *
     * -------------------------------------------------
     * Relevant sections of specification about typeNames:
     *
     * OGC 07-006 10.8.4.8:
     * The typeNames parameter is a list of one or more names of queryable entities in the catalogue's information model
     * that may be constrained in the predicate of the query. In the case of XML realization of the OGC core metadata
     * properties (Subclause 10.2.5), the element csw:Record is the only queryable entity. Other information models may
     * include more than one queryable component. For example, queryable components for the XML realization of the ebRIM
     * include rim:Service, rim:ExtrinsicObject and rim:Association. In such cases the application profile shall
     * describe how multiple typeNames values should be processed.
     * In addition, all or some of the these queryable entity names may be specified in the query to define which
     * metadata record elements the query should present in the response to the GetRecords operation.
     *
     * OGC 07-045:
     *
     * 8.2.2.1.1 Request (GetRecords)
     * TypeNames. Must support *one* of “csw:Record” or “gmd:MD_Metadata” in a query. Default value is “csw:Record”.
     *
     * So, in OGC 07-045, exactly one of csw:Record or gmd:MD_Metadata is mandated for typeName.
     *
     * ----------------------------------
     * Relevant specs about ElementNames:
     *
     * OGC 07-006 10.8.4.9:
     * The ElementName parameter is used to specify one or more metadata record elements, from the output schema
     * specified using the outputSchema parameter, that the query shall present in the response to the a GetRecords
     * operation. Since clause 10.2.5 realizes the core metadata properties using XML schema, the value of the
     * ElementName parameter would be an XPath expression perhaps using qualified names. In the general case, a complete
     * XPath expression may be required to correctly reference an element in the information model of the catalog.
     *
     * However, in the case where the typeNames attribute on the Query element contains a single value, the catalogue
     * can infer the first step in the path expression and it can be omitted. This is usually the case when querying the
     * core metadata properties since the only queryable target is csw:Record.
     *
     * If the metadata record element names are not from the schema specified using the outputSchema parameter, then the
     * service shall raise an exception as described in Subclause 10.3.7.
     *
     * OGC 07-045:
     * Usage of the ELEMENTNAME is not further specified here.
     *
     * ----------------------------------
     * Relevant specs about outputFormat:
     *
     * OGC 07-006 10.8.4.4 outputFormat parameter:
     * In the case where the output format is application/xml, the CSW shall generate an XML document that validates
     * against a schema document that is specified in the output document via the xsi:schemaLocation attribute defined
     * in XML.
     *
     *
     * @param context servicecontext everywhere
     * @param elementNames requested ElementNames
     * @param typeName requested typeName
     * @param schemaManager schemamanager
     * @param schema schema
     * @param result result
     * @param resultType requested ResultType
     * @param info ?
     * @param strategy - which ElementNames strategy to use (see Javadoc)
     * @return results of applying ElementNames filter
     * @throws InvalidParameterValueEx hmm
     */
    public static Element applyElementNames(ServiceContext context, Set<String> elementNames, String typeName,
                                            SchemaManager schemaManager, String schema, Element result,
                                            ResultType resultType, Element info, String strategy) throws InvalidParameterValueEx {
        if (elementNames != null) {

            if(StringUtils.isEmpty(strategy)) {
                strategy = DEFAULT_ELEMENTNAMES_STRATEGY;
            }

            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "SearchController dealing with # " + elementNames.size() + " elementNames using strategy " + strategy);

            MetadataSchema mds = schemaManager.getSchema(schema);
            List<Namespace> namespaces = mds.getSchemaNS();

            Element matchingMetadata = (Element)result.clone();
            if(strategy.equals("context") || strategy.equals("geonetwork26")) {
                // these strategies do not return complete metadata
                matchingMetadata.removeContent();
            }

            boolean metadataContainsAllRequestedElementNames = true;
            List<Element> nodes = new ArrayList<Element>();
            for(String elementName : elementNames) {
                if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
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
                    if(elementName.startsWith("/")) {
                        // use it as the xpath as is;
                        xpath = elementName;
                        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH, "elementname start with root: " + elementName);
                    }
                    // case 2: elementname does not start with /
                    else {
                        // case 2a: elementname starts with one of the supported typeNames (csw:Record or gmd:MD_Metadata)
                        // TODO do not hardcode namespace prefixes
                        if(elementName.startsWith("csw:Record") || elementName.startsWith("gmd:MD_Metadata")) {
                            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                                Log.debug(Geonet.CSW_SEARCH, "elementname starts with one of the supported typeNames : " + elementName);
                            // prepend /
                            xpath = "/" + elementName;
                        }
                        // case 2b: elementname does not start with one of the supported typeNames
                        else {
                            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                                Log.debug(Geonet.CSW_SEARCH, "elementname does not start with one of the supported typeNames : " + elementName);
                            // prepend with /typeName/
                            xpath = "/" + typeName + "//" + elementName ;
                        }
                    }
                    @SuppressWarnings("unchecked")
                    List<Element> elementsMatching = (List<Element>)Xml.selectDocumentNodes(result, xpath, namespaces);

                    if(strategy.equals("context")) {
                        if(Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
                            Log.debug(Geonet.CSW_SEARCH, "strategy is context, constructing context to root");
                        }

                        List<Element> elementsInContextMatching = new ArrayList<Element>();
                        for (Element match : elementsInContextMatching) {
                            Element parent = match.getParentElement();
                            while(parent != null) {
                                parent.removeContent();
                                parent.addContent((Element)match.clone());
                                match = (Element)parent.clone();
                                parent = parent.getParentElement();
                            }
                            elementsInContextMatching.add(match);
                        }
                        elementsMatching = elementsInContextMatching;
                    }
                    nodes.addAll(elementsMatching);

                    if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "elemName " + elementName + " matched # " + nodes.size() + " nodes");

                    if(nodes.size() == 0) {
                        metadataContainsAllRequestedElementNames = false;
                        break;
                    }
                }
                catch (Exception x) {
                    Log.error(Geonet.CSW_SEARCH, x.getMessage());
                    x.printStackTrace();
                    throw new InvalidParameterValueEx("elementName has invalid XPath : " + elementName, x.getMessage());
                }
            }

            if(metadataContainsAllRequestedElementNames == true) {
                if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "metadata containa all requested elementnames: included in response");

                if(strategy.equals("context") || strategy.equals("geonetwork26")) {
                    if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "adding only the matching fragments to result");
                    for(Element node: nodes) {
                        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH, "adding node:\n" + Xml.getString(node));
                        matchingMetadata.addContent((Content)node.clone());
                    }
                }
                else {
                    if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "adding the complete metadata to results");
                    if(strategy.equals("csw202")) {
                        GeonetContext geonetContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                        DataManager dataManager = geonetContext.getBean(DataManager.class);
                        boolean valid = dataManager.validate(result);
                        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH, "strategy csw202: only valid metadata is returned. This one is valid? " + valid);

                        if(!valid) {
                            return null;
                        }
                    }
                    matchingMetadata = result;
                }

                if (resultType == ResultType.RESULTS_WITH_SUMMARY) {
                    matchingMetadata.addContent((Content)info.clone());
                }
                result = matchingMetadata;
            }
            else {
                if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "metadata does not contain all requested elementnames: not included in response");
                return null;
            }
        }
        else {
            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "No ElementNames to apply");
        }
        return result;
    }

    /**
     * Applies stylesheet according to ElementSetName and schema.
     *
     * @param context Service context
     * @param schemaManager schemamanager
     * @param schema schema
     * @param result result
     * @param outputSchema requested OutputSchema
     * @param elementSetName requested ElementSetName
     * @param resultType requested ResultTYpe
     * @param id metadata id
     * @return metadata
     * @throws InvalidParameterValueEx hmm
     */
    public static Element applyElementSetName(ServiceContext context, SchemaManager schemaManager, String schema,
                                              Element result, String outputSchema, ElementSetName elementSetName,
                                              ResultType resultType, String id, String displayLanguage) throws InvalidParameterValueEx {
        Path schemaDir  = schemaManager.getSchemaCSWPresentDir(schema);
        Path styleSheet = schemaDir.resolve(outputSchema + "-" + elementSetName + ".xsl");

        if (!Files.exists(styleSheet)) {
            context.warning(
                String.format(
                    "OutputSchema '%s' not supported for metadata with '%s' (%s). Corresponding XSL transformation '%s' does not exist. The record will not be returned in response.",
                    outputSchema, id, schema, styleSheet.toString()));
            return null;
        } else {
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
            return result;
        }
    }
}
