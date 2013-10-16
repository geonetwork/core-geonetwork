package org.fao.geonet.repository;

import javax.annotation.Nonnull;

/**
 * Non spring-data-jpa query methods required for accessing CswServerInfo objects.
 * User: Jesse
 * Date: 9/20/13
 * Time: 9:24 AM
 */
public interface CswCapabilitiesInfoFieldRepositoryCustom {
    /**
     * Load all fields in the given language and construct a CswCapabilitiesInfo object from
     * the fields.
     *
     * @param languageCode the language of the info object to load.
     * @return all fields in the given language and construct a CswCapabilitiesInfo object from
     *         the fields.
     */
    @Nonnull
    CswCapabilitiesInfo findCswCapabilitiesInfo(@Nonnull String languageCode);

    /**
     * Save the info object as individual fields.
     *
     * @param info the info object to persist.
     */
    void save(@Nonnull CswCapabilitiesInfo info);
}
