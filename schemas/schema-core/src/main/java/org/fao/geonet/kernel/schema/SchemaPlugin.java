package org.fao.geonet.kernel.schema;

import com.google.common.collect.ImmutableSet;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Set;

/**
 * Created by francois on 6/16/14.
 */
public abstract class SchemaPlugin {
    public static final String IDENTIFIER = null;
    private static ImmutableSet<Namespace> allNamespaces = null;

    public String getIdentifier() {
        return IDENTIFIER;
    }

    public Set<Namespace> getNamespaces() {
        return ImmutableSet.copyOf(allNamespaces);
    }

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
