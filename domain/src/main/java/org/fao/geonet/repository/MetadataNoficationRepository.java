package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link MetadataNotification} entities.
 * 
 * @author Jesse
 */
public interface MetadataNoficationRepository extends JpaRepository<MetadataNotification, Integer>, JpaSpecificationExecutor<MetadataNotification> {
}
