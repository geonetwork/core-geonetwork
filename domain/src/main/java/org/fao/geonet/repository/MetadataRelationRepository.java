package org.fao.geonet.repository;

import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface MetadataRelationRepository extends GeonetRepository<MetadataRelation, MetadataRelationId>, JpaSpecificationExecutor<MetadataRelation> {
}
