package org.fao.geonet.repository;

import org.fao.geonet.domain.CswServerCapabilitiesInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link CswServerCapabilitiesInfo} entities.
 * 
 * @author Jesse
 */
public interface CswServerCapabilitiesInfoRepository extends JpaRepository<CswServerCapabilitiesInfo, Integer>, JpaSpecificationExecutor<CswServerCapabilitiesInfo> {
}
