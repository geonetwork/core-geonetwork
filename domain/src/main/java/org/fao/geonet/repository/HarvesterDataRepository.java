package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvesterData;
import org.fao.geonet.domain.HarvesterDataId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface HarvesterDataRepository extends GeonetRepository<HarvesterData, HarvesterDataId>,
        JpaSpecificationExecutor<HarvesterData> {
    /**
     * Find all the HarvesterData objects belonging to a particular harvester.
     *
     * @param harvesterUuid the harvester uuid
     */
    @Nonnull
    List<HarvesterData> findAllById_HarvesterUuid(@Nonnull String harvesterUuid);
}
