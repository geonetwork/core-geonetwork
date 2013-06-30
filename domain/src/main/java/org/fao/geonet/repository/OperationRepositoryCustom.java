package org.fao.geonet.repository;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.ReservedOperation;

/**
 * Custom (Non spring-data) Query methods for {@link Operation} entities.
 * 
 * @author Jesse
 *
 */
public interface OperationRepositoryCustom {
    /**
     * Find an Operation by using a reserved operation enum object as the identifier
     * 
     * @param operation operation to find.
     */
    @Nonnull
    Operation findReservedOperation(@Nonnull ReservedOperation operation);
}
