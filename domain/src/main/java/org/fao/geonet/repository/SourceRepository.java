package org.fao.geonet.repository;

import org.fao.geonet.domain.Source;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link Source} entities.
 *
 * @author Jesse
 */
public interface SourceRepository extends GeonetRepository<Source, Integer>, JpaSpecificationExecutor<Source> {
}
