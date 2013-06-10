package org.fao.geonet.kernel.repository;

import java.util.List;

import org.fao.geonet.kernel.domain.Setting;

public interface SettingRepositoryCustom {
    List<Setting> findByPath(String pathToSetting);
    Setting findOneByPath(String pathToSetting);
}
