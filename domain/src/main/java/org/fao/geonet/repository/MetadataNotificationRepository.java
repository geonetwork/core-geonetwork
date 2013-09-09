package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationId;
import org.jdom.Element;

import java.util.List;
import java.util.Map;

/**
 * Data Access object for accessing {@link MetadataNotification} entities.
 *
 * @author Jesse
 */
public interface MetadataNotificationRepository extends GeonetRepository<MetadataNotification, MetadataNotificationId>, MetadataNotificationRepositoryCustom {

}
