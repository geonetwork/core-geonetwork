package org.fao.geonet.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data Access object for the {@link Metadata} entities.
 *
 * @author Jesse
 */
public interface MetadataRepository extends GeonetRepository<Metadata, Integer>, MetadataRepositoryCustom {
    /**
     * Find one metadata by the metadata's uuid.
     * @param uuid the uuid of the metadata to find
     * @return one metadata or null.
     */
    @Nullable Metadata findOneByUuid(@Nonnull String uuid);

    /**
     * Find all metadata harvested by the identified harvester.
     *
     * @param uuid the uuid of the harvester
     * @return all metadata harvested by the identified harvester.
     */
    @Nonnull List<Metadata> findAllByHarvestInfo_Uuid(@Nonnull String uuid);
}
