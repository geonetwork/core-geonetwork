package org.fao.geonet.utils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;

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
