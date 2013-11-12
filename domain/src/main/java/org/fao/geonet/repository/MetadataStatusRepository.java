package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data Access object for accessing {@link MetadataStatus} entities.
 *
 * @author Jesse
 */
public interface MetadataStatusRepository extends GeonetRepository<MetadataStatus, MetadataStatusId>, MetadataStatusRepositoryCustom,
        JpaSpecificationExecutor<MetadataStatus> {
    /**
     * Find all the MetadataStatus objects by the associated metadata id.
     *
     * @param metadataId the metadata id.
     * @param sort       how to sort the results
     * @return all the MetadataStatus objects by the associated metadata id.
     */
    @Nonnull
    List<MetadataStatus> findAllById_MetadataId(int metadataId, Sort sort);
}
