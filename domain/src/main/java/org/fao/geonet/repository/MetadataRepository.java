package org.fao.geonet.repository;

import org.fao.geonet.domain.Metadata;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for the {@link Metadata} entities.
 *
 * @author Jesse
 */
public interface MetadataRepository extends GeonetRepository<Metadata, Integer>, MetadataRepositoryCustom,
        JpaSpecificationExecutor<Metadata> {
    /**
     * Find one metadata by the metadata's uuid.
     *
     * @param uuid the uuid of the metadata to find
     * @return one metadata or null.
     */
    @Nullable
    Metadata findOneByUuid(@Nonnull String uuid);

    /**
     * Find all metadata harvested by the identified harvester.
     *
     * @param uuid the uuid of the harvester
     * @return all metadata harvested by the identified harvester.
     */
    @Nonnull
    List<Metadata> findAllByHarvestInfo_Uuid(@Nonnull String uuid);

    /**
     * Increment popularity of metadata by 1.
     * @param mdId the id of the metadata
     */
    @Modifying
    @Query("UPDATE "+Metadata.TABLENAME+" m SET m.dataInfo.popularity = m.dataInfo.popularity + 1 WHERE m.id = ?1")
    void incrementPopularity(int mdId);}
