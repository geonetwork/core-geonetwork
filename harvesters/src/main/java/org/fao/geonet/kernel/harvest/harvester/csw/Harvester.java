//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.csw;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.ConstraintLanguage;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.CswOperation;
import org.fao.geonet.csw.common.CswServer;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetRecordsRequest;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.AbstractHttpRequest;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Content;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;
import org.jdom.Namespace;

class Harvester implements IHarvester<HarvestResult> {
    // FIXME : Currently switch from POST to GET for testing mainly.
    public static final String PREFERRED_HTTP_METHOD = AbstractHttpRequest.Method.POST.toString();

    private final static String ATTRIB_SEARCHRESULT_MATCHED = "numberOfRecordsMatched";

    private final static String ATTRIB_SEARCHRESULT_RETURNED = "numberOfRecordsReturned";

    private final static String ATTRIB_SEARCHRESULT_NEXT = "nextRecord";

    private static int GETRECORDS_REQUEST_MAXRECORDS = 20;

    private static String CONSTRAINT_LANGUAGE_VERSION = "1.1.0";

    //FIXME version should be parametrized
    private static final String GETCAPABILITIES_PARAMETERS = "SERVICE=CSW&REQUEST=GetCapabilities&VERSION=2.0.2";
    private final AtomicBoolean cancelMonitor;

