package org.fao.geonet.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access for the {@link Operation} entities
 * 
 * @author Jesse
 */
public interface OperationRepository extends GeonetRepository<Operation, Integer>, OperationRepositoryCustom {
    /**
     * Look up an operation using the name of the operation
     * 
     * @param name the name of the operation
     * @return null or the operation. An exception is thrown if more than one operation is found in database
     */
    @Nullable
    Operation findByName(@Nonnull String name);
}
