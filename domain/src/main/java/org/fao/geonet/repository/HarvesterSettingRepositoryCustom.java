package org.fao.geonet.repository;

import java.util.List;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.HarvesterSetting;
import org.springframework.data.repository.query.Param;

/**
 * Custom (Non spring-data) Query methods for {@link HarvesterSetting} entities.
 * 
 * @author Jesse
 * 
 */
public interface HarvesterSettingRepositoryCustom {
    /**
     * Get the root setting.
     */
    @Nonnull
    List<HarvesterSetting> findRoots();

    @Nonnull
    List<HarvesterSetting> findAllChildren(@Param("parentid") int parentid);

    List<HarvesterSetting> findChildrenByName(@Param("parentid") int parentid, @Param("name") String name);

    List<HarvesterSetting> findByPath(String pathToSetting);

    HarvesterSetting findOneByPath(String pathToSetting);
}