    private Logger log;
    private final CswParams params;
    private final ServiceContext context;

    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private final List<HarvestError> errors;


    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, CswParams params, List<HarvestError> errors) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
        this.errors = errors;
    }

    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;
        log.debug("Retrieving capabilities file for : " + params.getName());

        CswServer server = retrieveCapabilities(log);
        if (cancelMonitor.get()) {
            return new HarvestResult();
        }

        //--- perform all searches

        boolean error = false;
        HarvestResult result = new HarvestResult();
    	Set<String> uuids = new HashSet<String>();
        try {
            Aligner aligner = new Aligner(cancelMonitor, context, server, params, log);
            searchAndAlign(server, uuids, aligner, errors);
            result = aligner.cleanupRemovedRecords(uuids);
        } catch (Exception t) {
            error = true;
            log.error("Unknown error trying to harvest");
            log.error(t.getMessage());
            log.error(t);
            errors.add(new HarvestError(context, t));
        } catch (Throwable t) {
            error = true;
            log.fatal("Something unknown and terrible happened while harvesting");
            log.fatal(t.getMessage());
            errors.add(new HarvestError(context, t));
        }

        log.info("Total records processed in all searches :" + uuids.size());
        if (error) {
            log.warning("Due to previous errors the align process has not been called");
        }

        return result;
    }

    /**
     * Does CSW GetCapabilities request and check that operations needed for harvesting (ie.
     * GetRecords and GetRecordById) are available in remote node.
     */
    private CswServer retrieveCapabilities(Logger log) throws Exception {
        if (!Lib.net.isUrlValid(params.capabUrl))
            throw new BadParameterEx("Capabilities URL", params.capabUrl);

        XmlRequest req;
        // Support both full GetCapbilities URL or CSW entry point
        final GeonetHttpRequestFactory requestFactory = context.getBean(GeonetHttpRequestFactory.class);
        if (params.capabUrl.contains("GetCapabilities")) {
            req = requestFactory.createXmlRequest(new URL(params.capabUrl));
        } else {
            req = requestFactory.createXmlRequest(new URL(params.capabUrl + (params.capabUrl.contains("?") ? "&" : "?") + GETCAPABILITIES_PARAMETERS));
        }

        Lib.net.setupProxy(context, req);

        if (params.isUseAccount())
            req.setCredentials(params.getUsername(), params.getPassword());
        CswServer server = null;
        try {
            Element capabil = req.execute();

            if (log.isDebugEnabled())
                log.debug("Capabilities:\n" + Xml.getString(capabil));

            if (capabil.getName().equals("ExceptionReport"))
                CatalogException.unmarshal(capabil);

            server = new CswServer(capabil);

            if (!checkOperation(log, server, "GetRecords"))
                throw new OperationAbortedEx("GetRecords operation not found");

            if (!checkOperation(log, server, "GetRecordById"))
                throw new OperationAbortedEx("GetRecordById operation not found");

        } catch (BadXmlResponseEx e) {
            errors.add(new HarvestError(context, e, params.capabUrl));
            throw e;
        }

        return server;
    }

    private boolean checkOperation(Logger log, CswServer server, String name) {
        CswOperation oper = server.getOperation(name);

        if (oper == null) {
            log.warning("Operation not present in capabilities : " + name);
            return false;
        }

        if (oper.getGetUrl() == null && oper.getPostUrl() == null) {
            log.warning("Operation has no GET and POST bindings : " + name);
            return false;
        }

        return true;
    }


    /**
     * Does CSW GetRecordsRequest
     * @param server
     * @param uuids
     * @param aligner
     * @param harvesterErrors
     * @throws Exception
     */
    private void searchAndAlign(CswServer server, Set<String> uuids,
        Aligner aligner, List<HarvestError> harvesterErrors) throws Exception {
        int start = 1;

        GetRecordsRequest request = new GetRecordsRequest(context);

        request.setResultType(ResultType.RESULTS);
        //request.setOutputSchema(OutputSchema.OGC_CORE);	// Use default value
        if (StringUtils.isNotEmpty(params.sortBy)) {
            request.addSortBy(params.sortBy);
        }
        request.setElementSetName(ElementSetName.SUMMARY);
        request.setMaxRecords(GETRECORDS_REQUEST_MAXRECORDS);
        request.setDistribSearch(params.queryScope.equalsIgnoreCase("distributed"));
        request.setHopCount(params.hopCount);

        CswOperation oper = server.getOperation(CswServer.GET_RECORDS);

        // Use the preferred HTTP method and check one exist.
        configRequest(request, oper, server, PREFERRED_HTTP_METHOD);

        if (params.isUseAccount()) {
            log.debug("Logging into server (" + params.getUsername() + ")");
            request.setCredentials(params.getUsername(), params.getPassword());
        }

        if (StringUtils.isNotBlank(params.getApiKey())) { {
            log.debug("Using apiKey to authenticate");
            request.setApiKey(params.getApiKeyHeader(), params.getApiKey());
        }
        // Simple fallback mechanism. Try search with PREFERRED_HTTP_METHOD method, if fails change it
        try {
            log.debug(String.format("Trying the search with HTTP %s method.", PREFERRED_HTTP_METHOD));
            request.setStartPosition(start);
            doSearch(request, start, 1);
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug(ex.getMessage());
                log.debug(String.format("Due to errors, changing CSW harvester method to HTTP %s method.", PREFERRED_HTTP_METHOD.equals("GET") ? "POST" : "GET"));
            }
            errors.add(new HarvestError(context, ex));

            configRequest(request, oper, server, PREFERRED_HTTP_METHOD.equals("GET") ? "POST" : "GET");
        }


        while (true) {
            if (this.cancelMonitor.get()) {
              log.error("Harvester stopped in the middle of running!");
              //Returning whatever, we have to move on and finish!
              return;
            }
            request.setStartPosition(start);
            Element response = doSearch(request, start, GETRECORDS_REQUEST_MAXRECORDS);
            if (log.isDebugEnabled()) {
                log.debug("Number of child elements in response: " + response.getChildren().size());
            }

            Element results = response.getChild("SearchResults", Csw.NAMESPACE_CSW);
            // heikki: some providers forget to update their CSW namespace to the CSW 2.0.2 specification
            if (results == null) {
                // in that case, try to accommodate them anyway:
                results = response.getChild("SearchResults", Csw.NAMESPACE_CSW_OLD);
                if (results == null) {
                    throw new OperationAbortedEx("Missing 'SearchResults'", response);
                } else {
                    log.warning("Received GetRecords response with incorrect namespace: " + Csw.NAMESPACE_CSW_OLD);
                }
            }

            if(this.cancelMonitor.get()) {
              log.error("Harvester stopped in the middle of running!");
              //Returning whatever, we have to move on and finish!
              return;
            }
            @SuppressWarnings("unchecked")
            List<Element> list = results.getChildren();
            int foundCnt = 0;

            log.debug("Extracting all elements in the csw harvesting response");
            Set<RecordInfo> records = new LinkedHashSet<RecordInfo>();
            for (Element record : list) {
                try {
                    RecordInfo recInfo = getRecordInfo((Element) record.clone());

                    if (recInfo != null) {
                        records.add(recInfo);
                        uuids.add(recInfo.uuid);
                    }

                } catch (Exception ex) {
                    errors.add(new HarvestError(context, ex));
                    log.error("Unable to process record from csw (" + this.params.getName() + ")");
                    log.error("   Record failed: " + foundCnt);
                    log.debug("   Record: " + ((Element) record).getName());
                }

            }

            foundCnt += records.size();
            //Align here to keep memory clean
            aligner.align(records, harvesterErrors);

            //--- check to see if we have to perform other searches
            int matchedCount = getSearchResultAttribute(results, ATTRIB_SEARCHRESULT_MATCHED);
            int returnedCount = getSearchResultAttribute(results, ATTRIB_SEARCHRESULT_RETURNED);

            // nextRecord *is* required by CSW Specification, but some servers (e.g. terra catalog) are not returning this attribute
            // See https://github.com/geonetwork/core-geonetwork/issues/1429
            Integer nextRecord = getOptionalSearchResultAttribute(results, ATTRIB_SEARCHRESULT_NEXT);

            if (log.isDebugEnabled()) {
                log.debug("Records matched by the query : " + matchedCount);
                log.debug("Records declared in response : " + returnedCount);
                log.debug("Records found in response    : " + foundCnt);
                log.debug("Next record                  : " + nextRecord);
            }

            //== Some log lines, in case we do not like some of the received values

            if (returnedCount != GETRECORDS_REQUEST_MAXRECORDS) {
                log.warning("Declared number of returned records (" + returnedCount + ") does not match requested record count (" + GETRECORDS_REQUEST_MAXRECORDS + ")");
            }

            if (returnedCount != foundCnt) {
                log.warning("Declared number of returned records (" + returnedCount + ") does not match actual record count (" + foundCnt + ")");
            }

            if (nextRecord == null) {
                log.warning("Declared nextRecord is null");
            }

            //== Find out if the harvesting loop has completed

            // Check for standard CSW: A value of 0 means all records have been returned.
            if (nextRecord != null && nextRecord == 0) {
                break;
            }

            // Misbehaving CSW server:
            // GN 3.0.x: see https://github.com/geonetwork/core-geonetwork/issues/1537
            // startPosition=493&maxRecords=1
            //    <csw:SearchResults numberOfRecordsMatched="493" numberOfRecordsReturned="1" elementSet="summary" nextRecord="494">
            // startPosition=494&maxRecords=1
            //    <csw:SearchResults numberOfRecordsMatched="493" numberOfRecordsReturned="0" elementSet="summary" nextRecord="494">

            if (nextRecord != null && nextRecord > matchedCount) {
                log.warning("Forcing harvest end since next > matched");
                break;
            }

            // Another way to escape from an infinite loop

            if (returnedCount == 0) {
                log.warning("Forcing harvest end since numberOfRecordsReturned = 0");
                break;
            }

            // Some misbehaving CSW return nextRecord = 1 when start is over numberOfRecordsMatched
            // Break the loop if nextRecord is smaller than start.
            if (nextRecord != null && nextRecord < start) {
                log.warning(String.format("Forcing harvest end since nextRecord < start (nextRecord = %d, start = %d)", nextRecord, start));
                break;
            }

            // Start position of next record.
            // Note that some servers may return less records than requested (it's ok for CSW protocol)
            start += returnedCount;
        }

        log.debug("Records added to result list : " + uuids.size());
    }

    private void setUpRequest(GetRecordsRequest request, CswOperation oper, CswServer server, URL url,
                              ConstraintLanguage constraintLanguage, String constraint, AbstractHttpRequest.Method method) {

        request.setUrl(context, url);
        request.setServerVersion(server.getPreferredServerVersion());
        String preferredOutputSchema = oper.getPreferredOutputSchema();
        if (this.params.outputSchema != null && !this.params.outputSchema.isEmpty()) {
            preferredOutputSchema = this.params.outputSchema;
        }
        request.setOutputSchema(preferredOutputSchema);
        if (StringUtils.isNotEmpty(constraint)) {
            request.setConstraintLanguage(constraintLanguage);
            request.setConstraintLangVersion(CONSTRAINT_LANGUAGE_VERSION);
            request.setConstraint(constraint);
        }
        request.setMethod(method);

        // Adapt the typename parameter to the outputschema used
        if (this.params.outputSchema != null && !this.params.outputSchema.isEmpty()) {
            if ("http://www.isotc211.org/2005/gmd".equals(this.params.outputSchema)) {
                request.addTypeName(TypeName.getTypeName("gmd:MD_Metadata"));
            } else if ("http://www.opengis.net/cat/csw/2.0.2".equals(this.params.outputSchema)) {
                request.addTypeName(TypeName.getTypeName("csw:Record"));
            } else {
                request.addTypeName(TypeName.getTypeName("csw:Record"));
            }
        } else {
            for (String typeName : oper.getTypeNamesList()) {
                request.addTypeName(TypeName.getTypeName(typeName));
            }
        }
        request.setOutputFormat(oper.getPreferredOutputFormat());
    }

    /**
     * Configs the harvester request.
     */
    private void configRequest(final GetRecordsRequest request, final CswOperation oper, final CswServer server, final String preferredMethod)
        throws Exception {
        if (oper.getGetUrl() == null && oper.getPostUrl() == null) {
            throw new OperationAbortedEx("No GET or POST DCP available in this service.");
        }

        // Use the preferred HTTP method and check one exist.
        if (oper.getGetUrl() != null && preferredMethod.equals("GET") && oper.getConstraintLanguage().contains("cql_text")) {
            setUpRequest(request, oper, server, oper.getGetUrl(), ConstraintLanguage.CQL, getCqlConstraint(params.eltFilters, params.bboxFilter),
                AbstractHttpRequest.Method.GET);
        } else if (oper.getPostUrl() != null && preferredMethod.equals("POST") && oper.getConstraintLanguage().contains("filter")) {
            setUpRequest(request, oper, server, oper.getPostUrl(), ConstraintLanguage.FILTER, getFilterConstraint(params.eltFilters, params.bboxFilter),
                AbstractHttpRequest.Method.POST);
        } else {
            if (oper.getGetUrl() != null && oper.getConstraintLanguage().contains("cql_text")) {
                setUpRequest(request, oper, server, oper.getGetUrl(), ConstraintLanguage.CQL, getCqlConstraint(params.eltFilters, params.bboxFilter),
                    AbstractHttpRequest.Method.GET);
            } else if (oper.getPostUrl() != null && oper.getConstraintLanguage().contains("filter")) {
                setUpRequest(request, oper, server, oper.getPostUrl(), ConstraintLanguage.FILTER, getFilterConstraint(params.eltFilters, params.bboxFilter),
                    AbstractHttpRequest.Method.POST);
            } else {
                // TODO : add GET+FE and POST+CQL support
                log.warning("No GET (using CQL) or POST (using FE) DCP available in this service... Trying GET CQL anyway ...");
                setUpRequest(request, oper, server, oper.getGetUrl(), ConstraintLanguage.CQL, getCqlConstraint(params.eltFilters, params.bboxFilter),
                    AbstractHttpRequest.Method.GET);
            }
        }
    }

    public static ImmutableSet<String> bboxParameters;
    static {
        bboxParameters = ImmutableSet.<String>builder()
            .add("bbox-xmin")
            .add("bbox-ymin")
            .add("bbox-xmax")
            .add("bbox-ymax")
            .build();
    }
    private String getFilterConstraint(final Search s) {
        //--- collect queriables
        ArrayList<Element> queriables = new ArrayList<Element>();
        Map<String, Double> bboxCoordinates = new HashMap<String, Double>();

        if (!s.attributesMap.isEmpty()) {
            for (Map.Entry<String, String> entry : s.attributesMap.entrySet()) {
                if (entry.getValue() != null) {
                    // If the queriable has the namespace, use it
                    String queryableName = entry.getKey();
                    if (bboxParameters.contains(queryableName)
                        && StringUtils.isNotEmpty(entry.getValue())) {
                        bboxCoordinates.put(queryableName, Double.valueOf(entry.getValue()));
                    } else if (queryableName.contains("__")) {
                        queryableName = queryableName.replace("__", ":");
                        buildFilterQueryable(queriables, queryableName, entry.getValue());
                    } else if (!queryableName.contains(":")) {
                        queryableName = "csw:" + queryableName;
                        buildFilterQueryable(queriables, queryableName, entry.getValue());
                    }
                }
            }
        } else {
            log.debug("no search criterion specified, harvesting all ... ");
        }


        //--- build filter expression

        if (queriables.isEmpty()) {
            return null;
        }

        Element filter = new Element("Filter", Csw.NAMESPACE_OGC);

        if (queriables.size() == 1 && bboxCoordinates.size() == 0)
            filter.addContent(queriables.get(0));
        else {
            Element and = new Element("And", Csw.NAMESPACE_OGC);

            for (Element prop : queriables)
                and.addContent(prop);

            if (bboxCoordinates.size() > 0) {
                and.addContent(buildBboxFilter(bboxCoordinates));
            }
            filter.addContent(and);
        }

        return Xml.getString(filter);
    }


    /*
    Build an ogc:BBOX element from bbox coordinates.

    <ogc:Filter>
        <ogc:And>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName>csw:AnyText</ogc:PropertyName>
            <ogc:Literal>roads</ogc:Literal>
          </ogc:PropertyIsEqualTo>
          <ogc:BBOX>
            <ogc:PropertyName>ows:BoundingBox</ogc:PropertyName>
            <gml:Envelope>
              <gml:lowerCorner>47 -5</gml:lowerCorner>
              <gml:upperCorner>55 20</gml:upperCorner>
            </gml:Envelope>
          </ogc:BBOX>*/
    private Content buildBboxFilter(Map<String, Double> bboxCoordinates) {
        Namespace gml = Namespace.getNamespace("http://www.opengis.net/gml");

        Element bbox = new Element("BBOX", Csw.NAMESPACE_OGC);
        Element bboxProperty = new Element("PropertyName", Csw.NAMESPACE_OGC);
        bboxProperty.setText("ows:BoundingBox");
        bbox.addContent(bboxProperty);
        Element envelope = new Element("Envelope", gml);
        Element lowerCorner = new Element("lowerCorner", gml);
        lowerCorner.setText(bboxCoordinates.get("bbox-xmin") + " " + bboxCoordinates.get("bbox-ymin"));
        Element upperCorner = new Element("upperCorner", gml);
        upperCorner.setText(bboxCoordinates.get("bbox-xmax") + " " + bboxCoordinates.get("bbox-ymax"));
        envelope.addContent(lowerCorner);
        envelope.addContent(upperCorner);
        bbox.addContent(envelope);
        return bbox;
    }

    private void buildFilterQueryable(List<Element> queryables, String name, String value) {
        if (value.contains("%")) {
            buildFilterQueryable(queryables, name, value, "PropertyIsLike");
        } else {
            buildFilterQueryable(queryables, name, value, "PropertyIsEqualTo");
        }
    }

    private void buildFilterQueryable(List<Element> queryables, String name, String value, String operator) {
        if (value.length() == 0)
            return;

        // add Like operator
        Element prop;

        if (operator.equals("PropertyIsLike")) {
            prop = new Element(operator, Csw.NAMESPACE_OGC);
            prop.setAttribute("wildCard", "%");
            prop.setAttribute("singleChar", "_");
            prop.setAttribute("escapeChar", "\\");
        } else {
            prop = new Element(operator, Csw.NAMESPACE_OGC);
        }

        Element propName = new Element("PropertyName", Csw.NAMESPACE_OGC);
        Element literal = new Element("Literal", Csw.NAMESPACE_OGC);

        propName.setText(name);
        literal.setText(value);

        prop.addContent(propName);
        prop.addContent(literal);

        queryables.add(prop);
    }


    private String getFilterConstraint(List<Element> filters, Element bboxFilter) throws Exception {
        Path file = context.getAppPath().resolve("xml").resolve("csw").resolve("harvester-csw-filter.xsl");

        Element eltFilter = new Element("filters");
        for(Element e: filters) {
            Element e1 = (Element) e.clone();

            eltFilter.addContent(e1.detach());
        }

        Element cswFilter = Xml.transform(eltFilter, file);

        if (bboxFilter != null) {
            Map<String, Double> bboxCoordinates = new HashMap<>();
            bboxCoordinates.put("bbox-xmin", Double.parseDouble(bboxFilter.getChildText("bbox-xmin")));
            bboxCoordinates.put("bbox-ymin", Double.parseDouble(bboxFilter.getChildText("bbox-ymin")));
            bboxCoordinates.put("bbox-xmax", Double.parseDouble(bboxFilter.getChildText("bbox-xmax")));
            bboxCoordinates.put("bbox-ymax", Double.parseDouble(bboxFilter.getChildText("bbox-ymax")));

            if (cswFilter.getChildren().size() == 0) {
                cswFilter.addContent(buildBboxFilter(bboxCoordinates));
            } else {
                Element filterContent = ((Element) cswFilter.getChildren().get(0));
                filterContent = (Element) filterContent.detach();

                Element and = new Element("And", Csw.NAMESPACE_OGC);
                and.addContent(filterContent);
                and.addContent(buildBboxFilter(bboxCoordinates));
                cswFilter.setContent(and);
            }
        }

        if (cswFilter.getChildren().size() == 0) {
            return StringUtils.EMPTY;
        } else {
            return Xml.getString(cswFilter);
        }
    }

    private String getCqlConstraint(List<Element> filters, Element bboxFilter) throws Exception {
        String cqlFilter = "";

        if (filters.size() > 0) {
            Path file = context.getAppPath().resolve("xml").resolve("csw").resolve("harvester-csw-cql.xsl");

            Element eltFilter = new Element("filters");
            for(Element e: filters) {
                Element e1 = (Element) e.clone();

                eltFilter.addContent(e1.detach());
            }

            Element cswFilter = Xml.transform(eltFilter, file);
            cqlFilter = cswFilter.getText();
        }

        if (bboxFilter != null) {
            Map<String, Double> bboxCoordinates = new HashMap<>();
            bboxCoordinates.put("bbox-xmin", Double.parseDouble(bboxFilter.getChildText("bbox-xmin")));
            bboxCoordinates.put("bbox-ymin", Double.parseDouble(bboxFilter.getChildText("bbox-ymin")));
            bboxCoordinates.put("bbox-xmax", Double.parseDouble(bboxFilter.getChildText("bbox-xmax")));
            bboxCoordinates.put("bbox-ymax", Double.parseDouble(bboxFilter.getChildText("bbox-ymax")));

            if (StringUtils.isNotEmpty(cqlFilter)) {
                cqlFilter = cqlFilter + " AND ";
            }
            //BBOX(the_geom, -90, 40, -60, 45)
            cqlFilter = cqlFilter + String.format("BBOX(the_geom, %s, %s, %s, %s)",
                bboxCoordinates.get("bbox-xmin"), bboxCoordinates.get("bbox-ymin"),
                bboxCoordinates.get("bbox-xmax"), bboxCoordinates.get("bbox-ymax")
            );
        }

        return cqlFilter;
    }

    private Element doSearch(CatalogRequest request, int start, int max) throws Exception {
        try {
            log.debug("Searching on : " + params.getName() + " (" + start + ".." + (start + max) + ")");
            Element response = request.execute();
            if (log.isDebugEnabled()) {
                log.debug("Sent request " + request.getSentData());
                log.debug("Search results:\n" + Xml.getString(response));
            }

            return response;
        } catch (Exception e) {
            errors.add(new HarvestError(context, e));
            log.warning("Raised exception when searching : " + e);
            log.warning("Url: " + request.getHost());
            log.warning("Method: " + request.getMethod());
            log.warning("Sent request " + request.getSentData());
            throw new OperationAbortedEx("Raised exception when searching: " + e.getMessage(), e);
        }
    }

    private int getSearchResultAttribute(Element results, String attribName) throws OperationAbortedEx {
        String value = results.getAttributeValue(attribName);

        if (value == null) {
            throw new OperationAbortedEx("Missing '" + attribName + "' attribute in 'SearchResults'");
        }

        if (!Lib.type.isInteger(value)) {
            throw new OperationAbortedEx("Bad value for '" + attribName + "'", value);
        }

        return Integer.parseInt(value);
    }

    private Integer getOptionalSearchResultAttribute(Element results, String attribName) throws OperationAbortedEx {
        String value = results.getAttributeValue(attribName);

        if (value == null) {
            return null;
        }

        if (!Lib.type.isInteger(value)) {
            throw new OperationAbortedEx("Bad value for '" + attribName + "'", value);
        }

        return Integer.valueOf(value);
    }

    private RecordInfo getRecordInfo(Element record) {
        String name = record.getName();
        if (log.isDebugEnabled())
            log.debug("getRecordInfo (name): " + name);

        // get schema
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        // get uuid and date modified
        try {
            String schema = dm.autodetectSchema(record);
            if (log.isDebugEnabled())
                log.debug("getRecordInfo (schema): " + schema);

            String identif = dm.extractUUID(schema, record);
            if (identif.length() == 0) {
                log.warning("Record doesn't have a uuid : " + name);
                return null; // skip this one
            }

            String modified = dm.extractDateModified(schema, record);
            if (modified.length() == 0) modified = null;
            if (log.isDebugEnabled())
                log.debug("getRecordInfo: adding " + identif + " with modification date " + modified);
            return new RecordInfo(identif, modified);
        } catch (Exception e) {
            log.warning("Skipped record not in supported format : " + name);
        }

        // we get here if we didn't recognize the schema and/or couldn't get the
        // UUID or date modified
        return null;

    }
}
