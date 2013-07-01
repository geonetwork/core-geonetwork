package org.fao.geonet.repository;

import org.fao.geonet.domain.Languages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link Languages} entities.
 * 
 * @author Jesse
 */
public interface LanguagesRepository extends JpaRepository<Languages, Integer>, JpaSpecificationExecutor<Languages> {
}
