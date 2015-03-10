package org.fao.geonet.kernel.schema;

import org.jdom.Element;

import java.util.Set;

/**
 * Created by francois on 8/20/14.
 */
public interface AssociatedResourcesSchemaPlugin {

    /**
     * Analyse a metadata record and extract associated
     * resources.
     *
     * @return
     */
    public abstract Set<AssociatedResource> getAssociatedResourcesUUIDs(Element metadata);

    /**
     * Analyze a metadata record and extract associated parents
     *
     * @param metadata
     * @return
     */
    public abstract Set<String> getAssociatedParentUUIDs(Element metadata);


    public abstract Set<String> getAssociatedDatasetUUIDs(Element metadata);

    public abstract Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata);

    public abstract Set<String> getAssociatedSourceUUIDs(Element metadata);
}
