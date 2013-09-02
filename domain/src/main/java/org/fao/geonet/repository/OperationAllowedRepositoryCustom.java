package org.fao.geonet.repository;

import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Profile;
import org.springframework.data.jpa.domain.Specification;

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
    List<OperationAllowed> findByMetadataId(String metadataId);

    /**
     * Find all the metadata owned by the user with the given userId and that satisfy the given specification.
     *
     * @param userId the id of the owning user
     * @param specification an optional specification further restricting the OperationAllowed to load.
     *
     * @return all the metadata owned by the user with the given userId and that satisfy the given specification.
     */
    List<OperationAllowed> findAllWithOwner(int userId, Optional<Specification<OperationAllowed>> specification);
}
