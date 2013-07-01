package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link MetadataNotifier} entities.
 * 
 * @author Jesse
 */
public interface MetadataNotifierRepository extends JpaRepository<MetadataNotifier, Integer>, JpaSpecificationExecutor<MetadataNotifier> {
}
