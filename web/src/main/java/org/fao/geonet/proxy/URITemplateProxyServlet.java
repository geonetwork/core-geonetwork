/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.proxy;

import jeeves.server.UserSession;
import jeeves.server.sources.http.ServletPathFinder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.mapservices.MapService;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.specification.LinkSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This is a class extending the real proxy to make sure we can tweak specifics like removing the CSRF token on requests
 *
 * @author delawen
 */
public class URITemplateProxyServlet extends org.mitre.dsmiley.httpproxy.URITemplateProxyServlet {
    public static final String P_FORWARDEDHOST = "forwardHost";
    public static final String P_FORWARDEDHOSTPREFIXPATH = "forwardHostPrefixPath";
    private static final Logger LOGGER = Log.createLogger("URITemplateProxyServlet");
    private static final long serialVersionUID = 4847856943273604410L;
    private static final String P_SECURITY_MODE = "securityMode";
    private static final String P_IS_SECURED = "isSecured";

    private static final String TARGET_URI_NAME = "targetUri";

    /*
     * These are the "hop-by-hop" headers that should not be copied.
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html Overriding
     * parent
     */
    static {
        String[] headers = new String[]{
            "X-XSRF-TOKEN",
            "Access-Control-Allow-Origin",
            "Vary",
            "Access-Control-Allow-Credentials",
            "Strict-Transport-Security",
            "Etag"};
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }

    protected boolean doForwardHost = false;
    protected String doForwardHostPrefixPath = "";
    protected boolean isSecured = false;
    protected SECURITY_MODE securityMode;
    @Autowired
    MetadataLinkRepository metadataLinkRepository;
    private String username;
    private String password;

    /**
     * Init some properties from the servlet's init parameters. They try to be resolved the same way other GeoNetwork
     * configuration properties are resolved. If after checking externally no configuration can be found it relies into
     * the value of {@code targetUri} in web.xml
     * <ol>
     * <li> {@code ${GEONETWORK_APP_NAME}_${SERVLET_NAME}_TARGETURI}:
     *         Look for an environment variable, for example {@code MYWEBAPP_MICROSERVICESPROXY_TARGETURI}</li>
     * <li> {@code ${GEONETWORK_APP_NAME}.${SERVLET_NAME}.targetUri}: Look for a system property, for example
     *      {@code mywebapp.MicroservicesProxy.targetUri}</li>
     * <li> {@code ${GEONETWORK_APP_NAME}.${SERVLET_NAME}.targetUri}: Look for a property in {@code config.properties},
     *      for example {@code mywebapp.MicroservicesProxy.targetUri}</li>
     * <li> {@code GEONETWORK_${SERVLET_NAME}_TARGETURI}: Look for an environment variable starting by GEONETWORK, for example
     *      {@code GEONETWORK_MICROSERVICESPROXY_TARGETURI}</li>
     * <li> {@code geonetwork.${SERVLET_NAME}.targetUri}: Look for a property in {@code config.properties} starting by geonetwork,
     *      for example {@code geonetwork.MicroservicesProxy.targetUri}</li>
     * <li> {@code geonetwork.${SERVLET_NAME}.targetUri}: Look for a property in {@code config.properties} starting by geonetwork,
     *      for example {@code geonetwork.MicroservicesProxy.targetUri}</li>
     * </ol>
     * <p>
     * Finally, it checks the value of {@code targetUri} in web.xml if no external config has been found.
     * It stores the targetUri value found as an attribute in the ServletContext with the name $SERVLET_NAME.targetUri.
     *
     * @throws ServletException if the targetUri is not defined externally and the parameter is not even in web.xml.
     */
    @Override
    protected void initTarget() throws ServletException {
        securityMode = SECURITY_MODE.parse(getConfigParam(P_SECURITY_MODE));
        String doForwardHostString = getConfigParam(P_FORWARDEDHOST);
        if (doForwardHostString != null) {
            String doForwardHostPrefixPathString = getConfigParam(P_FORWARDEDHOSTPREFIXPATH);
            this.doForwardHost = Boolean.parseBoolean(doForwardHostString);
            this.doForwardHostPrefixPath =
                doForwardHostPrefixPathString != null ? doForwardHostPrefixPathString : "";
        }

        // Try to resolve it from java properties or environment variables.
        // The name is composed by the application base url,  servlet name, a point or an underscore and targetUri word.
        targetUriTemplate = getConfigValue(TARGET_URI_NAME);

        // If not set externally try to use the value from web.xml
        if (StringUtils.isBlank(targetUriTemplate)) {
            super.initTarget();
        }

        if (targetUriTemplate == null) {
            throw new ServletException(P_TARGET_URI + " is required in web.xml or set externally");
        }

        this.getServletContext().setAttribute(this.getServletName() + "." + P_TARGET_URI, targetUriTemplate);

        this.username = getConfigValue("username");
        this.password = getConfigValue("password");
        if (StringUtils.isBlank(this.username)) {
            this.username = getConfigParam("username");
            this.password = getConfigParam("password");
        }

        String doIsSecured = getConfigParam(P_IS_SECURED);
        if (doIsSecured != null) {
            isSecured = Boolean.parseBoolean(doIsSecured);
        }

    }

