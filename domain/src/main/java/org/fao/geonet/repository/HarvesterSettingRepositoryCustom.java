package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.Setting;

/**
 * Custom (Non spring-data) Query methods for {@link Setting} entities.
 * 
 * @author Jesse
 *
 */
public interface HarvesterSettingRepositoryCustom extends AbstractSettingRepoCustom<HarvesterSetting> {
}
