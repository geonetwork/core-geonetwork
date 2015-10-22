package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for the {@link MetadataIdentifierTemplate} entities.
 *
 * @author Jose García
 */
public interface MetadataIdentifierTemplateRepository extends
        GeonetRepository<MetadataIdentifierTemplate, Integer>,
        JpaSpecificationExecutor<MetadataIdentifierTemplate> {
    /**
     * Look up a metadata identifier template by its name.
     *
     * @param name the name of the metadata identifier template
     */
    @Nullable
    MetadataIdentifierTemplate findOneByName(@Nonnull String name);



}
