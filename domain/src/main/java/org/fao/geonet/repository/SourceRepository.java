package org.fao.geonet.repository;

import org.fao.geonet.domain.Source;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for accessing {@link Source} entities.
 *
 * @author Jesse
 */
public interface SourceRepository extends GeonetRepository<Source, String>, JpaSpecificationExecutor<Source> {
    /**
     * Find the source with the provided Name.
     *
     * @param name the name of the source to lookup
     * @return the source with the provided name or <code>null</code>.
     */
    public
    @Nullable
    Source findOneByName(@Nonnull String name);
}
