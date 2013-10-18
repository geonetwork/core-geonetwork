package org.fao.geonet.utils;

import org.fao.geonet.utils.XmlRequest;

/**
 * Factory interface for making different kinds of requests.  This is an interface so that tests can mock their own implementations.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:16 PM
 */
public class HttpRequestFactory {
    /**
     * Create a default XmlRequest.
     *
     * @return
     */
    public final XmlRequest createXmlRequest() {
        return createXmlRequest(null, 80, "http");
    }
    /**
     * Create a default XmlRequest.
     *
     * @return
     */
    public XmlRequest createXmlRequest(String host, int port, String protocol) {
        return new XmlRequest(null, 80, "http");
    }

    /**
     * Create a default XmlRequest.
     *
     * @return
     */
    public final XmlRequest createXmlRequest(String host) {
        return createXmlRequest(host, 80, "http");
    }
    /**
     * Create a default XmlRequest.
     *
     * @return
     */
    public final XmlRequest createXmlRequest(String host, int port) {
        return createXmlRequest(host, port, "http");
    }
}
