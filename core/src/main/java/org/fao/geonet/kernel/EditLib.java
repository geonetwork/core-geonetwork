//==============================================================================
//===
//=== EditLib
//===
//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel;

import static org.fao.geonet.constants.Edit.ChildElem.Attr.NAME;
import static org.fao.geonet.constants.Edit.ChildElem.Attr.NAMESPACE;
import static org.fao.geonet.constants.Edit.RootChild.CHILD;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.jxpath.ri.parser.Token;
import org.apache.commons.jxpath.ri.parser.XPathParser;
import org.apache.commons.jxpath.ri.parser.XPathParserConstants;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.schema.ISOPlugin;
import org.fao.geonet.kernel.schema.MetadataAttribute;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataType;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class EditLib {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.EDITOR);
    private static final Logger LOGGER_ADD_ELEMENT = LoggerFactory.getLogger(Geonet.EDITORADDELEMENT);
    private static final Logger LOGGER_FILL_ELEMENT = LoggerFactory.getLogger(Geonet.EDITORFILLELEMENT);
    private static final Logger LOGGER_EXPAND_ELEMENT = LoggerFactory.getLogger(Geonet.EDITOREXPANDELEMENT);

    public static final String XML_FRAGMENT_SEPARATOR = "&&&";
    public static final String COLON_SEPARATOR = "COLON";
    public static final String MSG_ELEMENT_NOT_FOUND_AT_REF = "Element not found at ref = ";

    private static final Joiner SLASH_STRING_JOINER = Joiner.on('/');

    private SchemaManager scm;
    private static final Map<String, Integer> htVersions = new ConcurrentHashMap<String, Integer>();

    public EditLib(SchemaManager scm) {
        this.scm = scm;
    }

    /**
     * Adds missing gml namespace to XML inputs, if it is missing.
     * @param fragment The fragment to be checked and processed.
     * @return The updated fragment.
     */
    public static String addGmlNamespaceToFragment(String fragment) {
        if (fragment.contains("<gml:") && !fragment.contains("xmlns:gml=\"")) {
            LOGGER.debug("  Add missing GML namespace.");
            fragment = fragment.replaceFirst("<gml:([^ >]+)", "<gml:$1 xmlns:gml=\"http://www.opengis.net/gml\"");
        }
        return fragment;
    }

    /**
     * Tag the element so the metadata-edit-embedded.xsl know which element is the element for display
     */
    public static void tagForDisplay(Element elem) {
        elem.setAttribute("addedObj", "true", Edit.NAMESPACE);
    }

    /**
     * Remove the tag element so the tag does not stay in the actual metadata.
     */
    public static void removeDisplayTag(Element elem) {
        elem.removeAttribute("addedObj", Edit.NAMESPACE);
    }

    /**
     * Expands a metadata adding all information needed for editing.
     */
    public String getVersionForEditing(String schema, String id, Element md) throws Exception {
        String version = getVersion(id, true);
        addEditingInfo(schema, md, 1, 0);
        return version;
    }

    public void enumerateTree(Element md) throws Exception {
        enumerateTree(md, 1, 0);
    }

    public void enumerateTreeStartingAt(Element md, int id, int parent) throws Exception {
        enumerateTree(md, id, parent);
    }

    public String getVersion(String id) {
        return getVersion(id, false);
    }

    public String getNewVersion(String id) {
        return getVersion(id, true);
    }

    /**
     * Given an element, creates all mandatory sub-elements. The given element should be empty.
     */
    public void fillElement(String schema, Element parent, Element md) throws Exception {
        fillElement(scm.getSchema(schema), scm.getSchemaSuggestions(schema), parent, md);
    }

    /**
     * Given an expanded tree, removes all info added for editing and replaces choice_elements with
     * their children.
     */
    public void removeEditingInfo(Element md) {
        //--- purge geonet: attributes
        for (Attribute attr: (List<Attribute>)new ArrayList(md.getAttributes())) {
            if (Edit.NAMESPACE.getPrefix().equals(attr.getNamespacePrefix())) {
                attr.detach();
            }
        }

        //--- purge geonet: children
        for (Element child: (List<Element>)new ArrayList(md.getChildren())) {
            if (!Edit.NAMESPACE.getPrefix().equals(child.getNamespacePrefix()))
                removeEditingInfo(child);
            else {
                child.detach();
            }
        }
    }

    /**
     * Returns the element at a given reference.
     *
     * @param md  the metadata element expanded with editing info
     * @param ref the element position in a pre-order visit
     */
    public Element findElement(Element md, String ref) {
        Element elem = md.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);

        if (elem != null && ref.equals(elem.getAttributeValue(Edit.Element.Attr.REF)))
            return md;

        //--- search on children

        @SuppressWarnings("unchecked")
        List<Element> list = md.getChildren();

        for (Element child : list) {
            if (!Edit.NAMESPACE.getPrefix().equals(child.getNamespacePrefix())) {
                child = findElement(child, ref);

                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    public Element addElement(MetadataSchema mdSchema, Element el, String qname) throws Exception {
        LOGGER_ADD_ELEMENT.debug("#### in addElement()");
        LOGGER_ADD_ELEMENT.debug("#### - parent = {}", el.getName());

        String name = getUnqualifiedName(qname);
        String ns = getNamespace(qname, el, mdSchema);
        String prefix = getPrefix(qname);

        LOGGER_ADD_ELEMENT.debug("#### - child name = {}", name);
        LOGGER_ADD_ELEMENT.debug("#### - child namespace = {}", ns);
        LOGGER_ADD_ELEMENT.debug("#### - child prefix = {}", prefix);
        LOGGER_ADD_ELEMENT.debug("#### - parents first child = {}", el.getChildren().stream().findFirst().toString());

        Element child = new Element(name, prefix, ns);

        addChildToParent(mdSchema, el, child, qname, false);

        //--- add mandatory sub-tags
        SchemaSuggestions mdSugg = scm.getSchemaSuggestions(mdSchema.getName());
        fillElement(mdSchema, mdSugg, el, child);

        return child;
    }

    /**
     * Adds XML fragment to the metadata record in the last element of the type of the element in
     * its parent.
     *
     * @param schema         The metadata schema
     * @param targetElement             The element
     * @param qname          The qualified name of the element
     * @param fragment       XML fragment
     * @param removeExisting Remove element of the same type before insertion
     * @throws IllegalStateException Fail to parse the fragment.
     */
    public void addFragment(String schema, Element targetElement, String qname, String fragment, boolean removeExisting) throws Exception {

        MetadataSchema mdSchema = scm.getSchema(schema);
        Element childToAdd;

        try {
            childToAdd = Xml.loadString(fragment, false);
        } catch (JDOMException e) {
            LOGGER_ADD_ELEMENT.error("EditLib : Error parsing XML fragment {}.", fragment);
            throw new IllegalStateException("EditLib : Error when loading XML fragment, " + e.getMessage());
        }

        addChildToParent(mdSchema, targetElement, childToAdd, qname, removeExisting);
    }

    private void addChildToParent(MetadataSchema mdSchema, Element targetElement, Element childToAdd, String qname, boolean removeExisting) throws Exception {
        LOGGER_ADD_ELEMENT.debug( "#### - child qname = {}", qname);

        String parentName = getParentNameFromChild(targetElement);
        LOGGER_ADD_ELEMENT.debug("#### - parent name for type retrieval = {}", parentName);

        String typeName = mdSchema.getElementType(targetElement.getQualifiedName(), parentName);
        LOGGER_ADD_ELEMENT.debug("#### - type name = {}", typeName);

        MetadataType type = mdSchema.getTypeInfo(typeName);
        LOGGER_ADD_ELEMENT.debug("#### - metadata type = {}", type);

        // remove everything and then, depending on removeExisting
        // readd all children to the element and assure a correct position for the new one: at the end of the others
        // or just add the new one
        List<Element> existingAllType = new ArrayList(targetElement.getChildren());
        targetElement.removeContent();
        for (String singleType: type.getAlElements()) {
            List<Element> existingForThisType = filterOnQname(existingAllType, singleType);
            LOGGER_ADD_ELEMENT.debug("####   - child of type {}, list size = {}", singleType, existingForThisType.size());
            if (!qname.equals(singleType) || !removeExisting) {
                for (Element existingChild : existingForThisType) {
                    targetElement.addContent(existingChild);
                    LOGGER_ADD_ELEMENT.debug("####		- add child {}", existingChild.toString());
                }
            }
            if (qname.equals(singleType)) {
                targetElement.addContent(childToAdd);
            }

            filterOnQname(existingAllType, "geonet:child")
                .stream()
                .filter(gnChild -> (gnChild.getAttributeValue("prefix") + ":" + gnChild.getAttributeValue("name")).equals(singleType))
                .findFirst()
                .ifPresent(targetElement::addContent);
        }

        Stream.concat(
            filterOnQname(existingAllType, "geonet:element").stream(),
            filterOnQname(existingAllType, "geonet:attribute").stream()
        ).forEach(targetElement::addContent);

    }

    public void addXMLFragments(String schema, Element md, Map<String, String> xmlInputs) throws Exception {
        // Loop over each XML fragments to insert or replace
        Map<String, Element> nodeRefToElem = new HashMap<>();
        for (Map.Entry<String, String> entry : xmlInputs.entrySet()) {
            String[] nodeConfig = entry.getKey().split("_");
            String nodeRef = nodeConfig[0];

            Element el = findElement(md, nodeConfig[0]);
            nodeRefToElem.put(nodeRef, el);
        }

        for (Map.Entry<String, String> entry : xmlInputs.entrySet()) {
            String xmlSnippetAsString = entry.getValue();
            String nodeName = null;
            boolean replaceExisting = false;

            String[] nodeConfig = entry.getKey().split("_");
            // Possibilities:
            // * X125
            // * X125_replace
            // * X125_gmdCOLONkeywords
            // * X125_gmdCOLONkeywords_replace
            String nodeRef = nodeConfig[0];

            if (nodeConfig[nodeConfig.length-1].equals("replace")) {
                replaceExisting = true;
            }

            if ((nodeConfig.length > 1) && !replaceExisting) {
                nodeName = nodeConfig[1].replace(COLON_SEPARATOR, ":");
            }

            Element el = nodeRefToElem.get(nodeRef);
            if (el == null) {
                LOGGER.error(MSG_ELEMENT_NOT_FOUND_AT_REF + nodeRef);
                continue;
            }


            if (xmlSnippetAsString == null || xmlSnippetAsString.equals("")) {
                continue;
            }
            String[] fragments = xmlSnippetAsString.split(XML_FRAGMENT_SEPARATOR);
            for (String fragment : fragments) {
                if (nodeName != null) {
                    LOGGER.debug("Add XML fragment; {} to element with ref: {}", fragment, nodeRef);
                    addFragment(schema, el, nodeName, fragment, replaceExisting);
                } else {
                    LOGGER.debug("Add XML fragment; {} to element with ref: {} replacing content.", fragment, nodeRef);
                    // clean before update
                    el.removeContent();
                    fragment = addGmlNamespaceToFragment(fragment);

                    // Add content
                    Element node = Xml.loadString(fragment, false);
                    if (replaceExisting) {
                        @SuppressWarnings("unchecked")
                        List<Element> children = node.getChildren();
                        if(children.size() > 0) {
                            for (Element child: children) {
                                el.addContent((Element) child.clone());
                            }
                        } else {
                            String textContent = node.getText();
                            el.addContent(textContent);
                        }
                        List<Attribute> attributes = node.getAttributes();
                        for (Attribute a : attributes) {
                            el.setAttribute((Attribute) a.clone());
                        }
                    } else {
                        el.addContent(node);
                    }
                }
            }
        }
    }

    /**
     * This does exactly the same thing as {@link #addElementOrFragmentFromXpath(Element,
     * MetadataSchema, String, AddElemValue, boolean)} except that it is done multiple times, once
     * for each element in the map
     *
     * @param metadataRecord            the record to update
     * @param xmlAndXpathInputs         the xpaths and new values
     * @param metadataSchema            the schema of the metadata record
     * @param createXpathNodeIfNotExist if true then xpaths will be created if they don't indentify
     *                                  an existing element or attribute. Otherwise only existing
     *                                  xpaths will be updated.
     * @return the number of updates.
     */
    public int addElementOrFragmentFromXpaths(Element metadataRecord,
                                              LinkedHashMap<String, AddElemValue> xmlAndXpathInputs,
                                              MetadataSchema metadataSchema,
                                              boolean createXpathNodeIfNotExist) throws JDOMException, IOException {


        int numUpdated = 0;
        // Loop over each XML fragments to insert, replace or delete
        for (Map.Entry<String, AddElemValue> entry : xmlAndXpathInputs.entrySet()) {
            String xpathProperty = entry.getKey();
            AddElemValue propertyValue = entry.getValue();

            final boolean isReplaceAllMode = propertyValue.getNodeValue() != null &&
                propertyValue.getNodeValue().getName()
                    .startsWith(SpecialUpdateTags.REPLACE_ALL);

            if (isReplaceAllMode) {
                // Remove all
                AddElemValue propertyValueToProcess = new AddElemValue(new Element("gn_delete"));

                addElementOrFragmentFromXpath(metadataRecord, metadataSchema, xpathProperty, propertyValueToProcess,
                    createXpathNodeIfNotExist);

                Element fragments = propertyValue.getNodeValue();

                for (Element fragment : (List<Element>) fragments.getChildren()) {
                    propertyValueToProcess = new AddElemValue("<gn_create>" + Xml.getString(fragment) + "</gn_create>");

                    boolean updated = addElementOrFragmentFromXpath(metadataRecord, metadataSchema, xpathProperty, propertyValueToProcess,
                        createXpathNodeIfNotExist);
                    if (updated) {
                        numUpdated++;
                    }
                }

            } else {
                boolean updated = addElementOrFragmentFromXpath(metadataRecord, metadataSchema, xpathProperty, propertyValue,
                    createXpathNodeIfNotExist);
                if (updated) {
                    numUpdated++;
                }
            }
        }
        return numUpdated;
    }

    /**
     * Update a metadata record for the xpath/value provided. The xpath (in accordance with JDOM
     * x-path) does not start with the root element for example:
     * <p/>
     * <code><pre>
     *     &lt;gmd:MD_Metadata>
     *         &lt;gmd:fileIdentifier>&lt;/gmd:fileIdentifier>
     *     &lt;gmd:MD_Metadata>
     * </pre></code>
     * <p/>
     * The xpath
     * <pre><code>  gmd:MD_Metadata/gmd:fileIdentifier</code></pre>
     * will <b>NOT</b> select any elements.  Instead one must use the xpath:
     * <pre><code>  gmd:fileIdentifier</code></pre>
     * to select the gmd:fileIdentifier element.
     * <p/>
     * To update the root element of the metadata use the xpath: "" (empty string)
     * <p/>
     * <p/>
     * The value could be a String to set the value of an element or and XML fragment to be inserted
     * for the element.
     * <p/>
     * If the xpath match an existing element, this element is updated. Only the first one is
     * updated if more than one match.
     * <p/>
     * <p/>
     * If it does not, each missing nodes of the xpath are created and the element inserted
     * according to the schema definition.
     * <p/>
     * If the end of the xpath is an attribute:
     * <code><pre>elem/@att</pre></code>
     * <p/>
     * Then the attribute of the element will be set instead of the text of the element.
     * <p/>
     * The rules for updating a node with Xml is as follows: <ul> <li> If the xml's root element is
     * the same as the element selected by the XPATH then node is replaced with the element.  For
     * example:
     * <code><pre>
     * Xpath: gmd:fileIdentifier
     * XML: &lt;gmd:fileIdentifier gco:nilReason='withheld'/>
     * Result: the gmd:fileIdentifier element in the metadata will be completely replaced with the
     * new one.  All attributes in the metadata
     *         will be lost and replaced with the attributes in the new element.
     *         </pre></code>
     * </li> <li> If the xml's root element == '{@value org.fao.geonet.kernel.EditLib.SpecialUpdateTags#REPLACE}'
     * (a magic tag) then the children of that element will be replace the element selected from the
     * metadata. </li> <li> If the xml's root element == '{@value org.fao.geonet.kernel.EditLib.SpecialUpdateTags#ADD}'
     * (a magic tag) then the children of that element will be added to the element selected from
     * the metadata. </li> <li> If the xml's root element == '{@value org.fao.geonet.kernel.EditLib.SpecialUpdateTags#DELETE}'
     * (a magic tag) then all elements matching the XPath are deleted. </li> <li> If the xml's root
     * element != the name (and namespace) of the element selected from the metadata then the xml
     * will replace the children of the element selected from the metadata. </li> </ul>
     *
     * @param metadataRecord            the metadata xml to update
     * @param metadataSchema            the schema of the metadata
     * @param xpathProperty             the xpath to the element to update/replace/add
     * @param value                     the string or xmlString to add/update/replace
     * @param createXpathNodeIfNotExist if the element identified by the xpath does not exist it
     *                                  will be create when this is true
     * @return true if the metadata was modified
     */
    public boolean addElementOrFragmentFromXpath(Element metadataRecord,
                                                 MetadataSchema metadataSchema,
                                                 String xpathProperty,
                                                 AddElemValue value,
                                                 boolean createXpathNodeIfNotExist) {
        boolean isUpdated = false;
        try {
            final boolean isValueXml = value.isXml();
            final boolean isDeleteMode = value.getNodeValue() != null &&
                value.getNodeValue().getName()
                    .startsWith(SpecialUpdateTags.DELETE);
            final boolean isCreateMode = value.getNodeValue() != null &&
                value.getNodeValue().getName()
                    .equals(SpecialUpdateTags.CREATE);

            LOGGER_ADD_ELEMENT.debug("Inserting at location {} the snippet or value {}", xpathProperty, value);

            xpathProperty = cleanRootFromXPath(xpathProperty, metadataRecord);
            final List<Object> nodeList = trySelectNode(metadataRecord, metadataSchema, xpathProperty, true).results;

            LOGGER_ADD_ELEMENT.debug("{} element matching XPath found.", nodeList.size());

            // If a property is not found in metadata,
            // or in create mode, create it...
            if (((nodeList.isEmpty() && createXpathNodeIfNotExist) || isCreateMode)
                && !isDeleteMode) {
                int indexOfRequiredPortion = -1;
                // Extract the XPath for the element to match. For:
                //  * Relative XPath (*//gmd:RS_Identifier)[2]/gmd:code/gco:CharacterString
                // xpath should be (*//gmd:RS_Identifier)[2]
                // * Absolute XPath with condition
                // gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision']
                // xpath should be gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date
                boolean relativeXpath = xpathProperty.startsWith("(");

                for (int i = 0; i < xpathProperty.length(); i++) {
                    final char c = xpathProperty.charAt(i);
                    if ((relativeXpath && (c == ')' || c == ']')) ||
                        (!relativeXpath && c == '[')) {
                        indexOfRequiredPortion = i + (relativeXpath ? 1 : 0);
                    }
                }
                if (indexOfRequiredPortion > 0) {
                    final String requiredXPath =
                        xpathProperty.substring(0, indexOfRequiredPortion);
                    final SelectResult selectResult = trySelectNode(metadataRecord,
                        metadataSchema,
                        requiredXPath,
                        false);
                    if (selectResult != null) {
                        Object elem = selectResult.result;
                        if (elem == null) {
                            isUpdated = createAndAddFromXPath(metadataRecord,
                                metadataSchema,
                                requiredXPath,
                                value);
                        } else if (elem instanceof Element) {
                            Element element = (Element) elem;

                            isUpdated = createAndAddFromXPath(element,
                                metadataSchema,
                                xpathProperty.substring(indexOfRequiredPortion),
                                value);
                        } else {
                            isUpdated = false;
                        }
                    }
                } else {
                    isUpdated = createAndAddFromXPath(metadataRecord,
                        metadataSchema,
                        xpathProperty,
                        value);
                }
            } else {
                // Update or delete matching node(s) ...
                Iterator<Object> iterator = nodeList.iterator();
                while (iterator.hasNext()) {
                    Object propNode = iterator.next();
                    // If a property is found,
                    // - handle deletion
                    // - Update text node or attributes
                    if (!isCreateMode) {
                        // And if magic tag is delete
                        // Delete a node
                        // <gn_delete/>
                        if (isDeleteMode) {
                            if (propNode instanceof Element) {
                                Element parent = ((Element) propNode).getParentElement();
                                if (parent != null) {
                                    Element matchingNode = ((Element) propNode);
                                    // Remove only matching node
                                    if (value.getNodeValue().getName()
                                        .equals(SpecialUpdateTags.DELETE)) {
                                        parent.removeContent(parent.indexOf(matchingNode));
                                    }
                                }
                            } else if (propNode instanceof Attribute) {
                                Element parent = ((Attribute) propNode).getParent();
                                Attribute targetAttribute = (Attribute) propNode;
                                parent.removeAttribute(
                                    targetAttribute.getName(),
                                    targetAttribute.getNamespace());
                            }
                        } else {
                            // Update element content with node
                            // <gn_replace|add>
                            //   <gmd:contact>
                            //     <gmd:CI_ResponsibleParty
                            if (propNode instanceof Element && isValueXml) {
                                // We need to know where to insert the element
                                // So do add fragment, will create an empty element of the
                                // node to insert class and substitute the created one by
                                // the XML snippet provided
                                doAddFragmentFromXpath(metadataSchema, value.getNodeValue(), (Element) propNode);
                            } else if (propNode instanceof Element && !isValueXml) {
                                // Update element text with value
                                ((Element) propNode).setText(value.getStringValue());
                            } else if (propNode instanceof Attribute && !isValueXml) {
                                ((Attribute) propNode).setValue(value.getStringValue());
                            }
                        }
                        isUpdated = true;
                    }
                }
            }
        } catch (JaxenException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return isUpdated;
    }

    /**
     * Performs the updating of the element selected from the metadata by the xpath.
     */
    private void doAddFragmentFromXpath(MetadataSchema metadataSchema,
                                        Element fragment, Element propEl) throws Exception {

        Element newValue = (Element) fragment.clone();
        if (newValue.getName().equals(SpecialUpdateTags.REPLACE) ||
            newValue.getName().equals(SpecialUpdateTags.ADD) ||
            newValue.getName().equals(SpecialUpdateTags.CREATE)) {

            final boolean isReplace = newValue.getName().equals(SpecialUpdateTags.REPLACE);
            if (isReplace) {
                propEl.removeContent();
            }

            @SuppressWarnings("unchecked")
            List<Object> children = Lists.newArrayList(newValue.getContent());
            for (Object o : children) {
                if (o instanceof Element) {
                    Element child = (Element) o;
                    if (LOGGER_ADD_ELEMENT.isDebugEnabled()) {
                        LOGGER_ADD_ELEMENT.debug(" > add " + Xml.getString(child));
                    }

                    child.detach();

                    final boolean childHasSameTypeAsTarget =
                        child.getName().equals(propEl.getName()) &&
                            child.getNamespace().equals(propEl.getNamespace());
                    if (isReplace) {
                        propEl.addContent(child);
                    } else if (childHasSameTypeAsTarget) {
                        Element parent = propEl.getParentElement();
                        if (parent == null) {
                            LOGGER_ADD_ELEMENT.error(String.format(
                                " > adding fragment from XPath in element %s which has no parent. This usually means that the element is not allowed in the XSD. Check this element in the metadata record.",
                                propEl.getName()
                            ));
                        } else {
                            int index = parent.indexOf(propEl);

                            // Non existing element already created
                            // eg. Insert xpath
                            // mdb:distributionInfo/mrd:MD_Distribution/mrd:distributor
                            // with snippet <mrd:distributor
                            // and record does not contain mrd:distributor.
                            // It was created on xpath analysis step above.
                            // In this case, insert the fragment at the target
                            // Check on children size may not be strict enough
                            if (propEl.getChildren().size() == 0) {
                                parent.setContent(index, child);
                            } else {
                                parent.addContent(index, child);
                            }
                        }
                    } else {
                        // Add an element of same type in the target node
                        final Element newElement = addElement(metadataSchema, propEl, child.getQualifiedName());
                        if (newElement.getParent() != null) {
                            propEl.setContent(propEl.indexOf(newElement), child);
                        } else if (child.getParentElement() == null) {
                            propEl.addContent(child);
                        }
                    }
                } else if (o instanceof Text) {
                    propEl.addContent((Content) (new Text(((Text) o).getText())));
                }
            }
        } else if (newValue.getName().equals(SpecialUpdateTags.DELETE)) {
            // Ignore the node
        } else if (newValue.getName().equals(propEl.getName()) &&
            newValue.getNamespace().equals(propEl.getNamespace())) {
            // If the target element has the same name as the element to add
            // Replace the target element by the new one
            int idx = propEl.getParentElement().indexOf(propEl);
            propEl.getParentElement().setContent(idx, newValue);
        } else {
            propEl.setContent(newValue);
        }
    }

    /**
     * Walk the XPath and create whatever missing parent element until the end of the XPath is
     * matched.
     *
     * @param value The node or value to add to the XPath matched element.
     */
    private boolean createAndAddFromXPath(Element metadataRecord, MetadataSchema metadataSchema, String xpathProperty, AddElemValue value) throws Exception {
        // Removes root metadata element for xpath filters
        xpathProperty = cleanRootFromXPath(xpathProperty, metadataRecord);

        List<String> xpathParts = Arrays.asList(xpathProperty.split("/"));
        SelectResult rootElem = trySelectNode(metadataRecord, metadataSchema, xpathParts.get(0), false);

        Pair<Element, String> result;
        if (rootElem.result instanceof Element) {
            result = findLongestMatch(metadataRecord, metadataSchema, xpathParts);
        } else {
            result = Pair.read(metadataRecord, SLASH_STRING_JOINER.join(xpathParts));
        }
        final Element elementToAttachTo = result.one();
        final Element cloneOfElementToAttachTo = (Element) elementToAttachTo.clone();

        // Creating the element at the xpath location
        // Walk the XPath from the start until the end or the start of a filter
        // expression.
        // Collect element namespace prefix and name, check element exist and
        // create them according to schema definition.
        final XPathParser xpathParser = new XPathParser(new StringReader(cloneOfElementToAttachTo.getQualifiedName() + "/" + result.two()));

        // Start from the root of the metadata document
        Token currentToken = xpathParser.getNextToken();
        Token previousToken = currentToken;

        int depth = 0;
        Element currentNode = cloneOfElementToAttachTo;
        boolean existingElement = true;
        boolean isAttribute = false;
        String currentElementName = "";
        String currentElementNamespacePrefix = "";
        String currentAttributeNamespacePrefix = "";

        // Stop when token is null, start of an expression is found ie. "["
        //
        // Stop when an expression [ starts
        // The expression is supposed to be part of the XML snippet to insert
        // If an existing element needs to be updated use the _Xref_replace mode
        // this mode is more precise with the geonet:element/@ref.
        while (currentToken != null &&
            currentToken.kind != 0 &&
            currentToken.kind != XPathParserLocalConstants.SQBRACKET_OPEN) {

            // TODO : check no .., descendant, ... are in the xpath
            // Only full xpath are supported.
            if (XPathParserLocalConstants.ILLEGAL_KINDS.contains(currentToken.kind)) {
                return false;
            }

            // build element name as the parser progress into the xpath ...
            if (currentToken.kind == XPathParserLocalConstants.ATTRIBUTE) {
                isAttribute = true;
            }
            // Match namespace prefix
            if (currentToken.kind == XPathParserLocalConstants.TEXT &&
                    previousToken.kind == XPathParserConstants.SLASH) {
                // get element namespace if element is text and previous was /
                // means qualified name only is supported
                currentElementNamespacePrefix = currentToken.image;
            } else if (isAttribute &&
                        previousToken.kind == XPathParserLocalConstants.TEXT &&
                        currentToken.kind == XPathParserLocalConstants.NAMESPACE_SEP) {
                currentAttributeNamespacePrefix = previousToken.image;
            } else if (currentToken.kind == XPathParserLocalConstants.TEXT &&
                previousToken.kind == XPathParserLocalConstants.NAMESPACE_SEP) {
                // get element name if element is text and previous was /
                currentElementName = currentToken.image;

                // Do not change anything to the root of the
                // metadata record which MUST be the root of
                // the xpath
                if (depth > 0) {
                    // If an element name is created
                    // Check the element exist in the metadata
                    // and create it if needed.
                    String qualifiedName = currentElementNamespacePrefix + ":" + currentElementName;
                    LOGGER_ADD_ELEMENT.debug("Check if {} exists in {}", qualifiedName, currentNode.getName());

                    Element nodeToCheck = currentNode.getChild(currentElementName,
                        Namespace.getNamespace(metadataSchema.getNS(currentElementNamespacePrefix)));

                    if (nodeToCheck != null) {
                        LOGGER_ADD_ELEMENT.debug(" > {} found", qualifiedName);
                        // Element found, no need to create it, continue walking the xpath.
                        currentNode = nodeToCheck;
                        existingElement &= true;
                    } else {
                        LOGGER_ADD_ELEMENT.debug(" > add new node {} inserted in {}", qualifiedName, currentNode.getName());

                        if (isAttribute) {
                            existingElement = false; // Attribute is created and set after.
                        } else if (metadataSchema.getElementValues(qualifiedName, currentNode.getQualifiedName()) != null) {
                            currentNode = addElement(metadataSchema, currentNode, qualifiedName);
                            existingElement = false;
                        } else {
                            // element not in schema so stop!
                            return false;
                        }
                    }
                }

                depth++;
                // Reset current element props
                currentElementName = "";
                currentElementNamespacePrefix = "";
            }

            previousToken = currentToken;
            currentToken = xpathParser.getNextToken();
        }

        if (value.isXml() && isAttribute) {
            throw new AssertionError(String.format(
                "Cannot set Xml on an attribute. Xpath:'%s' value: '%s'.",
                xpathProperty, Xml.getString(value.getNodeValue())
            ));
        }
        // The current node is an existing node or newly created one
        // Insert the XML value
        if (value.isXml()) {
            // If current node match the node name to insert
            // Insert the new node in its parent
            if (existingElement) {
                currentNode = addElement(metadataSchema,
                    currentNode.getParentElement(),
                    currentNode.getQualifiedName());
            }

            // clean before update
            // when adding the fragment child nodes or suggestion may also be added.
            // In this case, the snippet only has to be inserted
            currentNode.removeContent();

            doAddFragmentFromXpath(metadataSchema, value.getNodeValue(), currentNode);
        } else {
            if (isAttribute) {
                if (StringUtils.isNotEmpty(currentAttributeNamespacePrefix)) {
                    currentNode.setAttribute(previousToken.image,
                        value.getStringValue(),
                        Namespace.getNamespace(currentAttributeNamespacePrefix,
                            metadataSchema.getNS(currentAttributeNamespacePrefix)));
                } else {
                    currentNode.setAttribute(previousToken.image, value.getStringValue());
                }

            } else {
                currentNode.setText(value.getStringValue());
            }
        }

        // update worked so now we can update original element...
        elementToAttachTo.removeContent();
        List<Content> toAdd = Lists.newArrayList(cloneOfElementToAttachTo.getContent());
        List<Attribute> attributeToAdd = Lists.newArrayList(cloneOfElementToAttachTo.getAttributes());
        for (Attribute a : attributeToAdd) {
            elementToAttachTo.setAttribute(a.detach());
        }
        for (Content content : toAdd) {
            elementToAttachTo.addContent(content.detach());
        }
        return true;
    }

    @VisibleForTesting
    protected Pair<Element, String> findLongestMatch(final Element metadataRecord,
                                                     final MetadataSchema metadataSchema,
                                                     final List<String> xpathPropertyParts) {
        BitSet bitSet = new BitSet(xpathPropertyParts.size());
        return findLongestMatch(metadataRecord, metadataRecord, 0, metadataSchema,
            xpathPropertyParts.size() / 2, xpathPropertyParts, bitSet);
    }

    private Pair<Element, String> findLongestMatch(final Element metadataRecord, final Element bestMatch, final int indexOfBestMatch,
                                                   final MetadataSchema metadataSchema, final int nextIndex, final List<String> xpathPropertyParts,
                                                   BitSet visited) {

        if (visited.get(nextIndex)) {
            return Pair.read(bestMatch, SLASH_STRING_JOINER.join(xpathPropertyParts.subList(indexOfBestMatch, xpathPropertyParts.size())));
        }
        visited.set(nextIndex);

        // do linear search when for last couple elements of xpath
        if (xpathPropertyParts.size() - nextIndex < 3) {
            for (int i = xpathPropertyParts.size() - 1; i > -1; i--) {
                final String xpath = SLASH_STRING_JOINER.join(xpathPropertyParts.subList(0, i));
                SelectResult result = trySelectNode(metadataRecord, metadataSchema, xpath, false);
                if (result.result instanceof Element) {
                    return Pair.read((Element) result.result, SLASH_STRING_JOINER.join(xpathPropertyParts.subList(i,
                        xpathPropertyParts.size())));
                }
            }
            return Pair.read(bestMatch, SLASH_STRING_JOINER.join(xpathPropertyParts.subList(indexOfBestMatch, xpathPropertyParts.size())));
        } else {
            final SelectResult found = trySelectNode(metadataRecord, metadataSchema, SLASH_STRING_JOINER.join(xpathPropertyParts
                .subList(0,
                    nextIndex)), false);
            if (found.result instanceof Element) {
                Element newBest = (Element) found.result;
                int newIndex = nextIndex + ((xpathPropertyParts.size() - nextIndex) / 2);
                return findLongestMatch(metadataRecord, newBest, nextIndex, metadataSchema, newIndex, xpathPropertyParts, visited);
            } else if (!found.error) {
                int newNextIndex = indexOfBestMatch + ((nextIndex - indexOfBestMatch) / 2);
                return findLongestMatch(metadataRecord, bestMatch, indexOfBestMatch, metadataSchema,
                    newNextIndex, xpathPropertyParts, visited);
            } else {
                int newNextIndex = nextIndex + 1;
                return findLongestMatch(metadataRecord, bestMatch, indexOfBestMatch, metadataSchema,
                    newNextIndex, xpathPropertyParts, visited);
            }
        }
    }

    private SelectResult trySelectNode(Element metadataRecord, MetadataSchema metadataSchema, String xpathProperty, boolean allNodes) {
        if (xpathProperty.trim().isEmpty()) {
            List<Object> list = new ArrayList<>();
            list.add(metadataRecord);
            return SelectResult.of(list);
        }

        // Initialize the Xpath with all schema namespaces
        Map<String, String> mapNs = metadataSchema.getSchemaNSWithPrefix();


        try {
            JDOMXPath xpath = new JDOMXPath(xpathProperty);
            xpath.setNamespaceContext(new SimpleNamespaceContext(mapNs));
            // Select the node to update and check it exists
            if (allNodes) {
                return SelectResult.of(xpath.selectNodes(metadataRecord));
            } else {
                return SelectResult.of(xpath.selectSingleNode(metadataRecord));
            }
        } catch (JaxenException e) {
            LOGGER_ADD_ELEMENT.warn("An illegal xpath was used to locate an element: {}", xpathProperty);
            return SelectResult.ERROR;
        }
    }

    /**
     * Removes the version of the edit session for a metadata. Used when the edit session is finished.
     *
     * @param id
     */
    public void clearVersion(String id) {
        htVersions.remove(id);
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    private List<Element> filterOnQname(List<Element> children, String qname) {
        Vector<Element> result = new Vector<Element>();
        for (Element child : children) {
            if (child.getQualifiedName().equals(qname)) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * Returns the version of a metadata, incrementing it if necessary.
     */
    private synchronized String getVersion(String id, boolean increment) {
        Integer inVer = htVersions.get(id);

        if (inVer == null)
            inVer = 1;

        if (increment)
            inVer = inVer + 1;

        htVersions.put(id, inVer);

        return Integer.toString(inVer);
    }

    private void fillElement(MetadataSchema schema, SchemaSuggestions sugg, Element parent, Element element) throws Exception {
        String parentName = parent.getQualifiedName();
        fillElement(schema, sugg, parentName, element);
    }

    /**
     * @param schema     The metadata schema
     * @param sugg       The suggestion configuration for the schema
     * @param parentName The name of the parent
     * @param element    The element to fill
     */
    private void fillElement(MetadataSchema schema, SchemaSuggestions sugg, String parentName, Element element) throws Exception {
        String elemName = element.getQualifiedName();
        SchemaPlugin plugin = schema.getSchemaPlugin();
        boolean isISOPlugin = plugin instanceof ISOPlugin;
        ISOPlugin isoPlugin = isISOPlugin ? (ISOPlugin) plugin : null;

        boolean isSimpleElement = schema.isSimpleElement(elemName, parentName);

        LOGGER_FILL_ELEMENT.debug("#### Entering fillElement()");
        LOGGER_FILL_ELEMENT.debug("#### - elemName = {}", elemName);
        LOGGER_FILL_ELEMENT.debug("#### - parentName = {}", parentName);
        LOGGER_FILL_ELEMENT.debug("#### - isSimpleElement({}) = {}", elemName, isSimpleElement);

        // Nothing to fill - eg. gco:CharacterString
        if (isSimpleElement) {
            return;
        }

        MetadataType type = schema.getTypeInfo(schema.getElementType(elemName, parentName));
        boolean hasSuggestion = sugg.hasSuggestion(elemName, type.getElementList());
//        List<String> elementSuggestion = sugg.getSuggestedElements(elemName);
//        boolean hasSuggestion = elementSuggestion.size() != 0;

        LOGGER_FILL_ELEMENT.debug("#### - Type:");
        LOGGER_FILL_ELEMENT.debug("####   - name = {}", type.getName());
        LOGGER_FILL_ELEMENT.debug("####   - # attributes = {}", type.getAttributeCount());
        LOGGER_FILL_ELEMENT.debug("####   - # elements = {}", type.getElementCount());
        LOGGER_FILL_ELEMENT.debug("####   - # isOrType = {}", type.isOrType());
        LOGGER_FILL_ELEMENT.debug("####   - type = {}", type);
        LOGGER_FILL_ELEMENT.debug("#### - Has suggestion = {}", hasSuggestion);

        //-----------------------------------------------------------------------
        //--- handle attributes if mandatory or suggested
        //
        for (MetadataAttribute attr: type.getAlAttribs()) {
            LOGGER_FILL_ELEMENT.debug("####   - {} attribute = {}", attr.name);
            LOGGER_FILL_ELEMENT.debug("####     - required = {}", attr.required);
            LOGGER_FILL_ELEMENT.debug("####     - suggested = {}", sugg.isSuggested(elemName, attr.name));

            if (attr.required || sugg.isSuggested(elemName, attr.name)) {
                String value = "";

                if (attr.defValue != null) {
                    value = attr.defValue;
                    LOGGER_FILL_ELEMENT.debug("####     - value = {}", attr.defValue);
                }

                String uname = getUnqualifiedName(attr.name);
                String ns = getNamespace(attr.name, element, schema);
                String prefix = getPrefix(attr.name);
                if (!prefix.equals(""))
                    element.setAttribute(new Attribute(uname, value, Namespace.getNamespace(prefix, ns)));
                else
                    element.setAttribute(new Attribute(uname, value));
            }
        }


        //-----------------------------------------------------------------------
        //--- add mandatory children
        //
        //     isOrType if element has substitutes and one of them should be chosen
        if (!type.isOrType()) {
            for (int i = 0; i < type.getElementCount(); i++) {
                final String childName = type.getElementAt(i);
                final boolean childIsMandatory = type.getMinCardinAt(i) > 0;
                final boolean childIsSuggested = sugg.isSuggested(elemName, childName);
                final boolean childIsFiltered = sugg.isFiltered(elemName, childName);

                LOGGER_FILL_ELEMENT.debug("####   - {} element = {}", i, childName);
                LOGGER_FILL_ELEMENT.debug("####     - suggested = {}", childIsSuggested);
                LOGGER_FILL_ELEMENT.debug("####     - is mandatory = {}", childIsMandatory);

                if ((childIsMandatory || childIsSuggested) && !childIsFiltered) {

                    MetadataType elemType = schema.getTypeInfo(schema.getElementType(childName, elemName));
                    List<String> childSuggestion = sugg.getSuggestedElements(childName);
                    boolean childHasOneSuggestion = sugg.hasSuggestion(childName, elemType.getElementList()) && (CollectionUtils.intersection(elemType.getElementList(), childSuggestion).size() == 1);
                    boolean childHasOnlyCharacterStringSuggestion = childSuggestion.size() == 1 && childSuggestion.contains("gco:CharacterString");

                    LOGGER_FILL_ELEMENT.debug("####     - is or type = {}", elemType.isOrType());
                    LOGGER_FILL_ELEMENT.debug("####     - has suggestion = {}", childHasOneSuggestion);
                    LOGGER_FILL_ELEMENT.debug("####     - elem type list = {}", elemType.getElementList());
                    LOGGER_FILL_ELEMENT.debug("####     - suggested types list = {}", childSuggestion);

                    //--- There can be 'or' elements with other 'or' elements inside them.
                    //--- In this case we cannot expand the inner 'or' elements so the
                    //--- only way to solve the problem is to avoid the creation of them
                    if (
                        schema.isSimpleElement(elemName, childName) ||  // eg. gco:Decimal
                            !elemType.isOrType() ||                         // eg. gmd:EX_Extent
                            (elemType.isOrType() && (                       // eg. depends on schema-suggestions.xml
                                childHasOneSuggestion ||                    //   expand the only one suggestion - TODO - this needs improvements
                                    (childSuggestion.isEmpty() && elemType.getElementList().contains("gco:CharacterString")))
                                //   expand element which have no suggestion
                                // and have a gco:CharacterString substitute.
                                // gco:CharacterString is the default.
                            )
                        ) {
                        // Create the element
                        String name = getUnqualifiedName(childName);
                        String ns = getNamespace(childName, element, schema);
                        String prefix = getPrefix(childName);

                        Element child = new Element(name, prefix, ns);

                        // Add it to the element
                        element.addContent(child);

                        if (childHasOnlyCharacterStringSuggestion && isISOPlugin) {
                            child.addContent(isoPlugin.createBasicTypeCharacterString());
                        }

                        // Continue ....
                        fillElement(schema, sugg, element, child);
                    } else {
                        // Logging some cases to avoid
                        if (LOGGER_FILL_ELEMENT.isDebugEnabled()) {
                            if (elemType.isOrType() && isISOPlugin) {
                                if (elemType.getElementList().contains(isoPlugin.getBasicTypeCharacterStringName()) && !childHasOneSuggestion) {
                                    LOGGER_FILL_ELEMENT.debug("####   - (INNER) Requested expansion of an OR element having gco:CharacterString substitute and no suggestion: {}", element.getName());
                                } else {
                                    LOGGER_FILL_ELEMENT.debug("####   - WARNING (INNER): requested expansion of an OR element : {}", childName);
                                }
                            }
                        }
                    }
                }
            }
        } else if (isISOPlugin && type.getElementList().contains(isoPlugin.getBasicTypeCharacterStringName()) && !hasSuggestion) {
            // expand element which have no suggestion
            // and have a gco:CharacterString substitute.
            // gco:CharacterString is the default.
            LOGGER_FILL_ELEMENT.debug("####   - Requested expansion of an OR element having gco:CharacterString substitute and no suggestion: {}", element.getName());
            Element child = isoPlugin.createBasicTypeCharacterString();
            element.addContent(child);
        } else {
            // TODO: this could be supported if only one suggestion defined for an or element ?
            // It will require to get the proper namespace for the element
            LOGGER_FILL_ELEMENT.debug("####   - WARNING : requested expansion of an OR element : {}", element.getName());
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Tree expansion methods
    //---
    //--------------------------------------------------------------------------

    /**
     * Searches children of container elements for containers.
     */
    public List<Element> searchChildren(String chName, Element md, String schema) throws Exception {

        // FIXME? CHOICE_ELEMENT containers can only have one element in them
        // if there are more then the container will need to be duplicated
        // and the elements distributed? Doesn't seem to hurt so we'll leave it
        // for now........
        //

        boolean hasContent = false;
        Vector<Element> holder = new Vector<Element>();

        MetadataSchema mdSchema = scm.getSchema(schema);
        String chUQname = getUnqualifiedName(chName);
        String chPrefix = getPrefix(chName);
        String chNS = getNamespace(chName, md, mdSchema);
        Element container = new Element(chUQname, chPrefix, chNS);
        MetadataType containerType = mdSchema.getTypeInfo(chName);
        for (String elemName: containerType.getAlElements()) {
            LOGGER.debug("		-- Searching for child {}", elemName);
            List<Element> elems;
            if (edit_CHOICE_GROUP_SEQUENCE_in(elemName)) {
                elems = searchChildren(elemName, md, schema);
            } else {
                elems = filterOnQname(md.getChildren(), elemName);
            }
            for (Element elem : elems) {
                container.addContent((Element) elem.clone());
                hasContent = true;
            }
        }
        if (hasContent) {
            holder.add(container);
        } else {
            if (!chName.contains(Edit.RootChild.CHOICE)) {
                fillElement(schema, md, container);
                holder.add(container);
            }
        }
        return holder;
    }

    /**
     * Given an unexpanded tree, creates container elements and their children.
     */
    public void expandElements(String schema, Element md) throws Exception {
        // Do not process GeoNetwork element eg. validation report
        if (md.getNamespace() == Edit.NAMESPACE) {
            return;
        }
        //--- create containers and fill them with elements using a depth first
        //--- search

        @SuppressWarnings("unchecked")
        List<Element> childs = md.getChildren();
        for (Element child : childs) {
            expandElements(schema, child);
        }

        String name = md.getQualifiedName();
        String parentName = getParentNameFromChild(md);
        MetadataSchema mdSchema = scm.getSchema(schema);
        String typeName = mdSchema.getElementType(name, parentName);
        MetadataType thisType = mdSchema.getTypeInfo(typeName);

        if (thisType.hasContainers) {
            Vector<Content> holder = new Vector<Content>();

            for (String chName: thisType.getAlElements()) {
                if (edit_CHOICE_GROUP_SEQUENCE_in(chName)) {
                    List<Element> elems = searchChildren(chName, md, schema);
                    if (elems.size() > 0) {
                        holder.addAll(elems);
                    }
                } else {
                    List<Element> chElem = filterOnQname(md.getChildren(), chName);
                    for (Element elem : chElem) {
                        holder.add(elem.detach());
                    }
                }
            }
            md.removeContent();
            md.addContent(holder);
        }
    }

    /**
     * For each container element - descend and collect children.
     */
    private Vector<Object> getContainerChildren(Element md) {
        Vector<Object> result = new Vector<Object>();

        @SuppressWarnings("unchecked")
        List<Element> chChilds = md.getChildren();
        for (Element chChild : chChilds) {
            String chName = chChild.getName();
            if (edit_CHOICE_GROUP_SEQUENCE_in(chName)) {
                List<Object> moreChChilds = getContainerChildren(chChild);
                result.addAll(moreChChilds);
            } else {
                result.add(chChild.clone());
            }
        }
        return result;
    }

    /**
     * Contracts container elements.
     */
    public void contractElements(Element md) {
        //--- contract container children at each level in the XML tree

        Vector<Object> children = new Vector<Object>();
        @SuppressWarnings("unchecked")
        List<Content> childs = md.getContent();
        for (Content obj : childs) {
            if (obj instanceof Element) {
                Element mdCh = (Element) obj;
                String mdName = mdCh.getName();
                if (edit_CHOICE_GROUP_SEQUENCE_in(mdName)) {
                    if (mdCh.getChildren().size() > 0) {
                        Vector<Object> chChilds = getContainerChildren(mdCh);
                        if (chChilds.size() > 0) {
                            children.addAll(chChilds);
                        }
                    }
                } else {
                    children.add(mdCh.clone());
                }
            } else {
                children.add(obj);
            }
        }
        md.removeContent();
        md.addContent(children);

        //--- now move down to the next level

        for (Object obj : children) {
            if (obj instanceof Element) {
                contractElements((Element) obj);
            }
        }
    }

    /**
     * Does a pre-order visit enumerating each node.
     */
    private int enumerateTree(Element md, int ref, int parent) throws Exception {

        int thisRef = ref;
        int thisParent = ref;

        @SuppressWarnings("unchecked")
        List<Element> list = md.getChildren();

        for (Element child : list) {
            if (!Edit.NAMESPACE.getPrefix().equals(child.getNamespacePrefix())) {
                ref = enumerateTree(child, ref + 1, thisParent);
            }
        }

        Element elem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
        elem.setAttribute(new Attribute(Edit.Element.Attr.REF, thisRef + ""));
        elem.setAttribute(new Attribute(Edit.Element.Attr.PARENT, parent + ""));
        elem.setAttribute(new Attribute(Edit.Element.Attr.UUID, md.getQualifiedName() + "_" + UUID.randomUUID().toString()));
        md.addContent(elem);

        return ref;
    }

    /**
     * Finds the ref element with the maximum ref value and returns it.
     */
    public int findMaximumRef(Element md) {
        int iRef = 0;
        @SuppressWarnings("unchecked")
        Iterator<Element> mdIt = md.getDescendants(new ElementFilter("element"));
        while (mdIt.hasNext()) {
            Element elem = mdIt.next();
            String ref = elem.getAttributeValue("ref");
            if (ref != null) {
                int i = Integer.parseInt(ref);
                if (i > iRef) iRef = i;
            }
        }
        return iRef;
    }

    /**
     * Given a metadata, does a recursive scan adding information for editing.
     */
    public void expandTree(MetadataSchema schema, Element md) throws Exception {
        expandElement(schema, md);

        @SuppressWarnings("unchecked")
        List<Element> list = md.getChildren();

        for (Element child : list) {
            if (!Edit.NAMESPACE.getPrefix().equals(child.getNamespacePrefix())) {
                expandTree(schema, child);
            }
        }
    }

    private String getParentNameFromChild(Element child) {
        String parentName = "root";
        Element parent = child.getParentElement();
        if (parent != null) {
            parentName = parent.getQualifiedName();
        }
        return parentName;
    }

    /**
     * Adds editing information to a single element.
     */
    public void expandElement(MetadataSchema schema, Element md) throws Exception {
        LOGGER_EXPAND_ELEMENT.debug("entering expandElement()");

        String elemName = md.getQualifiedName();
        String parentName = getParentNameFromChild(md);

        LOGGER_EXPAND_ELEMENT.debug("elemName = {}", elemName);
        LOGGER_EXPAND_ELEMENT.debug("parentName = {}", parentName);

        String elemType = schema.getElementType(elemName, parentName);
        LOGGER_EXPAND_ELEMENT.debug("elemType = {}", elemType);

        Element elem = md.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
        addValues(schema, elem, elemName, parentName);

        if (schema.isSimpleElement(elemName, parentName)) {
            LOGGER_EXPAND_ELEMENT.debug("is simple element");
            return;
        }
        MetadataType type = schema.getTypeInfo(elemType);
        LOGGER_EXPAND_ELEMENT.debug("Type = {}", type);

        for (int i = 0; i < type.getElementCount(); i++) {
            String childQName = type.getElementAt(i);

            LOGGER_EXPAND_ELEMENT.debug("- childName = {}", childQName);
            if (childQName == null) continue; // schema extensions cause null types; just skip

            String childName = getUnqualifiedName(childQName);
            String childPrefix = getPrefix(childQName);
            String childNS = getNamespace(childQName, md, schema);

            LOGGER_EXPAND_ELEMENT.debug("- name      = {}", childName);
            LOGGER_EXPAND_ELEMENT.debug("- prefix    = {}", childPrefix);
            LOGGER_EXPAND_ELEMENT.debug("- namespace = {}", childNS);

            List<?> list = md.getChildren(childName, Namespace.getNamespace(childNS));
            if (list.isEmpty() && !(type.isOrType())) {
                LOGGER_EXPAND_ELEMENT.debug("- no children of this type already present");

                Element newElem = createElement(schema, elemName, childQName, childNS, type.getMinCardinAt(i), type.getMaxCardinAt(i));

                if (i == 0) insertFirst(md, newElem);
                else {
                    String prevQName = type.getElementAt(i - 1);
                    String prevName = getUnqualifiedName(prevQName);
                    String prevNS = getNamespace(prevQName, md, schema);
                    insertLast(md, prevName, prevNS, newElem);
                }
            } else {
                LOGGER_EXPAND_ELEMENT.debug("- {} children of this type already present", list.size());
                LOGGER_EXPAND_ELEMENT.debug("- min cardinality = {}", type.getMinCardinAt(i));
                LOGGER_EXPAND_ELEMENT.debug("- max cardinality = {}", type.getMaxCardinAt(i));

                for (int j = 0; j < list.size(); j++) {
                    Element listChild = (Element) list.get(j);
                    Element listElem = listChild.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
                    listElem.setAttribute(new Attribute(Edit.Element.Attr.UUID, listChild.getQualifiedName() + "_" + UUID.randomUUID().toString()));
                    listElem.setAttribute(new Attribute(Edit.Element.Attr.MIN, "" + type.getMinCardinAt(i)));
                    listElem.setAttribute(new Attribute(Edit.Element.Attr.MAX, "" + type.getMaxCardinAt(i)));

                    if (j > 0)
                        listElem.setAttribute(new Attribute(Edit.Element.Attr.UP, Edit.Value.TRUE));

                    if (j < list.size() - 1)
                        listElem.setAttribute(new Attribute(Edit.Element.Attr.DOWN, Edit.Value.TRUE));

                    if (list.size() > type.getMinCardinAt(i))
                        listElem.setAttribute(new Attribute(Edit.Element.Attr.DEL, Edit.Value.TRUE));

                    if (j < type.getMaxCardinAt(i) - 1)
                        listElem.setAttribute(new Attribute(Edit.Element.Attr.ADD, Edit.Value.TRUE));
                }
                if (list.size() < type.getMaxCardinAt(i))
                    insertLast(md, childName, childNS, createElement(schema, elemName, childQName, childNS, type.getMinCardinAt(i), type.getMaxCardinAt(i)));
            }
        }
        addAttribs(type, md, schema);
    }

    public String getUnqualifiedName(String qname) {
        int pos = qname.indexOf(':');
        if (pos < 0) return qname;
        else return qname.substring(pos + 1);
    }

    public String getPrefix(String qname) {
        int pos = qname.indexOf(':');
        if (pos < 0) return "";
        else return qname.substring(0, pos);
    }

    public String getNamespace(String qname, Element md, MetadataSchema schema) {
        // check the element first to see whether the namespace is declared locally
        String result = checkNamespaces(qname, md);
        if (!result.equals("UNKNOWN")) { return result;}

        // find root element, where namespaces *must* be declared
        Element root = md;
        while (root.getParent() != null && root.getParent() instanceof Element) {
            root = (Element) root.getParent();
        }
        result = checkNamespaces(qname, root);
        if (!result.equals("UNKNOWN")) { return result;}

        // finally if it isn't on the root element then check the list
        // namespaces we collected as we parsed the schema
        return getNamespace(qname, schema);
    }

    private String getNamespace(String qname, MetadataSchema schema) {
        // check the list of namespaces we collected as we parsed the schema
        String prefix = getPrefix(qname);
        if (!prefix.equals("")) {
            String result = schema.getNS(prefix);
            if (result != null) {
                return result;
            }
        }
        return "UNKNOWN";
    }

    private String checkNamespaces(String qname, Element md) {
        String prefix = getPrefix(qname);

        // loop on namespaces to fine the one corresponding to prefix
        Namespace rns = md.getNamespace();
        if (prefix.equals(rns.getPrefix())) return rns.getURI();
        for (Object o : md.getAdditionalNamespaces()) {
            Namespace ns = (Namespace) o;
            if (prefix.equals(ns.getPrefix())) {
                return ns.getURI();
            }
        }
        return "UNKNOWN";
    }

    private void insertFirst(Element md, Element child) {
        List<Element> list = new ArrayList(md.getChildren());
        md.removeContent();
        md.addContent(child);
        for (Element elem : list) {
            md.addContent(elem);
        }
    }

    private void insertLast(Element md, String childName, String childNS, Element child) {
        boolean added = false;

        @SuppressWarnings("unchecked")
        List<Element> list = md.getChildren();

        List<Element> v = new ArrayList<Element>();

        for (int i = 0; i < list.size(); i++) {
            Element el = list.get(i);

            v.add(el);

            if (equal(childName, childNS, el) && !added) {
                if (i == list.size() - 1 || !equal(el, list.get(i + 1))) {
                    v.add(child);
                    added = true;
                }
            }
        }

        md.removeContent();
        md.addContent(v);
    }

    private boolean equal(String childName, String childNS, Element el) {
        if (Edit.NAMESPACE.getURI().equals(el.getNamespaceURI())) {
            return CHILD.equals(el.getName())
                && childName.equals(el.getAttributeValue(NAME))
                && childNS.equals(el.getAttributeValue(NAMESPACE));
        } else
            return childName.equals(el.getName()) && childNS.equals(el.getNamespaceURI());
    }

    private boolean equal(Element el1, Element el2) {
        String elemNS1 = el1.getNamespaceURI();
        String elemNS2 = el2.getNamespaceURI();
        String geonetNS = Edit.NAMESPACE.getURI();

        if (geonetNS.equals(elemNS1) && geonetNS.equals(elemNS2)) {
            if (!CHILD.equals(el1.getName())) return false;
            if (!CHILD.equals(el2.getName())) return false;
            String name1 = el1.getAttributeValue(NAME);
            String ns1 = el1.getAttributeValue(NAMESPACE);
            String name2 = el2.getAttributeValue(NAME);
            String ns2 = el2.getAttributeValue(NAMESPACE);
            return name1.equals(name2) && ns1.equals(ns2);

        } else if (geonetNS.equals(elemNS1) && ! geonetNS.equals(elemNS2)) {
            if (!CHILD.equals(el1.getName())) return false;
            String name1 = el1.getAttributeValue(NAME);
            String ns1 = el1.getAttributeValue(NAMESPACE);
            return el2.getName().equals(name1) && el2.getNamespaceURI().equals(ns1);

        } else if (!geonetNS.equals(elemNS1) && geonetNS.equals(elemNS2)) {
            if (!CHILD.equals(el2.getName())) return false;
            String name2 = el2.getAttributeValue(NAME);
            String ns2 = el2.getAttributeValue(NAMESPACE);
            return el1.getName().equals(name2) && el1.getNamespaceURI().equals(ns2);

        } else { // if (!geonetNS.equals(elemNS1) && !geonetNS.equals(elemNS2)) {
            return el1.getName().equals(el2.getName()) && el1.getNamespaceURI().equals(el2.getNamespaceURI());
        }
    }

    /**
     * Returns MetadataType associated with an element.
     */
    public MetadataType getType(MetadataSchema mds, Element elem) throws Exception {

        String elemName = elem.getQualifiedName();
        String parentName = getParentNameFromChild(elem);

        String elemType = mds.getElementType(elemName, parentName);
        return mds.getTypeInfo(elemType);
    }

    /**
     * Creates a new element for editing - used by Ajax new element addition.
     */
    public Element createElement(String schema, Element child, Element parent) throws Exception {

        String childQName = child.getQualifiedName();

        MetadataSchema mds = scm.getSchema(schema);
        MetadataType mdt = getType(mds, parent);

        int min = -1, max = -1;

        for (int i = 0; i < mdt.getElementCount(); i++) {
            if (childQName.equals(mdt.getElementAt(i))) {
                min = mdt.getMinCardinAt(i);
                max = mdt.getMaxCardinAt(i);
            }
        }
        return createElement(mds, parent.getQualifiedName(), child.getQualifiedName(), child.getNamespaceURI(), min, max);
    }

    /**
     * Creates a new element for editing, adding all mandatory subtags.
     */
    private Element createElement(MetadataSchema schema, String parent, String qname, String childNS, int min, int max) throws Exception {

        Element child = new Element(CHILD, Edit.NAMESPACE);
        SchemaSuggestions mdSugg = scm.getSchemaSuggestions(schema.getName());

        child.setAttribute(new Attribute(NAME, getUnqualifiedName(qname)));
        child.setAttribute(new Attribute(Edit.ChildElem.Attr.PREFIX, getPrefix(qname)));
        child.setAttribute(new Attribute(NAMESPACE, childNS));
        child.setAttribute(new Attribute(Edit.ChildElem.Attr.UUID, CHILD + "_" + qname + "_" + UUID.randomUUID().toString()));
        child.setAttribute(new Attribute(Edit.ChildElem.Attr.MIN, "" + min));
        child.setAttribute(new Attribute(Edit.ChildElem.Attr.MAX, "" + max));

        String action = "replace"; // js adds new elements in place of this child
        if (!schema.isSimpleElement(qname, parent)) {
            String elemType = schema.getElementType(qname, parent);

            MetadataType type = schema.getTypeInfo(elemType);
            // Choice elements will be added if present in suggestion only.
            boolean useSuggestion = mdSugg.hasSuggestion(qname, type.getElementList());

            if (type.isOrType()) {
                // Here we handle elements with potential substitute suggested.
                // In most of the cases, elements have gco:CharacterString as one of the possible substitute.
                // gco:CharacterString is then used as a default substitute to use for those
                // elements. It could be a good idea to have that information in configuration file
                // (eg. like schema-substitute) in order to define the default substitute to use
                // for a type. TODO
                SchemaPlugin plugin = schema.getSchemaPlugin();
                boolean isISOPlugin = plugin instanceof ISOPlugin;
                ISOPlugin isoPlugin = isISOPlugin ? (ISOPlugin) plugin : null;

                if (isISOPlugin &&
                    type.getElementList().contains(
                        isoPlugin.getBasicTypeCharacterStringName()) &&
                    !useSuggestion) {
                    LOGGER.debug("OR element having gco:CharacterString substitute and no suggestion: {}", qname);

                    Element basicTypeNode = isoPlugin.createBasicTypeCharacterString();
                    Element newElem = createElement(schema, qname,
                        basicTypeNode.getQualifiedName(),
                        basicTypeNode.getNamespaceURI(), 1, 1);
                    child.addContent(newElem);
                } else {
                    action = "before"; // js adds new elements before this child
                    for (String chElem :type.getAlElements()) {
                        if (chElem.contains(Edit.RootChild.CHOICE)) {
                            List<String> chElems = recurseOnNestedChoices(schema, chElem, parent);

                            for (String chElem1 : chElems) {
                                chElem = chElem1;
                                if (!useSuggestion
                                    || (mdSugg.isSuggested(qname, chElem))) {
                                    // Add all substitute found in the schema or all suggested if suggestion
                                    createAndAddChoose(child, chElem);
                                }
                            }
                        } else {

                            if (!useSuggestion
                                || (mdSugg.isSuggested(qname, chElem))) {
                                // Add all substitute found in the schema or all suggested if suggestion
                                createAndAddChoose(child, chElem);
                            }
                        }
                    }
                }
            }
        }

        if (max == 1) action = "replace"; // force replace because one only
        child.setAttribute(new Attribute(Edit.ChildElem.Attr.ACTION, action));

        return child;
    }

    private List<String> recurseOnNestedChoices(MetadataSchema schema, String chElem, String parent) throws Exception {
        List<String> chElems = new ArrayList<String>();
        String elemType = schema.getElementType(chElem, parent);
        MetadataType type = schema.getTypeInfo(elemType);
        for (String subChElem: type.getAlElements()) {
            if (subChElem.contains(Edit.RootChild.CHOICE)) {
                List<String> subChElems = recurseOnNestedChoices(schema, subChElem, chElem);
                chElems.addAll(subChElems);
            } else {
                chElems.add(subChElem);
            }
        }
        return chElems;
    }

    private void createAndAddChoose(Element child, String chType) {
        Element choose = new Element(Edit.ChildElem.Child.CHOOSE, Edit.NAMESPACE);
        choose.setAttribute(new Attribute(Edit.Choose.Attr.NAME, chType));
        child.addContent(choose);
    }

    private void addValues(MetadataSchema schema, Element elem, String name, String parent) throws Exception {
        List<String> values = schema.getElementValues(name, parent);
        if (values != null)
            for (Object value : values) {
                Element text = new Element(Edit.Element.Child.TEXT, Edit.NAMESPACE);
                text.setAttribute(Edit.Attribute.Attr.VALUE, (String) value);

                elem.addContent(text);
            }
    }

    private void addAttribs(MetadataType type, Element md, MetadataSchema schema) {
        for (MetadataAttribute attr: type.getAlAttribs()) {

            Element attribute = new Element(Edit.RootChild.ATTRIBUTE, Edit.NAMESPACE);

            attribute.setAttribute(new Attribute(Edit.Attribute.Attr.NAME, attr.name));
            //--- add default value (if any)

            if (attr.defValue != null) {
                Element def = new Element(Edit.Attribute.Child.DEFAULT, Edit.NAMESPACE);
                def.setAttribute(Edit.Attribute.Attr.VALUE, attr.defValue);

                attribute.addContent(def);
            }

            for (String value : attr.values) {
                Element text = new Element(Edit.Attribute.Child.TEXT, Edit.NAMESPACE);
                text.setAttribute(Edit.Attribute.Attr.VALUE, value);

                attribute.addContent(text);
            }

            //--- handle 'add' and 'del' attribs

            boolean present;
            String uname = getUnqualifiedName(attr.name);
            String ns = getNamespace(attr.name, md, schema);
            String prefix = getPrefix(attr.name);
            if (!prefix.equals("")) {
                present = (md.getAttributeValue(uname, Namespace.getNamespace(prefix, ns)) != null);
                if (!present && attr.required && (attr.defValue != null)) { // Add it
                    md.setAttribute(new Attribute(uname, attr.defValue, Namespace.getNamespace(prefix, ns)));
                }
            } else {
                present = (md.getAttributeValue(attr.name) != null);
                if (!present && attr.required && (attr.defValue != null)) { // Add it
                    md.setAttribute(new Attribute(attr.name, attr.defValue));
                }
            }

            if (!present)
                attribute.setAttribute(new Attribute(Edit.Attribute.Attr.ADD, Edit.Value.TRUE));

            else if (!attr.required)
                attribute.setAttribute(new Attribute(Edit.Attribute.Attr.DEL, Edit.Value.TRUE));

            md.addContent(attribute);
        }
    }

    /**
     * If the xpath starts with the metadata root element, it's removed. Used to apply the Xpath
     * filters as the root element should not be included.
     *
     * Example:
     *
     * /gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString --> gmd:fileIdentifier/gco:CharacterString
     */
    private String cleanRootFromXPath(String xpathProperty, Element metadataRecord) {
        if (xpathProperty.startsWith("/")) {
            xpathProperty = xpathProperty.substring(1);
        }

        if (xpathProperty.startsWith(metadataRecord.getQualifiedName() + "/")) {
            xpathProperty = xpathProperty.substring(metadataRecord.getQualifiedName().length() + 1);
        }

        return xpathProperty;
    }

    private interface XPathParserLocalConstants {
        int SQBRACKET_OPEN = 84;
        int TEXT = 78;
        int NAMESPACE_SEP = 79;
        int ATTRIBUTE = 86;
        int PARENT = 83;
        int DESCENDANT = 7;
        Set<Integer> ILLEGAL_KINDS = Sets.newHashSet(PARENT, DESCENDANT);
    }

    // -- The following methods are used by services that use metadata-edit-embedded so the
    // -- classes know which element to transform

    /**
     * Special tags for updating metadata element by xpath.
     */
    public interface SpecialUpdateTags {
        /**
         * Replace the content of the target.
         */
        String REPLACE = "gn_replace";
        /**
         * Add to the target.
         */
        String ADD = "gn_add";
        /**
         * Create the target element and add.
         */
        String CREATE = "gn_create";
        /**
         * Delete the target.
         */
        String DELETE = "gn_delete";

        /**
         * Multiple target updates
         */
        String REPLACE_ALL = "gn_replace_all";
    }

    private static class SelectResult {
        private static final SelectResult ERROR = new SelectResult(null, true);

        final List<Object> results;
        final Object result;
        final boolean error;

        private SelectResult(List<Object> results, boolean error) {
            this.result = null;
            this.results = results == null ? new ArrayList<>() : results;
            this.error = error;
        }

        private SelectResult(Object result, boolean error) {
            this.result = result;
            this.results = new ArrayList<>();
            this.error = error;
        }

        private static SelectResult of(Object result) {
            return new SelectResult(result, false);
        }

        private static SelectResult of(List<Object> results) {
            return new SelectResult(results, false);
        }
    }

    private void addEditingInfo(String schema, Element md, int id, int parent) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MD before editing infomation:\n{}", Xml.getString(md));
        }
        enumerateTree(md, id, parent);
        expandTree(scm.getSchema(schema), md);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MD after editing infomation::\n{}", Xml.getString(md));
        }
    }

    private boolean edit_CHOICE_GROUP_SEQUENCE_in(String name) {
        return name.contains(Edit.RootChild.CHOICE) ||
            name.contains(Edit.RootChild.GROUP) ||
            name.contains(Edit.RootChild.SEQUENCE);
    }
}
