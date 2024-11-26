/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.editing;

import com.google.common.base.Optional;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MultilingualSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Log;
import org.jdom.*;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import java.util.*;

/**
 * Embedded Metadata Update API for AJAX Editor support
 */
public class AjaxEditUtils extends EditUtils {

    public AjaxEditUtils(ServiceContext context) {
        super(context);
    }

    /**
     * TODO javadoc.
     */
    protected static Element getMetadataFromSession(UserSession session, String id) throws ResourceNotFoundException {
        if (Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR, "Retrieving metadata from session " + session.getUserId());
        Element md = (Element) session.getProperty(Geonet.Session.METADATA_EDITING + id);
        if (md == null) {
            throw new ResourceNotFoundException(String.format(
                "Requested metadata with id '%s' is not available in current session. " +
                    "Open an editing session on this record first.", id));
        }
        md.detach();
        return md;
    }

    /**
     * Apply a list of changes to the metadata record in current editing session.
     *
     * <p> The changes are a list of KVP. A key contains at least the element identifier from the
     * meta-document. A key starting with an "X" should contain an XML fragment for the value. </p>
     * <p>
     * The following KVP combinations are allowed:
     *   <ul>
     *     <li>ElementId=ElementValue </li>
     *     <li>ElementId_AttributeName=AttributeValue</li>
     *     <li>ElementId_AttributeNamespacePrefixCOLONAttributeName=AttributeValue</li>
     *     <li>XElementId=ElementValue</li> <li>XElementId_replace=ElementValue</li>
     *     <li>XElementId_ElementName=ElementValue</li>
     *     <li>XElementId_ElementName_replace=ElementValue</li>
     *     <li>P{key}=xpath with P{key}_xml=XML snippet</li>
     *   </ul>
     * <p>
     * ElementName MUST contain "{@value EditLib#COLON_SEPARATOR}" instead of ":" for prefixed
     * elements.
     *
     * <p> When using X key ElementValue could contains many XML fragments (eg. &lt;gmd:keywords
     * .../&gt;{@value EditLib#XML_FRAGMENT_SEPARATOR}&lt;gmd:keywords .../&gt;) separated by {@link
     * EditLib#XML_FRAGMENT_SEPARATOR}. All those fragments are inserted to the last element of this type
     * in its parent if ElementName is set. If not, the element with ElementId is replaced. If
     * _replace suffix is used, then all elements having the same type than elementId are removed
     * before insertion.
     *
     * </p>
     *
     * <p>
     * <pre>
     *  _Pd2295e223:/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/
     *              gmd:citation/gmd:CI_Citation/
     *              gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue =
     * 'creation']
     *
     *  _Pd2295e223_xml:&lt;gmd:date/&gt; ... &lt;/gmd:date&gt;
     * </pre>
     * </p>
     *
     * @param id          Metadata internal identifier.
     * @param changes     List of changes to apply.
     * @param currVersion Editing version which is checked against current editing version.
     * @return The update metadata record
     */
    protected Element applyChangesEmbedded(String id,
                                           Map<String, String> changes, String currVersion) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);

        String schema = dataManager.getMetadataSchema(id);
        MetadataSchema metadataSchema = dataManager.getSchema(schema);
        EditLib editLib = dataManager.getEditLib();

        // --- check if the metadata has been modified from last time
        if (currVersion != null && !editLib.getVersion(id).equals(currVersion)) {
            Log.error(Geonet.EDITOR, "Version mismatch: had " + currVersion +
                " but expected " + editLib.getVersion(id));
            return null;
        }

        // --- get metadata from session
        Element md = getMetadataFromSession(session, id);

        // Store XML fragments to be handled after other elements update
        Map<String, String> xmlInputs = new HashMap<String, String>();
        LinkedHashMap<String, AddElemValue> xmlAndXpathInputs = new LinkedHashMap<String, AddElemValue>();

        // Preprocess
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            String originalRef = entry.getKey().trim();
            String ref = null;
            String value = entry.getValue().trim();
            String originalAttributeName = null;
            String parsedAttributeName = null;

            // Avoid empty key
            if (originalRef.equals("")) {
                continue;
            }

            // No pre processes for ref starting
            // with "P" (for XPath mode)
            // or "X" (for XML mode)
            // or "lang_" (for multilingual fields)
            // Updates for these refs are handled in next step
            if (originalRef.startsWith("X") || originalRef.startsWith("P") || originalRef.startsWith("lang_")) {
                continue;
            }

            if (refIsAttribute(originalRef)) {
                originalAttributeName = parseRefAndGetAttribute(originalRef);
                ref = parseRefAndGetNewRef(originalRef);
                Pair<Namespace, String> attributePair = parseAttributeName(originalAttributeName, EditLib.COLON_SEPARATOR, id, md, editLib);
                parsedAttributeName = attributePair.one().getPrefix() + ":" + attributePair.two();
            } else {
                continue;
            }

            String actualRef = ref != null ? ref : originalRef;
            Element el = editLib.findElement(md, actualRef);
            if (el == null) {
                Log.error(Geonet.EDITOR, EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + originalRef);
                continue;
            }
            SchemaPlugin schemaPlugin = SchemaManager.getSchemaPlugin(schema);
            schemaPlugin.processElement(el, originalRef, parsedAttributeName, value);
        }

        // --- update elements
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            String ref = entry.getKey().trim();
            String value = entry.getValue().trim();
            String attribute = null;

            // Avoid empty key
            if (ref.equals("")) {
                continue;
            }

            // Catch element starting with a X to replace XML fragments
            if (ref.startsWith("X")) {
                ref = ref.substring(1);
                xmlInputs.put(ref, value);
            } else if (ref.startsWith("P") && ref.endsWith("_xml")) {
                // P{key}=xpath works with P{key}_xml=XML snippet, see next condition
            } else if (ref.startsWith("P") && !ref.endsWith("_xml")) {
                // Catch element starting with a P for xpath update mode
                String snippet = changes.get(ref + "_xml");

                if (Log.isDebugEnabled(Geonet.EDITOR)) {
                    Log.debug(Geonet.EDITOR, "Add element by XPath: " + value);
                    Log.debug(Geonet.EDITOR, "  Snippet is : " + snippet);
                }

                if (snippet != null && !"".equals(snippet)) {
                    xmlAndXpathInputs.put(value, new AddElemValue(snippet));
                } else {
                    Log.warning(Geonet.EDITOR, "No XML snippet or value found for xpath " + value + " and element ref " + ref);
                }
            } else if (ref.startsWith("lang_")) {
                updatedLocalizedTextElement(md, schema, ref, value, editLib);
            } else {
                int at = ref.indexOf('_');
                if (at != -1) {
                    attribute = ref.substring(at + 1);
                    ref = ref.substring(0, at);
                }

                Element el = editLib.findElement(md, ref);
                if (el == null) {
                    Log.error(Geonet.EDITOR, EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);
                    continue;
                }

                // Process attribute
                if (attribute != null) {
                    Pair<Namespace, String> attInfo = parseAttributeName(attribute, EditLib.COLON_SEPARATOR, id, md, editLib);
                    String localname = attInfo.two();
                    Namespace attrNS = attInfo.one();
                    if (el.getAttribute(localname, attrNS) != null) {
                        el.setAttribute(new Attribute(localname, value, attrNS));
                    }
                } else {
                    // Process element value
                    @SuppressWarnings("unchecked")
                    List<Content> content = el.getContent();

                    for (Iterator<Content> iterator = content.iterator(); iterator.hasNext(); ) {
                        Content content2 = iterator.next();
                        if (content2 instanceof Text) {
                            iterator.remove();
                        }
                    }
                    el.addContent(value);
                }
            }
        }

        // Deals with XML fragments to insert or update
        if (!xmlInputs.isEmpty()) {
            editLib.addXMLFragments(schema, md, xmlInputs);
        }

        // Deals with XML fragments and XPath to insert or update
        if (!xmlAndXpathInputs.isEmpty()) {
            editLib.addElementOrFragmentFromXpaths(md, xmlAndXpathInputs, metadataSchema, true);
        }

        setMetadataIntoSession(session, (Element) md.clone(), id);

        // --- remove editing info
        editLib.removeEditingInfo(md);
        editLib.contractElements(md);

        return (Element) md.detach();
    }

    /**
     * Reads a ref and extract the ID part from it.
     *
     * @param ref the ref to check.
     * @return the ID part.
     */
    private String parseRefAndGetNewRef(String ref) {
        String newRef = ref;
        int underscorePosition = ref.indexOf('_');
        if (underscorePosition != -1) {
            newRef = ref.substring(0, underscorePosition);
        }
        return newRef;
    }

    /**
     * Reads a ref and extract the attribute part from it.
     *
     * @param ref the ref to check.
     * @return the attribute part or null if ref doesn't contain an attribute name.
     */
    private String parseRefAndGetAttribute(String ref) {
        String attribute = null;
        int underscorePosition = ref.indexOf('_');
        if (underscorePosition != -1) {
            attribute = ref.substring(underscorePosition + 1);
        }
        return attribute;
    }

    /**
     * Checks if a ref name represents an attribute. This kind of ref is like <code>ID_ATTRIBUTENAME</code>.
     *
     * @param ref a ref element.
     * @return true ref is an attribute, false in other case.
     */
    private boolean refIsAttribute(String ref) {
        return ref.indexOf('_') != -1;
    }

    /**
     * TODO javadoc.
     */
    private void setMetadataIntoSession(UserSession session, Element md, String id) {
        if (Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR, "Storing metadata in session " + session.getUserId());
        session.setProperty(Geonet.Session.METADATA_EDITING + id, md);
    }

    /**
     * For Ajax Editing : removes metadata from session.
     */
    public void removeMetadataEmbedded(UserSession session, String id) {
        if (Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR, "Removing metadata from session " + session.getUserId());
        session.removeProperty(Geonet.Session.METADATA_EDITING + id);
        session.removeProperty(Geonet.Session.VALIDATION_REPORT + id);
    }

    /**
     * For Ajax Editing : gets Metadata from database and places it in session.
     */
    public Element getMetadataEmbedded(ServiceContext srvContext, String id, boolean forEditing, boolean withValidationErrors) throws Exception {
        boolean keepXlinkAttributes = false;
        Element md = dataManager.getMetadata(srvContext, id, forEditing, withValidationErrors, keepXlinkAttributes);
        UserSession session = srvContext.getUserSession();
        setMetadataIntoSession(session, md, id);
        return md;
    }

    /**
     * For Ajax Editing : adds an element or an attribute to a metadata element ([add] link).
     *
     * @param session       User session.
     * @param id            Metadata identifier.
     * @param ref           Reference of the parent element to add the element.
     * @param name          Name of the element or attribute to add, with the namespace
     * @param childName     Empty for inserting element, `geonet:attribute` for attributes.
     * @return
     * @throws Exception
     */
    public synchronized List<Element> addElementEmbedded(UserSession session, String id, String ref, String name, String childName) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);
        String schema = dataManager.getMetadataSchema(id);
        //--- get metadata from session
        Element md = getMetadataFromSession(session, id);

        //--- ref is parent element so find it
        EditLib editLib = dataManager.getEditLib();
        Element el = editLib.findElement(md, ref);
        if (el == null)
            throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);

        //--- locate the geonet:element and geonet:info elements and clone for
        //--- later re-use
        Element info = null;

        if (md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE) != null) {
            info = (Element) (md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE)).clone();
            md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        }

        List<Element> children = new ArrayList<>();
        MetadataSchema mds = dataManager.getSchema(schema);

        if (childName != null) {
            if (childName.equals("geonet:attribute")) {
                Element child = null;

                String defaultValue = "";
                @SuppressWarnings("unchecked")
                List<Element> attributeDefs = el.getChildren(Edit.RootChild.ATTRIBUTE, Edit.NAMESPACE);
                for (Element attributeDef : attributeDefs) {
                    if (attributeDef != null && attributeDef.getAttributeValue(Edit.Attribute.Attr.NAME).equals(name)) {
                        Element defaultChild = attributeDef.getChild(Edit.Attribute.Child.DEFAULT, Edit.NAMESPACE);
                        if (defaultChild != null) {
                            defaultValue = defaultChild.getAttributeValue(Edit.Attribute.Attr.VALUE);
                        }
                        attributeDef.removeAttribute(Edit.Attribute.Attr.ADD);
                        attributeDef.setAttribute(new Attribute(Edit.Attribute.Attr.DEL, "true"));
                    }
                }

                Pair<Namespace, String> attInfo = parseAttributeName(name, ":", id, md, editLib);
                //--- Add new attribute with default value
                el.setAttribute(new Attribute(attInfo.two(), defaultValue, attInfo.one()));

                child = el;
                children.add(child);

            } else {
                //--- normal element
                Element child = editLib.addElement(mds, el, name);
                if (!childName.equals("")) {
                    //--- or element
                    String uChildName = editLib.getUnqualifiedName(childName);
                    String prefix = editLib.getPrefix(childName);
                    String ns = editLib.getNamespace(childName, md, mds);
                    if (prefix.equals("")) {
                        prefix = editLib.getPrefix(el.getName());
                        ns = editLib.getNamespace(el.getName(), md, mds);
                    }
                    Element orChild = new Element(uChildName, prefix, ns);
                    child.addContent(orChild);

                    //--- add mandatory sub-tags
                    editLib.fillElement(schema, child, orChild);
                }

                children.add(child);
            }
        } else {
            List<String> metadataLanguages = new ArrayList<>();
            if (mds.getSchemaPlugin() instanceof MultilingualSchemaPlugin) {
                // Metadata languages are only required if the schema plugin requires to duplicate the added
                // element for each language and the element to add is multilingual.
                // See {@link org.fao.geonet.kernel.schema.MultilingualSchemaPlugin#duplicateElementsForMultilingual()}
                metadataLanguages = ((MultilingualSchemaPlugin) mds.getSchemaPlugin()).getMetadataLanguages(md);
            }

            children = editLib.addElements(mds, el, name, metadataLanguages);
        }

        //--- now enumerate the new child (if not a simple attribute)
        if ((childName == null || !childName.equals("geonet:attribute")) && (children != null)) {
            for (Element c: children) {
                int iRef = editLib.findMaximumRef(md);
                editLib.enumerateTreeStartingAt(c, iRef + 1, Integer.parseInt(ref));
                editLib.expandTree(mds, c);
            }
        }
        if ((info != null) && (children != null)) {
            for (Element c: children) {
                //--- remove and re-attach the info element to the child
                c.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
                c.addContent((Element) info.clone());
            }
        }

          /* When adding an gmx:Anchor to an element, due to the following code gets also a gco:CharacterString in EditLib.

           Remove the gco:CharacterString subelement in this case.

          } else if (isISOPlugin &&
            type.getElementList().contains(
                isoPlugin.getBasicTypeCharacterStringName()) &&
            !hasSuggestion) {
            // expand element which have no suggestion
            // and have a gco:CharacterString substitute.
            // gco:CharacterString is the default.
            if (Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                Log.debug(Geonet.EDITORFILLELEMENT, "####   - Requested expansion of an OR element having gco:CharacterString substitute and no suggestion: " + element.getName());
            }
            Element child = isoPlugin.createBasicTypeCharacterString();
            element.addContent(child);
        */
        if (childName != null && childName.equals("gmx:Anchor") && (children != null)) {
            for (Element c: children) {
                if (c.getChild("CharacterString", ISO19139Namespaces.GCO) != null) {
                    c.removeChild("CharacterString", ISO19139Namespaces.GCO);
                }
            }
        }

        if (info != null) {
            //--- attach the info element to the metadata root)
            md.addContent((Element) info.clone());
        }

        //--- store the metadata in the session again
        setMetadataIntoSession(session, (Element) md.clone(), id);

        // Return element added
        return children;
    }

    /**
     * For Ajax Editing : removes an element from a metadata ([del] link).
     */
    public synchronized Element deleteElementEmbedded(UserSession session, String id, String ref, String parentRef) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);

        String schema = dataManager.getMetadataSchema(id);

        //--- get metadata from session
        Element md = getMetadataFromSession(session, id);

        //--- locate the geonet:info element and clone for later re-use
        Element info = (Element) (md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE)).clone();
        md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);

        //--- get element to remove
        EditLib editLib = dataManager.getEditLib();
        Element el = editLib.findElement(md, ref);

        if (el == null)
            throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);


        String uName = el.getName();
        Namespace ns = el.getNamespace();
        Element parent = el.getParentElement();
        Element result = null;
        if (parent != null) {
            int me = parent.indexOf(el);

            //--- check and see whether the element to be deleted is the last one of its kind
            Filter elFilter = new ElementFilter(uName, ns);
            if (parent.getContent(elFilter).size() == 1) {

                //--- get geonet child element with attribute name = unqualified name
                Filter chFilter = new ElementFilter(Edit.RootChild.CHILD, Edit.NAMESPACE);
                @SuppressWarnings("unchecked")
                List<Element> children = parent.getContent(chFilter);

                for (Element ch : children) {
                    String name = ch.getAttributeValue("name");
                    if (name != null && name.equals(uName)) {
                        result = (Element) ch.clone();
                        break;
                    }
                }

                // -- now delete the element as requested
                parent.removeContent(me);

                //--- existing geonet child element not present so create it and insert it
                //--- where the last element was deleted
                if (result == null) {
                    result = editLib.createElement(schema, el, parent);
                    parent.addContent(me, result);
                }

                result.setAttribute(Edit.ChildElem.Attr.PARENT, parentRef);
                result.addContent(info);
            }
            //--- if not the last one then just delete it
            else {
                parent.removeContent(me);
            }
        } else {
            throw new IllegalStateException("Element at ref = " + ref + " doesn't have a parent");
        }

        // if we don't need a child then create a geonet:null element
        if (result == null) {
            result = new Element(Edit.RootChild.NULL, Edit.NAMESPACE);
            result.addContent(info);
        }

        //--- reattach the info element to the metadata
        md.addContent((Element) info.clone());

        //--- store the metadata in the session again
        setMetadataIntoSession(session, (Element) md.clone(), id);

        return result;
    }

    /**
     * Removes attribute in embedded mode.
     *
     * @param ref Attribute identifier (eg. _169_uom).
     */
    public synchronized Element deleteAttributeEmbedded(UserSession session, String id, String ref) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);

        String[] token = ref.split("_");
        String elementId = token[1];
        String attributeName = token[2];
        Element result = new Element(Edit.RootChild.NULL, Edit.NAMESPACE);

        //--- get metadata from session
        Element md = getMetadataFromSession(session, id);

        //--- get element to remove
        EditLib editLib = dataManager.getEditLib();
        Element el = editLib.findElement(md, elementId);

        if (el != null) {
            Pair<Namespace, String> attInfo = parseAttributeName(attributeName, ":", id, md, editLib);
            el.removeAttribute(attInfo.two(), attInfo.one());
        }

        //--- store the metadata in the session again
        setMetadataIntoSession(session, (Element) md.clone(), id);

        return result;
    }

    private Pair<Namespace, String> parseAttributeName(String attributeName, String separator,
                                                       String id, Element md, EditLib editLib) throws Exception {

        Integer indexColon = attributeName.indexOf(separator);
        String localname = attributeName;
        Namespace attrNS = Namespace.NO_NAMESPACE;
        // ... with qualified name
        if (indexColon != -1) {
            String prefix = attributeName.substring(0, indexColon);
            localname = attributeName.substring(indexColon + separator.length());
            String schema = dataManager.getMetadataSchema(id);
            String namespace = editLib.getNamespace(prefix + ":" + localname, md, dataManager.getSchema(schema));
            attrNS = Namespace.getNamespace(prefix, namespace);
        }
        return Pair.write(attrNS, localname);
    }

    /**
     * For Ajax Editing : swap element with sibling ([up] and [down] links).
     */
    public synchronized void swapElementEmbedded(UserSession session, String id, String ref, boolean down) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);

        dataManager.getMetadataSchema(id);

        //--- get metadata from session
        Element md = getMetadataFromSession(session, id);

        //--- get element to swap
        EditLib editLib = dataManager.getEditLib();
        Element elSwap = editLib.findElement(md, ref);

        if (elSwap == null)
            throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);

        //--- swap the elements
        int iSwapIndex = -1;

        @SuppressWarnings("unchecked")
        List<Element> list = elSwap.getParentElement().getChildren(elSwap.getName(), elSwap.getNamespace());

        int i = -1;
        for (Element element : list) {
            i++;
            if (element == elSwap) {
                iSwapIndex = i;
                break;
            }
        }

        if (iSwapIndex == -1)
            throw new IllegalStateException("Index not found for element --> " + elSwap);

        if (down) swapElements(elSwap, list.get(iSwapIndex + 1));
        else swapElements(elSwap, list.get(iSwapIndex - 1));

        //--- store the metadata in the session again
        setMetadataIntoSession(session, (Element) md.clone(), id);

    }

    /**
     * For Ajax Editing : retrieves metadata from session and validates it.
     */
    public Element validateMetadataEmbedded(UserSession session, String id, String lang) throws Exception {
        String schema = dataManager.getMetadataSchema(id);

        //--- get metadata from session and clone it for validation
        Element realMd = getMetadataFromSession(session, id);
        Element md = (Element) realMd.clone();

        //--- remove editing info
        EditLib editLib = dataManager.getEditLib();
        editLib.removeEditingInfo(md);
        editLib.contractElements(md);
        String parentUuid = null;
        md = dataManager.updateFixedInfo(schema, Optional.of(Integer.valueOf(id)), null, md, parentUuid, UpdateDatestamp.NO, context);
        Processor.processXLink(md, this.context);

        //--- do the validation on the metadata
        return metadataValidator.doValidate(session, schema, id, md, lang, false).one();

    }

    /**
     * For Editing : adds an attribute from a metadata ([add] link). FIXME: Modify and use within
     * Ajax controls
     */
    public synchronized boolean addAttribute(String id, String ref, String name, String currVersion) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);

        Element md = xmlSerializer.select(context, id);

        //--- check if the metadata has been deleted
        if (md == null)
            return false;

        String schema = dataManager.getMetadataSchema(id);
        EditLib editLib = dataManager.getEditLib();
        editLib.expandElements(schema, md);
        editLib.enumerateTree(md);

        //--- check if the metadata has been modified from last time
        if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
            return false;

        //--- get element to add
        Element el = editLib.findElement(md, ref);

        if (el == null)
            Log.error(Geonet.DATA_MANAGER, EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);
        //throw new IllegalStateException("Element not found at ref = " + ref);

        //--- remove editing info added by previous call
        editLib.removeEditingInfo(md);

        if (el != null) {
            el.setAttribute(new Attribute(name, ""));
        }

        editLib.contractElements(md);
        String parentUuid = null;
        md = dataManager.updateFixedInfo(schema, Optional.of(Integer.valueOf(id)), null, md, parentUuid, UpdateDatestamp.NO, context);
        String changeDate = null;
        xmlSerializer.update(id, md, changeDate, false, null, context);

        dataManager.indexMetadata(id, true);

        return true;
    }

    /**
     * For Editing : removes an attribute from a metadata ([del] link). FIXME: Modify and use within
     * Ajax controls
     */
    public synchronized boolean deleteAttribute(String id, String ref, String name, String currVersion) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);

        Element md = xmlSerializer.select(context, id);

        //--- check if the metadata has been deleted
        if (md == null)
            return false;

        String schema = dataManager.getMetadataSchema(id);
        EditLib editLib = dataManager.getEditLib();
        editLib.expandElements(schema, md);
        editLib.enumerateTree(md);

        //--- check if the metadata has been modified from last time
        if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
            return false;

        //--- get element to remove
        Element el = editLib.findElement(md, ref);

        if (el == null)
            throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);

        //--- remove editing info added by previous call
        editLib.removeEditingInfo(md);

        el.removeAttribute(name);

        editLib.contractElements(md);
        String parentUuid = null;
        md = dataManager.updateFixedInfo(schema, Optional.of(Integer.valueOf(id)), null, md, parentUuid, UpdateDatestamp.NO, context);

        String changeDate = null;
        xmlSerializer.update(id, md, changeDate, false, null, context);

        dataManager.indexMetadata(id, true);

        return true;
    }
}
