package org.fao.geonet.utils;

import com.google.common.base.Predicate;
import org.apache.http.client.methods.HttpRequestBase;
import org.jdom.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * An XmlRequest implementation for tests.
 *
 * User: Jesse
 * Date: 10/20/13
 * Time: 10:24 AM
 */
public class MockXmlRequest extends XmlRequest {

    private Map<Predicate<HttpRequestBase>, ClientHttpResponse> _mapping = new HashMap<Predicate<HttpRequestBase>, ClientHttpResponse>();

    public MockXmlRequest(String host, int port, String protocol) {
        super(host, port, protocol, null);
    }

    @Override
    protected ClientHttpResponse doExecute(HttpRequestBase httpMethod) throws IOException {

        for (Map.Entry<Predicate<HttpRequestBase>, ClientHttpResponse> entry : _mapping.entrySet()) {
            if (entry.getKey().apply(httpMethod)) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException(httpMethod+" is not have a mapped request");
    }

    /**
     * Begin mapping a request to a response.
     * @param path
     * @return
     */
    public MockXmlRequestWithWhen when(String path) {
        return new MockXmlRequestWithWhen(path);
    }

    /**
     * Begin mapping a request to a response.
     * @param predicate
     * @return
     */
    public MockXmlRequestWithWhen when(Predicate<HttpRequestBase> predicate) {
        return new MockXmlRequestWithWhen(predicate);
    }

    /**
     * The in-between object that provides the methods for completing a {@link MockXmlRequest} request mapping.
     */
    public class MockXmlRequestWithWhen {
        private final Predicate<HttpRequestBase> _predicate;

        private MockXmlRequestWithWhen(String path) {
            this._predicate = new PathMatchingPredicate(path);
        }

        public MockXmlRequestWithWhen(Predicate<HttpRequestBase> predicate) {
            this._predicate = predicate;
        }

        public MockXmlRequest thenReturn(InputStream response) {
            try {
                return thenReturn(Xml.loadStream(response));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public MockXmlRequest thenReturn(Element response) {
            try {
                return thenReturn(new MockClientHttpResponse(Xml.getString(response).getBytes("UTF-8"), HttpStatus.OK));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        public MockXmlRequest thenReturn(ClientHttpResponse response) {
            MockXmlRequest.this._mapping.put(_predicate, response);
            return MockXmlRequest.this;
        }

    }

    private static class PathMatchingPredicate implements Predicate<HttpRequestBase> {

        private final String _path;
        private final String _query;

        public PathMatchingPredicate(String path) {
            String[] parts = path.split("\\?");
            this._path = parts[0];
            if (parts.length == 2) {
                this._query = parts[1];
            } else {
                this._query = "";
            }
        }

        @Override
        public boolean apply(@Nullable HttpRequestBase input) {
            final URI uri = input.getURI();
            final boolean equalPath = uri.getPath().equalsIgnoreCase(_path);
            final boolean equalQuery;

            if (uri.getQuery() == null) {
                equalQuery = "".equals(_query);
            } else {
                equalQuery = uri.getQuery().equalsIgnoreCase(_query);
            }

            return equalPath && equalQuery;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PathMatchingPredicate that = (PathMatchingPredicate) o;

            if (_path != null ? !_path.equals(that._path) : that._path != null) return false;
            if (_query != null ? !_query.equals(that._query) : that._query != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = _path != null ? _path.hashCode() : 0;
            result = 31 * result + (_query != null ? _query.hashCode() : 0);
            return result;
        }
    }
}
