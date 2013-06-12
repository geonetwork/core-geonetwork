package org.geonetwork.http.proxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jeeves.utils.Log;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.geonetwork.http.proxy.util.RequestUtil;
import org.geonetwork.http.proxy.util.ServletConfigUtil;

/**
 * Http proxy for ajax calls
 *
 * @author Jose Garcia
 */
public class HttpProxyServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GetMethod httpGet = null;

        try {
            String url = RequestUtil.getParameter(request, PARAM_URL, defaultProxyUrl);
            String host = url.split("/")[2];

            // Get the proxy parameters
            //TODO: Add dependency injection to set proxy config from GeoNetwork settings, using also the credentials configured
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");

            // Get rest of parameters to pass to proxied url
            HttpMethodParams urlParams = new HttpMethodParams();

            @SuppressWarnings("unchecked")
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                if (!paramName.equalsIgnoreCase(PARAM_URL)) {
                    urlParams.setParameter(paramName, request.getParameter(paramName));
                }
            }

            // Checks if allowed host
            if (!isAllowedHost(host)) {
                //throw new ServletException("This proxy does not allow you to access that location.");
                returnExceptionMessage(response, "This proxy does not allow you to access that location.");
                return;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                HttpClient client = new HttpClient();

                // Added support for proxy
                if (proxyHost != null && proxyPort != null) {
                    client.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort));
                }

                httpGet = new GetMethod(url);
                httpGet.setParams(urlParams);
                client.executeMethod(httpGet);

                if (httpGet.getStatusCode() == HttpStatus.SC_OK) {
                    Header contentType = httpGet.getResponseHeader(HEADER_CONTENT_TYPE);
                    String[] contentTypesReturned = contentType.getValue().split(";");
                    if (!isValidContentType(contentTypesReturned[0])) {
                        contentTypesReturned = contentType.getValue().split(" ");
                        if (!isValidContentType(contentTypesReturned[0])) {
                            throw new ServletException("Status: 415 Unsupported media type");
                        }
                    }

                    // Sets response contentType
                    response.setContentType(getResponseContentType(contentTypesReturned));

                    String responseBody = httpGet.getResponseBodyAsString().trim();

                    PrintWriter out = response.getWriter();
                    out.print(responseBody);

                    out.flush();
                    out.close();

                } else {
                    returnExceptionMessage(response, "Unexpected failure: " + httpGet.getStatusLine().toString());
                }

                httpGet.releaseConnection();

            } else {
                //throw new ServletException("only HTTP(S) protocol supported");
                returnExceptionMessage(response, "only HTTP(S) protocol supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //throw new ServletException("Some unexpected error occurred. Error text was: " + e.getMessage());
            returnExceptionMessage(response, "Some unexpected error occurred. Error text was: " + e.getMessage());
        } finally {
            if (httpGet != null) httpGet.releaseConnection();
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PostMethod httpPost = null;

        try {
            String url = RequestUtil.getParameter(request, PARAM_URL, defaultProxyUrl);
            String host = url.split("/")[2];

            // Get the proxy parameters
            //TODO: Add dependency injection to set proxy config from GeoNetwork settings, using also the credentials configured
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");

            // Get rest of parameters to pass to proxied url
            HttpMethodParams urlParams = new HttpMethodParams();

            @SuppressWarnings("unchecked")
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                if (!paramName.equalsIgnoreCase(PARAM_URL)) {
                    urlParams.setParameter(paramName, request.getParameter(paramName));
                }
            }

            // Checks if allowed host
            if (!isAllowedHost(host)) {
                //throw new ServletException("This proxy does not allow you to access that location.");
                returnExceptionMessage(response, "This proxy does not allow you to access that location.");
                return;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                httpPost = new PostMethod(url);
                httpPost.setParams(urlParams);

                // Transfer bytes from in to out
                PrintWriter out = response.getWriter();
                String body = RequestUtil.inputStreamAsString(request);

                HttpClient client = new HttpClient();

                // Added support for proxy
                if (proxyHost != null && proxyPort != null){
                    client.getHostConfiguration().setProxy(proxyHost, Integer.valueOf(proxyPort));
                }

                RequestEntity entity = new StringRequestEntity(body, request.getContentType(), request.getCharacterEncoding());
                httpPost.setRequestEntity(entity);

                client.executeMethod(httpPost);

                if (httpPost.getStatusCode() == HttpStatus.SC_OK) {
                    Header contentType = httpPost.getResponseHeader(HEADER_CONTENT_TYPE);
                    String[] contentTypesReturned = contentType.getValue().split(";");
                    if (!isValidContentType(contentTypesReturned[0])) {
                        contentTypesReturned = contentType.getValue().split(" ");
                        if (!isValidContentType(contentTypesReturned[0])) {
                            throw new ServletException("Status: 415 Unsupported media type");
                        }
                    }

                    // Sets response contentType
                    response.setContentType(getResponseContentType(contentTypesReturned));

                    String responseBody = httpPost.getResponseBodyAsString();

                    out.print(responseBody);
                    out.flush();
                    out.close();

                } else {
                    returnExceptionMessage(response, "Unexpected failure: " + httpPost.getStatusLine().toString());
                }

                httpPost.releaseConnection();

            } else {
                //throw new ServletException("only HTTP(S) protocol supported");
                returnExceptionMessage(response, "only HTTP(S) protocol supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //throw new ServletException("Some unexpected error occurred. Error text was: " + e.getMessage());
            returnExceptionMessage(response, "Some unexpected error occurred. Error text was: " + e.getMessage());
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

    private void returnExceptionMessage(HttpServletResponse response, String message) throws IOException {
        response.setContentType("Content-Type: text/plain");

        response.setContentLength(message.length());

        PrintWriter out = response.getWriter();
        out.print(message);
        response.flushBuffer();
    }
}
