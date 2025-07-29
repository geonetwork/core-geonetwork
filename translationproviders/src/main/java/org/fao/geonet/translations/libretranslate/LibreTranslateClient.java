/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.json.JSONObject;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LibreTranslateClient {
    private static final String LOGGER_NAME = "geonetwork.translate";

    private final String serviceUrl;
    private final String apiKey;

    private final GeonetHttpRequestFactory requestFactory;

    public LibreTranslateClient(String serviceUrl, String apiKey) {
        this.serviceUrl = serviceUrl;
        this.apiKey = apiKey;

        this.requestFactory = ApplicationContextHolder.get().getBean(GeonetHttpRequestFactory.class);
    }

    public LibreTranslateResponse translate(String text, String fromLanguage, String toLanguage)
        throws LibreTranslateClientException {
        HttpPost postMethod = new HttpPost(this.serviceUrl);

        postMethod.addHeader( new BasicHeader("Content-Type",  "application/json") );
        postMethod.addHeader( new BasicHeader("Accept",  "application/json") );

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("q", text);
        jsonObject.put("source", fromLanguage);
        jsonObject.put("target", toLanguage);
        jsonObject.put("format", "text");
        jsonObject.put("api_key", this.apiKey);

        StringEntity entity = new StringEntity(jsonObject.toString(), StandardCharsets.UTF_8.name());
        postMethod.setEntity(entity);

        try (ClientHttpResponse httpResponse = executeRequest(postMethod)) {
            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            String responseBody = IOUtils.toString(httpResponse.getBody(), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            if (status != HttpStatus.SC_OK) {
                LibreTranslateErrorResponse errorMessage = mapper.readValue(responseBody, LibreTranslateErrorResponse.class);
                String message = String.format(
                    "Failed to create translate text '%s' from '%s' to '%s'. Status is %d. Error is %s. Response body: %s",
                    text, fromLanguage, toLanguage, status,
                    httpResponse.getStatusText(), errorMessage.getError());
                Log.info(LOGGER_NAME, message);
                throw new LibreTranslateClientException(message);
            } else {
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
