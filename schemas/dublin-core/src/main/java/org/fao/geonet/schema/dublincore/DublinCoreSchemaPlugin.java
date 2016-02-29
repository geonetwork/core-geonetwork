package org.fao.geonet.schema.dublincore;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.util.Map;
import java.util.Set;

/**
 * Created by francois on 6/15/14.
 */
public class DublinCoreSchemaPlugin
        extends org.fao.geonet.kernel.schema.SchemaPlugin
        implements AssociatedResourcesSchemaPlugin {
    public static final String IDENTIFIER = "dublin-core";

    private static ImmutableSet<Namespace> allNamespaces;
    private static Map<String, Namespace> allTypenames;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
                .add(DublinCoreNamespaces.DC)
                .add(DublinCoreNamespaces.DCT)
                .build();

        allTypenames = ImmutableMap.<String, Namespace>builder()
                .put("csw:Record", Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"))
                .put("dcat", Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#"))
                .build();
    }

    public DublinCoreSchemaPlugin() {
        super(IDENTIFIER);
    }


    /**
     * Always return null. Not implemented for dublin core records.
     *
     * @param metadata
     * @return
     */
    public Set<AssociatedResource> getAssociatedResourcesUUIDs(Element metadata) {
        return null;
    }

    @Override
    public Set<String> getAssociatedParentUUIDs(Element metadata) {
        ElementFilter elementFilter = new ElementFilter("isPartOf", DublinCoreNamespaces.DCT);
        return Xml.filterElementValues(
                metadata,
                elementFilter,
                null, null,
                null);
    }

    public Set<String> getAssociatedDatasetUUIDs(Element metadata) {
        return null;
    }

    ;

    public Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata) {
        return null;
    }

    ;

    public Set<String> getAssociatedSourceUUIDs(Element metadata) {
        return null;
    }

    @Override
    public Map<String, Namespace> getCswTypeNames() {
        return allTypenames;
    }
}
