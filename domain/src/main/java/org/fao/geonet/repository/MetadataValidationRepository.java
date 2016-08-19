package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataValidation} entities.
 *
 * @author Jesse
 */
public interface MetadataValidationRepository extends GeonetRepository<MetadataValidation, MetadataValidationId>,
        MetadataValidationRepositoryCustom, JpaSpecificationExecutor<MetadataValidation> {
    /**
     * Find all validation entities related to the metadata identified by metadataId.
     *
     * @param metadataId the id of the metadata.
     * @return the list of MetadataValidation objects related to the metadata identified
     */
    List<MetadataValidation> findAllById_MetadataId(int metadataId);

}
