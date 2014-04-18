package org.fao.geonet.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
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
 * User: Jesse
 * Date: 11/4/13
 * Time: 8:32 AM
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
            final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL ("http://localhost:"+port+ urlPath));
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
            XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL ("http://localhost:"+port+ permUrlPath));
            Element response = xmlRequest.execute();
            assertEquals(Xml.getString(expectedResponse), Xml.getString(response));

            xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL ("http://localhost:"+port+ tempUrlPath));
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

        final HttpRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals("user:pass", httpRequestBase.getURI().getUserInfo());
        assertEquals(1234, httpRequestBase.getURI().getPort());
        assertEquals("http", httpRequestBase.getURI().getScheme());
        assertEquals("/path", httpRequestBase.getURI().getPath());
        assertEquals("queryString", httpRequestBase.getURI().getQuery());
        assertEquals("fragment", httpRequestBase.getURI().getFragment());
    }

    @Test
    public void testCreateXmlRequestURLDefaultPortHttp() throws Exception {
        final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL
                ("http://host/path?queryString#fragment"));

        final HttpRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals(80, httpRequestBase.getURI().getPort());
        assertEquals("http", httpRequestBase.getURI().getScheme());
        assertEquals("/path", httpRequestBase.getURI().getPath());
        assertEquals("queryString", httpRequestBase.getURI().getQuery());
        assertEquals("fragment", httpRequestBase.getURI().getFragment());
    }

    @Test
    public void testCreateXmlRequestURLDefaultPortHttps() throws Exception {
        final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest(new URL
                ("https://host/path?queryString#fragment"));

        final HttpRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals(443, httpRequestBase.getURI().getPort());
        assertEquals("https", httpRequestBase.getURI().getScheme());
        assertEquals("/path", httpRequestBase.getURI().getPath());
        assertEquals("queryString", httpRequestBase.getURI().getQuery());
        assertEquals("fragment", httpRequestBase.getURI().getFragment());
    }

    @Test (expected = IllegalStateException.class)
    public void testAlternateXmlRequestNoArgConstructor() throws Exception {
        final XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest();
        xmlRequest.setupHttpMethod();
    }
    @Test (expected = IllegalArgumentException.class)
    public void testAlternateXmlRequestIllegalProtocol() throws Exception {
        new GeonetHttpRequestFactory().createXmlRequest("host", 1234, "ftp");
    }

    @Test
    public void testAlternateXmlRequestFactoryMethods() throws Exception {
        XmlRequest xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 1234, "http");
        HttpRequestBase httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals(1234, httpRequestBase.getURI().getPort());
        assertEquals("http", httpRequestBase.getURI().getScheme());
        assertEquals("", httpRequestBase.getURI().getPath());
        assertEquals(null, httpRequestBase.getURI().getQuery());
        assertEquals(null, httpRequestBase.getURI().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 1234);
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals(1234, httpRequestBase.getURI().getPort());
        assertEquals("http", httpRequestBase.getURI().getScheme());
        assertEquals("", httpRequestBase.getURI().getPath());
        assertEquals(null, httpRequestBase.getURI().getQuery());
        assertEquals(null, httpRequestBase.getURI().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 443);
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals(443, httpRequestBase.getURI().getPort());
        assertEquals("https", httpRequestBase.getURI().getScheme());
        assertEquals("", httpRequestBase.getURI().getPath());
        assertEquals(null, httpRequestBase.getURI().getQuery());
        assertEquals(null, httpRequestBase.getURI().getFragment());

                xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host", 80);
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals(80, httpRequestBase.getURI().getPort());
        assertEquals("http", httpRequestBase.getURI().getScheme());
        assertEquals("", httpRequestBase.getURI().getPath());
        assertEquals(null, httpRequestBase.getURI().getQuery());
        assertEquals(null, httpRequestBase.getURI().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest("host");
        httpRequestBase = xmlRequest.setupHttpMethod();

        assertTrue(httpRequestBase instanceof HttpGet);
        assertEquals("host", httpRequestBase.getURI().getHost());
        assertEquals(80, httpRequestBase.getURI().getPort());
        assertEquals("http", httpRequestBase.getURI().getScheme());
        assertEquals("", httpRequestBase.getURI().getPath());
        assertEquals(null, httpRequestBase.getURI().getQuery());
        assertEquals(null, httpRequestBase.getURI().getFragment());

        xmlRequest = new GeonetHttpRequestFactory().createXmlRequest();

        assertEquals(null, xmlRequest.getHost());
        assertEquals(80,  xmlRequest.getPort());
        assertEquals("http", xmlRequest.getProtocol());
        assertEquals(null, xmlRequest.getAddress());
        assertEquals(null, xmlRequest.getQuery());
        assertEquals(null, xmlRequest.getFragment());

    }
}
