package org.fao.geonet.kernel.repository;

import java.util.List;

import org.fao.geonet.kernel.domain.OperationAllowed;

public interface OperationAllowedRepositoryCustom {
    /**
     * Converts metadataId to integer and performs a search.
     *
     * @param metadataId id of metadata
     * @return
     */
    public List<OperationAllowed> findByMetadataId(String metadataId);
}
