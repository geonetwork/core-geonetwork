/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.schema.iso19139;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.kernel.schema.*;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;

import java.util.*;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.*;

/**
 * Created by francois on 6/15/14.
 */
public class ISO19139SchemaPlugin
    extends SchemaPlugin
    implements
    AssociatedResourcesSchemaPlugin,
    MultilingualSchemaPlugin,
    ExportablePlugin,
    ISOPlugin,
    LinkAwareSchemaPlugin {
    public static final String IDENTIFIER = "iso19139";

    public static ImmutableSet<Namespace> allNamespaces;
    private static Map<String, Namespace> allTypenames;
    private static Map<String, String> allExportFormats;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
            .add(GCO)
            .add(GMD)
            .add(SRV)
            .build();
        allTypenames = ImmutableMap.<String, Namespace>builder()
            .put("csw:Record", Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"))
            .put("gmd:MD_Metadata", GMD)
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
        super(IDENTIFIER, allNamespaces);
    }


    /**
     * Return sibling relation defined in aggregationInfo.
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
                        Element agId = (Element) sib.getChild("aggregateDataSetIdentifier", GMD)
                            .getChildren().get(0);
                        String sibUuid = getChild(agId, "code", GMD)
                            .getChildText("CharacterString", GCO);
                        final Element associationTypeEl = getChild(sib, "associationType", GMD);
                        String associationType = getChild(associationTypeEl, "DS_AssociationTypeCode", GMD)
                            .getAttributeValue("codeListValue");
                        final Element initiativeTypeEl = getChild(sib, "initiativeType", GMD);
                        String initiativeType = "";
                        if (initiativeTypeEl != null) {
                            initiativeType = getChild(initiativeTypeEl, "DS_InitiativeTypeCode", GMD)
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
        ElementFilter elementFilter = new ElementFilter("parentIdentifier", GMD);
        return Xml.filterElementValues(
            metadata,
            elementFilter,
            "CharacterString", GCO,
            null);
    }

    public Set<String> getAssociatedDatasetUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "operatesOn", SRV);
    }

    public Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "featureCatalogueCitation", GMD);
    }

    public Set<String> getAssociatedSourceUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "source", GMD);
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
     * Add a LocalisedCharacterString to an element. In ISO19139, the translation are stored
     * gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString.
     *
     * <pre>
     * <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
     *    <gco:CharacterString>Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
     *    <gmd:PT_FreeText>
     *        <gmd:textGroup>
     *            <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en
     * ISO19139 (multilingue)</gmd:LocalisedCharacterString>
     *        </gmd:textGroup>
     * </pre>
     */
    @Override
    public void addTranslationToElement(Element element, String languageIdentifier, String value) {
        // An ISO19139 element containing translation has an xsi:type attribute
        element.setAttribute("type", "gmd:PT_FreeText_PropertyType",
            Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));

        // Create a new translation for the language
        Element langElem = new Element("LocalisedCharacterString", GMD);
        langElem.setAttribute("locale", "#" + languageIdentifier);
        langElem.setText(value);
        Element textGroupElement = new Element("textGroup", GMD);
        textGroupElement.addContent(langElem);

        // Get the PT_FreeText node where to insert the translation into
        Element freeTextElement = element.getChild("PT_FreeText", GMD);
        if (freeTextElement == null) {
            freeTextElement = new Element("PT_FreeText", GMD);
            element.addContent(freeTextElement);
        }
        freeTextElement.addContent(textGroupElement);
    }

    /**
     * Remove all multilingual aspect of an element. Keep the md language localized strings
     * as default gco:CharacterString for the element.
     *
     * @param element
     * @param langs Metadata languages. The main language MUST be the first one.
     * @return
     * @throws JDOMException
     */
    @Override
    public Element removeTranslationFromElement(Element element, List<String> langs) throws JDOMException {
        String mainLanguage = langs != null && langs.size() > 0 ? langs.get(0) : "#EN";

        List<Element> nodesWithStrings = (List<Element>) Xml.selectNodes(element, "*//gmd:PT_FreeText", Arrays.asList(GMD));

        for(Element e : nodesWithStrings) {
            // Retrieve or create the main language element
            Element mainCharacterString = ((Element)e.getParent()).getChild("CharacterString", GCO);
            if (mainCharacterString == null) {
                // create it if it does not exist
                mainCharacterString = new Element("CharacterString", GCO);
                ((Element)e.getParent()).addContent(0, mainCharacterString);
            }

            // Retrieve the main language value if exist
            List<Element> mainLangElement = (List<Element>) Xml.selectNodes(
                e,
                "*//gmd:LocalisedCharacterString[@locale='" + mainLanguage + "']",
                Arrays.asList(GMD));

            // Set the main language value
            if (mainLangElement.size() == 1) {
                String mainLangString = mainLangElement.get(0).getText();

                if (StringUtils.isNotEmpty(mainLangString)) {
                    mainCharacterString.setText(mainLangString);
                } else if (mainCharacterString.getAttribute("nilReason", GCO) == null){
                    ((Element)mainCharacterString.getParent()).setAttribute("nilReason", "missing", GCO);
                }
            } else if (StringUtils.isEmpty(mainCharacterString.getText())) {
                ((Element)mainCharacterString.getParent()).setAttribute("nilReason", "missing", GCO);
            }
        }

        // Remove unused lang entries
        // eg. the directory entry contains more languages than requested.
        List<Element> translationNodes = (List<Element>)Xml.selectNodes(element, "*//node()[@locale]");
        for(Element el : translationNodes) {
            // Remove all translations if there is no or only one language requested
            if(langs.size() <= 1 ||
                !langs.contains(el.getAttribute("locale").getValue())) {
                Element parent = (Element)el.getParent();
                parent.detach();
            }
        }

        // Remove PT_FreeText which might be emptied by above processing
        for(Element el : nodesWithStrings) {
            if (el.getChildren().size() == 0) {
                el.detach();
            }
        }

        // Sort all children elements translation
        // according to the language list.
        // When a directory entry is added as an xlink, the URL
        // contains an ordered list of language and this ordre must
        // be preserved in order to display fields in the editor in the same
        // order as other element in the record.
        if (langs.size() > 1) {
            List<Element> elementList = (List<Element>)Xml.selectNodes(element,
                                        ".//*[gmd:PT_FreeText]",
                                               Arrays.asList(GMD));
            for(Element el : elementList) {
                final Element ptFreeText = el.getChild("PT_FreeText", GMD);
                List<Element> orderedTextGroup = new ArrayList<>();
                for (String l : langs) {
                    List<Element> node = (List<Element>) Xml.selectNodes(ptFreeText, "gmd:textGroup[*/@locale='" + l + "']", Arrays.asList(GMD));
                    if (node != null && node.size() == 1) {
                        orderedTextGroup.add((Element) node.get(0).clone());
                    }
                }
                ptFreeText.removeContent();
                ptFreeText.addContent(orderedTextGroup);
            }
        }


        return element;
    }


    @Override
    public String getBasicTypeCharacterStringName() {
        return "gco:CharacterString";
    }

    @Override
    public Element createBasicTypeCharacterString() {
        return new Element("CharacterString", GCO);
    }

    @Override
    public Element addOperatesOn(Element serviceRecord,
                                 Map<String, String> layers,
                                 String serviceType,
                                 String baseUrl) {

        Element root = serviceRecord
            .getChild("identificationInfo", GMD)
            .getChild("SV_ServiceIdentification", SRV);

        if (root != null) {

            // Coupling type MUST be present as it is the insertion point
            // for coupledResource
            Element couplingType = root.getChild("couplingType", SRV);
            int coupledResourceIdx = root.indexOf(couplingType);

            layers.keySet().forEach(uuid -> {
                String layerName = layers.get(uuid);

                // Create coupled resources elements to register all layername
                // in service metadata. This information could be used to add
                // interactive map button when viewing service metadata.
                Element coupledResource = new Element("coupledResource", SRV);
                coupledResource.setAttribute("nilReason", "synchronizedFromOGC", GCO);
                Element scr = new Element("SV_CoupledResource", SRV);


                // Create operation according to service type
                Element operation = new Element("operationName", SRV);
                Element operationValue = new Element("CharacterString", GCO);

                if (serviceType.startsWith("WMS"))
                    operationValue.setText("GetMap");
                else if (serviceType.startsWith("WFS"))
                    operationValue.setText("GetFeature");
                else if (serviceType.startsWith("WCS"))
                    operationValue.setText("GetCoverage");
                else if (serviceType.startsWith("SOS"))
                    operationValue.setText("GetObservation");
                operation.addContent(operationValue);

                // Create identifier (which is the metadata identifier)
                Element id = new Element("identifier", SRV);
                Element idValue = new Element("CharacterString", GCO);
                idValue.setText(uuid);
                id.addContent(idValue);

                // Create scoped name element as defined in CSW 2.0.2 ISO profil
                // specification to link service metadata to a layer in a service.
                Element scopedName = new Element("ScopedName", GCO);
                scopedName.setText(layerName);

                scr.addContent(operation);
                scr.addContent(id);
                scr.addContent(scopedName);
                coupledResource.addContent(scr);

                // Add coupled resource before coupling type element
                if (coupledResourceIdx != -1) {
                    root.addContent(coupledResourceIdx, coupledResource);
                }


                // Add operatesOn element at the end of identification section.
                Element op = new Element("operatesOn", SRV);
                op.setAttribute("nilReason", "synchronizedFromOGC", GCO);
                op.setAttribute("uuidref", uuid);

                String hRefLink = baseUrl + "api/records/" + uuid + "/formatters/xml";
                op.setAttribute("href", hRefLink, XLINK);

                root.addContent(op);
            });
        }

        return serviceRecord;
    }

    @Override
    public List<Extent> getExtents(Element record) {
        List<Extent> extents = new ArrayList<>();

        ElementFilter bboxFinder = new ElementFilter("EX_GeographicBoundingBox", GMD);
        @SuppressWarnings("unchecked")
        Iterator<Element> bboxes = record.getDescendants(bboxFinder);
        while (bboxes.hasNext()) {
            Element box = bboxes.next();
            try {
                extents.add(new Extent(
                    Double.valueOf(box.getChild("westBoundLongitude", GMD).getChild("Decimal", GCO).getText()),
                    Double.valueOf(box.getChild("eastBoundLongitude", GMD).getChild("Decimal", GCO).getText()),
                    Double.valueOf(box.getChild("southBoundLatitude", GMD).getChild("Decimal", GCO).getText()),
                    Double.valueOf(box.getChild("northBoundLatitude", GMD).getChild("Decimal", GCO).getText())
                    ));
            } catch (NullPointerException e) {}
        }
        return extents;
    }

    @Override
    public Map<String, Namespace> getCswTypeNames() {
        return allTypenames;
    }

    @Override
    public Map<String, String> getExportFormats() {
        return allExportFormats;
    }

    /**
     * Process some of the ISO elements which can have substitute.
     *
     * For example, a CharacterString can have a gmx:Anchor as a substitute
     * to encode a text value + an extra URL. To make the transition between
     * CharacterString and Anchor transparent, this method takes care of
     * creating the appropriate element depending on the presence of an xlink:href attribute.
     * If the attribute is empty, then a CharacterString is used, if a value is set, an Anchor is created.
     *
     * @param el element to process.
     * @param attributeRef the attribute reference
     * @param parsedAttributeName the name of the attribute, for example <code>xlink:href</code>
     * @param attributeValue the attribute value
     * @return
     */
    @Override
    public Element processElement(Element el,
                                  String attributeRef,
                                  String parsedAttributeName,
                                  String attributeValue) {
        if (Log.isDebugEnabled(LOGGER_NAME)) {
            Log.debug(LOGGER_NAME, String.format(
                "Processing element %s, attribute %s with attributeValue %s.",
                    el, attributeRef, attributeValue));
        }

        boolean elementToProcess = isElementToProcess(el);

        if (elementToProcess && parsedAttributeName.equals("xlink:href")) {
            boolean isEmptyLink = StringUtils.isEmpty(attributeValue);
            boolean isMultilingualElement = el.getName().equals("LocalisedCharacterString");

            if (isMultilingualElement) {
                // The attribute provided relates to the CharacterString and not to the LocalisedCharacterString
                Element targetElement = el.getParentElement().getParentElement().getParentElement()
                                            .getChild("CharacterString", GCO);
                if (targetElement != null) {
                    el = targetElement;
                }
            }

            if (isEmptyLink) {
                el.setNamespace(GCO).setName("CharacterString");
                el.removeAttribute("href", XLINK);
                return el;
            } else {
                el.setNamespace(GMX).setName("Anchor");
                el.setAttribute("href", "", XLINK);
                return el;
            }
        } else if (elementToProcess && StringUtils.isNotEmpty(parsedAttributeName) &&
            parsedAttributeName.startsWith(":")) {
            // eg. :codeSpace
            el.setAttribute(parsedAttributeName.substring(1), attributeValue);
            return el;
        } else {
            return super.processElement(el, attributeRef, parsedAttributeName, attributeValue);
        }

    }

    /**
     * Checks if an element requires processing in {@link #processElement(Element, String, String, String)}.
     *
     * @param el Element to check.
     *
     * @return boolean indicating if the element requires processing or not.
     */
    protected boolean isElementToProcess(Element el) {
        if (el == null) return false;

        return elementsToProcess.contains(el.getQualifiedName());
    }

    /**
     * Return an ordered list of record languages.
     * The main language is the first.
     *
     * This may be used when substituting elements by their matching
     * XLinks. In such case, the XLink must contains an ordered list of
     * language codes.
     *
     * @param md The record to analyze
     * @return An ordered list of ISO 3 letters codes
     */
    public static List<String> getLanguages(Element md) {
        List<String> languages = new ArrayList<>();
        try {
            // Main language for the record
            Attribute mainLanguageAttribute = (Attribute) Xml.selectSingle(md, "gmd:language/*/@codeListValue", ISO19139SchemaPlugin.allNamespaces.asList());

            if (mainLanguageAttribute != null &&
                StringUtils.isNotEmpty(mainLanguageAttribute.getValue())) {
                languages.add(mainLanguageAttribute.getValue());
            }
            final String mainLanguage = languages.size() == 1 ? languages.get(0) : "";

            // Append all other locales as ordered in the locale section
            List<Attribute> locales = (List<Attribute>) Xml.selectNodes(md, "gmd:locale/*/gmd:languageCode/*/@codeListValue", ISO19139SchemaPlugin.allNamespaces.asList());
            if (locales != null && locales.size() > 0) {
                locales.forEach(a -> {
                    // Main language may be repeated in locale section
                    // at least in GN case - do not add it twice
                    if (StringUtils.isNotEmpty(mainLanguage) && !mainLanguage.equals(a.getValue())) {
                        languages.add(a.getValue());
                    }
                });
            }
        } catch (JDOMException e) {
        }
        return languages;
    }

    public <L, M> RawLinkPatternStreamer<L, M> createLinkStreamer(ILinkBuilder<L, M> linkbuilder) {
        RawLinkPatternStreamer patternStreamer = new RawLinkPatternStreamer(linkbuilder);
        patternStreamer.setNamespaces(ISO19139SchemaPlugin.allNamespaces.asList());
        patternStreamer.setRawTextXPath(".//*[name() = 'gco:CharacterString' or name() = 'gmd:URL']");
        return patternStreamer;
    }
}
