package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link MetadataNotifier} entities.
 * 
 * @author Jesse
 */
public interface MetadataNotifierRepository extends JpaRepository<MetadataNotifier, Integer>, JpaSpecificationExecutor<MetadataNotifier> {
    /**
     * Find all the enabled/disabled notifiers
     * @param enabled if true then  find the enabled
     */
    List<MetadataNotifier> findAllByEnabled(boolean enabled);
}
