package org.fao.geonet.kernel.schema;

import com.google.common.collect.ImmutableSet;
import org.jdom.Namespace;

import java.util.Set;

/**
 * Created by francois on 6/16/14.
 */
public abstract class SchemaPlugin implements CSWPlugin {
    public static final String LOGGER_NAME = "geonetwork.schema-plugin";

    public final String identifier;

    public String getIdentifier() {
        return identifier;
    }


    protected SchemaPlugin(String identifier) {
        this.identifier = identifier;
    }


    private static ImmutableSet<Namespace> allNamespaces;
    public Set<Namespace> getNamespaces() {
        return this.allNamespaces;
    }
    public void setAllNamespaces(ImmutableSet<Namespace> allNamespaces) {
        this.allNamespaces = allNamespaces;
    }
}