    private String getConfigValue(String suffix) {
        String result;

        // Property defined according to webapp name
        ServletPathFinder pathFinder = new ServletPathFinder(getServletContext());
        String baseUrl = pathFinder.getBaseUrl();
        String webappName = "";
        if (StringUtils.isNotEmpty(baseUrl)) {
            webappName = baseUrl.substring(1);
        }
        LOGGER.info(
            "Looking for " + webappName + "." + getServletName() + "." + suffix + " in Environment variables, " +
                "System properties and config.properties entries");
        result = resolveConfigValue(webappName + "." + getServletName() + "." + suffix);


        if (StringUtils.isBlank(result)) {
            // GEONETWORK is the default prefix

            LOGGER.info(
                "Looking for geonetwork." + getServletName() + "." + suffix + "  in Environment variables, " +
                    "System properties and config.properties entries");
            result = resolveConfigValue("geonetwork." + getServletName() + "." + suffix);
        }
        return result;
    }

    private String resolveConfigValue(String propertyName) {
        String propertyValue = null;
        try {
            Map<String, Object> environmentVariables = new HashMap<>(System.getenv());
            SystemEnvironmentPropertySource sysEnvPropSource = new SystemEnvironmentPropertySource("environment",
                environmentVariables);
            propertyValue = (String) sysEnvPropSource.getProperty(propertyName);
            if (propertyValue == null) {
                // Check Java properties
                propertyValue = System.getProperties().getProperty(propertyName);
            }
            if (propertyValue == null) {
                // look for an entry in config.properties
                Properties configProperties = new Properties();
                try (InputStream is = getServletContext().getResourceAsStream("/WEB-INF/config.properties")) {
                    configProperties.load(is);
                    propertyValue = configProperties.getProperty(propertyName);
                }
            }

        } catch (IOException e) {
            LOGGER.error("Error initiating " + getServletName() + " servlet property " + propertyName);
            LOGGER.error(e);
        }
        return propertyValue;
    }

    /**
     * Add the basic authentication
     */
    @Override
    protected HttpClient createHttpClient() {
        HttpClientBuilder clientBuilder = getHttpClientBuilder()
            .setDefaultRequestConfig(buildRequestConfig())
            .setDefaultSocketConfig(buildSocketConfig());

        clientBuilder.setMaxConnTotal(maxConnections);
        clientBuilder.setMaxConnPerRoute(maxConnections);

        if (!doHandleCompression) {
            clientBuilder.disableContentCompression();
        }

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        clientBuilder = clientBuilder.useSystemProperties();
        return buildHttpClient(clientBuilder);
    }

    /**
     * Creates the HttpClient used to make the proxied requests.
     * It configures the client to use system properties like
     * <code>http.proxyHost</code> and <code>http.httpPort</code>.
     * <p>
     * Called from {@link #init(ServletConfig)}.
     *
     * @param clientBuilder the httpClient builder used for creating the client.
     */
    @Override
    protected HttpClient buildHttpClient(HttpClientBuilder clientBuilder) {
        return clientBuilder.build();
    }

    @Override
    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        super.copyRequestHeaders(servletRequest, proxyRequest);
        if (doForwardHost) {
            StringBuffer url = servletRequest.getRequestURL();
            String uri = servletRequest.getRequestURI();
            String host = url.substring(servletRequest.getScheme().length() + 3, url.indexOf(uri));

            proxyRequest.setHeader("X-Forwarded-Host", host);
            proxyRequest.setHeader("X-Forwarded-Proto", servletRequest.getScheme());
            proxyRequest.setHeader("X-Forwarded-Prefix", servletRequest.getContextPath() + this.doForwardHostPrefixPath);
        }

        // remove host on proxy request to avoid issues in case of redirection
        proxyRequest.removeHeaders("Host");

