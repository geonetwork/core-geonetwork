package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvesterSetting;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for accessing {@link HarvesterSetting} entities.
 *
 * @author Jesse
 */
public interface HarvesterSettingRepository extends GeonetRepository<HarvesterSetting, Integer>,
        JpaSpecificationExecutor<HarvesterSetting>, HarvesterSettingRepositoryCustom {

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
     * @return All settings with the given name.
     */
    List<HarvesterSetting> findAllByName(@Nonnull String name);
    /**
     * Find all the settings with the given name and value.
     *
     * @param name the setting name.
     * @param value the setting value.
     * @return All settings with the given name and value.
     */
    List<HarvesterSetting> findAllByNameAndValue(@Nonnull String name, @Nonnull String value);
    /**
     * Find the settings with the given name and value. Null is returned if not found.
     *
     * @param name the setting name.
     * @param value the setting value.
     * @return The setting with the given name and value.
     */
    @Nullable
    HarvesterSetting findOneByNameAndValue(@Nonnull String name, @Nonnull String value);
}
