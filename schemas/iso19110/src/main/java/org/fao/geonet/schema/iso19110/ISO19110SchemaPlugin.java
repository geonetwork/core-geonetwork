package org.fao.geonet.schema.iso19110;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by francois on 6/15/14.
 */
public class ISO19110SchemaPlugin
        extends org.fao.geonet.kernel.schema.SchemaPlugin
        implements
                ISOPlugin {
    public static final String IDENTIFIER = "iso19110";

    private static ImmutableSet<Namespace> allNamespaces;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
                .add(ISO19110Namespaces.GFC)
                .add(ISO19139Namespaces.GCO)
                .add(ISO19139Namespaces.GMD)
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
}
