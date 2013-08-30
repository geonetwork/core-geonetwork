package org.fao.geonet.repository;

import org.fao.geonet.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.Language} entities.
 * 
 * @author Jesse
 */
public interface LanguageRepository extends JpaRepository<Language, String>, LanguageRepositoryCustom {
}
