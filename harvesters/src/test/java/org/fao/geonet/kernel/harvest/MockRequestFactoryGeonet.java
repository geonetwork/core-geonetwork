package org.fao.geonet.kernel.harvest;

import com.vividsolutions.jts.util.Assert;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates requests that return hardcoded responses.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:30 PM
 */
public class MockRequestFactoryGeonet extends GeonetHttpRequestFactory {

    private Map<Object, Object> _requests = new HashMap<Object, Object>();

    @Override
    public XmlRequest createXmlRequest(String host, int port, String protocol) {
        final Object request = _requests.get(new Request(host, port, protocol));
        if (request == null) {
            throw new IllegalArgumentException("Unexpected request: "+protocol+"://"+host+":"+port);
        }
        return (XmlRequest) request;
    }

    public void registerRequest(String host, int port, String protocol, XmlRequest request) {
        Assert.isTrue(request != null);
        _requests.put(new Request(host, port, protocol), request);
    }

    public void clear() {
        _requests.clear();
    }


    private static class Request {
        final String protocol;
        final int port;
        final String host;

        public Request(String host, int port, String protocol) {
            this.port = port;
            this.protocol = protocol;
            this.host = host;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Request)) return false;

            Request request = (Request) o;

            if (port != request.port) return false;
            if (host != null ? !host.equals(request.host) : request.host != null) return false;
            if (protocol != null ? !protocol.equals(request.protocol) : request.protocol != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = protocol != null ? protocol.hashCode() : 0;
            result = 31 * result + port;
            result = 31 * result + (host != null ? host.hashCode() : 0);
            return result;
        }
    }
}
