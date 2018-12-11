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
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.apache.commons.httpclient.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;

import java.io.InputStreamReader;
import java.text.MessageFormat;

import static org.fao.geonet.doi.client.DoiSettings.LOGGER_NAME;

/**
 * Doi API client.
 *
 * See https://mds.datacite.org/static/apidoc.
 *
 * @author Jose García
 */
public class DoiClient {

    public static final String DOI_ENTITY = "DOI";
    public static final String ALL_DOI_ENTITY = "All DOI";
    public static final String DOI_METADATA_ENTITY = "DOI metadata";
    private String serverUrl;
    private String username;
    private String password;

    private boolean testMode;

    protected GeonetHttpRequestFactory requestFactory;

    public DoiClient(String serverUrl, String username, String password) {
        this(serverUrl, username, password, true);
    }

    public DoiClient(String serverUrl, String username, String password, boolean testMode) {
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
        this.username = username;
        this.password = password;
        this.testMode = testMode;

        requestFactory =
            ApplicationContextHolder.get().getBean(GeonetHttpRequestFactory.class);
    }

    /**
     * POST will mint new DOI if specified DOI doesn't exist.
     * This method will attempt to update URL if you specify existing DOI.
     * Standard domains and quota restrictions check will be performed.
     * Datacentre's doiQuotaUsed will be increased by 1. A new record in Datasets will be created.
     *
     * @param doi   The DOI prefix for the datacenter / identifier eg. 10.5072/TEST-1
     * @param url   The landing page
     * @throws DoiClientException
     */
    public void createDoi(String doi, String url)
            throws DoiClientException {

        String requestBody = MessageFormat.format(
                "doi={0}\nurl={1}",
                doi, url);

        create(createUrl("doi"), requestBody,
            "text/plain;charset=UTF-8",
            DOI_ENTITY);
    }

    /**
     * This request returns an URL associated with a given DOI.
     *
     * @param doi
     * @return If response status is 200: URL representing a dataset;
     *          empty for 204;
     *          null for not found;
     *          otherwise short explanation for non-200 status.
     * @throws DoiClientException
     */
    public String retrieveDoi(String doi)
            throws DoiClientException {

        return retrieve(createUrl("doi/" + doi), DOI_ENTITY);
    }

    /**
     * This request returns a list of all DOIs for the requesting datacentre.
     * There is no guaranteed order.
     *
     * @param doi
     * @return If response status is 200: list of DOIs, one DOI per line; empty for 204.
     * @throws DoiClientException
     */
    public String retrieveAllDoi(String doi)
            throws DoiClientException {

        return retrieve(createUrl("doi"), ALL_DOI_ENTITY);
    }

    /**
     * This request stores new version of metadata.
     * The request body must contain valid XML.
     *
     *
     * @param doi eg. 10.5072/JQX3-61AT
     * @param doiMetadata XML in DataCite format as String
     * @throws DoiClientException
     */
    public void createDoiMetadata(String doi, String doiMetadata)
            throws DoiClientException {

//        create(createUrl("metadata/" + doi),
        create(createUrl("metadata"),
            doiMetadata,
            "application/xml", DOI_METADATA_ENTITY);
    }

    /**
     * This request returns the most recent version of metadata associated with a given DOI.
     *
     * @param doi
     * @return
     * @throws DoiClientException
     */
    public String retrieveDoiMetadata(String doi)
            throws DoiClientException {
        return retrieve(createUrl("metadata/" + doi),
            "DOI metadata");
    }


    /**
     * This request marks a dataset as 'inactive'.
     * To activate it again, POST new metadata or set the isActive-flag in the user interface.
     *
     * @param doi
     * @throws DoiClientException
     */
    public void deleteDoiMetadata(String doi)
            throws DoiClientException {

        ClientHttpResponse httpResponse = null;
        HttpDelete deleteMethod = null;

        try {
            Log.debug(LOGGER_NAME, "   -- URL: " + this.serverUrl + "/metadata");

            deleteMethod = new HttpDelete(createUrl("metadata/" + doi));

            httpResponse = requestFactory.execute(
                deleteMethod,
                new UsernamePasswordCredentials(username, password), AuthScope.ANY);
            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            // Ignore NOT FOUND (trying to delete a non existing doi metadata)
            if ((status != HttpStatus.SC_NOT_FOUND) && (status != HttpStatus.SC_OK)) {
                Log.info(LOGGER_NAME, "Delete DOI metadata end -- Error: " + httpResponse.getStatusText());

                throw new DoiClientException( httpResponse.getStatusText() );
            } else {
                Log.info(LOGGER_NAME, "DeleteDOI metadata end");
            }

        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage());
            throw new DoiClientException(ex.getMessage());

        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }
    }

    public void deleteDoi(String doi)
        throws DoiClientException {

        ClientHttpResponse httpResponse = null;
        HttpDelete deleteMethod = null;

        try {
            Log.debug(LOGGER_NAME, "   -- URL: " + this.serverUrl + "/metadata");

            deleteMethod = new HttpDelete(createUrl("doi/" + doi));

            httpResponse = requestFactory.execute(
                deleteMethod,
                new UsernamePasswordCredentials(username, password), AuthScope.ANY);
            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            // Ignore NOT FOUND (trying to delete a non existing doi metadata)
            if ((status != HttpStatus.SC_NOT_FOUND) && (status != HttpStatus.SC_OK)) {
                Log.info(LOGGER_NAME, "Delete DOI end -- Error: " + httpResponse.getStatusText());

                throw new DoiClientException( httpResponse.getStatusText() );
            } else {
                Log.info(LOGGER_NAME, "DeleteDOI end");
            }

        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage());
            throw new DoiClientException(ex.getMessage());

        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }
    }

    /**
     * See https://support.datacite.org/docs/mds-api-guide#section-register-metadata
     */
    private void create(String url, String body, String contentType, String entity)
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

            if (status != HttpStatus.SC_CREATED) {
                String message = String.format(
                    "Failed to create '%s' with '%s'. Status is %d. Error is %s.",
                    url, body, status,
                    httpResponse.getStatusText());

                Log.info(LOGGER_NAME, message);
                throw new DoiClientException(message);
            } else {
                Log.info(LOGGER_NAME, String.format(
                    "DOI metadata created at %s.", url));
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

    private String retrieve(String url, String entity)
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
     * Builds service url with server url.
     *
     * @param service
     * @return
     */
    private String createUrl(String service) {
        return this.serverUrl +
            (this.serverUrl.endsWith("/") ? "" : "/") +
            service;
    }
}
