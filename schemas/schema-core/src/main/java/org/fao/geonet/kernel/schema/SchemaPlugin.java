package org.fao.geonet.kernel.schema;

import com.google.common.collect.ImmutableSet;
import org.jdom.Namespace;

import java.util.Set;

/**
 * Created by francois on 6/16/14.
 */
public abstract class SchemaPlugin {
    public static final String LOGGER_NAME = "geonetwork.schema-plugin";
    private static ImmutableSet<Namespace> allNamespaces = null;
    public final String identifier;

    protected SchemaPlugin(String identifier) {
        this.identifier = identifier;
    }


    public String getIdentifier() {
        return identifier;
    }

    public Set<Namespace> getNamespaces() {
        return ImmutableSet.copyOf(allNamespaces);
    }
}
