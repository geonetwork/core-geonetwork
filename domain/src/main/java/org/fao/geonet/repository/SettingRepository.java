package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.Setting;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link Setting} entities.
 *
 * @author Jesse
 */
public interface SettingRepository extends GeonetRepository<Setting, String>, JpaSpecificationExecutor<Setting> {

  List<Setting> findByInternal(boolean b);
}