        // Only attempt this logic is the Authorization is currently not used.
        if (StringUtils.isEmpty(servletRequest.getHeader(HttpHeaders.AUTHORIZATION))) {

            // List of authentication url to apply the logic.
            List<MapService> mapServiceList = ApplicationContextHolder.get().getBean("securedMapServices", List.class);

            // Only continue if the current request matches one of our list of authentication url patterns
            Optional<MapService> result = mapServiceList.stream().filter(u ->
                (MapService.UrlType.valueOf(u.getUrlType()).equals(MapService.UrlType.TEXT) && proxyRequest.getRequestLine().getUri().contains(u.getUrl())) ||
                    (MapService.UrlType.valueOf(u.getUrlType()).equals(MapService.UrlType.REGEXP) && proxyRequest.getRequestLine().getUri().matches(u.getUrl()))
            ).findFirst();
            if (result.isPresent()) {
                if (MapService.AuthType.valueOf(result.get().getAuthType()).equals(MapService.AuthType.BASIC)) {
                    proxyRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((result.get().getUsername() + ":" + result.get().getPassword()).getBytes()));
                } else {
                    if (MapService.AuthType.valueOf(result.get().getAuthType()).equals(MapService.AuthType.BEARER)) {
                        // In order to get a bearer token the user needs to be authenticated. - If not authenticated then we skip and the request will be made as anonymous.
                        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                            SecurityProviderUtil securityProviderUtil = SecurityProviderConfiguration.getSecurityProviderUtil();

                            if (securityProviderUtil != null) {
                                String authenticationHeaderValue = securityProviderUtil.getSSOAuthenticationHeaderValue();
                                if (!StringUtils.isEmpty(authenticationHeaderValue)) {
                                    proxyRequest.setHeader(HttpHeaders.AUTHORIZATION, authenticationHeaderValue);
                                }
                            } else {
                                throw new IllegalArgumentException("Invalid or Unsupported authentication type " + result.get().getAuthType() + " for current security provider");
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown authentication type " + result.get().getAuthType());
                    }
                }
            }
        }
    }

    /**
     * Hides the {{@link org.mitre.dsmiley.httpproxy.ProxyServlet#getContentLength(HttpServletRequest)}} private method.
     */
    private long getContentLength(HttpServletRequest request) {
        String contentLengthHeader = request.getHeader("Content-Length");
        return contentLengthHeader != null ? Long.parseLong(contentLengthHeader) : -1L;
    }

    @Override
    protected HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri, HttpServletRequest servletRequest) throws IOException {
        HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
        InputStreamEntity entity = new InputStreamEntity(servletRequest.getInputStream(), this.getContentLength(servletRequest));

        // https://github.com/mitre/HTTP-Proxy-Servlet/issues/67
        if ("GET".equals(method) || !isSecured) {
            eProxyRequest.setEntity(entity);
        } else {
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            eProxyRequest.setEntity(bufferedHttpEntity);
        }

        return eProxyRequest;
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws ServletException, IOException {

        switch (securityMode) {
            case NONE:
                super.service(servletRequest, servletResponse);
                break;
            case DB_LINK_CHECK:
                boolean proxyCallAllowed = false;

                // Check if user is authenticated
                try {
                    UserSession userSession = ApiUtils.getUserSession(servletRequest.getSession());
                    if (userSession.isAuthenticated()) {
                        proxyCallAllowed = true;
                    }
                } catch (SecurityException securityException) {
                    servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                        securityException.getMessage());
                }

                // Check if the link requested is in database link list
                if (!proxyCallAllowed) {
                    try {
                        URI uri = new URI(servletRequest.getParameter("url"));
                        String host = uri.getHost();
                        LinkRepository linkRepository =
                            ApplicationContextHolder.get().getBean(LinkRepository.class);
                        long linksFound = linkRepository.count(
                            LinkSpecs.filter(host, null, null,
                                null, null, null));
                        if (linksFound == 0) {
                            String message = String.format(
                                "The proxy does not allow to access '%s' " +
                                    "because the URL host was not registered in any metadata records.",
                                uri
                            );
                            if (linkRepository.count() == 0) {
                                servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "The proxy is configured with DB_LINK_CHECK mode " +
                                        "but the MetadataLink table is empty. " +
                                        "Administrator may need to analyze record links from the admin console " +
                                        "in order to register URL allowed by the proxy. " + message);
                            }
                            servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, message);
                        }
                        proxyCallAllowed = linksFound > 0;
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(String.format(
                            "'%s' is invalid. Error is: '%s'",
                            servletRequest.getParameter("url"),
                            e.getMessage()
                        ));
                    }
                }

                if (proxyCallAllowed) {
                    super.service(servletRequest, servletResponse);
                }
                break;
        }
    }

    private enum SECURITY_MODE {
        NONE,
        /**
         * Check if the host of the requested URL is registered in
         * at least one analyzed link in a metadata record.
         */
        DB_LINK_CHECK;

        public static SECURITY_MODE parse(String value) {
            if ("DB_LINK_CHECK".equals(value)) {
                return DB_LINK_CHECK;
            }
            return NONE;
        }
    }
}
