package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface MetadataRelationRepository extends GeonetRepository<MetadataRelation, MetadataRelationId>,
        JpaSpecificationExecutor<MetadataRelation> {
}
