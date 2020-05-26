package org.fao.geonet.proxy;

import jeeves.server.UserSession;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.specification.LinkSpecs;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This is a class extending the real proxy to make sure we can tweak specifics like removing the CSRF token on requests
 *
 * @author delawen
 */
public class URITemplateProxyServlet extends org.mitre.dsmiley.httpproxy.URITemplateProxyServlet {

    private static final long serialVersionUID = 4847856943273604410L;
    private static final String P_SECURITY_MODE = "securityMode";

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

    protected SECURITY_MODE securityMode;

    @Autowired
    MetadataLinkRepository metadataLinkRepository;

    /**
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

    protected void initTarget() throws ServletException {
        securityMode = SECURITY_MODE.parse(getConfigParam(P_SECURITY_MODE));
        super.initTarget();
    }

    /**
     * Creates the HttpClient used to make the proxied requests.
     * It configures the client to use system properties like
     * <code>http.proxyHost</code> and <code>http.httpPort</code>.
     * <p>
     * Called from {@link #init(ServletConfig)}.
     *
     * @param requestConfig the configuration used for the request made by the client.
     */
    @Override
    protected HttpClient createHttpClient(RequestConfig requestConfig) {
        return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .useSystemProperties()
            .build();
    }

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
                if (proxyCallAllowed == false
                    && securityMode == SECURITY_MODE.DB_LINK_CHECK) {
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
}
