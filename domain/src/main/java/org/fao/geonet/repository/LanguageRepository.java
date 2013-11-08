package org.fao.geonet.repository;

import org.fao.geonet.domain.Language;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.Language} entities.
 *
 * @author Jesse
 */
public interface LanguageRepository extends GeonetRepository<Language, String>, LanguageRepositoryCustom {
}
