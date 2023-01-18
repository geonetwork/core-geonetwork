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

package org.fao.geonet.kernel.harvest.harvester.simpleUrl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.utils.Xml.isXMLLike;

/**
 * Harvest metadata from a JSON source.
 * <p>
 * The JSON source can be a simple JSON file or
 * an URL with indication on how to pass paging information.
 *
 * This harvester has been tested with CKAN search API.
 */
class Harvester implements IHarvester<HarvestResult> {
    public static final String LOGGER_NAME = "geonetwork.harvester.simpleurl";

    private final AtomicBoolean cancelMonitor;
    private Logger log;
    private SimpleUrlParams params;
    private ServiceContext context;

    @Autowired
    GeonetHttpRequestFactory requestFactory;

    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();

    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, SimpleUrlParams params) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
    }

    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;
        log.debug("Retrieving from harvester: " + params.getName());

        requestFactory = context.getBean(GeonetHttpRequestFactory.class);

        String[] urlList = params.url.split("\n");
        boolean error = false;
        Aligner aligner = new Aligner(cancelMonitor, context, params, log);

        for (String url : urlList) {
            log.debug("Loading URL: " + url);
            String content = retrieveUrl(url, log);
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }
            log.debug("Response is: " + content);

            int numberOfRecordsToHarvest = -1;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObj = null;
            Element xmlObj = null;
            SimpleUrlResourceType type = content.startsWith("<?xml version") || isXMLLike(content)
                ? SimpleUrlResourceType.XML : SimpleUrlResourceType.JSON;
            if (type == SimpleUrlResourceType.XML) {
                xmlObj = Xml.loadString(content, false);
            } else {
                jsonObj = objectMapper.readTree(content);
            }

            if (StringUtils.isNotEmpty(params.numberOfRecordPath)) {
                try {
                    if (type == SimpleUrlResourceType.XML) {
                        Object element = Xml.selectSingle(xmlObj, params.numberOfRecordPath, xmlObj.getAdditionalNamespaces());
                        if (element != null) {
                            String s = getXmlElementTextValue(element);
                            numberOfRecordsToHarvest = Integer.parseInt(s);
                        }
                    } else {
                        numberOfRecordsToHarvest = jsonObj.at(params.numberOfRecordPath).asInt();
                    }
                    log.debug("Number of records to harvest: " + numberOfRecordsToHarvest);
                } catch (Exception e) {
                    log.error(String.format("Failed to extract total in response at path %s. Error is: %s",
                        params.numberOfRecordPath, e.getMessage()));
                }
            }
            Map<String, Element> allUuids = new HashMap<String, Element>();
            try {
                Map<String, Element> uuids = new HashMap<String, Element>();
                List<String> listOfUrlForPages = buildListOfUrl(params, numberOfRecordsToHarvest);
                for (int i = 0; i < listOfUrlForPages.size(); i++) {
                    if (i != 0) {
                        content = retrieveUrl(listOfUrlForPages.get(i), log);
                        if (type == SimpleUrlResourceType.XML) {
                            xmlObj = Xml.loadString(content, false);
                        } else {
                            jsonObj = objectMapper.readTree(content);
                        }
                    }
                    JsonNode nodes = null;
                    List<Element> xmlNodes = null;
                    if (StringUtils.isNotEmpty(params.loopElement)) {
                        try {
                            if (type == SimpleUrlResourceType.XML) {
                                xmlNodes = Xml.selectNodes(xmlObj, params.loopElement, xmlObj.getAdditionalNamespaces());
                                log.debug(String.format("%d records found in XML response.", xmlNodes.size()));
                            } else {
                                nodes = jsonObj.at(params.loopElement);
                                log.debug(String.format("%d records found in JSON response.", nodes.size()));
                            }


                            if (type == SimpleUrlResourceType.XML && xmlNodes != null) {
                                xmlNodes.forEach(record -> {
                                    String uuid =
                                        null;
                                    try {
                                        uuid = getXmlElementTextValue(Xml.selectSingle(record, params.recordIdPath, record.getAdditionalNamespaces()));
                                        uuids.put(uuid, record);
                                    } catch (JDOMException e) {
                                        log.error(String.format("Failed to extract UUID for record. Error is %s.",
                                            e.getMessage()));
                                        aligner.getResult().badFormat ++;
                                        aligner.getResult().totalMetadata ++;
                                    }
                                });
                            } else if (nodes != null) {
                                nodes.forEach(record -> {
                                    String uuid = null;
                                    try {
                                        uuid = this.extractUuidFromIdentifier(record.at(params.recordIdPath).asText());
                                    } catch (Exception e) {
                                        log.error(String.format("Failed to collect record UUID at path %s. Error is: %s",
                                            params.recordIdPath, e.getMessage()));
                                    }
                                    String apiUrlPath = params.url.split("\\?")[0];
                                    URL apiUrl = null;
                                    try {
                                        apiUrl = new URL(apiUrlPath);
                                        String nodeUrl = new StringBuilder(apiUrl.getProtocol()).append("://").append(apiUrl.getAuthority()).toString();
                                        Element xml = convertRecordToXml(record, uuid, apiUrlPath, nodeUrl);
                                        uuids.put(uuid, xml);
                                    } catch (MalformedURLException e) {
                                        log.warning(String.format("Failed to parse JSON source URL. Error is: %s", e.getMessage()));
                                    }
                                });
                            }
                            aligner.align(uuids, errors);
                            allUuids.putAll(uuids);
                        } catch (Exception e) {
                            log.error(String.format("Failed to collect record in response at path %s. Error is: %s",
                                params.loopElement, e.getMessage()));
                        }
                    }
                }
                aligner.cleanupRemovedRecords(allUuids.keySet());
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

            log.info("Total records processed in all searches :" + allUuids.size());
            if (error) {
                log.warning("Due to previous errors the align process has not been called");
            }
        }
        return aligner.getResult();
    }

    private String getXmlElementTextValue(Object element) {
        String s = null;
        if (element instanceof Text) {
            s = ((Text) element).getTextNormalize();
        } else if (element instanceof Attribute) {
            s = ((Attribute) element).getValue();
        } else if (element instanceof String) {
            s = (String) element;
        }
        return s;
    }

    private String extractUuidFromIdentifier(final String identifier ) {
        String uuid = identifier;
        if (Lib.net.isUrlValid(uuid)) {
            uuid = uuid.replaceFirst(".*/([^/?]+).*", "$1");
        }
        return uuid;
    }

    @VisibleForTesting
    protected List<String> buildListOfUrl(SimpleUrlParams params, int numberOfRecordsToHarvest) {
        List<String> urlList = new ArrayList<String>();
        if (StringUtils.isEmpty(params.pageSizeParam)) {
            urlList.add(params.url);
            return urlList;
        }

        int numberOfRecordsPerPage = -1;
        final String pageSizeParamValue = params.url.replaceAll(".*[?&]" + params.pageSizeParam + "=([0-9]+).*", "$1");
        if (StringUtils.isNumeric(pageSizeParamValue)) {
            numberOfRecordsPerPage = Integer.parseInt(pageSizeParamValue);
        } else {
            log.warning(String.format(
                    "Page size param '%s' not found or is not a numeric in URL '%s'. Can't build a list of pages.",
                    params.pageSizeParam, params.url));
            urlList.add(params.url);
            return urlList;
        }

        final String pageFromParamValue = params.url.replaceAll(".*[?&]" + params.pageFromParam + "=([0-9]+).*", "$1");
        boolean startAtZero = false;
        if (StringUtils.isNumeric(pageFromParamValue)) {
            startAtZero = Integer.parseInt(pageFromParamValue) == 0;
        } else {
            log.warning(String.format(
                    "Page from param '%s' not found or is not a numeric in URL '%s'. Can't build a list of pages.",
                    params.pageFromParam, params.url));
            urlList.add(params.url);
            return urlList;
        }


        int numberOfPages = (int) Math.abs((numberOfRecordsToHarvest + (startAtZero ? -1 : 0)) / numberOfRecordsPerPage) + 1;

        for (int i = 0; i < numberOfPages; i++) {
            int from = i * numberOfRecordsPerPage + (startAtZero ? 0 : 1);
            int size = i == numberOfPages - 1 ? // Last page
                    numberOfRecordsToHarvest - from + (startAtZero ? 0 : 1) :
                    numberOfRecordsPerPage;
            String url = params.url
                    .replaceAll(params.pageFromParam + "=[0-9]+", params.pageFromParam + "=" + from)
                    .replaceAll(params.pageSizeParam + "=[0-9]+", params.pageSizeParam + "=" + size);
            urlList.add(url);
        }

        return urlList;
    }

    private Element convertRecordToXml(JsonNode record, String uuid, String apiUrl, String nodeUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String recordAsXml = XML.toString(
                    new JSONObject(
                            objectMapper.writeValueAsString(record)), "record");
            recordAsXml = Xml.stripNonValidXMLCharacters(recordAsXml)
                    .replace("<@", "<")
                    .replace("</@", "</")
                    .replaceAll("(:)(?![^<>]*<)", "_"); // this removes colon from property names
            Element recordAsElement = Xml.loadString(recordAsXml, false);
            recordAsElement.addContent(new Element("uuid").setText(uuid));
            recordAsElement.addContent(new Element("apiUrl").setText(apiUrl));
            recordAsElement.addContent(new Element("nodeUrl").setText(nodeUrl));
            Path importXsl = context.getAppPath().resolve(Geonet.Path.IMPORT_STYLESHEETS);
            final Path xslPath = importXsl.resolve(params.toISOConversion + ".xsl");
            return Xml.transform(recordAsElement, xslPath);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Does CSW GetCapabilities request and check that operations needed for harvesting (ie.
     * GetRecords and GetRecordById) are available in remote node.
     *
     * @return
     */
    private String retrieveUrl(String url, Logger log) throws Exception {
        if (!Lib.net.isUrlValid(url))
            throw new BadParameterEx("Invalid URL", url);
        HttpGet httpMethod = null;
        ClientHttpResponse httpResponse = null;

        try {
            httpMethod = new HttpGet(createUrl(url));
            httpResponse = requestFactory.execute(httpMethod);
            int status = httpResponse.getRawStatusCode();
            Log.debug(LOGGER_NAME, "Request status code: " + status);
            return CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
        } finally {
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
            IOUtils.closeQuietly(httpResponse);
        }
    }

    private URI createUrl(String jsonUrl) throws URISyntaxException {
        // TODO: Add paging and loop
        return new URI(jsonUrl);
    }


    public List<HarvestError> getErrors() {
        return errors;
    }
}
