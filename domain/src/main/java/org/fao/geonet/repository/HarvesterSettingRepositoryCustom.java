package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvesterSetting;

import javax.annotation.Nonnull;

import java.util.List;

/**
 * Custom (Non spring-data) Query methods for {@link HarvesterSetting} entities.
 *
 * @author Jesse
 */
public interface HarvesterSettingRepositoryCustom {
    /**
     * Get the root settings objects (all settings without parents)
     */
    @Nonnull
    List<HarvesterSetting> findRoots();

    /**
     * Find all settings with the parent id
     *
     * @param parentid an id of the parent setting
     * @return all settings with the parent id
     */
    @Nonnull
    List<HarvesterSetting> findAllChildren(int parentid);

    /**
     * Find the ids of all the all settings with the parent id
     *
     * @param parentid an id of the parent setting
     * @return the ids of all the all settings with the parent id
     */
    @Nonnull
    List<Integer> findAllChildIds(int parentid);

    /**
     * Find all settings with the parent id and name.
     *
     * @param parentid the parentid parameter
     * @param name     the name parameter
     * @return
     */
    List<HarvesterSetting> findChildrenByName(int parentid, String name);

    /**
     * Find all settings on the given path.
     *
     * @param pathToSetting the path to search for.
     * @return all settings on the given path.
     */
    List<HarvesterSetting> findAllByPath(String pathToSetting);

    /**
     * Get the first setting with the given path.
     *
     * @param pathToSetting the path.
     * @return the first setting with the given path.
     */
    HarvesterSetting findOneByPath(String pathToSetting);

    /**
     * Get all settings on given names of settings with one request
     * @param names
     * @return
     */
	List<HarvesterSetting> findAllByNames(List<String> names);
}
