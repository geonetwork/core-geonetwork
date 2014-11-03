package org.fao.geonet.repository;


/**
 * Repository class for InspireAtomFeed.
 *
 * @author Jose García
 */
public interface InspireAtomFeedRepositoryCustom  {
    /**
     * Retrieve metadata dataset uuid from the dataset id and dataset ns.
     *
     * @param datasetIdCode
     * @param datasetIdNs
     * @return
     */
    public String retrieveDatasetUuidFromIdentifierNs(final String datasetIdCode, final String datasetIdNs);

    /**
     * Retrieve metadata dataset uuid from the dataset id.
     *
     * @param datasetIdCode
     * @return
     */
    public String retrieveDatasetUuidFromIdentifier(final String datasetIdCode);
}
