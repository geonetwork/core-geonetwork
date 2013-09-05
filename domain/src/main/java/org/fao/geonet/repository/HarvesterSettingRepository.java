package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.HarvesterSetting;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access object for accessing {@link HarvesterSetting} entities.
 * 
 * @author Jesse
 */
public interface HarvesterSettingRepository extends GeonetRepository<HarvesterSetting, Integer>, HarvesterSettingRepositoryCustom {

    /**
     * The prefix in a path for finding a setting by its id.
     */
    String ID_PREFIX = "id:";
    /**
     * The path separator.
     */
    String SEPARATOR = "/";

    /**
     * Find all the settings with the given name.
     *
     * @param name the setting name.
     *
     * @return All settings with the given name.
     */
    List<HarvesterSetting> findByName(String name);
}
