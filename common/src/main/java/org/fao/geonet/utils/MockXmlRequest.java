/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.utils;

import com.google.common.base.Predicate;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.jdom.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import jakarta.annotation.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * An XmlRequest implementation for tests.
 *
 * User: Jesse Date: 10/20/13 Time: 10:24 AM
 */
public class MockXmlRequest extends XmlRequest {

    private Set<Predicate<HttpUriRequestBase>> _unaccessed = new HashSet<Predicate<HttpUriRequestBase>>();
    private Map<Predicate<HttpUriRequestBase>, Callable<ClientHttpResponse>> _mapping = new LinkedHashMap<Predicate<HttpUriRequestBase>,
        Callable<ClientHttpResponse>>();

    public MockXmlRequest(String host, int port, String protocol) {
        super(host, port, protocol, null);
    }

    @Override
    protected ClientHttpResponse doExecute(HttpUriRequestBase httpMethod) throws IOException {

        for (Map.Entry<Predicate<HttpUriRequestBase>, Callable<ClientHttpResponse>> entry : _mapping.entrySet()) {
            if (entry.getKey().apply(httpMethod)) {
                try {
                    _unaccessed.remove(entry.getKey());
                    return entry.getValue().call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        throw new IllegalArgumentException(httpMethod + " is not have a mapped request");
    }

    /**
     * Begin mapping a request to a response.
     */
    public MockXmlRequestWithWhen when(String path) {
        return new MockXmlRequestWithWhen(path);
    }

    /**
     * Begin mapping a request to a response.
     */
    public MockXmlRequestWithWhen when(Predicate<HttpUriRequestBase> predicate) {
        return new MockXmlRequestWithWhen(predicate);
    }

    public List<String> getUnaccessedRequests() {
        List<String> missed = new ArrayList<String>();
        for (Predicate<HttpUriRequestBase> predicate : _unaccessed) {
            missed.add(predicate.toString());
        }
        return missed;
    }

    private static class PathMatchingPredicate implements Predicate<HttpUriRequestBase> {

        private final String _path;

        public PathMatchingPredicate(String path) {
            this._path = path;
        }

        @Override
        public boolean apply(@Nullable HttpUriRequestBase input) {
            if (input == null) {
                return false;
            }
            final URI uri;
            try {
                uri = input.getUri();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            final boolean equalPath = uri.toString().equalsIgnoreCase(_path);

            return input instanceof HttpGet && equalPath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PathMatchingPredicate that = (PathMatchingPredicate) o;

            if (_path != null ? !_path.equals(that._path) : that._path != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return _path != null ? _path.hashCode() : 0;
        }
    }

    /**
     * The in-between object that provides the methods for completing a {@link MockXmlRequest}
     * request mapping.
     */
    public class MockXmlRequestWithWhen {
        private final Predicate<HttpUriRequestBase> _predicate;

        private MockXmlRequestWithWhen(String path) {
            this._predicate = new PathMatchingPredicate(path);
        }

        public MockXmlRequestWithWhen(Predicate<HttpUriRequestBase> predicate) {
            this._predicate = predicate;
        }

        public MockXmlRequest thenReturn(InputStream response) {
            try {
                return thenReturn(Xml.loadStream(response));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public MockXmlRequest thenReturn(final Element response) {
            return thenReturn(new Callable<ClientHttpResponse>() {
                @Override
                public ClientHttpResponse call() throws Exception {
                    return new MockClientHttpResponse(Xml.getString(response).getBytes("UTF-8"), HttpStatus.OK);
                }
            });
        }

        public MockXmlRequest thenReturn(Callable<ClientHttpResponse> response) {
            MockXmlRequest.this._mapping.put(_predicate, response);
            MockXmlRequest.this._unaccessed.add(_predicate);
            return MockXmlRequest.this;
        }

    }
}
