package org.fao.geonet.repository;

import com.google.common.base.Optional;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

/**
 * Custom (Non spring-data) Query methods for {@link OperationAllowed} entities.
 *
 * @author Jesse
 */
public interface OperationAllowedRepositoryCustom {
    /**
     * Converts metadataId to integer and performs a search.
     *
     * @param metadataId id of metadata
     * @return the OperationsAllowed entities with the given metadataId
     */
    @Nonnull
    List<OperationAllowed> findByMetadataId(@Nonnull String metadataId);

    /**
     * Find all the metadata owned by the user with the given userId and that satisfy the given specification.
     *
     * @param userId        the id of the owning user
     * @param specification an optional specification further restricting the OperationAllowed to load.
     * @return all the metadata owned by the user with the given userId and that satisfy the given specification.
     */
    @Nonnull
    List<OperationAllowed> findAllWithOwner(@Nonnull int userId, @Nonnull Optional<Specification<OperationAllowed>> specification);

    /**
     * Find all the ids identified by the idAttribute of the values returned by the spec.
     *
     * @param spec        the specification for selecting which elements to load
     * @param idAttribute the attribute of the OperationAllowedId to return in the list
     * @return the list of ids returned.
     */
    @Nonnull
    List<Integer> findAllIds(@Nonnull Specification<OperationAllowed> spec, @Nonnull SingularAttribute<OperationAllowedId,
            Integer> idAttribute);

    /**
     * Delete all OperationsAllowed entities with the give metadata and group ids.
     *
     * @param metadataId the metadata id
     * @param groupId    the group id
     */
    @Nonnegative
    int deleteAllByMetadataIdExceptGroupId(int metadataId, int groupId);

    /**
     * Delete all the {@link OperationAllowed} with the given id in the id component selected by the idAttribute.
     *
     * @param idAttribute The attribute of {@link OperationAllowedId} to match against the provided id.
     * @param id          the id to use as the key for selecting which entities to delete.
     * @return the number of entities deleted.
     */
    @Nonnegative
    int deleteAllByIdAttribute(@Nonnull SingularAttribute<OperationAllowedId, Integer> idAttribute, int id);
}
