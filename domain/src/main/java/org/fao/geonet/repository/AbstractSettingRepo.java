package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.AbstractSetting;
import org.fao.geonet.domain.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access object for accessing {@link Setting} entities.
 * 
 * @author Jesse
 */
public interface AbstractSettingRepo<T extends AbstractSetting> extends JpaRepository<T, Integer>, AbstractSettingRepoCustom<T> {

    String ID_PREFIX = "id:";
    String SEPARATOR = "/";

    List<T> findByName(String name);
}
