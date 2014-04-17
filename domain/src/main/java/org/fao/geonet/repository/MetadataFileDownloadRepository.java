package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataFileDownload;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataFileDownload} entities.
 *
 * @author Jose García
 */
public interface MetadataFileDownloadRepository extends GeonetRepository<MetadataFileDownload, Integer>, JpaSpecificationExecutor<MetadataFileDownload> {
}
