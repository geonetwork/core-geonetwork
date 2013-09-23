package org.fao.geonet.repository;

import com.google.common.base.Optional;
import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

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
     * @param spec the specification for selecting which elements to load
     * @param idAttribute the attribute of the OperationAllowedId to return in the list
     *
     * @return the list of ids returned.
     */
    @Nonnull
    List<Integer> findAllIds(@Nonnull Specification<OperationAllowed> spec, @Nonnull SingularAttribute<OperationAllowedId, Integer> idAttribute);
}
