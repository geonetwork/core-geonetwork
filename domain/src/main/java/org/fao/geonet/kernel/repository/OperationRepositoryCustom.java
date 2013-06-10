package org.fao.geonet.kernel.repository;

import javax.annotation.Nonnull;

import org.fao.geonet.kernel.domain.Operation;
import org.fao.geonet.kernel.domain.ReservedOperation;

public interface OperationRepositoryCustom {
    /**
     * Find an Operation by using a reserved operation enum object as the identifier
     * 
     * @param operation operation to find.
     */
    @Nonnull
    Operation findReservedOperation(@Nonnull ReservedOperation operation);
}
