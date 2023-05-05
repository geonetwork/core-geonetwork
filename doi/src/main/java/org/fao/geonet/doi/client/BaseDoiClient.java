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
package org.fao.geonet.doi.client;

import com.google.common.io.CharStreams;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.springframework.http.client.ClientHttpResponse;

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

            httpResponse = requestFactory.execute(
                postMethod,
                new UsernamePasswordCredentials(username, password), AuthScope.ANY);
            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            if (status != successStatus) {
                String responseBody = CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
                String message = String.format(
                    "Failed to create '%s' with '%s'. Status is %d. Error is %s. Response body: %s",
                    url, body, status,
                    httpResponse.getStatusText(), responseBody);
                Log.info(LOGGER_NAME, message);
                throw new DoiClientException(message);
            } else {
                Log.info(LOGGER_NAME, String.format(
                    successMessage, url));
            }
        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage());
            throw new DoiClientException(ex.getMessage());

        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
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


            httpResponse = requestFactory.execute(getMethod,
                new UsernamePasswordCredentials(username, password), AuthScope.ANY);
            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            if (status == HttpStatus.SC_OK) {
                return CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
            } else if (status == HttpStatus.SC_NO_CONTENT) {
                return null; // Not found
            } else if (status == HttpStatus.SC_NOT_FOUND) {
                return null; // Not found
            } else {
                Log.info(LOGGER_NAME, "Retrieve DOI metadata end -- Error: " + httpResponse.getStatusText());

                throw new DoiClientException( httpResponse.getStatusText() +
                    CharStreams.toString(new InputStreamReader(httpResponse.getBody())));
            }

        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage());
            throw new DoiClientException(ex.getMessage());

        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }
    }
}
