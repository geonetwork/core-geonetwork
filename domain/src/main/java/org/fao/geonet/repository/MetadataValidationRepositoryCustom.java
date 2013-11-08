package org.fao.geonet.repository;

/**
 * Custom repository methods for the MetadataValidationRepository
 * User: Jesse
 * Date: 9/5/13
 * Time: 10:17 PM
 */
public interface MetadataValidationRepositoryCustom {
    /**
     * Delete all the entities that are related to the indicated metadata.
     *
     * @param metadataId the id of the metadata.
     * @return the number of rows deleted
     */
    int deleteAllById_MetadataId(int metadataId);
}
