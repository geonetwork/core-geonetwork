//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.lib.NetLib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpResponse;
import sun.net.ftp.FtpLoginException;
import org.fao.geonet.utils.Log;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

public class UrlChecker {

    @Autowired
    SettingManager settingManager;

     static String userAgentPropertyNameDefault =  "GeoNetwork URL Link Checker";

     public String urlCheckerUserAgent = null;

    public String getUserAgent() {
        if ((urlCheckerUserAgent == null) || urlCheckerUserAgent.contains("$"))  // not set in properties file
            return userAgentPropertyNameDefault; // use default
        return urlCheckerUserAgent;
    }

    public void setUserAgent(String ua) {
        urlCheckerUserAgent = ua;
    }

    private final Function<HttpClientBuilder, Void> HTTP_CLIENT_CONFIGURATOR = new Function<HttpClientBuilder, Void>() {
        @Nullable
        @Override
        public Void apply(@Nullable HttpClientBuilder originalConfig) {
            RequestConfig.Builder config = RequestConfig.custom()
                    .setConnectTimeout(10000, TimeUnit.MILLISECONDS)
                    .setConnectionRequestTimeout(10000, TimeUnit.MILLISECONDS)
                    .setResponseTimeout(10000, TimeUnit.MILLISECONDS);
            RequestConfig requestConfig = config.build();
            originalConfig.setDefaultRequestConfig(requestConfig);
            originalConfig.setUserAgent(getUserAgent());
            return null;
        }
    };



    @Autowired
    protected GeonetHttpRequestFactory requestFactory;

    public LinkStatus getUrlStatus(String url) {
        try {
            if (url.startsWith("ftp")) {
                return getFTPStatus(url);
            }
            LinkStatus status =  getUrlStatus(url, 5);
            Log.info(Geonet.GEONETWORK,"getUrlStatus for: "+url);
            Log.info(Geonet.GEONETWORK,"result: "+status);
            return status;
        } catch (Exception e) {
            return buildExceptionStatus(e);
        }
    }

    private LinkStatus getFTPStatus(String url) throws IOException {
        LinkStatus linkStatus = new LinkStatus();
        linkStatus.setFailing(false);
        try {
            URLConnection con = new URL(url).openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.getInputStream().close();
            linkStatus.setStatusValue("OK");
            linkStatus.setStatusInfo("new URL(url).openStream() success.");
        } catch (FtpLoginException e) {
            linkStatus.setStatusValue("Need username/password");
            linkStatus.setStatusInfo("new URL(url).openStream() need username/password.");
        }
        return linkStatus;
    }

    private LinkStatus getUrlStatus(String url, int tryNumber) throws IOException {
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
        }
    }


    private ClientHttpResponse getResponseFromServer(String url) throws IOException {
        HttpHead head = new HttpHead(url);

        Function<HttpClientBuilder, Void> HTTP_CLIENT_CONFIGURATOR2 = new Function<HttpClientBuilder, Void>() {

            @Nullable
            @Override
            public Void apply(@Nullable HttpClientBuilder originalConfig) {
                HTTP_CLIENT_CONFIGURATOR.apply(originalConfig); // call the base one
                //given a URL, find its host name
                String hostname = "";
                try {
                    URL _url = new URL(url);
                    hostname = _url.getHost();
                } catch (MalformedURLException e) {
                    Log.info(Geonet.GEONETWORK,"UrlChecker: cannot determine hostname from url: "+url);
                 }
                //now we have hostname, we can configure proxy
                Lib.net.setupProxy(settingManager, originalConfig, hostname);
                return null;
            }
        };

        ClientHttpResponse response = requestFactory.execute(head, HTTP_CLIENT_CONFIGURATOR2);
        if (!shouldTryGetInsteadOfHead(response.getRawStatusCode())) {
            return response;
        }
        HttpGet get = new HttpGet(url);
        return requestFactory.execute(get, HTTP_CLIENT_CONFIGURATOR2);
    }

    private boolean shouldTryGetInsteadOfHead(int statusCode) {
        return  statusCode == HttpStatus.SC_NOT_FOUND ||
                statusCode == HttpStatus.SC_BAD_REQUEST ||
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

    private LinkStatus buildExceptionStatus(Exception e) {
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
