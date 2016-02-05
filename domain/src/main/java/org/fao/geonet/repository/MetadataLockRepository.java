package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataLock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataLock}
 * entities.
 *
 * @author Jesse
 */
public interface MetadataLockRepository
        extends GeonetRepository<MetadataLock, Integer>,
        MetadataLockRepositoryCustom, JpaSpecificationExecutor<MetadataLock> {
}
