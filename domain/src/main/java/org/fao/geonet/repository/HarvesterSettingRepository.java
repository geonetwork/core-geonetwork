package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.HarvesterSetting;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access object for accessing {@link HarvesterSetting} entities.
 * 
 * @author Jesse
 */
public interface HarvesterSettingRepository extends JpaRepository<HarvesterSetting, Integer>, HarvesterSettingRepositoryCustom {

    public static final String ID_PREFIX = "id:";
    String SEPARATOR = "/";

    List<HarvesterSetting> findByName(String name);
}
