//=============================================================================
//===	Copyright (C) 2001-2019 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.url;

import com.google.common.base.Function;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;

public class UrlChecker {

    private static final Function<HttpClientBuilder, Void> HTTP_CLIENT_CONFIGURATOR = new Function<HttpClientBuilder, Void>() {
        @Nullable
        @Override
        public Void apply(@Nullable HttpClientBuilder originalConfig) {
            RequestConfig.Builder config = RequestConfig.custom()
                    .setConnectTimeout(10000)
                    .setConnectionRequestTimeout(10000)
                    .setSocketTimeout(10000);
            RequestConfig requestConfig = config.build();
            originalConfig.setDefaultRequestConfig(requestConfig);
            originalConfig.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/73.0.3683.86 Chrome/73.0.3683.86 Safari/537.36");
            return null;
        }
    };

    @Autowired
    protected GeonetHttpRequestFactory requestFactory;

    public LinkStatus getUrlStatus(String url) {
        return getUrlStatus(url, 5);
    }

    private LinkStatus getUrlStatus(String url, int tryNumber) {
        if (tryNumber < 1) {
            return buildTooManyRedirectStatus();
        }

        try (ClientHttpResponse response = getResponseFromServer(url)) {
            org.springframework.http.HttpStatus statusCode = buildStatusCode(response);
            if (statusCode.is3xxRedirection() && response.getHeaders().containsKey("Location")) {
                // follow the redirects
                return getUrlStatus(response.getHeaders().getFirst("Location"), tryNumber - 1);
            }
            return buildStatus(response, !statusCode.is2xxSuccessful());
        } catch (IOException e) {
            return buildIOExceptionStatus(e);
        }
    }


    private ClientHttpResponse getResponseFromServer(String url) throws IOException {
        HttpHead head = new HttpHead(url);
        ClientHttpResponse response = requestFactory.execute(head, HTTP_CLIENT_CONFIGURATOR);
        if (!shouldTryGetInsteadOfHead(response.getRawStatusCode())) {
            return response;
        }
        HttpGet get = new HttpGet(url);
        return requestFactory.execute(get);
    }

    private boolean shouldTryGetInsteadOfHead(int statusCode) {
        return  statusCode == HttpStatus.SC_BAD_REQUEST ||
                statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED ||
                statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    private LinkStatus buildTooManyRedirectStatus() {
        LinkStatus linkStatus = new LinkStatus();
        linkStatus.setStatusValue("310");
        linkStatus.setStatusInfo("ERR_TOO_MANY_REDIRECTS");
        linkStatus.setFailing(true);
        return linkStatus;
    }

    private LinkStatus buildIOExceptionStatus(IOException e) {
        LinkStatus linkStatus = new LinkStatus();
        linkStatus.setStatusValue("4XX");
        linkStatus.setStatusInfo(e.getMessage());
        linkStatus.setFailing(true);
        return linkStatus;
    }

    private LinkStatus buildStatus(ClientHttpResponse response, boolean failed) throws IOException {
        LinkStatus linkStatus = new LinkStatus();
        linkStatus.setStatusValue(response.getRawStatusCode() + "");
        linkStatus.setStatusInfo(response.getStatusText());
        linkStatus.setFailing(failed);
        return linkStatus;
    }


    private org.springframework.http.HttpStatus buildStatusCode(ClientHttpResponse response) throws IOException {
        try {
            return response.getStatusCode();
        } catch (Exception e) {
            return org.springframework.http.HttpStatus.valueOf((response.getRawStatusCode() / 100) * 100);
        }
    }
}
