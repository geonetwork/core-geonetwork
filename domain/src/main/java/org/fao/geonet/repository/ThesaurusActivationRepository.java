package org.fao.geonet.repository;

import org.fao.geonet.domain.ThesaurusActivation;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link ThesaurusActivation} entities.
 *
 * @author Jesse
 */
public interface ThesaurusActivationRepository extends GeonetRepository<ThesaurusActivation, String>,
        JpaSpecificationExecutor<ThesaurusActivation> {
}
