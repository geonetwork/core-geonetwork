/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.translations.libretranslate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class LibreTranslateClient {
    private static final String LOGGER_NAME = "geonetwork.translate";

    private String serviceUrl;
    private String apiKey;

    private GeonetHttpRequestFactory requestFactory;

    public LibreTranslateClient(String serviceUrl, String apiKey) {
        this.serviceUrl = serviceUrl;
        this.apiKey = apiKey;

        this.requestFactory = ApplicationContextHolder.get().getBean(GeonetHttpRequestFactory.class);
    }

    public LibreTranslateResponse translate(String text, String fromLanguage, String toLanguage)
        throws LibreTranslateClientException {
        HttpPost postMethod = new HttpPost(this.serviceUrl);

        ((HttpUriRequest) postMethod).addHeader( new BasicHeader("Content-Type",  "application/x-www-form-urlencoded") );

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("q", text));
        urlParameters.add(new BasicNameValuePair("source", fromLanguage));
        urlParameters.add(new BasicNameValuePair("target", toLanguage));
        urlParameters.add(new BasicNameValuePair("format", "text"));
        urlParameters.add(new BasicNameValuePair("api_key", this.apiKey));

        try {
            HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
            postMethod.setEntity(postParams);

        } catch (UnsupportedEncodingException ex) {
            throw new LibreTranslateClientException(ex.getMessage());
        }

        try (ClientHttpResponse httpResponse = executeRequest(postMethod)) {
            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            String responseBody = CharStreams.toString(new InputStreamReader(httpResponse.getBody()));

            if (status != HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();
                LibreTranslateErrorResponse errorMessage = mapper.readValue(responseBody, LibreTranslateErrorResponse.class);
                String message = String.format(
                    "Failed to create translate text '%s' from '%s' to '%s'. Status is %d. Error is %s. Response body: %s",
                    text, fromLanguage, toLanguage, status,
                    httpResponse.getStatusText(), errorMessage.getError());
                Log.info(LOGGER_NAME, message);
                throw new LibreTranslateClientException(message);
            } else {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(responseBody, LibreTranslateResponse.class);
            }
        } catch (LibreTranslateClientException ex) {
            throw ex;
        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage(), ex);
            throw new LibreTranslateClientException(ex.getMessage(), ex);
        }
    }

    protected ClientHttpResponse executeRequest(HttpUriRequest method) throws IOException {
        final String requestHost = method.getURI().getHost();

        final Function<HttpClientBuilder, Void> requestConfiguration = new Function<>() {
            @Nullable
            @Override
            public Void apply(@Nonnull HttpClientBuilder input) {
                Lib.net.setupProxy(ApplicationContextHolder.get().getBean(SettingManager.class), input, requestHost);
                input.useSystemProperties();

                return null;
            }
        };

        return requestFactory.execute(method, requestConfiguration);
    }
}
