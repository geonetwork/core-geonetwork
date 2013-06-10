package org.fao.geonet.kernel.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.kernel.domain.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Access object for the Group objects.
 *
 * @author Jesse
 */
public interface MetadataRepository extends JpaRepository<Metadata, Integer>, MetadataRepositoryCustom {
    public @Nullable Metadata findByUuid(@Nonnull String uuid);
}
