package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataUrnTemplate;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for the {@link org.fao.geonet.domain.MetadataUrnTemplate} entities.
 *
 * @author Jose García
 */
public interface MetadataUrnTemplateRepository extends GeonetRepository<MetadataUrnTemplate, Integer>, JpaSpecificationExecutor<MetadataUrnTemplate> {
    /**
     * Look up a metadata urn template by its name.
     *
     * @param name the name of the metadata urn template
     */
    @Nullable
    MetadataUrnTemplate findOneByName(@Nonnull String name);



}
