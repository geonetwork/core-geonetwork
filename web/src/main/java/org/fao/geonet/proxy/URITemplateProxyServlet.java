package org.fao.geonet.proxy;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import javax.servlet.ServletConfig;

/**
 * This is a class extending the real proxy to make sure we can tweak specifics like removing the CSRF token on requests
 *
 * @author delawen
 */
public class URITemplateProxyServlet extends org.mitre.dsmiley.httpproxy.URITemplateProxyServlet {

    private static final long serialVersionUID = 4847856943273604410L;

    /**
     * These are the "hop-by-hop" headers that should not be copied. http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html Overriding
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

    /**
     * Creates the HttpClient used to make the proxied requests. It configures the client to use system properties like
     * <code>http.proxyHost</code> and <code>http.httpPort</code>.
     *
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
}
