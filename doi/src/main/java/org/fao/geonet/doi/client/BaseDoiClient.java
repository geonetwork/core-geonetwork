//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.doi.client;

import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.springframework.http.client.ClientHttpResponse;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.InputStreamReader;

import static org.fao.geonet.doi.client.DoiSettings.LOGGER_NAME;

public class BaseDoiClient {

    protected String apiUrl;
    protected String doiPublicUrl;
    protected String username;
    protected String password;

    protected GeonetHttpRequestFactory requestFactory;


    protected void create(String url, String body, String contentType,
                        int successStatus, String successMessage)
        throws DoiClientException {

        ClientHttpResponse httpResponse = null;
        HttpPost postMethod = null;

        try {
            Log.debug(LOGGER_NAME, "   -- URL: " + url);

            postMethod = new HttpPost(url);


            ((HttpUriRequest) postMethod).addHeader( new BasicHeader("Content-Type",  contentType + ";charset=UTF-8") );
            Log.debug(LOGGER_NAME, "   -- Request body: " + body);

            StringEntity requestEntity = new StringEntity(
                body,
                contentType,
                "UTF-8");

            postMethod.setEntity(requestEntity);

            httpResponse = executeRequest(postMethod);

            int status = httpResponse.getStatusCode().value();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            if (status != successStatus) {
                String responseBody = CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
                String message = String.format(
                    "Failed to create '%s' with '%s'. Status is %d. Error is %s. Response body: %s",
                    url, body, status,
                    httpResponse.getStatusText(), responseBody);
                Log.info(LOGGER_NAME, message);
                throw new DoiClientException(String.format(
                    "Error creating DOI: %s",
                    message))
                    .withMessageKey("exception.doi.serverErrorCreate")
                    .withDescriptionKey("exception.doi.serverErrorCreate.description", new String[]{message});
            } else {
                Log.info(LOGGER_NAME, String.format(
                    successMessage, url));
            }
        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage(), ex);
            throw new DoiClientException(String.format(
                "Error creating DOI: %s",
                ex.getMessage()))
                .withMessageKey("exception.doi.serverErrorCreate")
                .withDescriptionKey("exception.doi.serverErrorCreate.description", new String[]{ex.getMessage()});

        } finally {
            if (postMethod != null) {
                postMethod.reset();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }
    }


    protected String retrieve(String url)
        throws DoiClientException {

        ClientHttpResponse httpResponse = null;
        HttpGet getMethod = null;

        try {
            Log.debug(LOGGER_NAME, "   -- URL: " + url);

            getMethod = new HttpGet(url);

            httpResponse = executeRequest(getMethod);

            int status = httpResponse.getStatusCode().value();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            if (status == HttpStatus.SC_OK) {
                return CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
            } else if (status == HttpStatus.SC_NO_CONTENT) {
                return null; // Not found
            } else if (status == HttpStatus.SC_NOT_FOUND) {
                return null; // Not found
            } else {
                Log.info(LOGGER_NAME, "Retrieve DOI metadata end -- Error: " + httpResponse.getStatusText());

                String message = httpResponse.getStatusText() +
                    CharStreams.toString(new InputStreamReader(httpResponse.getBody()));

                throw new DoiClientException(String.format(
                    "Error retrieving DOI: %s",
                    message))
                    .withMessageKey("exception.doi.serverErrorRetrieve")
                    .withDescriptionKey("exception.doi.serverErrorRetrieve.description", new String[]{message});

            }

        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage(), ex);
            throw new DoiClientException(String.format(
                "Error retrieving DOI: %s",
                ex.getMessage()))
                .withMessageKey("exception.doi.serverErrorRetrieve")
                .withDescriptionKey("exception.doi.serverErrorRetrieve.description", new String[]{ex.getMessage()});

        } finally {
            if (getMethod != null) {
                getMethod.reset();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }
    }


    protected ClientHttpResponse executeRequest(HttpUriRequest method) throws Exception {
        final String requestHost = method.getURI().getHost();

        final Function<HttpClientBuilder, Void> requestConfiguration = new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nonnull HttpClientBuilder input) {
                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(username, password.toCharArray()));
                input.setDefaultCredentialsProvider(credentialsProvider);

                Lib.net.setupProxy(ApplicationContextHolder.get().getBean(SettingManager.class), input, requestHost);
                input.useSystemProperties();

                return null;
            }
        };

        return requestFactory.execute(method, requestConfiguration);
    }
}
