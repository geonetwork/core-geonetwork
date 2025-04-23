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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpDelete;
import org.fao.geonet.ApplicationContextHolder;
import static org.fao.geonet.doi.client.DoiManager.DOI_DEFAULT_URL;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.springframework.http.client.ClientHttpResponse;

import java.text.MessageFormat;

import static org.fao.geonet.doi.client.DoiSettings.LOGGER_NAME;

/**
 * Doi API client.
 *
 * See https://mds.datacite.org/static/apidoc.
 *
 * @author Jose García
 */
public class DoiDataciteClient extends BaseDoiClient implements IDoiClient {

    public DoiDataciteClient(String apiUrl, String username, String password, String doiPublicUrl) {
        this.apiUrl = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
        this.doiPublicUrl = StringUtils.isEmpty(doiPublicUrl) ? DOI_DEFAULT_URL : doiPublicUrl.endsWith("/") ? doiPublicUrl : doiPublicUrl + "/";
        this.username = username;
        this.password = password;

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
    @Override
    public void createDoi(String doi, String url)
            throws DoiClientException {

        String requestBody = MessageFormat.format(
                "doi={0}\nurl={1}",
                doi, url);

        String apiurl = createUrl("doi");
        this.create(apiurl,
            requestBody,
            "text/plain;charset=UTF-8",
            HttpStatus.SC_CREATED,
            String.format(
                "DOI metadata created at %s.", apiurl));
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
    @Override
    public String retrieveDoi(String doi)
            throws DoiClientException {

        return retrieve(createUrl("doi/" + doi));
    }

    /**
     * This request returns a list of all DOIs for the requesting datacentre.
     * There is no guaranteed order.
     *
     * @param doi
     * @return If response status is 200: list of DOIs, one DOI per line; empty for 204.
     * @throws DoiClientException
     */
    @Override
    public String retrieveAllDoi(String doi)
            throws DoiClientException {

        return retrieve(createUrl("doi"));
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
    @Override
    public void createDoiMetadata(String doi, String doiMetadata)
            throws DoiClientException {

        String apiurl = createUrl("metadata");
        this.create(apiurl,
            doiMetadata,
            "application/xml",
            HttpStatus.SC_CREATED,
            String.format(
                "DOI metadata created at %s.", apiurl));
    }

    /**
     * This request returns the most recent version of metadata associated with a given DOI.
     *
     * @param doi
     * @return
     * @throws DoiClientException
     */
    @Override
    public String retrieveDoiMetadata(String doi)
            throws DoiClientException {
        return retrieve(createUrl("metadata/" + doi));
    }


    /**
     * This request marks a dataset as 'inactive'.
     * To activate it again, POST new metadata or set the isActive-flag in the user interface.
     *
     * @param doi
     * @throws DoiClientException
     */
    @Override
    public void deleteDoiMetadata(String doi)
            throws DoiClientException {

        ClientHttpResponse httpResponse = null;
        HttpDelete deleteMethod = null;

        try {
            Log.debug(LOGGER_NAME, "   -- URL: " + this.apiUrl + "/metadata");

            deleteMethod = new HttpDelete(createUrl("metadata/" + doi));

            httpResponse = executeRequest(deleteMethod);

            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            // Ignore NOT FOUND (trying to delete a non existing doi metadata)
            if ((status != HttpStatus.SC_NOT_FOUND) && (status != HttpStatus.SC_OK)) {
                Log.info(LOGGER_NAME, "Delete DOI metadata end -- Error: " + httpResponse.getStatusText());

                String message = httpResponse.getStatusText();

                throw new DoiClientException(String.format(
                    "Error deleting DOI: %s",
                    message))
                    .withMessageKey("exception.doi.serverErrorDelete")
                    .withDescriptionKey("exception.doi.serverErrorDelete.description", new String[]{message});
            } else {
                Log.info(LOGGER_NAME, "DeleteDOI metadata end");
            }

        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage(), ex);
            throw new DoiClientException(String.format(
                "Error deleting DOI: %s",
                ex.getMessage()))
                .withMessageKey("exception.doi.serverErrorDelete")
                .withDescriptionKey("exception.doi.serverErrorDelete.description", new String[]{ex.getMessage()});

        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }
    }

    @Override
    public void deleteDoi(String doi)
        throws DoiClientException {

        ClientHttpResponse httpResponse = null;
        HttpDelete deleteMethod = null;

        try {
            Log.debug(LOGGER_NAME, "   -- URL: " + this.apiUrl + "/metadata");

            deleteMethod = new HttpDelete(createUrl("doi/" + doi));

            httpResponse = executeRequest(deleteMethod);

            int status = httpResponse.getRawStatusCode();

            Log.debug(LOGGER_NAME, "   -- Request status code: " + status);

            // Ignore NOT FOUND (trying to delete a non existing doi metadata)
            if ((status != HttpStatus.SC_NOT_FOUND) && (status != HttpStatus.SC_OK)) {
                Log.info(LOGGER_NAME, "Delete DOI end -- Error: " + httpResponse.getStatusText());

                String message = httpResponse.getStatusText();

                throw new DoiClientException(String.format(
                    "Error deleting DOI: %s",
                    message))
                    .withMessageKey("exception.doi.serverErrorDelete")
                    .withDescriptionKey("exception.doi.serverErrorDelete.description", new String[]{message});
            } else {
                Log.info(LOGGER_NAME, "DeleteDOI end");
            }

        } catch (Exception ex) {
            Log.error(LOGGER_NAME, "   -- Error (exception): " + ex.getMessage(), ex);

            throw new DoiClientException(String.format(
                "Error deleting DOI: %s",
                ex.getMessage()))
                .withMessageKey("exception.doi.serverErrorDelete")
                .withDescriptionKey("exception.doi.serverErrorDelete.description", new String[]{ex.getMessage()});

        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
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
