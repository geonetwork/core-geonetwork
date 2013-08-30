package org.fao.geonet.repository;

import org.fao.geonet.domain.Language;

import java.util.List;

/**
 * Methods for accessing Language Repository that cannot be automatically be implemented by spring-data-jpa.
 *
 * User: Jesse
 * Date: 8/30/13
 * Time: 8:18 AM
 */
public interface LanguageRepositoryCustom {
    List<Language> findAllByInspireFlag(boolean inspire);
}
