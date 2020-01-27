package org.fao.geonet.repository;

import org.fao.geonet.domain.AnnotationEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnnotationRepository extends GeonetRepository<AnnotationEntity, Integer>, AnnotationRepositoryCustom, JpaSpecificationExecutor<AnnotationEntity> {
}
