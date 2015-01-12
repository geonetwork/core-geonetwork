package org.fao.geonet;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Locale;

/**
 * A Mock for responding to http requests.
 *
 * Created by Jesse on 1/24/14.
 */
public class MockCloseableHttpResponse implements CloseableHttpResponse {
    private final HttpResponse _response;

    public MockCloseableHttpResponse(int responseCode, String statusReason, byte[] response) {
        _response = new BasicHttpResponse(HttpVersion.HTTP_1_1, responseCode, statusReason);
        _response.setEntity(new ByteArrayEntity(response));
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public StatusLine getStatusLine() {
        return _response.getStatusLine();
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        _response.setStatusLine(statusline);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        _response.setStatusLine(ver, code);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        _response.setStatusLine(ver, code, reason);
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        _response.setStatusCode(code);
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        _response.setReasonPhrase(reason);
    }

    @Override
    public HttpEntity getEntity() {
        return _response.getEntity();
    }

    @Override
    public void setEntity(HttpEntity entity) {
        _response.setEntity(entity);
    }

    @Override
    public Locale getLocale() {
        return _response.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        _response.setLocale(loc);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return _response.getProtocolVersion();
    }

    @Override
    public boolean containsHeader(String name) {
        return _response.containsHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return _response.getHeaders(name);
    }

    @Override
    public Header getFirstHeader(String name) {
        return _response.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return _response.getLastHeader(name);
    }

    @Override
    public Header[] getAllHeaders() {
        return _response.getAllHeaders();
    }

    @Override
    public void addHeader(Header header) {
        _response.addHeader(header);
    }

    @Override
    public void addHeader(String name, String value) {
        _response.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        _response.setHeader(header);
    }

    @Override
    public void setHeader(String name, String value) {
        _response.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        _response.setHeaders(headers);
    }

    @Override
    public void removeHeader(Header header) {
        _response.removeHeader(header);
    }

    @Override
    public void removeHeaders(String name) {
        _response.removeHeaders(name);
    }

    @Override
    public HeaderIterator headerIterator() {
        return _response.headerIterator();
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return _response.headerIterator(name);
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
        return _response.getParams();
    }

    @Override
    @Deprecated
    public void setParams(HttpParams params) {
        _response.setParams(params);
    }
}
