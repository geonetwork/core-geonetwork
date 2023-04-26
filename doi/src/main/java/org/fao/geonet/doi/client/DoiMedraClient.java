//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.springframework.http.client.ClientHttpResponse;

import java.io.InputStreamReader;

import static org.fao.geonet.doi.client.DoiSettings.LOGGER_NAME;

/**
 * Doi API client for European Registration Agency of DOI.
 *
 * See https://www.medra.org/.
 */
public class DoiMedraClient implements IDoiClient {

    public static final String MEDRA_SEARCH_KEY = "medra.org";
    public static final String DOI_ENTITY = "DOI";
    public static final String ALL_DOI_ENTITY = "All DOI";
    public static final String DOI_METADATA_ENTITY = "DOI metadata";
    public static final String MEDRA_NOT_SUPPORTED_EXCEPTION_MESSAGE = "Not supported by European Registration Agency of DOI.";

    private String apiUrl;
    private String doiPublicUrl;
    private String username;
    private String password;


    protected GeonetHttpRequestFactory requestFactory;

    public DoiMedraClient(String apiUrl, String username, String password, String doiPublicUrl) {
        this.apiUrl = apiUrl;
        this.doiPublicUrl = doiPublicUrl.endsWith("/") ? doiPublicUrl : doiPublicUrl + "/";
        this.username = username;
        this.password = password;

        requestFactory =
            ApplicationContextHolder.get().getBean(GeonetHttpRequestFactory.class);
    }

    @Override
    public void createDoi(String doi, String url) throws DoiClientException {
        // Medra is not like Datacite with a 2 steps process
    }

    @Override
    public String retrieveDoi(String doi) throws DoiClientException {
        return retrieve(createPublicUrl(doi));
    }

    @Override
    public String retrieveAllDoi(String doi) throws DoiClientException {
        throw new DoiClientException(MEDRA_NOT_SUPPORTED_EXCEPTION_MESSAGE);
    }

    /**
     * Rest API
     * A REST-API is made available to register DOIs for datasets in a B2B environment.
     *
     * A B2B client can submit a DOI Registration Message in OP system by sending an HTTP POST Request to the endpoint https://ra.publications.europa.eu/servlet/ws/doidata with:
     *
     *  request body: the DOI Registration Message XML document, UTT-8 encoded;
     *  request headers: "Content-Type:application/xml;charset=UTF-8";
     *  basic authentication: username and password of its user.
     * The max size of the xml document in the request body is 10Mb.
     *
     * The xml document in the request body must be valid according to OP DOI RA registration schema for datasets.
     *
     * The HTTP Response status will be:
     *
     *  200 OK: the submission was successful;
     *  400 Bad Request: in case of invalid XML;
     *  401 Unauthorized: the user cannot access the API;
     *  415 Unsupported Media Type: the HTTP Content-Type header is not "application/xml";
     *  500 Internal Server Error: internal server error.
     * The HTTP Response Content-Type will be "text/plain". The HTTP Response body will contain a free text with an error message, if any.
     *
     * The registration is asynchronous.
     *
     * @param doi eg. 10.5072/JQX3-61AT
     * @param doiMetadata XML in DataCite format as String
     * @throws DoiClientException
     */
    @Override
    public void createDoiMetadata(String doi, String doiMetadata)
            throws DoiClientException {

        create(this.apiUrl,
            doiMetadata,
            "application/xml");
    }

    @Override
    public String retrieveDoiMetadata(String doi) throws DoiClientException {
        return null;
    }

    @Override
    public void deleteDoiMetadata(String doi) throws DoiClientException {
        // DOI metadata can't be deleted
    }

    @Override
    public void deleteDoi(String doi) throws DoiClientException {
        // DOI can't be deleted
    }


    private void create(String url, String body, String contentType)
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

            if (status != HttpStatus.SC_OK) {
                String responseBody = CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
                String message = String.format(
                    "Failed to create '%s' with '%s'. Status is %d. Error is %s. Response body: %s",
                    url, body, status,
                    httpResponse.getStatusText(), responseBody);
                Log.info(LOGGER_NAME, message);
                throw new DoiClientException(message);
            } else {
                Log.info(LOGGER_NAME, String.format(
                    "DOI metadata registration sent to %s.", url));
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

    private String retrieve(String url)
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

    /**
     * Builds API endpoint url based on server URL configured in settings.
     *
     * eg. https://mds.datacite.org/doi/10.12770/38e00808-ffca-4266-943f-b691d3ca0bec
     */
    @Override
    public String createUrl(String service) {
        return this.apiUrl + service;
    }

    /**
     * Builds final DOI url based on public URL configured in settings.
     * Default is https://doi.org.
     *
     * eg. https://doi.org/10.17882/80771
     */
    @Override
    public String createPublicUrl(String doi) {
        return this.doiPublicUrl + doi;
    }
}
