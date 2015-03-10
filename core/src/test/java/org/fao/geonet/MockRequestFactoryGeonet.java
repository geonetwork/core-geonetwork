package org.fao.geonet;

import com.google.common.base.Function;
import com.vividsolutions.jts.util.Assert;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.MockXmlRequest;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates requests that return hardcoded responses.
 * <p/>
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:30 PM
 */
public class MockRequestFactoryGeonet extends GeonetHttpRequestFactory {

    private Map<Request, Object> _requests = new HashMap<Request, Object>();
    private Set<Request> _uncalledRequests = new HashSet<Request>();
    private Set<Request> _throwExceptionIfCalledRequests = new HashSet<Request>();
    private HttpClientBuilder _builder = Mockito.mock(HttpClientBuilder.class);
    private final CloseableHttpClient _mockClient = new MockCloseableHttpClient();

    {
        Mockito.when(_builder.build()).thenReturn(_mockClient);
    }

    @Override
    public XmlRequest createXmlRequest(String host, int port, String protocol) {
        final Request key = new Request(host, port, protocol, null);
        final Object request = getRequest(key);
        return (XmlRequest) request;
    }


    @Override
    public ClientHttpResponse execute(HttpUriRequest request, Function<HttpClientBuilder, Void> configurator) throws IOException {
        final URI uri = request.getURI();
        final Request key = new Request(uri.getHost(), uri.getPort(), uri.getScheme(), null);
        final XmlRequest xmlRequest = (XmlRequest) getRequest(key);
        return new MockClientHttpResponse(Xml.getString(xmlRequest.execute()).getBytes(Constants.CHARSET), HttpStatus.OK);
    }

    private Object getRequest(Request key) {
        final Object request = _requests.get(key);
        if (_throwExceptionIfCalledRequests.contains(key)) {
            throw new AssertionError("Request "+key+" should not have been made");
        }

        _uncalledRequests.remove(key);
        if (request == null) {
            throw new IllegalArgumentException("Unexpected request: " + key);
        }
        return request;
    }

    public void registerRequest(boolean expectedToBeCalled, String host, int port, String protocol, XmlRequest request) {
        final Request key = new Request(host, port, protocol, null);
        registerRequestInner(expectedToBeCalled, key, request);
    }

    public void registerRequest(boolean expectedToBeCalled, URI uri, MockCloseableHttpResponse request) {
        final Request key = new Request(uri);
        registerRequestInner(expectedToBeCalled, key, request);
    }

    private void registerRequestInner(boolean expectedToBeCalled, Request key, Object request) {
        Assert.isTrue(request != null);
        _requests.put(key, request);
        if (expectedToBeCalled) {
            _uncalledRequests.add(key);
        } else {
            _throwExceptionIfCalledRequests.add(key);
        }
    }

    public void clear() {
        _uncalledRequests.clear();
        _throwExceptionIfCalledRequests.clear();
        _requests.clear();
    }

    public void assertAllRequestsCalled() {
        StringBuilder errors = new StringBuilder();
        if (!_uncalledRequests.isEmpty()) {
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

    @Override
    public HttpClientBuilder getDefaultHttpClientBuilder() {
        return _builder;
    }

    private static class Request {
        final String protocol;
        final int port;
        final String host;
        final String path;

        public Request(String host, int port, String protocol, String path) {
            this.port = port;
            this.protocol = protocol;
            this.host = host;
            this.path = path == null ? "" : path;
        }

        public Request(URI uri) {
            this(uri.getAuthority(),
                    uri.getPort() == -1 ?
                            uri.getScheme().equalsIgnoreCase("https")?443:80
                        :
                            uri.getPort() ,
                    uri.getScheme(),
                    uri.getPath());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Request request = (Request) o;

            if (port != request.port) return false;
            if (host != null ? !host.equals(request.host) : request.host != null) return false;
            if (path != null ? !path.equals(request.path) : request.path != null) return false;
            if (protocol != null ? !protocol.equals(request.protocol) : request.protocol != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = protocol != null ? protocol.hashCode() : 0;
            result = 31 * result + port;
            result = 31 * result + (host != null ? host.hashCode() : 0);
            result = 31 * result + (path != null ? path.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return protocol + "://" + host + ":" + port+path;
        }
    }

    private class MockCloseableHttpClient extends CloseableHttpClient {

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException,
                ClientProtocolException {
            try {
                final URI uri = new URI(request.getRequestLine().getUri());

                final Request requestKey = new Request(uri);

                return (CloseableHttpResponse) getRequest(requestKey);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }

        @Override
        public HttpParams getParams() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClientConnectionManager getConnectionManager() {
            throw new UnsupportedOperationException();
        }
    }
}
