package org.fao.geonet.proxy;

import org.apache.http.message.BasicHeader;

/**
 * This is a class extending the real proxy to make sure we can tweak specifics like removing the CSRF token on requests
 * 
 * @author delawen
 *
 */
public class URITemplateProxyServlet extends org.mitre.dsmiley.httpproxy.URITemplateProxyServlet {

    private static final long serialVersionUID = 4847856943273604410L;

    /**
     * These are the "hop-by-hop" headers that should not be copied. http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html Overriding
     * parent
     */
    static {
        String[] headers = new String[] { "X-XSRF-TOKEN" };
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }
}
