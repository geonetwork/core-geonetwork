package org.fao.geonet.repository;

import java.util.List;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.HarvesterSetting;

/**
 * Custom (Non spring-data) Query methods for {@link HarvesterSetting} entities.
 * 
 * @author Jesse
 * 
 */
public interface HarvesterSettingRepositoryCustom {
    /**
     * Get the root settings objects (all settings without parents)
     */
    @Nonnull
    List<HarvesterSetting> findRoots();

    /**
     * Find all settings with the parent id
     * @param parentid an id of the parent setting
     * @return all settings with the parent id
     */
    @Nonnull
    List<HarvesterSetting> findAllChildren(int parentid);

    /**
     * Find all settings with the parent id and name. 
     * @param parentid the parentid parameter
     * @param name the name parameter
     * @return
     */
    List<HarvesterSetting> findChildrenByName(int parentid, String name);

    /**
     * Find all settings on the given path.
     *
     * @param pathToSetting the path to search for.
     *
     * @return all settings on the given path.
     */
    List<HarvesterSetting> findByPath(String pathToSetting);

    /**
     * Get the first setting with the given path.
     *
     * @param pathToSetting the path.
     * 
     * @return the first setting with the given path.
     */
    HarvesterSetting findOneByPath(String pathToSetting);
}
