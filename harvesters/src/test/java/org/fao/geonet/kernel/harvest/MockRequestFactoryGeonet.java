package org.fao.geonet.kernel.harvest;

import com.vividsolutions.jts.util.Assert;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.MockXmlRequest;
import org.fao.geonet.utils.XmlRequest;

import java.util.*;

/**
 * Creates requests that return hardcoded responses.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:30 PM
 */
public class MockRequestFactoryGeonet extends GeonetHttpRequestFactory {

    private Map<Object, Object> _requests = new HashMap<Object, Object>();
    private Set<Object> _uncalledRequests = new HashSet<Object>();

    @Override
    public XmlRequest createXmlRequest(String host, int port, String protocol) {
        final Request key = new Request(host, port, protocol);
        final Object request = _requests.get(key);
        _uncalledRequests.remove(key);
        if (request == null) {
            throw new IllegalArgumentException("Unexpected request: "+protocol+"://"+host+":"+port);
        }
        return (XmlRequest) request;
    }

    public void registerRequest(String host, int port, String protocol, XmlRequest request) {
        Assert.isTrue(request != null);
        final Request key = new Request(host, port, protocol);
        _requests.put(key, request);
        _uncalledRequests.add(key);
    }

    public void clear() {
        _uncalledRequests.clear();
        _requests.clear();
    }

    public void assertAllRequestsCalled() {
        StringBuilder errors = new StringBuilder();
        if (!_uncalledRequests.isEmpty()){
            errors.append("There are mapped requests that where never called:\n\n" + _uncalledRequests + "\n\n");
        }

        for (Object o : _requests.values()) {
            if (o instanceof MockXmlRequest) {
                MockXmlRequest mockXmlRequest = (MockXmlRequest) o;
                List<String> requestErrors = mockXmlRequest.getUnaccessedRequests();

                for (String requestError : requestErrors) {
                    errors.append("\n\n");
                    errors.append(requestError);
                }
            }
        }

        org.junit.Assert.assertTrue(errors.toString(), errors.length() == 0);
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

        @Override
        public String toString() {
            return protocol + "://" +host+":"+port;
        }
    }
}
