package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.*;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Map;

/**
 * Class responsible for querying the data layer in order to calculate various statistics related to the metadata.
 * <p/>
 * User: Jesse
 * Date: 9/21/13
 * Time: 4:23 PM
 */
@Component
public class MetadataStatisticsQueries {

    private final EntityManager _entityManager;


    /**
     * Constructor.
     *
     * @param entityManager an entitymanager to use for performing and creating queries.
     */
    public MetadataStatisticsQueries(EntityManager entityManager) {
        this._entityManager = entityManager;
    }

    /**
     * Calculate the sum of the popularity of each metadata in a category and return the results as a map from a category to
     * the popularity sum.
     *
     * @return A mapping from a MetadataCategory to the sum of the popularity of all metadata in that category.
     */
    public Map<MetadataCategory, Integer> getMetadataCategoryToPopularityMap() {
        return null;
    }

    /**
     * Count the number of metadata in a category and return the results as a map from a category to
     * the metadata count.
     *
     * @return A mapping from a MetadataCategory to the number of metadata in that category.
     */
    public Map<MetadataCategory, Integer> getMetadataCategoryToMetadataCountMap() {
        return null;
    }

    /**
     * Count the number of metadata per group.
     *
     * @return A mapping from group to the number of metadata in that group.
     */
    public Map<Group, Integer> getGroupOwnerToMetadataCountMap() {
        return null;
    }

    /**
     * Count the number of metadata owner per user.
     *
     * @return a mapping from a user to the number of metadata that user owns.  If a user is not in the listing then the user does
     *         not own any metadata.
     */
    public Map<User, Integer> getOwnerToMetadataCountMap() {
        return null;
    }

    /**
     * Count the number metadata per metadata source.
     *
     * @return a mapping from a source to the number of metadata from that source.  If a source is not in the mapping then the source
     *         does not own any metadata.
     */
    public Map<Source, Integer> getSourceToMetadataCountMap() {
        return null;
    }

    /**
     * Count the number of metadata per schema type.
     *
     * @return a mapping from schemaId to number of metadata in that schema.
     */
    public Map<String, Integer> getSchemaToMetadataCountMap() {
        return null;
    }

    /**
     * Count the number of metadata per MetadataType (template, metadata, sub-template).
     *
     * @return a mapping from MetadataType to number of metadata in that type.
     */
    public Map<MetadataType, Integer> getMetadataTypeToMetadataCountMap() {
        return null;
    }

    /**
     * Calculate the number of harvested vs non-harvested metadata.
     *
     * @param typesToInclude the metadata types to include in the calculation.
     * @return a mapping from isHarvested (true is harvested) to the number of metadata.
     */
    public Map<Boolean, Integer> getIsHarvestedToMetadataCountMap(MetadataType... typesToInclude) {
        return null;
    }

    /**
     * Calculate the number of metadata for the given MetadataStatus type.
     *
     * @return a mapping from MetadataStatus to the number of metadata that have that status.
     */
    public Map<MetadataStatus, Integer> getMetadataStatusToMetadataCountMap() {
        return null;
    }

    /**
     * Calculate the number of metadata that have a given MetadataValidationStatus.
     *
     * @return a mapping from MetadataValidationStatus to the number of metadata with that validation type.
     */
    public Map<MetadataValidationStatus, Integer> getMetadataValidationStatusToMetadataCountMap() {
        return null;
    }

    /**
     * Calculate the number of metadata grouped by validation type and each MetadataValidationStatus.
     *
     * @return a mapping from Pair&lt;ValidationType, MetadataValidationStatus>
     */
    public Map<Pair<String, MetadataValidationStatus>, Integer> getMetadataValidationTypeToMetadataCountMap() {
        return null;
    }


}
