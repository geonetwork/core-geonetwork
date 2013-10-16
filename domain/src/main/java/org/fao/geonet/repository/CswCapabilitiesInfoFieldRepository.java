package org.fao.geonet.repository;

import org.fao.geonet.domain.CswCapabilitiesInfoField;

import java.util.List;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.CswCapabilitiesInfoField} entities.
 *
 * @author Jesse
 */
public interface CswCapabilitiesInfoFieldRepository extends GeonetRepository<CswCapabilitiesInfoField, Integer>,
        CswCapabilitiesInfoFieldRepositoryCustom {
    /**
     * Find all the Capabilities Info objects for the given field.
     *
     * @param fieldName the name of the field to find.
     */
    List<CswCapabilitiesInfoField> findAllByFieldName(String fieldName);

    /**
     * Find all the info for the given language.
     *
     * @param langId the 3 letter language identifier
     * @return all the info for the given language.
     */
    List<CswCapabilitiesInfoField> findAllByLangId(String langId);
}
