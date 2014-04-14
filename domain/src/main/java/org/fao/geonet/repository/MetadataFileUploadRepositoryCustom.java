package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataFileUpload;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataFileUpload} entities.
 *
 * @author Jose García
 */
public interface MetadataFileUploadRepositoryCustom {

    MetadataFileUpload findByMetadataIdAndFileNameNotDeleted(int metadataId, String fileName);

}
