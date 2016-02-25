package org.fao.geonet.schema.iso19139;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.fao.geonet.kernel.schema.*;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by francois on 6/15/14.
 */
public class ISO19139SchemaPlugin
        extends org.fao.geonet.kernel.schema.SchemaPlugin
        implements
                AssociatedResourcesSchemaPlugin,
                MultilingualSchemaPlugin,
                ExportablePlugin,
                ISOPlugin {
    public static final String IDENTIFIER = "iso19139";

    private static ImmutableSet<Namespace> allNamespaces;
    private static Map<String, Namespace> allTypenames;
    private static Map<String, String> allExportFormats;
    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
                .add(ISO19139Namespaces.GCO)
                .add(ISO19139Namespaces.GMD)
                .add(ISO19139Namespaces.SRV)
                .build();
        allTypenames = ImmutableMap.<String, Namespace>builder()
                .put("csw:Record", Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"))
                .put("gmd:MD_Metadata", ISO19139Namespaces.GMD)
                .put("dcat", Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#"))
                .build();

        allExportFormats = ImmutableMap.<String, String>builder()
                // This is more for all basic iso19139 profiles using this bean as default
                // The conversion is not available in regular iso19139 plugin.
                // This is for backward compatibility.
                .put("convert/to19139.xsl", "metadata-iso19139.xml")
                .build();
    }

    public ISO19139SchemaPlugin() {
        super(IDENTIFIER);
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
            sibs = Xml.selectNodes(
                            metadata,
                            XPATH_FOR_AGGRGATIONINFO,
                            allNamespaces.asList());


            for (Object o : sibs) {
                try {
                    if (o instanceof Element) {
                        Element sib = (Element) o;
                        Element agId = (Element) sib.getChild("aggregateDataSetIdentifier", ISO19139Namespaces.GMD)
                                .getChildren().get(0);
                        String sibUuid = getChild(agId, "code", ISO19139Namespaces.GMD)
                                .getChildText("CharacterString", ISO19139Namespaces.GCO);
                        final Element associationTypeEl = getChild(sib, "associationType", ISO19139Namespaces.GMD);
                        String associationType = getChild(associationTypeEl, "DS_AssociationTypeCode", ISO19139Namespaces.GMD)
                                .getAttributeValue("codeListValue");
                        final Element initiativeTypeEl = getChild(sib, "initiativeType", ISO19139Namespaces.GMD);
                        String initiativeType = "";
                        if (initiativeTypeEl != null) {
                            initiativeType = getChild(initiativeTypeEl, "DS_InitiativeTypeCode", ISO19139Namespaces.GMD)
                                    .getAttributeValue("codeListValue");
                        }
                        AssociatedResource resource = new AssociatedResource(sibUuid, initiativeType, associationType);
                        listOfResources.add(resource);
                    }
                } catch (Exception e) {
                    Log.error(Log.JEEVES, "Error getting resources UUIDs", e);
                }
            }
        } catch (Exception e) {
            Log.error(Log.JEEVES, "Error getting resources UUIDs", e);
        }
        return listOfResources;
    }

    private Element getChild(Element el, String name, Namespace namespace) {
        final Element child = el.getChild(name, namespace);
        if (child == null) {
            return new Element(name, namespace);
        }
        return child;
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

    public Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "featureCatalogueCitation", ISO19139Namespaces.GMD);
    }

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

    @Override
    public List<Element> getTranslationForElement(Element element, String languageIdentifier) {
        final String path = ".//gmd:LocalisedCharacterString" +
                "[@locale='#" + languageIdentifier + "']";
        try {
            XPath xpath = XPath.newInstance(path);
            @SuppressWarnings("unchecked")
            List<Element> matches = xpath.selectNodes(element);
            return matches;
        } catch (Exception e) {
            Log.debug(LOGGER_NAME, getIdentifier() + ": getTranslationForElement failed " +
                    "on element " + Xml.getString(element) +
                    " using XPath '" + path +
                    "updatedLocalizedTextElement exception " + e.getMessage());
        }
        return null;
    }

    /**
     *  Add a LocalisedCharacterString to an element. In ISO19139, the translation are
     *  stored gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString.
     *
     * <pre>
     * <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
     *    <gco:CharacterString>Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
     *    <gmd:PT_FreeText>
     *        <gmd:textGroup>
     *            <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en ISO19139 (multilingue)</gmd:LocalisedCharacterString>
     *        </gmd:textGroup>
     * </pre>
     *
     * @param element
     * @param languageIdentifier
     * @param value
     */
    @Override
    public void addTranslationToElement(Element element, String languageIdentifier, String value) {
        // An ISO19139 element containing translation has an xsi:type attribute
        element.setAttribute("type", "gmd:PT_FreeText_PropertyType",
                Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));

        // Create a new translation for the language
        Element langElem = new Element("LocalisedCharacterString", ISO19139Namespaces.GMD);
        langElem.setAttribute("locale", "#" + languageIdentifier);
        langElem.setText(value);
        Element textGroupElement = new Element("textGroup", ISO19139Namespaces.GMD);
        textGroupElement.addContent(langElem);

        // Get the PT_FreeText node where to insert the translation into
        Element freeTextElement = element.getChild("PT_FreeText", ISO19139Namespaces.GMD);
        if (freeTextElement == null) {
            freeTextElement = new Element("PT_FreeText", ISO19139Namespaces.GMD);
            element.addContent(freeTextElement);
        }
        freeTextElement.addContent(textGroupElement);
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

    @Override
    public Map<String, String> getExportFormats() {
        return allExportFormats;
    }
}
