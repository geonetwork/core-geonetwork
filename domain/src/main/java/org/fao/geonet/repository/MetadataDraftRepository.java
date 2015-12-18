package org.fao.geonet.repository;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.MetadataDraft;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for the {@link MetadataDraft} entities.
 *
 * @author Jesse
 */
public interface MetadataDraftRepository
        extends GeonetRepository<MetadataDraft, Integer>,
        MetadataDraftRepositoryCustom, JpaSpecificationExecutor<MetadataDraft> {
    /**
     * Find one metadata by the metadata's uuid.
     *
     * @param uuid
     *            the uuid of the metadata to find
     * @return one metadata or null.
     */
    @Nullable
    MetadataDraft findOneByUuid(@Nonnull String uuid);

    /**
     * Find all metadata harvested by the identified harvester.
     *
     * @param uuid
     *            the uuid of the harvester
     * @return all metadata harvested by the identified harvester.
     */
    @Nonnull
    List<MetadataDraft> findAllByHarvestInfo_Uuid(@Nonnull String uuid);
}
