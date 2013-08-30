package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link MetadataStatus} entities.
 * 
 * @author Jesse
 */
public interface MetadataStatusRepository extends JpaRepository<MetadataStatus, MetadataStatusId> {
}
