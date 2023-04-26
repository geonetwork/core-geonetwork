package org.fao.geonet.doi.client;

public interface IDoiClient {
    void createDoi(String doi, String url)
        throws DoiClientException;

    String retrieveDoi(String doi)
        throws DoiClientException;

    String retrieveAllDoi(String doi)
        throws DoiClientException;

    void createDoiMetadata(String doi, String doiMetadata)
        throws DoiClientException;

    String retrieveDoiMetadata(String doi)
        throws DoiClientException;

    void deleteDoiMetadata(String doi)
        throws DoiClientException;

    void deleteDoi(String doi)
        throws DoiClientException;

    String createUrl(String service);

    String createPublicUrl(String doi);
}
