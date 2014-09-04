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
    public static final String LOGGER_NAME = "geonetwork.schema-plugin";
    private static ImmutableSet<Namespace> allNamespaces = null;

    public String getIdentifier() {
        return IDENTIFIER;
    }

    public Set<Namespace> getNamespaces() {
        return ImmutableSet.copyOf(allNamespaces);
    }
}
