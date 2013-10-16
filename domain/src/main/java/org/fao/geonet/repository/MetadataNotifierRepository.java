package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotifier;

/**
 * Data Access object for accessing {@link MetadataNotifier} entities.
 *
 * @author Jesse
 */
public interface MetadataNotifierRepository extends GeonetRepository<MetadataNotifier, Integer>, MetadataNotifierRepositoryCustom {

}
