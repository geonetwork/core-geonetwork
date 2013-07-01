package org.fao.geonet.repository;

import org.fao.geonet.domain.Thesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link Thesaurus} entities.
 * 
 * @author Jesse
 */
public interface ThesaurusRepository extends JpaRepository<Thesaurus, Integer>, JpaSpecificationExecutor<Thesaurus> {
}
