package org.fao.geonet.schema.iso19139;

import com.google.common.collect.ImmutableSet;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by francois on 6/15/14.
 */
public class ISO19139SchemaPlugin extends org.fao.geonet.kernel.schema.SchemaPlugin {
    public static final String IDENTIFIER = "iso19139";

    private static ImmutableSet<Namespace> allNamespaces;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
                .add(ISO19139Namespaces.GCO)
                .add(ISO19139Namespaces.GMD)
                .add(ISO19139Namespaces.SRV)
                .build();
    }

    /**
     * Return sibling relation defined in aggregationInfo.
     *
     * @param metadata
     * @return
     */
    public Set<AssociatedResource> getAssociatedResourcesUUIDs(Element metadata) {

        String XPATH_FOR_AGGRGATIONINFO = "*//gmd:aggregationInfo/*" +
                "[gmd:aggregateDataSetIdentifier/*/gmd:code " +
                "and gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue!='']";
        Set<AssociatedResource> listOfResources = new HashSet<AssociatedResource>();
        List<?> sibs = null;
        try {
            sibs = Xml
                    .selectNodes(
                            metadata,
                            XPATH_FOR_AGGRGATIONINFO,
                            allNamespaces.asList());


            for (Object o : sibs) {
                if (o instanceof Element) {
                    Element sib = (Element) o;
                    Element agId = (Element) sib.getChild("aggregateDataSetIdentifier", ISO19139Namespaces.GMD)
                            .getChildren().get(0);
                    String sibUuid = agId.getChild("code", ISO19139Namespaces.GMD)
                            .getChildText("CharacterString", ISO19139Namespaces.GCO);
                    String initType = sib.getChild("initiativeType", ISO19139Namespaces.GMD)
                            .getChild("DS_InitiativeTypeCode", ISO19139Namespaces.GMD)
                            .getAttributeValue("codeListValue");

                    AssociatedResource resource = new AssociatedResource(sibUuid, initType, "");
                    listOfResources.add(resource);
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return listOfResources;
    }

    @Override
    public Set<String> getAssociatedParentUUIDs(Element metadata) {
        ElementFilter elementFilter = new ElementFilter("parentIdentifier", ISO19139Namespaces.GMD);
        return Xml.filterElementValues(
                metadata,
                elementFilter,
                "CharacterString", ISO19139Namespaces.GCO,
                null);
    }

    public Set<String> getAssociatedDatasetUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "operatesOn", ISO19139Namespaces.SRV);
    }

    ;

    public Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "featureCatalogueCitation", ISO19139Namespaces.GMD);
    }

    ;

    public Set<String> getAssociatedSourceUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "source", ISO19139Namespaces.GMD);
    }

    private Set<String> getAttributeUuidrefValues(Element metadata, String tagName, Namespace namespace) {
        ElementFilter elementFilter = new ElementFilter(tagName, namespace);
        return Xml.filterElementValues(
                metadata,
                elementFilter,
                null, null,
                "uuidref");
    }

    ;
}
