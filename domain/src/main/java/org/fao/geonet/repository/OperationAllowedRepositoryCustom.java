package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.OperationAllowed;
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
     * @return
     */
    public List<OperationAllowed> findByMetadataId(String metadataId);
}
