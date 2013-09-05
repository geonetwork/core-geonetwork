package org.fao.geonet.repository;

import org.fao.geonet.domain.ThesaurusActivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link ThesaurusActivation} entities.
 * 
 * @author Jesse
 */
public interface ThesaurusRepository extends GeonetRepository<ThesaurusActivation, Integer>, JpaSpecificationExecutor<ThesaurusActivation> {
}
