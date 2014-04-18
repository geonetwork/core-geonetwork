package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataFileUpload;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataFileUpload} entities.
 *
 * @author Jose García
 */
public interface MetadataFileUploadRepository extends GeonetRepository<MetadataFileUpload, Integer>,
        JpaSpecificationExecutor<MetadataFileUpload>, MetadataFileUploadRepositoryCustom {

}
