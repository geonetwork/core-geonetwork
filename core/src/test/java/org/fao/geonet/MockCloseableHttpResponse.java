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

package org.fao.geonet;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicHttpResponse;
/* No generic migration for classes in the `org.apache.http.params` package exists, please migrate manually */
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

/**
 * A Mock for responding to http requests so we can check status code and
 * body content.
 *
 * Created by Jesse on 1/24/14.
 */
public class MockCloseableHttpResponse implements ClassicHttpResponse {
    private final BasicClassicHttpResponse _response;
    private int code;

    public MockCloseableHttpResponse(int responseCode, String statusReason, byte[] response) {
        _response = new BasicClassicHttpResponse(responseCode,statusReason);
        _response.setEntity(new ByteArrayEntity(response,ContentType.APPLICATION_OCTET_STREAM));
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public int getCode() {
        return _response.getCode();
    }

    @Override
    public void setCode(int code) {
        _response.setCode(code);
    }

    @Override
    public String getReasonPhrase() {
        return _response.getReasonPhrase();
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
    public boolean containsHeader(String name) {
        return _response.containsHeader(name);
    }

    @Override
    public int countHeaders(String name) {
        return _response.countHeaders(name);
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
    public Header getHeader(String name) throws ProtocolException {
        return _response.getHeader(name);
    }

    @Override
    public Header[] getHeaders() {
        return _response.getHeaders();
    }

    @Override
    public Header getLastHeader(String name) {
        return _response.getLastHeader(name);
    }

    @Override
    public Iterator<Header> headerIterator() {
        return _response.headerIterator();
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return _response.headerIterator(name);
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        _response.setVersion(version);
    }

    @Override
    public ProtocolVersion getVersion() {
        return _response.getVersion();
    }

    @Override
    public void addHeader(Header header) {
        _response.addHeader(header);
    }

    @Override
    public void addHeader(String name, Object value) {
        _response.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        _response.setHeader(header);
    }

    @Override
    public void setHeader(String name, Object value) {
        _response.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        _response.setHeaders(headers);
    }

    @Override
    public boolean removeHeader(Header header) {
        return _response.removeHeader(header);
    }

    @Override
    public boolean removeHeaders(String name) {
        return _response.removeHeaders(name);
    }

}
