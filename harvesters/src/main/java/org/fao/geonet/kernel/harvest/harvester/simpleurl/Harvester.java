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

package org.fao.geonet.kernel.harvest.harvester.simpleurl;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.fao.geonet.utils.Xml.isRDFLike;
import static org.fao.geonet.utils.Xml.isXMLLike;

/**
 * Harvest metadata from a URL source.
 * <p>
 * The URL source can be a simple JSON, XML or RDF file or
 * an URL with indication on how to pass paging information.
 * <p>
 * This harvester has been tested with CKAN, OpenDataSoft,
 * OGC API Records, DCAT feeds.
 */
class Harvester implements IHarvester<HarvestResult> {
    public static final String LOGGER_NAME = "geonetwork.harvester.simpleurl";

    private final AtomicBoolean cancelMonitor;
    private Logger log;
    private final SimpleUrlParams params;
    private final ServiceContext context;

    @Autowired
    GeonetHttpRequestFactory requestFactory;

    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private final List<HarvestError> errors;

    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, SimpleUrlParams params, List<HarvestError> errors) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
        this.errors = errors;
    }

    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;
        log.debug("Retrieving from harvester: " + params.getName());

        requestFactory = context.getBean(GeonetHttpRequestFactory.class);

        String[] urlList = params.url.split("\n");
        boolean error = false;
        Aligner aligner = new Aligner(cancelMonitor, context, params, log);
        Set<String> listOfUuids = new HashSet<>();

        for (String url : urlList) {
            log.debug("Loading URL: " + url);
            String content = retrieveUrl(url);
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }
            log.debug("Response is: " + content);

            int numberOfRecordsToHarvest = -1;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObj = null;
            Element xmlObj = null;
            SimpleUrlResourceType type;

            if (isRDFLike(content)) type = SimpleUrlResourceType.RDFXML;
            else if (isXMLLike(content)) type = SimpleUrlResourceType.XML;
            else type = SimpleUrlResourceType.JSON;

            if (type == SimpleUrlResourceType.XML
                || type == SimpleUrlResourceType.RDFXML) {
                xmlObj = Xml.loadString(content, false);
            } else {
                jsonObj = objectMapper.readTree(content);
            }

            // TODO: Add page support for Hydra in RDFXML feeds ?
            if (StringUtils.isNotEmpty(params.numberOfRecordPath)) {
                try {
                    if (type == SimpleUrlResourceType.XML) {
                        Object element = Xml.selectSingle(xmlObj, params.numberOfRecordPath, xmlObj.getAdditionalNamespaces());
                        if (element != null) {
                            String s = getXmlElementTextValue(element);
                            numberOfRecordsToHarvest = Integer.parseInt(s);
                        }
                    } else if (type == SimpleUrlResourceType.JSON) {
                        numberOfRecordsToHarvest = jsonObj.at(params.numberOfRecordPath).asInt();
                    }
                    log.debug("Number of records to harvest: " + numberOfRecordsToHarvest);
                } catch (Exception e) {
                    errors.add(new HarvestError(context, e));
                    log.error(String.format("Failed to extract total in response at path %s. Error is: %s",
                        params.numberOfRecordPath, e.getMessage()));
                }
            }
            try {
                List<String> listOfUrlForPages = buildListOfUrl(params, numberOfRecordsToHarvest);
                for (int i = 0; i < listOfUrlForPages.size(); i++) {
                    if (i != 0) {
                        content = retrieveUrl(listOfUrlForPages.get(i));
                        if (type == SimpleUrlResourceType.XML) {
                            xmlObj = Xml.loadString(content, false);
                        } else {
                            jsonObj = objectMapper.readTree(content);
                        }
                    }
                    if (StringUtils.isNotEmpty(params.loopElement)
                        || type == SimpleUrlResourceType.RDFXML) {
                        Map<String, Element> uuids = new HashMap<>();
                        try {
                            if (type == SimpleUrlResourceType.XML) {
                                collectRecordsFromXml(xmlObj, uuids, aligner);
                            } else if (type == SimpleUrlResourceType.RDFXML) {
                                collectRecordsFromRdf(xmlObj, uuids, aligner);
                            } else if (type == SimpleUrlResourceType.JSON) {
                                collectRecordsFromJson(jsonObj, uuids, aligner);
                            }
                            aligner.align(uuids, errors);
                            listOfUuids.addAll(uuids.keySet());
                        } catch (Exception e) {
                            errors.add(new HarvestError(this.context, e));
                            log.error(String.format("Failed to collect record in response at path %s. Error is: %s",
                                params.loopElement, e.getMessage()));
                        }
                    }
                }
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

            log.info("Total records processed in all searches :" + listOfUuids.size());
            if (error) {
                log.warning("Due to previous errors the align process has not been called");
            }
        }
        aligner.cleanupRemovedRecords(listOfUuids);
        return aligner.getResult();
    }

    private void collectRecordsFromJson(JsonNode jsonObj,
                                        Map<String, Element> uuids,
                                        Aligner aligner) {
        JsonNode nodes = jsonObj.at(params.loopElement);
        log.debug(String.format("%d records found in JSON response.", nodes.size()));

        Function<JsonNode, String> uuidExtractor;
        try {
            uuidExtractor = buildJsonPointerExtractor();
        } catch (IllegalArgumentException pointerException) {
            try {
                uuidExtractor = buildJsonPathExtractor();
            } catch (JsonPathException pathException) {
                RuntimeException exception = new RuntimeException("Failed to compile recordIdPath as either JsonPointer or JsonPath!", pathException);
                exception.addSuppressed(pointerException);
                throw exception;
            }
        }

        for (JsonNode jsonRecord : nodes) {
            String uuid = null;
            try {
                uuid = this.extractUuidFromIdentifier(uuidExtractor.apply(jsonRecord));
            } catch (Exception e) {
                log.error(String.format("Failed to collect record UUID at path %s. Error is: %s",
                        params.recordIdPath, e.getMessage()));
            }
            String apiUrlPath = params.url.split("\\?")[0];
            try {
                URL apiUrl = new URL(apiUrlPath);
                String nodeUrl = apiUrl.getProtocol() + "://" + apiUrl.getAuthority();
                Element xml = convertJsonRecordToXml(jsonRecord, uuid, apiUrlPath, nodeUrl);
                uuids.put(uuid, xml);
            } catch (MalformedURLException e) {
                errors.add(new HarvestError(this.context, e));
                log.warning(String.format("Failed to parse JSON source URL. Error is: %s", e.getMessage()));
            }
        }
    }

    private Function<JsonNode, String> buildJsonPointerExtractor() {
        JsonPointer pointer = JsonPointer.compile(params.recordIdPath);
        return record -> record.at(pointer).asText();
    }

    private Function<JsonNode, String> buildJsonPathExtractor() {
        JsonPath path = JsonPath.compile(params.recordIdPath);
        Configuration configuration = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .mappingProvider(new JacksonMappingProvider());
        return record -> {
            // as per RFC9535, the result of a JsonPath is always an array
            ArrayNode node = path.read(record, configuration);
            // always return the first element
            return node.get(0).asText();
        };
    }

    private void collectRecordsFromRdf(Element xmlObj,
                                       Map<String, Element> uuids,
                                       Aligner aligner) {
        Map<String, Element> rdfNodes = null;
        try {
            rdfNodes = RDFUtils.getAllUuids(xmlObj);
        } catch (Exception e) {
            errors.add(new HarvestError(this.context, e));
            log.error(String.format("Failed to find records in RDF graph. Error is: %s",
                e.getMessage()));
        }
        if (rdfNodes != null) {
            log.debug(String.format("%d records found in RDFXML response.", rdfNodes.size()));

            // TODO: Add param
            boolean hashUuid = true;
            rdfNodes.forEach((uuid, xml) -> {
                if (hashUuid) {
                    uuid = Sha1Encoder.encodeString(uuid);
                }
                Element output = applyConversion(xml, uuid);
                if (output != null) {
                    uuids.put(uuid, output);
                }
            });
        }
    }

    private void collectRecordsFromXml(Element xmlObj,
                                       Map<String, Element> uuids,
                                       Aligner aligner) {
        List<Element> xmlNodes = null;
        try {
            xmlNodes = Xml.selectNodes(xmlObj, params.loopElement, xmlObj.getAdditionalNamespaces());
        } catch (JDOMException e) {
            log.error(String.format("Failed to query records using %s. Error is: %s",
                params.loopElement, e.getMessage()));
        }

        if (xmlNodes != null) {
            log.debug(String.format("%d records found in XML response.", xmlNodes.size()));

            xmlNodes.forEach(element -> {
                try {
                    String uuid = getXmlElementTextValue(Xml.selectSingle(element, params.recordIdPath, element.getAdditionalNamespaces()));
                    uuids.put(uuid, applyConversion(element, null));
                } catch (JDOMException e) {
                    log.error(String.format("Failed to extract UUID for record. Error is %s.",
                        e.getMessage()));
                    aligner.getResult().badFormat++;
                    aligner.getResult().totalMetadata++;
                }
            });
        }
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

    private String extractUuidFromIdentifier(final String identifier) {
        String uuid = identifier;
        if (Lib.net.isUrlValid(uuid)) {
            uuid = uuid.replaceFirst(".*/([^/?]+).*", "$1");
        }
        return uuid;
    }

    @VisibleForTesting
    protected List<String> buildListOfUrl(SimpleUrlParams params, int numberOfRecordsToHarvest) {
        List<String> urlList = new ArrayList<>();
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
        boolean startAtZero;
        if (StringUtils.isNumeric(pageFromParamValue)) {
            startAtZero = Integer.parseInt(pageFromParamValue) == 0;
        } else {
            log.warning(String.format(
                "Page from param '%s' not found or is not a numeric in URL '%s'. Can't build a list of pages.",
                params.pageFromParam, params.url));
            urlList.add(params.url);
            return urlList;
        }


        int numberOfPages = Math.abs((numberOfRecordsToHarvest + (startAtZero ? -1 : 0)) / numberOfRecordsPerPage) + 1;

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

    private Element convertJsonRecordToXml(JsonNode jsonRecord, String uuid, String apiUrl, String nodeUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String recordAsXml = XML.toString(
                new JSONObject(
                    objectMapper.writeValueAsString(jsonRecord)), "record");
            recordAsXml = Xml.stripNonValidXMLCharacters(recordAsXml)
                .replace("<@", "<")
                .replace("</@", "</")
                .replaceAll("(:|%)(?![^<>]*<)", "_"); // this removes colon and % from property names
            Element recordAsElement = Xml.loadString(recordAsXml, false);
            recordAsElement.addContent(new Element("uuid").setText(uuid));
            recordAsElement.addContent(new Element("apiUrl").setText(apiUrl));
            recordAsElement.addContent(new Element("nodeUrl").setText(nodeUrl));
            return applyConversion(recordAsElement, null);
        } catch (Exception e) {
            log.error(String.format("Failed to convert JSON record %s to XML. Error is: %s",
                uuid, e.getMessage()));
        }
        return null;
    }

    private Element applyConversion(Element input, String uuid) {
        if (StringUtils.isNotEmpty(params.toISOConversion)) {
            Path xslPath = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class)
                .getXsltConversion(params.toISOConversion);
            try {
                HashMap<String, Object> xslParams = new HashMap<>();
                if (uuid != null) {
                    xslParams.put("uuid", uuid);
                }
                return Xml.transform(input, xslPath, xslParams);
            } catch (Exception e) {
                errors.add(new HarvestError(this.context, e));
                log.error(String.format("Failed to apply conversion %s to record %s. Error is: %s",
                    params.toISOConversion, uuid, e.getMessage()));
                return null;
            }
        } else {
            return input;
        }
    }

    /**
     * Read the response of the URL.
     */
    private String retrieveUrl(String url) throws Exception {
        if (!Lib.net.isUrlValid(url))
            throw new BadParameterEx("Invalid URL", url);
        HttpGet httpMethod = null;
        ClientHttpResponse httpResponse = null;

        try {
            httpMethod = new HttpGet(createUrl(url));
            if (params.getApiKey() != null && !params.getApiKey().trim().isEmpty()) {
                String headerName = (params.getApiKeyHeader() != null && !params.getApiKeyHeader().isBlank())
                    ? params.getApiKeyHeader()
                    : "Authorization";
                httpMethod.addHeader(headerName, params.getApiKey());
            }

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
        return new URI(jsonUrl);
    }
}
