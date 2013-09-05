package org.fao.geonet.repository;

import org.fao.geonet.domain.CswServerCapabilitiesInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link CswServerCapabilitiesInfo} entities.
 * 
 * @author Jesse
 */
public interface CswServerCapabilitiesInfoRepository extends GeonetRepository<CswServerCapabilitiesInfo, Integer>, JpaSpecificationExecutor<CswServerCapabilitiesInfo> {
    /**
     * Find all the Capabilities Info objects for the given field.
     * @param fieldName the name of the field to find.
     */
    List<CswServerCapabilitiesInfo> findAllByField(String fieldName);
}
