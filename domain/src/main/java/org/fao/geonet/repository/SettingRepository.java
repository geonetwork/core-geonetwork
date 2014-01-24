package org.fao.geonet.repository;

import org.fao.geonet.domain.Setting;

/**
 * Data Access object for accessing {@link Setting} entities.
 * 
 * @author Jesse
 */
public interface SettingRepository extends GeonetRepository<Setting, String>,
    SettingRepositoryCustom {
}