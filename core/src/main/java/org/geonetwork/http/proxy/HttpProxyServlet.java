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

package org.geonetwork.http.proxy;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.geonetwork.http.proxy.util.RequestUtil;
import org.geonetwork.http.proxy.util.ServletConfigUtil;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Http proxy for ajax calls
 *
 * @author Jose Garcia
 */
public class HttpProxyServlet extends HttpServlet {

    protected transient AutowireCapableBeanFactory ctx;

    private static final long serialVersionUID = 1L;

    // Url to proxy
    private static final String PARAM_URL = "url";

    // Content type parameter name in header
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    // Servlet init parameters set in servlet definition in web.xml
    private static final String INIT_PARAM_ALLOWED_HOSTS = "AllowedHosts";
    private static final String INIT_PARAM_ALLOWED_CONTENT_TYPES = "AllowedContentTypes";
    private static final String INIT_PARAM_DEFAULT_PROXY_URL = "DefaultProxyUrl";

    // Default URL for proxy
    private String defaultProxyUrl;

    // List of allowed hosts for the proxy
    private List<InetAddress> allowedHosts;

    // List of valid content types for request
    private String[] validContentTypes;


    /**
     * Initializes servlet Content Types allowed and the host to use in the proxy
     *
     * @param servletConfig         Servlet configuration
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        String allowedHostsValues = ServletConfigUtil.getInitParamValue(servletConfig, INIT_PARAM_ALLOWED_HOSTS);
        String validContentTypesValues = ServletConfigUtil.getInitParamValue(servletConfig, INIT_PARAM_ALLOWED_CONTENT_TYPES);

        // Default proxy url when url parameter is not provided in request
        defaultProxyUrl = ServletConfigUtil.getInitParamValue(servletConfig, INIT_PARAM_DEFAULT_PROXY_URL);

        // List of allowed hosts accessed by proxy. If empty, all hosts are allowed
        if (StringUtils.isNotEmpty(allowedHostsValues)) {
            String[] hostNames = allowedHostsValues.split(",");
            List<InetAddress> addresses = new ArrayList<InetAddress>(hostNames.length * 2);
            for (String host : hostNames) {
                try {
                    InetAddress[] allByName = InetAddress.getAllByName(host);
                    for (InetAddress inetAddress : allByName) {
                        addresses.add(inetAddress);
                    }
                } catch (UnknownHostException e) {
                    Log.error(Geonet.GEONETWORK+".httpproxy", "Error resolving address of host:"+host, e);
                }
            }
            if(!addresses.isEmpty()) {
                this.allowedHosts = addresses;
            }
        }

        // List of allowed content types for request
        if (validContentTypesValues != null)
            validContentTypes = validContentTypesValues.split(",");
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpHead method = null;

        try {

            String url = RequestUtil.getParameter(request, PARAM_URL, defaultProxyUrl);
            String host = url.split("/")[2];
            final String uri = createURI(request, url);


            // Get the proxy parameters
            //TODO: Add dependency injection to set proxy config from GeoNetwork settings, using also the credentials configured
//            String proxyHost = System.getProperty("http.proxyHost");
//            String proxyPort = System.getProperty("http.proxyPort");

            // Checks if allowed host
            if (!isAllowedHost(host)) {
                //throw new ServletException("This proxy does not allow you to access that location.");
                response.sendError(org.springframework.http.HttpStatus.BAD_REQUEST.value(), "This proxy does not allow you to access " +
                                                                                            "that location.");
                return;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                method = new HttpHead(uri);

                ConfigurableApplicationContext applicationContext = JeevesDelegatingFilterProxy.getApplicationContextFromServletContext
                        (getServletContext());

                SettingManager sm = applicationContext.getBean(SettingManager.class);

                final HttpClientBuilder clientBuilder = applicationContext.getBean(GeonetHttpRequestFactory.class).getDefaultHttpClientBuilder();

                // Added support for proxy
                Lib.net.setupProxy(sm, clientBuilder, new URL(url).getHost());

                HttpClient client = clientBuilder.build();

                final HttpResponse httpResponse = client.execute(method);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Header contentType = httpResponse.getLastHeader(HEADER_CONTENT_TYPE);
                    String[] contentTypesReturned = contentType.getValue().split(";");
                    if (!isValidContentType(contentTypesReturned[0])) {
                        contentTypesReturned = contentType.getValue().split(" ");
                        if (!isValidContentType(contentTypesReturned[0])) {
                            response.sendError(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE,
                                    String.format(
                                            "Status: 415 Unsupported media type '%s'",
                                            contentTypesReturned[0]));
                            return;
                        }
                    }

                    // Sets response contentType
                    response.setContentType(getResponseContentType(contentTypesReturned));

                    final String bodyAsString = IOUtils.toString(httpResponse.getEntity().getContent(), Constants.ENCODING).trim();
                    if (bodyAsString != null) {
                        String responseBody = bodyAsString.trim();

                        PrintWriter out = response.getWriter();
                        out.print(responseBody);

                        out.flush();
                        out.close();
                    }

                } else {
                    returnExceptionMessage(response,
                            httpResponse.getStatusLine().getStatusCode(),
                            "Unexpected failure: " + httpResponse.getStatusLine().getReasonPhrase()
                    );
                }

            } else {
                //throw new ServletException("only HTTP(S) protocol supported");
                response.sendError(org.springframework.http.HttpStatus.BAD_REQUEST.value(), "only HTTP(S) protocol supported");
            }
        } catch (Exception e) {
            response.sendError(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Some unexpected error occurred. Error text was: " + e.getMessage());
        } finally {
            if (method != null) method.releaseConnection();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpGet httpGet = null;

        try {
            String url = RequestUtil.getParameter(request, PARAM_URL, defaultProxyUrl);
            String host = url.split("/")[2];
            final String uri = createURI(request, url);


            // Checks if allowed host
            if (!isAllowedHost(host)) {
                //throw new ServletException("This proxy does not allow you to access that location.");
                returnExceptionMessage(response, HttpStatus.SC_FORBIDDEN, "This proxy does not allow you to access that location.");
                return;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                ConfigurableApplicationContext applicationContext = JeevesDelegatingFilterProxy.getApplicationContextFromServletContext
                        (getServletContext());

                SettingManager sm = applicationContext.getBean(SettingManager.class);

                final HttpClientBuilder clientBuilder = applicationContext.getBean(GeonetHttpRequestFactory.class).getDefaultHttpClientBuilder();

                // Added support for proxy
                Lib.net.setupProxy(sm, clientBuilder, new URL(url).getHost());

                HttpClient client = clientBuilder.build();

                httpGet = new HttpGet(uri);

                final HttpResponse httpResponse = client.execute(httpGet);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Header contentType = httpResponse.getLastHeader(HEADER_CONTENT_TYPE);
                    String[] contentTypesReturned = contentType.getValue().split(";");
                    if (!isValidContentType(contentTypesReturned[0])) {
                        contentTypesReturned = contentType.getValue().split(" ");
                        if (!isValidContentType(contentTypesReturned[0])) {
                            throw new ServletException(
                                    String.format("Status: 415 Unsupported media type '%s'",
                                            contentTypesReturned[0]));
                        }
                    }

                    // Sets response contentType
                    response.setContentType(getResponseContentType(contentTypesReturned));

                    String responseBody = IOUtils.toString(httpResponse.getEntity().getContent(), response.getCharacterEncoding()).trim();

                    PrintWriter out = response.getWriter();
                    out.print(responseBody);

                    out.flush();
                    out.close();

                } else {
                    returnExceptionMessage(response,
                           httpResponse.getStatusLine().getStatusCode(),
                            "Unexpected failure: " + httpResponse.getStatusLine().getReasonPhrase()
                    );
                }

                httpGet.releaseConnection();

            } else {
                returnExceptionMessage(response, HttpStatus.SC_FORBIDDEN, "only HTTP(S) protocol supported");
            }
        } 
        catch (UnknownHostException e) {
            e.printStackTrace();
            response.sendError(HttpStatus.SC_NOT_FOUND, "url can't be found");
        } catch (Exception e) {
            e.printStackTrace();
            //throw new ServletException("Some unexpected error occurred. Error text was: " + e.getMessage());
            returnExceptionMessage(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Some unexpected error occurred. Error text was: " + e.getMessage());
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
    }

    private String createURI(HttpServletRequest request, String url) {
        // Get rest of parameters to pass to proxied url
        StringBuilder paramString = new StringBuilder();

        @SuppressWarnings("unchecked")
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (!paramName.equalsIgnoreCase(PARAM_URL)) {
                if (paramString.length() > 0) {
                    paramString.append('&');
                }
               paramString.append(paramName).append("=").append(request.getParameter(paramName));
            }
        }
        if(paramString.length() > 0) {
            url += "?" + paramString;
        }
        return url;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpPost httpPost = null;

        try {
            String url = RequestUtil.getParameter(request, PARAM_URL, defaultProxyUrl);
            String host = url.split("/")[2];

            String uri = createURI(request, url);

            // Checks if allowed host
            if (!isAllowedHost(host)) {
                //throw new ServletException("This proxy does not allow you to access that location.");
            	returnExceptionMessage(response, HttpStatus.SC_FORBIDDEN, "This proxy does not allow you to access that location.");
                return;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                httpPost = new HttpPost(uri );

                ConfigurableApplicationContext applicationContext = JeevesDelegatingFilterProxy.getApplicationContextFromServletContext
                        (getServletContext());

                SettingManager sm = applicationContext.getBean(SettingManager.class);

                final HttpClientBuilder clientBuilder = applicationContext.getBean(GeonetHttpRequestFactory.class).getDefaultHttpClientBuilder();

                // Added support for proxy
                Lib.net.setupProxy(sm, clientBuilder, new URL(url).getHost());

                HttpClient client = clientBuilder.build();

                // Transfer bytes from in to out
                PrintWriter out = response.getWriter();
                String body = RequestUtil.inputStreamAsString(request);

                String ct = request.getContentType();
                if (ct != null) {
                    String[] cts = ct.split(";");
                    ct = cts[0].trim();
                } else {
                    ct = "text/plain";
                }

                final ContentType contentType1 = ContentType.create(ct,
                        request.getCharacterEncoding());
                StringEntity entity = new StringEntity(body, contentType1);
                httpPost.setEntity(entity);

                final HttpResponse httpResponse = client.execute(httpPost);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Header contentType = httpResponse.getLastHeader(HEADER_CONTENT_TYPE);
                    String[] contentTypesReturned = contentType.getValue().split(";");
                    if (!isValidContentType(contentTypesReturned[0])) {
                        contentTypesReturned = contentType.getValue().split(" ");
                        if (!isValidContentType(contentTypesReturned[0])) {
                            throw new ServletException(
                                    String.format(
                                        "Status: 415 Unsupported media type '%s'",
                                        contentTypesReturned[0]));
                        }
                    }

                    // Sets response contentType
                    response.setContentType(getResponseContentType(contentTypesReturned));
                    String responseBody = IOUtils.toString(httpResponse.getEntity().getContent());

                    out.print(responseBody);
                    out.flush();
                    out.close();

                } else {
                	returnExceptionMessage(response,
                            httpResponse.getStatusLine().getStatusCode(),
                            "Unexpected failure: " + httpResponse.getStatusLine().getReasonPhrase()
                    );
                }

            } else {
                //throw new ServletException("only HTTP(S) protocol supported");
            	returnExceptionMessage(response, HttpStatus.SC_FORBIDDEN, "only HTTP(S) protocol supported");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            response.sendError(HttpStatus.SC_NOT_FOUND, "url can't be found");
        } catch (Throwable e) {
            e.printStackTrace();
            //throw new ServletException("Some unexpected error occurred. Error text was: " + e.getMessage());
            returnExceptionMessage(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Some unexpected error occurred. Error text was: " + e.getMessage());
        } finally {
            if (httpPost != null) httpPost.releaseConnection();
        }
    }
    
    /**
     * Gets the contentType for response
     *
     * @param contentTypes Content types returned by request
     * @return Content type for response
     */
    private String getResponseContentType(String[] contentTypes) {
        String ct = "";
        String charset = ";charset=ISO-8859-1";
        if (contentTypes.length >= 1) ct = contentTypes[0];
        if (contentTypes.length >= 2) charset = ";" + contentTypes[1];

        if ((ct.equals("application/vnd.ogc.gml")) ||
                (ct.equals("text/plain")) ||
                (ct.equals("text/html")) ||
                (ct.equals("application/vnd.ogc.se_xml")) ||
                (ct.equals("application/vnd.ogc.sld+xml")) ||
                (ct.equals("application/vnd.ogc.wms_xml")))

            return "text/xml" + charset;

        else
            return "" + ct + charset;
    }

    /**
     * Checks if a host is valid for proxy
     *
     * @param host Hosts to validate
     * @return True if host is allowed or no restrictions for hosts (allowedHosts not defined)
     *         False in other case
     */
    private boolean isAllowedHost(String host) {
        if(host == null || host.trim().isEmpty()) return false;
        if (allowedHosts == null || allowedHosts.isEmpty()) return true;

        InetAddress[] targetAddr;

        try {
            targetAddr = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            return false;
        }

        for (InetAddress address : allowedHosts) {
            for (InetAddress targetOpt : targetAddr) {
                if (targetOpt.equals(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a content type from request is valid
     *
     * @param contentType Content type to validate
     * @return True if content type is valid, false in other case
     */
    private boolean isValidContentType(String contentType) {
        if (validContentTypes == null) return false;

        for (String ct : validContentTypes) {
            if (ct.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    private void returnExceptionMessage(HttpServletResponse response, int codeStatus, String message) throws IOException {
        response.setContentType("Content-Type: text/plain");
        response.setStatus(codeStatus);
        response.setContentLength(message.length());

        PrintWriter out = response.getWriter();
        out.print(message);
        response.flushBuffer();
    }
}
