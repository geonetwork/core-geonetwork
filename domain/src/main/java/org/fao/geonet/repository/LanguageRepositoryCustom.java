package org.fao.geonet.repository;

import org.fao.geonet.domain.Language;

import java.util.List;

/**
 * Methods for accessing Language Repository that cannot be automatically be implemented by spring-data-jpa.
 * <p/>
 * User: Jesse
 * Date: 8/30/13
 * Time: 8:18 AM
 */
public interface LanguageRepositoryCustom {
    /**
     * Find all the languages based on whether the language is an inspire language or not.
     *
     * @param inspire if true find all inspire languages
     * @return Find all the languages based on whether the language is an inspire language or not.
     */
    List<Language> findAllByInspireFlag(boolean inspire);

    /**
     * Find the default language.
     *
     * @return the default language.
     */
    Language findOneByDefaultLanguage();
}
