package org.fao.geonet.repository;

import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.ReservedOperation;

import javax.annotation.Nonnull;

/**
 * Custom (Non spring-data) Query methods for {@link Operation} entities.
 *
 * @author Jesse
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
