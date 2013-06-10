package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.Setting;

public interface SettingRepositoryCustom {
    List<Setting> findByPath(String pathToSetting);
    Setting findOneByPath(String pathToSetting);
}
