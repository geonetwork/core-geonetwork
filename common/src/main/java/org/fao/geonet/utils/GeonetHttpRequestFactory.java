package org.fao.geonet.utils;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Factory interface for making different kinds of requests.  This is an interface so that tests can mock their own implementations.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:16 PM
 */
public class GeonetHttpRequestFactory {
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
        return new XmlRequest(host, port, protocol, this);
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
        String protocol = "http";
        if (port == 443) {
            protocol = "https";
        }
        return createXmlRequest(host, port, protocol);
    }

    /**
     * Ceate an XmlRequest from a url.
     *
     * @param url the url of the request.
     *
     * @return the XmlRequest.
     */
    public final XmlRequest createXmlRequest(URL url) {
        final int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
        final XmlRequest request = createXmlRequest(url.getHost(), port,
                url.getProtocol());

        request.setAddress(url.getPath());
        request.setQuery(url.getQuery());
        request.setFragment(url.getRef());
        request.setUserInfo(url.getUserInfo());

        return request;
    }

    public ClientHttpResponse execute(HttpUriRequest request) throws IOException {
        final Function<HttpClientBuilder, Void> noop = new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable HttpClientBuilder input) {
                return null;
            }
        };
        return execute(request, noop);
    }

    public ClientHttpResponse execute(HttpUriRequest request, final Credentials credentials, final AuthScope authScope) throws IOException {
        final Function<HttpClientBuilder, Void> setCredentials = new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nonnull HttpClientBuilder input) {

                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(authScope, credentials);
                input.setDefaultCredentialsProvider(credentialsProvider);

                return null;
            }
        };
        return execute(request, setCredentials);
    }

    public ClientHttpResponse execute(HttpUriRequest request, Function<HttpClientBuilder, Void> configurator) throws IOException {
        CloseableHttpClient httpClient = null;
        try {
            final HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setRedirectStrategy(new LaxRedirectStrategy());
            configurator.apply(builder);

            httpClient = builder.build();
            return new AdaptingResponse(httpClient.execute(request));
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    private static class AdaptingResponse extends AbstractClientHttpResponse {

        private final CloseableHttpResponse _response;

        public AdaptingResponse(CloseableHttpResponse response) {
            this._response = response;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return _response.getStatusLine().getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return _response.getStatusLine().getReasonPhrase();
        }

        @Override
        public void close() {
            try {
                _response.close();
            } catch (IOException e) {
                Log.error("org.fao.geonet", "Failure closing HttpResponse", e);
            }
        }

        @Override
        public InputStream getBody() throws IOException {
            return _response.getEntity().getContent();
        }

        @Override
        public HttpHeaders getHeaders() {
            final HttpHeaders httpHeaders = new HttpHeaders();

            final Header[] headers = _response.getAllHeaders();

            for (Header header : headers) {
                final HeaderElement[] elements = header.getElements();
                for (HeaderElement element : elements) {
                    httpHeaders.add(header.getName(), element.getValue());
                }
            }
            return httpHeaders;
        }
    }

}
