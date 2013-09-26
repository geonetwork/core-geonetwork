package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.*;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
     * @return A mapping from a MetadataCategory to the sum of the popularity of all metadata in that category. If a category is not in
     *         the map then there are no metadata in that category (and thus the popularity is 0 for that category).
     */
    public Map<MetadataCategory, Integer> getMetadataCategoryToPopularityMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<MetadataCategory> metadataCategoryRoot = cbQuery.from(MetadataCategory.class);

        final Expression<Set<MetadataCategory>> metadataCategoriesPath = metadataRoot.get(Metadata_.categories);
        cbQuery.where(cb.isMember(metadataCategoryRoot, metadataCategoriesPath));

        final Path<Integer> metadataCategoryIdPath = metadataCategoryRoot.get(MetadataCategory_.id);
        cbQuery.groupBy(metadataCategoryIdPath);

        Path<Integer> popularityPath = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.popularity);
        cbQuery.select(cb.tuple(metadataCategoryRoot, cb.sum(popularityPath)));

        Map<MetadataCategory, Integer> results = new HashMap<MetadataCategory, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, MetadataCategory.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Count the number of metadata in a category and return the results as a map from a category to
     * the metadata count.
     *
     * @return A mapping from a MetadataCategory to the number of metadata in that category.  If a category is not in
     *         the map then there are no metadata in that category.
     */
    public Map<MetadataCategory, Integer> getMetadataCategoryToMetadataCountMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<MetadataCategory> metadataCategoryRoot = cbQuery.from(MetadataCategory.class);

        final Expression<Set<MetadataCategory>> metadataCategoriesPath = metadataRoot.get(Metadata_.categories);
        cbQuery.where(cb.isMember(metadataCategoryRoot, metadataCategoriesPath));

        final Path<Integer> metadataCategoryIdPath = metadataCategoryRoot.get(MetadataCategory_.id);
        cbQuery.groupBy(metadataCategoryIdPath);

        cbQuery.select(cb.tuple(metadataCategoryRoot, cb.count(metadataRoot)));

        Map<MetadataCategory, Integer> results = new HashMap<MetadataCategory, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, MetadataCategory.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Count the number of metadata per group.
     *
     * @return A mapping from group to the number of metadata in that group.
     */
    public Map<Group, Integer> getGroupOwnerToMetadataCountMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<Group> groupRoot = cbQuery.from(Group.class);

        final Path<Integer> groupOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);
        final Path<Integer> groupIdPath = groupRoot.get(Group_.id);
        cbQuery.where(cb.equal(groupOwnerPath, groupIdPath));

        cbQuery.groupBy(groupOwnerPath);

        cbQuery.select(cb.tuple(groupRoot, cb.count(metadataRoot)));

        Map<Group, Integer> results = new HashMap<Group, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, Group.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Count the number of metadata owner per user.
     *
     * @return a mapping from a user to the number of metadata that user owns.  If a user is not in the listing then the user does
     *         not own any metadata.
     */
    public Map<User, Integer> getOwnerToMetadataCountMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<User> userRoot = cbQuery.from(User.class);

        final Path<Integer> ownerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.owner);
        final Path<Integer> userIdPath = userRoot.get(User_.id);
        cbQuery.where(cb.equal(ownerPath, userIdPath));

        cbQuery.groupBy(ownerPath);

        cbQuery.select(cb.tuple(userRoot, cb.count(metadataRoot)));

        Map<User, Integer> results = new HashMap<User, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, User.class), tuple.get(1, Long.class).intValue());
        }
        return results;

    }

    /**
     * Count the number metadata per metadata source.
     *
     * @return a mapping from a source to the number of metadata from that source.  If a source is not in the mapping then the source
     *         does not own any metadata.
     */
    public Map<Source, Integer> getSourceToMetadataCountMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<Source> sourceRoot = cbQuery.from(Source.class);

        final Path<String> sourcePathInMetadata = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.sourceId);
        final Path<String> sourceUuidPath = sourceRoot.get(Source_.uuid);

        cbQuery.select(cb.tuple(sourceRoot, cb.count(metadataRoot)))
                .where(cb.equal(sourcePathInMetadata, sourceUuidPath))
                .groupBy(sourceUuidPath);

        Map<Source, Integer> results = new HashMap<Source, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, Source.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Count the number of metadata per schema type.
     *
     * @return a mapping from schemaId to number of metadata in that schema.
     */
    public Map<String, Integer> getSchemaToMetadataCountMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);

        final Path<String> schemaTypePath = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.schemaId);

        cbQuery.select(cb.tuple(schemaTypePath, cb.count(metadataRoot)))
                .groupBy(schemaTypePath);

        Map<String, Integer> results = new HashMap<String, Integer>();

        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, String.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Count the number of metadata per MetadataType (template, metadata, sub-template).
     *
     * @return a mapping from MetadataType to number of metadata in that type.
     */
    public Map<MetadataType, Integer> getMetadataTypeToMetadataCountMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);

        final Path<Character> typePath = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);

        cbQuery.select(cb.tuple(typePath, cb.count(metadataRoot))).groupBy(typePath);

        Map<MetadataType, Integer> results = new HashMap<MetadataType, Integer>();

        for (MetadataType type : MetadataType.values()) {
            results.put(type, 0);
        }

        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            MetadataType metadataType = MetadataType.lookup(tuple.get(0, Character.class));
            results.put(metadataType, tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Calculate the number of harvested vs non-harvested metadata.
     *
     * @param typesToInclude the metadata types to include in the calculation.
     * @return a mapping from isHarvested (true is harvested) to the number of metadata.
     */
    public Map<Boolean, Integer> getIsHarvestedToMetadataCountMap(MetadataType... typesToInclude) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);

        final Path<Character> isHarvestedPath = metadataRoot.get(Metadata_.harvestInfo).get(MetadataHarvestInfo_.harvested_JPAWorkaround);

        cbQuery.select(cb.tuple(isHarvestedPath, cb.count(metadataRoot)))
                .groupBy(isHarvestedPath);

        Map<Boolean, Integer> results = new HashMap<Boolean, Integer>();
        results.put(true, 0);
        results.put(false, 0);

        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            final boolean isHarvested = Constants.toBoolean_fromYNChar(tuple.get(0, Character.class));
            results.put(isHarvested, tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Calculate the number of metadata for the given MetadataStatus type.
     *
     * @return a mapping from MetadataStatus to the number of metadata that have that status.
     */
    public Map<StatusValue, Integer> getStatusValueToMetadataCountMap() {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<MetadataStatus> metadataStatusRoot = cbQuery.from(MetadataStatus.class);
        final Path<Integer> metadataStatusMetadataId = metadataStatusRoot.get(MetadataStatus_.id).get(MetadataStatusId_.metadataId);

        cbQuery.select(cb.tuple(metadataStatusRoot, cb.count(metadataStatusMetadataId))).groupBy(metadataStatusRoot);

        Map<StatusValue, Integer> results = new HashMap<StatusValue, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, StatusValue.class), tuple.get(1, Long.class).intValue());
        }

        return results;
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
    public Map<Pair<String, MetadataValidationStatus>, Integer> getMetadataValidationTypeAndStatusToMetadataCountMap() {
        return null;
    }


}
