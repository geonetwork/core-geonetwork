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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.jdom.Element;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test creating requests.
 * <p/>
 * User: Jesse Date: 11/4/13 Time: 8:32 AM
 */
public class GeonetHttpRequestFactoryTest {
    @Test
    public void testReadUrl() throws Exception {
        final int port = 29483;
        InetSocketAddress address = new InetSocketAddress(port);
        HttpServer httpServer = HttpServer.create(address, 0);
        final Element expectedResponse = new Element("resource").addContent(new Element("id").setText("test"));
        HttpHandler requestHandler = new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = Xml.getString(expectedResponse).getBytes();
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
                    response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        };
        final String urlPath = "/1234.xml";
        httpServer.createContext(urlPath, requestHandler);
        try {
            httpServer.start();
            final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL("http://localhost:" + port + urlPath));
            final Element response = xmlRequest.execute();
            assertEquals(Xml.getString(expectedResponse), Xml.getString(response));
        } finally {
            httpServer.stop(0);
        }
    }

    @Test
    @Ignore // Ignore because it requires a running instance
    public void testBasicAuthenticationWithPreemptiveMode() throws Exception {
        XmlRequest req = new GeonetHttpRequestFactory().
            createXmlRequest(new URL("http://localhost:8081"));
        req.setCredentials("admin", "admin");
        req.setAddress("/geonetwork/srv/eng/xml.info");
        req.addParam("type", "me");

        Element response = req.execute();
        assertEquals(response.getName(), "info");
        assertEquals(response.getChild("me").getAttributeValue("authenticated"), "false");

        req.setPreemptiveBasicAuth(true);
        response = req.execute();
        assertEquals(response.getName(), "info");
        assertEquals(response.getChild("me").getAttributeValue("authenticated"), "true");
    }

    @Test
    public void testFollowsRedirects() throws Exception {
        final int port = 29484;
        InetSocketAddress address = new InetSocketAddress(port);
        HttpServer httpServer = HttpServer.create(address, 0);

        final Element expectedResponse = new Element("resource").addContent(new Element("id").setText("test"));
        HttpHandler finalHandler = new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = Xml.getString(expectedResponse).getBytes();
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
                    response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        };
        final String finalUrlPath = "/final.xml";
        httpServer.createContext(finalUrlPath, finalHandler);

        HttpHandler permRedirectHandler = new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = finalUrlPath.getBytes();
                exchange.getResponseHeaders().add("location", finalUrlPath);
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM,
                    response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        };
        final String permUrlPath = "/permRedirect.xml";
        httpServer.createContext(permUrlPath, permRedirectHandler);

        HttpHandler tempRedirectHandler = new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = finalUrlPath.getBytes();
                exchange.getResponseHeaders().add("location", finalUrlPath);
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP,
                    response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        };
        final String tempUrlPath = "/tempRedirect.xml";
        httpServer.createContext(tempUrlPath, tempRedirectHandler);


        try {
            httpServer.start();
            XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL("http://localhost:" + port + permUrlPath));
            Element response = xmlRequest.execute();
            assertEquals(Xml.getString(expectedResponse), Xml.getString(response));

            xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL("http://localhost:" + port + tempUrlPath));
            response = xmlRequest.execute();
            assertEquals(Xml.getString(expectedResponse), Xml.getString(response));
        } finally {
            httpServer.stop(0);
        }
    }

    @Test
    public void testCreateXmlRequestURL() throws Exception {
        final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL
            ("http://user:pass@host:1234/path?queryString#fragment"));

        final HttpUriRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        assertEquals("user:pass", httpRequestBase.getUri().getUserInfo());
        assertEquals(1234, httpRequestBase.getUri().getPort());
        assertEquals("http", httpRequestBase.getUri().getScheme());
        assertEquals("/path", httpRequestBase.getUri().getPath());
        assertEquals("queryString", httpRequestBase.getUri().getQuery());
        assertEquals("fragment", httpRequestBase.getUri().getFragment());
    }

    @Test
    public void testCreateXmlRequestURLDefaultPortHttp() throws Exception {
        final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL
            ("http://host/path?queryString#fragment"));

        final HttpUriRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        //Expects -1 instead of 80, this sentinal value will remove the :80 from the URI.
        assertEquals(-1, httpRequestBase.getUri().getPort());
        assertEquals("http", httpRequestBase.getUri().getScheme());
        assertEquals("/path", httpRequestBase.getUri().getPath());
        assertEquals("queryString", httpRequestBase.getUri().getQuery());
        assertEquals("fragment", httpRequestBase.getUri().getFragment());
    }

    @Test
    public void testCreateXmlRequestURLDefaultPortHttps() throws Exception {
        final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL
            ("https://host/path?queryString#fragment"));

        final HttpUriRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        //Expects -1 instead of 80, this sentinal value will remove the :443 from the URI.
        assertEquals(-1, httpRequestBase.getUri().getPort());
        assertEquals("https", httpRequestBase.getUri().getScheme());
        assertEquals("/path", httpRequestBase.getUri().getPath());
        assertEquals("queryString", httpRequestBase.getUri().getQuery());
        assertEquals("fragment", httpRequestBase.getUri().getFragment());
    }

    @Test(expected = IllegalStateException.class)
    public void testAlternateXmlRequestNoArgConstructor() throws Exception {
        final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest();
        xmlRequest.setupHttpMethod();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAlternateXmlRequestIllegalProtocol() throws Exception {
        new GeonetHttpRequestFactory().createXmlRequest("host", 1234, "ftp");
    }

    @Test
    public void testAlternateXmlRequestFactoryMethods() throws Exception {
        XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 1234, "http");
        HttpUriRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        assertEquals(1234, httpRequestBase.getUri().getPort());
        assertEquals("http", httpRequestBase.getUri().getScheme());
        assertEquals("", httpRequestBase.getUri().getPath());
        assertEquals(null, httpRequestBase.getUri().getQuery());
        assertEquals(null, httpRequestBase.getUri().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 1234);
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        assertEquals(1234, httpRequestBase.getUri().getPort());
        assertEquals("http", httpRequestBase.getUri().getScheme());
        assertEquals("", httpRequestBase.getUri().getPath());
        assertEquals(null, httpRequestBase.getUri().getQuery());
        assertEquals(null, httpRequestBase.getUri().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 443);
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        assertEquals(443, httpRequestBase.getUri().getPort());
        assertEquals("https", httpRequestBase.getUri().getScheme());
        assertEquals("", httpRequestBase.getUri().getPath());
        assertEquals(null, httpRequestBase.getUri().getQuery());
        assertEquals(null, httpRequestBase.getUri().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 80);
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        assertEquals(80, httpRequestBase.getUri().getPort());
        assertEquals("http", httpRequestBase.getUri().getScheme());
        assertEquals("", httpRequestBase.getUri().getPath());
        assertEquals(null, httpRequestBase.getUri().getQuery());
        assertEquals(null, httpRequestBase.getUri().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host");
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getUri().getHost());
        assertEquals(80, httpRequestBase.getUri().getPort());
        assertEquals("http", httpRequestBase.getUri().getScheme());
        assertEquals("", httpRequestBase.getUri().getPath());
        assertEquals(null, httpRequestBase.getUri().getQuery());
        assertEquals(null, httpRequestBase.getUri().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest();

        assertEquals(null, xmlRequest.getHost());
        assertEquals(80, xmlRequest.getPort());
        assertEquals("http", xmlRequest.getProtocol());
        assertEquals(null, xmlRequest.getAddress());
        assertEquals(null, xmlRequest.getQuery());
        assertEquals(null, xmlRequest.getFragment());

    }
}
