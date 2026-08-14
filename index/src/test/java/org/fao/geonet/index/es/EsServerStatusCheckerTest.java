/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.index.es;

import co.elastic.clients.transport.TransportException;
import co.elastic.clients.transport.http.TransportHttpClient;
import co.elastic.clients.util.BinaryData;
import org.fao.geonet.index.State;
import org.fao.geonet.index.Status;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EsServerStatusCheckerTest {

    private static final String SERVER_URL = "http://localhost:9200";

    @Test
    public void serverStatusIsReported() {
        Status status = check(new EsRestClientStub("green", null));
        assertEquals(State.GREEN, status.getState());

        status = check(new EsRestClientStub("yellow", null));
        assertEquals(State.YELLOW, status.getState());

        status = check(new EsRestClientStub("red", null));
        assertEquals(State.RED, status.getState());
    }

    @Test
    public void connectionFailureIsReportedAsUninitialized() {
        Status status = check(new EsRestClientStub(null, new ConnectException("Connection refused")));

        assertEquals(State.UNINITIALIZED, status.getState());
        assertTrue(status.getMessage(), status.getMessage().startsWith("Unable to revive connection to " + SERVER_URL));
    }

    /**
     * A response which the client can not decode is not a connectivity problem, it usually means
     * that the server version is not the one the client is built for.
     */
    @Test
    public void decodingFailureIsReportedAsVersionIssue() {
        Status status = check(new EsRestClientStub(null, transportException()));

        assertEquals(State.RED, status.getState());
        assertTrue(status.getMessage(), status.getMessage().contains("can not be decoded"));
        assertTrue(status.getMessage(), status.getMessage().contains("index server version is compatible"));
    }

    private Status check(EsRestClient client) {
        EsServerStatusChecker checker = new EsServerStatusChecker();
        checker.setStatus(new Status("index"));
        checker.client = client;
        return checker.checkState();
    }

    private TransportException transportException() {
        return new TransportException(new ResponseStub(), "Failed to decode response", "es/cluster.health");
    }

    /**
     * Returns the configured status or throws the configured error.
     */
    private static class EsRestClientStub extends EsRestClient {
        private final String serverStatus;
        private final IOException error;

        EsRestClientStub(String serverStatus, IOException error) {
            this.serverStatus = serverStatus;
            this.error = error;
            setServerUrl(SERVER_URL);
        }

        @Override
        public String getServerStatus() throws IOException {
            if (error != null) {
                throw error;
            }
            return serverStatus;
        }
    }

    /**
     * Minimal response required to build a {@link TransportException}.
     */
    private static class ResponseStub implements TransportHttpClient.Response {
        @Override
        public TransportHttpClient.Node node() {
            return new TransportHttpClient.Node(SERVER_URL);
        }

        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public String header(String name) {
            return null;
        }

        @Override
        public List<String> headers(String name) {
            return Collections.emptyList();
        }

        @Override
        public BinaryData body() {
            return null;
        }

        @Override
        public Object originalResponse() {
            return null;
        }

        @Override
        public void close() {
        }
    }
}
