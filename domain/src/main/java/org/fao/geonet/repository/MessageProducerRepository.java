package org.fao.geonet.repository;

import org.fao.geonet.domain.MessageProducerEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageProducerRepository extends GeonetRepository<MessageProducerEntity, Long> {
    @Query("SELECT p from MessageProducerEntity p where p.wfsHarvesterParamEntity.url = (:url) and p.wfsHarvesterParamEntity.typeName = (:featureType)")
    MessageProducerEntity findOneByUrlAndFeatureType(@Param("url") String url, @Param("featureType") String featureType);
}
