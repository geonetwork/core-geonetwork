package org.fao.geonet.schema.iso19110;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.ISOPlugin;
import org.fao.geonet.kernel.schema.MultilingualSchemaPlugin;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;

import java.util.*;

/**
 * Created by francois on 6/15/14.
 */
public class ISO19110SchemaPlugin
        extends org.fao.geonet.kernel.schema.SchemaPlugin
        implements
                ISOPlugin {
    public static final String IDENTIFIER = "iso19110";

    private static ImmutableSet<Namespace> allNamespaces;
    private static Map<String, Namespace> allTypenames;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
                .add(ISO19110Namespaces.GFC)
                .add(ISO19139Namespaces.GCO)
                .add(ISO19139Namespaces.GMD)
                .build();

        allTypenames = ImmutableMap.<String, Namespace>builder()
                .put("csw:Record", Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"))
                .put("gfc:FC_FeatureCatalogue", ISO19110Namespaces.GFC)
                .put("dcat", Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#"))
                .build();
    }

    public ISO19110SchemaPlugin() {
        super(IDENTIFIER);
    }

    @Override
    public String getBasicTypeCharacterStringName() {
        return "gco:CharacterString";
    }

    @Override
    public Element createBasicTypeCharacterString() {
        return new Element("CharacterString", ISO19139Namespaces.GCO);
    }

    @Override
    public Map<String, Namespace> getCswTypeNames() {
        return allTypenames;
    }
}
