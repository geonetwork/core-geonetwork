package org.fao.geonet.repository;

/**
 * Custom crafted methods for the MetadataRatingByIpRepository.
 * <p/>
 * User: jeichar
 * Date: 9/5/13
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MetadataRatingByIpRepositoryCustom {
    /**
     * Calculate the average of all the ratings for the given metadata.
     * <p>
     * The method will take the sum of all ratings for the metadata and divide by
     * the number of records (the average value)
     * </p>
     *
     * @param metadataId the metadata id.
     * @return the sum of all the rating.
     */
    int averageRating(int metadataId);

    /**
     * Delete all the entities that are related to the indicated metadata.
     *
     * @param metadataId the id of the metadata.
     * @return the number of rows deleted
     */
    int deleteAllById_MetadataId(int metadataId);
}
