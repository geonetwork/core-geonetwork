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

import org.apache.commons.httpclient.HttpStatus;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;

/**
 * Doi API client for European Registration Agency of DOI.
 *
 * See https://www.medra.org/.
 */
public class DoiMedraClient extends BaseDoiClient implements IDoiClient {

    public static final String MEDRA_SEARCH_KEY = "medra.org";
    public static final String MEDRA_NOT_SUPPORTED_EXCEPTION_MESSAGE = "Not supported by European Registration Agency of DOI.";

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
        this.create(this.apiUrl,
            doiMetadata,
            "application/xml",
            HttpStatus.SC_OK,
            String.format(
                "DOI metadata registration sent to %s.", this.apiUrl));
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
