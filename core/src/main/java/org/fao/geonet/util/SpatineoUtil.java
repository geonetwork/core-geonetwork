package org.fao.geonet.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpGet;

import javax.annotation.Nullable;
import org.apache.http.impl.client.HttpClientBuilder;
import com.google.common.base.Function;
import org.fao.geonet.lib.Lib;
import jeeves.server.context.ServiceContext;

public class SpatineoUtil {
    private static final String API = "https://api.spatineo.com/monitor/resolverRequest";
    private static final String LOGGER_NAME = "geonetwork.spatineo";

    public static Node registerServiceInSpatineoMonitor(String url, Integer maxNumberOfCalls) {
        DOMOutputter outputter = new DOMOutputter();
        Element report = new Element("report");
        try {
            Map<String, String> registrations = registerService(url, maxNumberOfCalls);
            registrations.forEach((k, e) -> {
                Element service = new Element("service");
                service.setAttribute("id", k);
                service.addContent(Xml.getXmlFromJSON(e));
                report.addContent(service);
            });
        } catch (TimeoutException | IOException e) {
            report.setAttribute("error", e.getMessage());
        }
        try {
            return outputter.output(new Document(report));
        } catch (JDOMException e) {
            Log.error(LOGGER_NAME,
                "Failed to build Spatineo registration report: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Register a WMS/WFS/ESRI service using Spatineo API.
     * See https://api.spatineo.com/monitor/add.html.
     * Returns Spatineo ID to access the service monitoring
     * pages. eg. https://directory.spatineo.com/service/{id}/index.html
     */
    public static Map<String, String> registerService(String url, Integer maxNumberOfCalls) throws TimeoutException, IOException {
        ServiceContext context = ServiceContext.get();
        final GeonetHttpRequestFactory requestFactory = context.getBean(GeonetHttpRequestFactory.class);
        HttpGet req = new HttpGet(API);
        final String requestHost = req.getURI().getHost();
        HttpPost method = null;
        ObjectMapper mapper = new ObjectMapper();
        String cookie = null;
        int maxTry = maxNumberOfCalls != null ? maxNumberOfCalls : 10;
        int i = 0;
        Map<String, String> listOfServices = new HashMap<>();

        ClientHttpResponse httpResponse = null;
        try {
            Log.debug(LOGGER_NAME, String.format("Sending registration for %s ...", url));
            method = new HttpPost(API);
            List<BasicNameValuePair> params = new ArrayList<>(1);
            params.add(new BasicNameValuePair("url", url));
            method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            while (i < maxTry) {
                if (StringUtils.isNotEmpty(cookie)) {
                    method.setEntity(null);
                    method.setHeader(HttpHeaders.COOKIE, "JSESSIONID=" + cookie);
                }
                httpResponse = requestFactory.execute(method, new Function<HttpClientBuilder, Void>() {
                    @Nullable
                    @Override
                    public Void apply(@Nullable HttpClientBuilder input) {
                        Lib.net.setupProxy(context, input, requestHost);
                        return null;
                    }
                });

                int status = httpResponse.getRawStatusCode();
                i++;

                Log.debug(LOGGER_NAME, String.format(
                    "Registration for %s returned status code: %d", url, status));

                if (status == HttpStatus.SC_OK) {
                    cookie = cookie == null ? httpResponse.getHeaders().get(HttpHeaders.SET_COOKIE).get(0) : cookie;
                    String body = CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
                    JsonNode responseNode = mapper.readTree(body);
                    JsonNode data = responseNode.get("data");
                    if (data != null) {
                        JsonNode result = data.get("result");
                        if (result != null && "fail".equals(result.textValue())) {
                            JsonNode message = data.get("message");
                            String errorMessage = String.format(
                                "Registration failed after %d calls. Error: %s",
                                i, message);
                            Log.error(LOGGER_NAME, errorMessage);
                            throw new RuntimeException(errorMessage);
                        }

                        JsonNode eventType = data.get("eventType");
                        if (eventType != null && "new".equals(eventType.textValue())) {
                            JsonNode wmsServiceId = data.get("wmsServiceID");
                            if (wmsServiceId != null) {
                                listOfServices.put(
                                    wmsServiceId.toString(),
                                    data.toString());
                            }
                            Log.info(LOGGER_NAME, String.format(
                                "Registration done for service %s. Id is %s. Response is: %s",
                                url, wmsServiceId, body));
                        } else if (eventType != null && "finished".equals(eventType.textValue())) {
                            Log.info(LOGGER_NAME, String.format(
                                "Registration completed for %s", url));
                            return listOfServices;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            String errorMessage = String.format(
                "Registration failed for %s after %d calls. Error: %s",
                url, i, ex.getMessage());
            Log.error(LOGGER_NAME, errorMessage);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
            IOUtils.closeQuietly(httpResponse);
        }

        String errorMessage = String.format(
            "Registration not finalized for %s after %d calls. Increase number of calls to the API.", url, i);
        Log.error(LOGGER_NAME, errorMessage);
        throw new TimeoutException(errorMessage);
    }
}
