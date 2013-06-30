package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.Setting;

/**
 * Custom (Non spring-data) Query methods for {@link Setting} entities.
 * 
 * @author Jesse
 *
 */
public interface SettingRepositoryCustom {
    List<Setting> findByPath(String pathToSetting);
    Setting findOneByPath(String pathToSetting);
}
