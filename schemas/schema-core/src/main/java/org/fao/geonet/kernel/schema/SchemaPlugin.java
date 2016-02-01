package org.fao.geonet.kernel.schema;

import com.google.common.collect.ImmutableSet;
import org.jdom.Namespace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by francois on 6/16/14.
 */
public abstract class SchemaPlugin implements CSWPlugin {
    public static final String LOGGER_NAME = "geonetwork.schema-plugin";

    protected SchemaPlugin(String identifier,
                           ImmutableSet<Namespace> allNamespaces) {
        this.identifier = identifier;
        this.allNamespaces = allNamespaces;
    }

    public final String identifier;

    public String getIdentifier() {
        return identifier;
    }

    private List<SavedQuery> savedQueries = new ArrayList<>();

    public List<SavedQuery> getSavedQueries() {
        return savedQueries;
    }

    public void setSavedQueries(List<SavedQuery> savedQueries) {
        this.savedQueries = savedQueries;
    }

    public @Nullable SavedQuery getSavedQuery(@Nonnull String queryKey) {
        Iterator<SavedQuery> iterator = this.getSavedQueries().iterator();
        while (iterator.hasNext()) {
            SavedQuery query = iterator.next();
            if (queryKey.equals(query.getId())) {
                return query;
            }
        }
        return null;
    }

    private ImmutableSet<Namespace> allNamespaces;

    public Set<Namespace> getNamespaces() {
        return allNamespaces;
    }
}
