package org.fao.geonet.repository.statistic;

import com.google.common.base.Optional;
import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class responsible for querying the data layer in order to calculate various statistics related to the metadata.
 * <p/>
 * StatusValue: Jesse
 * Date: 9/21/13
 * Time: 4:23 PM
 */
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
     * Calculate a value (determined by spec) of each metadata in a category and return the results as a map from a category to
     * the statistic value. If a category is not in the map then there are no metadata in that category (and thus the statistic is 0
     * for that category).
     *
     * @param spec the spec that calculates the value associated with each grouping
     * @return A mapping from a MetadataCategory to the statistic of all metadata in that category. If a category is not in
     *         the map then there are no metadata in that category (and thus the statistic is 0 for that category).
     */
    public Map<MetadataCategory, Integer> getMetadataCategoryToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<MetadataCategory> metadataCategoryRoot = cbQuery.from(MetadataCategory.class);

        final Expression<Set<MetadataCategory>> metadataCategoriesPath = metadataRoot.get(Metadata_.categories);
        cbQuery.where(cb.isMember(metadataCategoryRoot, metadataCategoriesPath));

        final Path<Integer> metadataCategoryIdPath = metadataCategoryRoot.get(MetadataCategory_.id);
        cbQuery.groupBy(metadataCategoryIdPath);

        cbQuery.select(cb.tuple(metadataCategoryRoot, spec.getSelection(cb, metadataRoot)));

        Map<MetadataCategory, Integer> results = new HashMap<MetadataCategory, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, MetadataCategory.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Calculate a value (determined by spec) of each metadata with the given group owner and return the results as a map from a group to
     * the statistic value. If a group is not in the map then there are no metadata in that group (and thus the statistic is 0
     * for that group).
     *
     * @param spec the spec that calculates the value associated with each grouping
     * @return A mapping from a Group to the statistic of all metadata in that category. If a group is not in
     *         the map then there are no metadata in that group (and thus the statistic is 0 for that group).
     */
    public Map<Group, Integer> getGroupOwnerToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<Group> groupRoot = cbQuery.from(Group.class);

        final Path<Integer> groupOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);
        final Path<Integer> groupIdPath = groupRoot.get(Group_.id);
        cbQuery.where(cb.equal(groupOwnerPath, groupIdPath));

        cbQuery.groupBy(groupRoot);

        cbQuery.select(cb.tuple(groupRoot, spec.getSelection(cb, metadataRoot)));

        Map<Group, Integer> results = new HashMap<Group, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, Group.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Calculate a value (determined by spec) of each metadata with the given owner and return the results as a map from a StatusValue to
     * the statistic value. If a StatusValue is not in the map then there are no metadata in that StatusValue (and thus the statistic is 0
     * for that StatusValue).
     *
     * @param spec the spec that calculates the value associated with each StatusValue
     * @return A mapping from a StatusValue to the statistic of all metadata in that category. If a StatusValue is not in
     *         the map then there are no metadata in that StatusValue (and thus the statistic is 0 for that StatusValue).
     */
    public Map<User, Integer> getOwnerToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<User> userRoot = cbQuery.from(User.class);

        final Path<Integer> ownerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.owner);
        final Path<Integer> userIdPath = userRoot.get(User_.id);
        cbQuery.where(cb.equal(ownerPath, userIdPath));

        cbQuery.groupBy(userRoot);

        cbQuery.select(cb.tuple(userRoot, spec.getSelection(cb, metadataRoot)));

        Map<User, Integer> results = new HashMap<User, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, User.class), tuple.get(1, Long.class).intValue());
        }
        return results;

    }

    /**
     * Calculate a value (determined by spec) of each metadata with the given owner and return the results as a map from a source to
     * the statistic value. If a source is not in the map then there are no metadata in that source (and thus the statistic is 0
     * for that source).
     *
     * @param spec the spec that calculates the value associated with each source
     * @return A mapping from a Source to the statistic of all metadata in that category. If a source is not in
     *         the map then there are no metadata in that source (and thus the statistic is 0 for that source).
     */
    public Map<Source, Integer> getSourceToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<Source> sourceRoot = cbQuery.from(Source.class);

        final Path<String> sourcePathInMetadata = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.sourceId);
        final Path<String> sourceUuidPath = sourceRoot.get(Source_.uuid);

        cbQuery.select(cb.tuple(sourceRoot, spec.getSelection(cb, metadataRoot)))
                .where(cb.equal(sourcePathInMetadata, sourceUuidPath))
                .groupBy(sourceUuidPath);

        Map<Source, Integer> results = new HashMap<Source, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, Source.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Calculate a value (determined by spec) of each metadata with the given owner and return the results as a map from a schema to
     * the statistic value. If a schema is not in the map then there are no metadata in that schema (and thus the statistic is 0
     * for that schema).
     *
     * @param spec the spec that calculates the value associated with each schema
     * @return A mapping from a Schema to the statistic of all metadata in that category. If a schema is not in
     *         the map then there are no metadata in that schema (and thus the statistic is 0 for that schema).
     */
    public Map<String, Integer> getSchemaToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);

        final Path<String> schemaTypePath = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.schemaId);

        cbQuery.select(cb.tuple(schemaTypePath, spec.getSelection(cb, metadataRoot)))
                .groupBy(schemaTypePath);

        Map<String, Integer> results = new HashMap<String, Integer>();

        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, String.class), tuple.get(1, Long.class).intValue());
        }
        return results;
    }

    /**
     * Calculate a value (determined by spec) of each metadata with the given owner and return the results as a map from a metadatatype to
     * the statistic value. If a metadatatype is not in the map then there are no metadata in that metadatatype (and thus the statistic
     * is 0
     * for that metadatatype).
     *
     * @param spec the spec that calculates the value associated with each metadatatype
     * @return A mapping from a Metadatatype to the statistic of all metadata in that category. If a metadatatype is not in
     *         the map then there are no metadata in that metadatatype (and thus the statistic is 0 for that metadatatype).
     */
    public Map<MetadataType, Integer> getMetadataTypeToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);

        final Path<Character> typePath = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);

        cbQuery.select(cb.tuple(typePath, spec.getSelection(cb, metadataRoot))).groupBy(typePath);

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
     * Calculate a value (determined by spec) of each metadata with the given owner and return the results as a map from a IsHarvested
     * value to
     * the statistic value. If a IsHarvested value is not in the map then there are no metadata in that IsHarvested value (and thus the
     * statistic is 0
     * for that IsHarvested value).
     *
     * @param spec the spec that calculates the value associated with each IsHarvested value
     * @return A mapping from a IsHarvested value to the statistic of all metadata in that category. If a IsHarvested value is not in
     *         the map then there are no metadata in that IsHarvested value (and thus the statistic is 0 for that IsHarvested value).
     */
    public Map<Boolean, Integer> getIsHarvestedToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);

        final Path<Character> isHarvestedPath = metadataRoot.get(Metadata_.harvestInfo).get(MetadataHarvestInfo_.harvested_JPAWorkaround);

        cbQuery.select(cb.tuple(isHarvestedPath, spec.getSelection(cb, metadataRoot)))
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
     * Calculate a value (determined by spec) of each metadata with the given owner and return the results as a map from a StatusValue to
     * the statistic value. If a StatusValue is not in the map then there are no metadata in that StatusValue (and thus the statistic is 0
     * for that StatusValue).
     *
     * @param spec the spec that calculates the value associated with each StatusValue
     * @return A mapping from a StatusValue to the statistic of all metadata in that category. If a StatusValue is not in
     *         the map then there are no metadata in that StatusValue (and thus the statistic is 0 for that StatusValue).
     */
    public Map<StatusValue, Integer> getStatusValueToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<MetadataStatus> metadataStatusRoot = cbQuery.from(MetadataStatus.class);
        final Root<StatusValue> statusValueRoot = cbQuery.from(StatusValue.class);

        final Path<StatusValue> statusValuePath = metadataStatusRoot.get(MetadataStatus_.statusValue);

        final Predicate equalMetadataId = cb.equal(
                metadataRoot.get(Metadata_.id),
                metadataStatusRoot.get(MetadataStatus_.id)
                        .get(MetadataStatusId_.metadataId));
        final Predicate equalStatusValue = cb.equal(statusValuePath, statusValueRoot);
        cbQuery.select(cb.tuple(statusValueRoot, spec.getSelection(cb, metadataRoot)))
                .where(cb.and(equalStatusValue, equalMetadataId))
                .groupBy(statusValueRoot);

        Map<StatusValue, Integer> results = new HashMap<StatusValue, Integer>();
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, StatusValue.class), tuple.get(1, Long.class).intValue());
        }

        return results;
    }

    /**
     * Calculate a value (determined by spec) of each metadata with the given owner and return the results as a map from a
     * MetadataValidationStatus to
     * the statistic value. If a MetadataValidationStatus is not in the map then there are no metadata in that MetadataValidationStatus
     * (and thus the statistic is 0
     * for that MetadataValidationStatus).
     *
     * @param spec the spec that calculates the value associated with each MetadataValidationStatus
     * @return A mapping from a MetadataValidationStatus to the statistic of all metadata in that category. If a
     * MetadataValidationStatus is not in
     *         the map then there are no metadata in that MetadataValidationStatus (and thus the statistic is 0 for that
     *         MetadataValidationStatus).
     */
    public Map<MetadataValidationStatus, Integer> getMetadataValidationStatusToStatMap(MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<MetadataValidation> metadataValidationRoot = cbQuery.from(MetadataValidation.class);

        final Path<MetadataValidationStatus> statusPath = metadataValidationRoot.get(MetadataValidation_.status);
        final Expression<Integer> metadataIdPath = metadataRoot.get(Metadata_.id);
        Expression<Integer> metadataValidationMetadataIdPath = metadataValidationRoot.get(MetadataValidation_.id).get
                (MetadataValidationId_.metadataId);


        final Long notValidatedCount = calculateNotValidatedStat(spec, cb);

        cbQuery.select(cb.tuple(statusPath, spec.getSelection(cb, metadataRoot)))
                .where(cb.equal(metadataIdPath, metadataValidationMetadataIdPath))
                .groupBy(statusPath);

        Map<MetadataValidationStatus, Integer> results = new HashMap<MetadataValidationStatus, Integer>();

        results.put(MetadataValidationStatus.NEVER_CALCULATED, notValidatedCount.intValue());
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            results.put(tuple.get(0, MetadataValidationStatus.class), tuple.get(1, Long.class).intValue());
        }

        return results;
    }

    private Long calculateNotValidatedStat(MetadataStatisticSpec spec, CriteriaBuilder cb) {
        final CriteriaQuery<Number> notValidatedQuery = cb.createQuery(Number.class);
        final Root<Metadata> metadataRoot = notValidatedQuery.from(Metadata.class);


        final Subquery<Integer> selectMetadataValidationMetadataIds = notValidatedQuery.subquery(Integer.class);
        final Root<MetadataValidation> metadataValidationRoot = selectMetadataValidationMetadataIds.from(MetadataValidation.class);
        final Path<Integer> metadataValidationMetadataIdPath = metadataValidationRoot.get(MetadataValidation_.id).get
                (MetadataValidationId_.metadataId);
        selectMetadataValidationMetadataIds.select(metadataValidationMetadataIdPath);


        final Path<Integer> metadataIdPath = metadataRoot.get(Metadata_.id);
        notValidatedQuery.select(spec.getSelection(cb, metadataRoot)).where(cb.not(metadataIdPath.in
                (selectMetadataValidationMetadataIds)));

        final Number singleResult = _entityManager.createQuery(notValidatedQuery).getSingleResult();
        if (singleResult == null) {
            return 0L;
        }
        return singleResult.longValue();
    }

    /**
     * Calculate the statistic grouped by validation type and each MetadataValidationStatus.
     * <p>
     * The metadata that have not been validated are also in the map as Pair.read(null, MetadataValidationStatus.NEVER_CALCULATED)
     * </p>
     *
     * @param spec the spec that calculates the value associated with each grouping
     * @return a mapping from Pair&lt;ValidationType, MetadataValidationStatus> to the statistical value
     */
    public Map<Pair<String, MetadataValidationStatus>, Integer> getMetadataValidationTypeAndStatusToStatMap
    (MetadataStatisticSpec spec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<MetadataValidation> metadataValidationRoot = cbQuery.from(MetadataValidation.class);

        final Path<String> statusTypePath = metadataValidationRoot.get(MetadataValidation_.id).get(MetadataValidationId_.validationType);
        final Path<MetadataValidationStatus> statusPath = metadataValidationRoot.get(MetadataValidation_.status);
        final Expression<Integer> metadataIdPath = metadataRoot.get(Metadata_.id);
        Expression<Integer> metadataValidationMetadataIdPath = metadataValidationRoot.get(MetadataValidation_.id).get
                (MetadataValidationId_.metadataId);


        final Long notValidatedCount = calculateNotValidatedStat(spec, cb);

        cbQuery.select(cb.tuple(statusTypePath, statusPath, spec.getSelection(cb, metadataRoot)))
                .where(cb.equal(metadataIdPath, metadataValidationMetadataIdPath))
                .groupBy(statusTypePath, statusPath);

        Map<Pair<String, MetadataValidationStatus>, Integer> results = new HashMap<Pair<String, MetadataValidationStatus>, Integer>();

        results.put(Pair.<String, MetadataValidationStatus>read(null, MetadataValidationStatus.NEVER_CALCULATED),
                notValidatedCount.intValue());
        for (Tuple tuple : _entityManager.createQuery(cbQuery).getResultList()) {
            final String metadataValidationType = tuple.get(0, String.class);
            final MetadataValidationStatus metadataValidationStatus = tuple.get(1, MetadataValidationStatus.class);
            final int count = tuple.get(2, Long.class).intValue();
            results.put(Pair.read(metadataValidationType, metadataValidationStatus), count);
        }

        return results;
    }

    /**
     * Calculate the statistic of all the selected metadata.
     *
     * @param spec         the spec that calculates the value associated with each grouping
     * @param optionalSpec an optional specification for selecting which metadata to sum.
     * @return the total of the statistic of all the selected metadata.
     */
    public int getTotalStat(MetadataStatisticSpec spec, Optional<Specification<Metadata>> optionalSpec) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Number> query = cb.createQuery(Number.class);
        final Root<Metadata> root = query.from(Metadata.class);

        if (optionalSpec.isPresent()) {
            query.where(optionalSpec.get().toPredicate(root, query, cb));
        }

        query.select(spec.getSelection(cb, root));
        final Number result = _entityManager.createQuery(query).getSingleResult();
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

    /**
     * Calculate the statistic from the metadata linked to in the selected OperationAllowed.
     *
     * @param metadataStatisticSpec         the statistic to calculate
     * @param operationAllowedSpecification the specification to use to select the metadata.
     * @return the calculated statistic from the metadata linked to in the selected OperationAllowed.
     */
    @Nonnegative
    public int getStatBasedOnOperationAllowed(@Nonnull MetadataStatisticSpec metadataStatisticSpec,
                                              @Nonnull Specification<OperationAllowed> operationAllowedSpecification) {

        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Number> query = cb.createQuery(Number.class);
        final Root<Metadata> metadataRoot = query.from(Metadata.class);

        Subquery<Integer> subquery = query.subquery(Integer.class);
        final Root<OperationAllowed> opAllowedRoot = subquery.from(OperationAllowed.class);
        final Predicate opAllowedPredicate = operationAllowedSpecification.toPredicate(opAllowedRoot, query, cb);
        subquery.where(opAllowedPredicate);
        final Path<Integer> opAllowedMetadataId = opAllowedRoot.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId);
        subquery.select(opAllowedMetadataId);

        query.select(metadataStatisticSpec.getSelection(cb, metadataRoot))
                .where(metadataRoot.get(Metadata_.id).in(subquery));

        Number result = _entityManager.createQuery(query).getSingleResult();
        if (result == null) {
            return 0;
        } else {
            return result.intValue();
        }
    }
}
