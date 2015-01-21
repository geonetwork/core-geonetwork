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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.jxpath.ri.parser.Token;
import org.apache.commons.jxpath.ri.parser.XPathParser;
import org.apache.commons.jxpath.ri.parser.XPathParserConstants;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.schema.MetadataAttribute;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataType;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

/**
 * TODO javadoc.
 *
 */
public class EditLib {
    private Hashtable<String, Integer> htVersions   = new Hashtable<String, Integer>(1000);
	private SchemaManager scm;

    public static final String XML_FRAGMENT_SEPARATOR = "&&&";
    public static final String COLON_SEPARATOR = "COLON";
    public static final String MSG_ELEMENT_NOT_FOUND_AT_REF = "Element not found at ref = ";
    
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

    /**
     * Init structures.
     *
     * @param scm
     */
	public EditLib(SchemaManager scm) {
		this.scm = scm;
        htVersions.clear();
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

    /**
     * Expands a metadata adding all information needed for editing.
     *
     * @param schema
     * @param id
     * @param md
     * @return
     * @throws Exception
     */
	public String getVersionForEditing(String schema, String id, Element md) throws Exception {
		String version = getVersion(id, true) +"";
	  addEditingInfo(schema,md,1,0);
		return version;
	}

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param md
     * @param id
     * @param parent
     * @throws Exception
     */
	public void addEditingInfo(String schema, Element md, int id, int parent) throws Exception {
        if(Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR,"MD before editing infomation:\n" + Xml.getString(md));
		enumerateTree(md,id,parent);
		expandTree(scm.getSchema(schema), md);
        if(Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR,"MD after editing infomation:\n" + Xml.getString(md));
	}

    /**
     * TODO javadoc.
     *
     * @param md
     * @throws Exception
     */
	public void enumerateTree(Element md) throws Exception {
		enumerateTree(md,1,0);
	}

    /**
     * TODO javadoc.
     *
     * @param md
     * @param id
     * @param parent
     * @throws Exception
     */
	public void enumerateTreeStartingAt(Element md, int id, int parent) throws Exception {
		enumerateTree(md,id,parent);
	}

    /**
     * TODO javadoc.
     *
     * @param id
     * @return
     */
	public String getVersion(String id) {
		return Integer.toString(getVersion(id, false));
	}

    /**
     * TODO javadoc.
     *
     * @param id
     * @return
     */
	public String getNewVersion(String id) {
		return Integer.toString(getVersion(id, true));
	}

    /**
     * Given an element, creates all mandatory sub-elements. The given element should be empty.
     * @param schema
     * @param parent
     * @param md
     * @throws Exception
     */
	public void fillElement(String schema, Element parent, Element md) throws Exception {
		fillElement(scm.getSchema(schema), scm.getSchemaSuggestions(schema), parent, md);
	}

    /**
     * Given an expanded tree, removes all info added for editing and replaces choice_elements with their children.
     *
     * @param md
     */
	public void removeEditingInfo(Element md) {
		//--- purge geonet: attributes

		@SuppressWarnings("unchecked")
        List<Attribute> listAtts = md.getAttributes();
		for (int i=0; i<listAtts.size(); i++) {
			Attribute attr = listAtts.get(i);
			if (Edit.NAMESPACE.getPrefix().equals(attr.getNamespacePrefix())) {
				attr.detach();
				i--;
			}
		}

		//--- purge geonet: children
		@SuppressWarnings("unchecked")
        List<Element> list = md.getChildren();
		for (int i=0; i<list.size(); i++) {
			Element child = list.get(i);
			if (!Edit.NAMESPACE.getPrefix().equals(child.getNamespacePrefix()))
				removeEditingInfo(child);
			else {
				child.detach();
				i--;
			}
		}
	}

    /**
     * Returns the element at a given reference.
     *
     * @param md the metadata element expanded with editing info
     * @param ref the element position in a pre-order visit
     * @return
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

    /**
     * TODO javadoc.
     *
     * @param mdSchema
     * @param el
     * @param qname
     * @return
     * @throws Exception
     */
	public Element addElement(MetadataSchema mdSchema, Element el, String qname) throws Exception {
        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)){
            Log.debug(Geonet.EDITORADDELEMENT,"#### in addElement()");
            Log.debug(Geonet.EDITORADDELEMENT,"#### - parent = " + el.getName());
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child qname = " + qname);
        }

		String name   = getUnqualifiedName(qname);
		String ns     = getNamespace(qname, el, mdSchema);
		String prefix = getPrefix(qname);
		String parentName = getParentNameFromChild(el);

        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
            Log.debug(Geonet.EDITORADDELEMENT,"#### - parent name for type retrieval = " + parentName);
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child name = " + name);
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child namespace = " + ns);
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child prefix = " + prefix);
        }
		@SuppressWarnings("unchecked")
        List<Element> childS = el.getChildren();
		if (childS.size() > 0) {
			Element elChildS = childS.get(0);
			Log.debug(Geonet.EDITORADDELEMENT,"#### 	- parents first child = " + elChildS.getName());
		}

		Element child = new Element(name, prefix, ns);

		SchemaSuggestions mdSugg   = scm.getSchemaSuggestions(mdSchema.getName());

		String typeName = mdSchema.getElementType(el.getQualifiedName(),parentName);

        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT))
            Log.debug(Geonet.EDITORADDELEMENT,"#### - type name = " + typeName);

 		MetadataType type = mdSchema.getTypeInfo(typeName);

        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT))
            Log.debug(Geonet.EDITORADDELEMENT,"#### - metadata tpe = " + type);

		//--- collect all children, adding the new one at the end of the others

		Vector<Element> children = new Vector<Element>();

		for(int i=0; i<type.getElementCount(); i++) {
			List<Element> list = getChildren(el, type.getElementAt(i));

            if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT))
                Log.debug(Geonet.EDITORADDELEMENT,"####   - child of type " + type.getElementAt(i) + " list size = " + list.size());
            for (Element aChild : list) {
                children.add(aChild);
                if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT))
                    Log.debug(Geonet.EDITORADDELEMENT, "####		- add child " + aChild.toString());
            }

			if (qname.equals(type.getElementAt(i)))
				children.add(child);
		}
		//--- remove everything and then add all collected children to the element to assure a correct position for the
		// new one

		el.removeContent();
        for (Element aChildren : children) {
            el.addContent(aChildren);
        }

		//--- add mandatory sub-tags
		fillElement(mdSchema, mdSugg, el, child);

		return child;
	}
	
    /**
     * Adds XML fragment to the metadata record in the last element
     * of the type of the element in its parent.
     * 
     * @param schema The metadata schema
     * @param el The element
     * @param qname The qualified name of the element
     * @param fragment XML fragment
     * @param removeExisting Remove element of the same type before insertion
     * @throws Exception
     * @throws IllegalStateException Fail to parse the fragment.
     */
    public void addFragment(String schema, Element el, String qname, String fragment, boolean removeExisting) throws Exception {
        
        MetadataSchema mdSchema = scm.getSchema(schema);
        String parentName = getParentNameFromChild(el);
        Element fragElt;

        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT))
            Log.debug(Geonet.EDITORADDELEMENT, "Add XML fragment for element name:" + qname + ", parent: " + parentName);
        
        try {
            fragElt = Xml.loadString(fragment, false);
        }
        catch (JDOMException e) {
            Log.error(Geonet.EDITORADDELEMENT, "EditLib : Error parsing XML fragment " + fragment);
            throw new IllegalStateException("EditLib : Error when loading XML fragment, " + e.getMessage());
        }
        
        String typeName = mdSchema.getElementType(el.getQualifiedName(), parentName);
        MetadataType type = mdSchema.getTypeInfo(typeName);
        
        // --- collect all children, adding the new one at the end of the others
        Vector<Element> children = new Vector<Element>();
        
        for (int i = 0; i < type.getElementCount(); i++) {
            // Add existing children of all types
            List<Element> list = getChildren(el, type.getElementAt(i));
            if (qname.equals(type.getElementAt(i)) && removeExisting) {
                // Remove all existing children of the type of element to add
            } else {
                for (Element aList : list) {
                    children.add(aList);
                }
            }
            if (qname.equals(type.getElementAt(i)))
                children.add(fragElt);
        }
        // --- remove everything and then add all collected children to the element
        // --- to assure a correct position for the new one
        el.removeContent();
        for (Element aChildren : children) {
            el.addContent(aChildren);
        }
    }

    public void addXMLFragments(String schema, Element md, Map<String, String> xmlInputs) throws Exception, IOException,
        JDOMException {
      // Loop over each XML fragments to insert or replace
      for (Map.Entry<String, String> entry : xmlInputs.entrySet()) {
          String nodeRef = entry.getKey();
          String xmlSnippetAsString = entry.getValue();
          String nodeName = null;
          boolean replaceExisting = false;
          
          String[] nodeConfig = nodeRef.split("_");
          // Possibilities:
          // * X125
          // * X125_replace
          // * X125_gmdCOLONkeywords
          // * X125_gmdCOLONkeywords_replace
          nodeRef = nodeConfig[0];
          
          if (nodeConfig.length > 1 && nodeConfig[1] != null) {
              if (nodeConfig[1].equals("replace")) {
                  replaceExisting = true;
              } else {
                  nodeName = nodeConfig[1].replace(COLON_SEPARATOR, ":");
              }
          }
          
          if (nodeConfig.length > 2 && nodeConfig[2] != null) {
              if (nodeConfig[2].equals("replace")) {
                  replaceExisting = true;
              }
          }
          
          
          // Get element to fill
          Element el = findElement(md, nodeRef);
          if (el == null) {
              Log.error(Geonet.EDITOR, MSG_ELEMENT_NOT_FOUND_AT_REF + nodeRef);
              continue;
          }
          
          
          if (xmlSnippetAsString != null && !xmlSnippetAsString.equals("")) {
              String[] fragments = xmlSnippetAsString.split(XML_FRAGMENT_SEPARATOR);
              for (String fragment : fragments) {
                  if (nodeName != null) {
                      if(Log.isDebugEnabled(Geonet.EDITOR))
                          Log.debug(Geonet.EDITOR, "Add XML fragment; " + fragment + " to element with ref: " + nodeRef);
                      
                      addFragment(schema, el, nodeName, fragment, replaceExisting);
                  } else {
                      if(Log.isDebugEnabled(Geonet.EDITOR))
                          Log.debug(Geonet.EDITOR, "Add XML fragment; " + fragment
                              + " to element with ref: " + nodeRef + " replacing content.");
                      
                      // clean before update
                      el.removeContent();
                      fragment = addNamespaceToFragment(fragment);
                      
                      // Add content
                      Element node = Xml.loadString(fragment, false);
                      if (replaceExisting) {
                          @SuppressWarnings("unchecked")
                          List<Element> children = node.getChildren();
                          for (int i = 0; i < children.size(); i++) {
                              el.addContent(children.get(i).detach());
                          }
                          List<Attribute> attributes = node.getAttributes();
                          for (Attribute a : attributes) {
                              el.setAttribute((Attribute)a.clone());
                          }
                      } else {
                          el.addContent(node);
                      }
                  }
              }
          }
      }
    }

    /**
     * This does exactly the same thing as
     * {@link #addElementOrFragmentFromXpath(org.jdom.Element, org.fao.geonet.kernel.schema.MetadataSchema, String, AddElemValue, boolean)}
     * except that it is done multiple times, once for each element in the map
     *
     * @param metadataRecord the record to update
     * @param xmlAndXpathInputs the xpaths and new values
     * @param metadataSchema the schema of the metadata record
     * @param createXpathNodeIfNotExist if true then xpaths will be created if they don't indentify an existing element or attribute.
     *                                  Otherwise only existing xpaths will be updated.
     * @return the number of updates.
     */
    public int addElementOrFragmentFromXpaths(Element metadataRecord, Map<String, AddElemValue> xmlAndXpathInputs,
                                              MetadataSchema metadataSchema, boolean createXpathNodeIfNotExist) {


        int numUpdated = 0;
        // Loop over each XML fragments to insert or replace
        for (Map.Entry<String, AddElemValue> entry : xmlAndXpathInputs.entrySet()) {
            String xpathProperty = entry.getKey();
            AddElemValue propertyValue = entry.getValue();
            boolean updated = addElementOrFragmentFromXpath(metadataRecord, metadataSchema, xpathProperty, propertyValue,
                    createXpathNodeIfNotExist);
            if (updated) {
                numUpdated ++;
            }
        }

        return numUpdated;
    }



    private static interface XPathParserLocalConstants {
        int SQBRACKET_OPEN = 84;
        int TEXT = 78;
        int NAMESPACE_SEP = 79;
        int ATTRIBUTE = 86;
        int PARENT = 83;
        int DESCENDANT = 7;
        Set<Integer> ILLEGAL_KINDS = Sets.newHashSet(PARENT, DESCENDANT);
    }

    /**
     * Special tags for updating metadata element by xpath.
     */
    public static interface SpecialUpdateTags {
        String REPLACE = "gn_replace";
        String ADD = "gn_add";
    }


    /**
     * Update a metadata record for the xpath/value provided. The xpath (in accordance with JDOM x-path) does not start
     * with the root element for example:
     * <p/>
     * <code><pre>
     *     &lt;gmd:MD_Metadata>
     *         &lt;gmd:fileIdentifier>&lt;/gmd:fileIdentifier>
     *     &lt;gmd:MD_Metadata>
     * </pre></code>
     * <p/>
     * The xpath
     *      <pre><code>  gmd:MD_Metadata/gmd:fileIdentifier</code></pre>
     * will <b>NOT</b> select any elements.  Instead one must use the xpath:
     *      <pre><code>  gmd:fileIdentifier</code></pre>
     * to select the gmd:fileIdentifier element.
     * <p/>
     * To update the root element of the metadata use the xpath: "" (empty string)
     * <p/>
     * <p/>
     * The value could be a String to set the value of an element or
     * and XML fragment to be inserted for the element.
     * <p/>
     * If the xpath match an existing element, this element is updated.
     * Only the first one is updated if more than one match.
     * <p/>
     * <p/>
     * If it does not, each missing nodes of the xpath are created and
     * the element inserted according to the schema definition.
     * <p/>
     * If the end of the xpath is an attribute:
     * <code><pre>elem/@att</pre></code>
     * <p/>
     * Then the attribute of the element will be set instead of the text of the element.
     * <p/>
     * The rules for updating a node with Xml is as follows:
     * <ul>
     *     <li>
     *         If the xml's root element is the same as the element selected by the XPATH then node is replaced with the element.  For
     *         example:
     *         <code><pre>
     * Xpath: gmd:fileIdentifier
     * XML: &lt;gmd:fileIdentifier gco:nilReason='withheld'/>
     * Result: the gmd:fileIdentifier element in the metadata will be completely replaced with the new one.  All attributes in the metadata
     *         will be lost and replaced with the attributes in the new element.
     *         </pre></code>
     *     </li>
     *     <li>
     *         If the xml's root element == '{@value org.fao.geonet.kernel.EditLib.SpecialUpdateTags#REPLACE}' (a magic tag) then the
     *         children of that element will be replace the element selected from the metadata.
     *     </li>
     *     <li>
     *         If the xml's root element == '{@value org.fao.geonet.kernel.EditLib.SpecialUpdateTags#ADD}' (a magic tag) then the children of that element will be added to the
     *         element selected from the metadata.
     *     </li>
     *     <li>
     *         If the xml's root element != the name (and namespace) of the element selected from the metadata then the xml will replace
     *         the children of the element selected from the metadata.
     *     </li>
     * </ul>
     *
     * @param metadataRecord the metadata xml to update
     * @param metadataSchema the schema of the metadata
     * @param xpathProperty the xpath to the element to update/replace/add
     * @param value the string or xmlString to add/update/replace
     * @param createXpathNodeIfNotExist if the element identified by the xpath does not exist it will be create when this is true
     *
     * @return true if the metadata was modified
     */
    public boolean addElementOrFragmentFromXpath(Element metadataRecord, MetadataSchema metadataSchema,
                                              String xpathProperty, AddElemValue value, boolean createXpathNodeIfNotExist) {

        try {
            if (value.isXml() && xpathProperty.matches(".*@[^/\\]]+")) {
                throw new AssertionError("Cannot set Xml on an attribute.  Xpath:'"+xpathProperty+"' value: '"+value+"'");
            }
            if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
                Log.debug(Geonet.EDITORADDELEMENT, "Inserting at location " + xpathProperty + " the snippet or value " + value);
            }

            final Object propNode = trySelectNode(metadataRecord, metadataSchema, xpathProperty).result;

            if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
                Log.debug(Geonet.EDITORADDELEMENT, "XPath found in metadata: " + (propNode != null));
            }


            // If a property is not found in metadata, create it...
            if (propNode != null) {
                // Update element content with node
                if (propNode instanceof Element && value.isXml()) {
                    doAddFragmentFromXpath(metadataSchema, value.getNodeValue(), (Element) propNode);
                } else if (propNode instanceof Element && !value.isXml()) {
                    // Update element text with value
                    ((Element) propNode).setText(value.getStringValue());
                } else if (propNode instanceof Attribute && !value.isXml()) {
                    ((Attribute) propNode).setValue(value.getStringValue());
                } else {
                    return false;
                }
                
                return true;
            } else {
                if (createXpathNodeIfNotExist) {
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
                        if ((relativeXpath && (c == ')' ||  c == ']')) || (!relativeXpath && c == '[')) {
                            indexOfRequiredPortion = i + (relativeXpath ? 1 : 0);
                        }
                    }
                    if(indexOfRequiredPortion > 0) {
                        final String requiredXPath = xpathProperty.substring(0, indexOfRequiredPortion);
                        Object elem = trySelectNode(metadataRecord, metadataSchema, requiredXPath).result;
                        if (elem == null) {
                            return createAndAddFromXPath(metadataRecord, metadataSchema, requiredXPath, value);
                        } else if (elem instanceof Element) {
                            Element element = (Element) elem;

                            return createAndAddFromXPath(element, metadataSchema, xpathProperty.substring(indexOfRequiredPortion), value);
                        } else {
                            return false;
                        }
                    } else {
                        return createAndAddFromXPath(metadataRecord, metadataSchema, xpathProperty, value);
                    }
                }
            }
        } catch (JaxenException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Performs the updating of the element selected from the metadata by the xpath.
     */
    private void doAddFragmentFromXpath(MetadataSchema metadataSchema, Element newValue, Element propEl) throws Exception {

        if (newValue.getName().equals(SpecialUpdateTags.REPLACE) || newValue.getName().equals(SpecialUpdateTags.ADD)) {
            final boolean isReplace = newValue.getName().equals(SpecialUpdateTags.REPLACE);
            if (isReplace) {
                propEl.removeContent();
            }

            @SuppressWarnings("unchecked")
            List<Element> children = Lists.newArrayList(newValue.getChildren());
            for (Element child : children) {
                if (Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
                    Log.debug(Geonet.EDITORADDELEMENT, " > add " + Xml.getString(child));
                }
                child.detach();
                if (isReplace) {
                    propEl.addContent(child);
                } else {
                    final Element newElement = addElement(metadataSchema, propEl, child.getQualifiedName());
                    if (newElement.getParent() != null) {
                        propEl.setContent(propEl.indexOf(newElement), child);
                    } else if (child.getParentElement() == null) {
                        propEl.addContent(child);
            }
                }
            }
        } else  if (newValue.getName().equals(propEl.getName()) && newValue.getNamespace().equals(propEl.getNamespace())) {
            int idx = propEl.getParentElement().indexOf(propEl);
            propEl.getParentElement().setContent(idx, newValue);
        } else {
            propEl.setContent(newValue);
        }
    }

    private boolean createAndAddFromXPath(Element metadataRecord, MetadataSchema metadataSchema, String xpathProperty, AddElemValue value) throws Exception {
        if (xpathProperty.startsWith("/")) {
            xpathProperty = xpathProperty.substring(1);
        }
        if (xpathProperty.startsWith(metadataRecord.getQualifiedName()+"/")) {
            xpathProperty = xpathProperty.substring(metadataRecord.getQualifiedName().length()+1);
        }
        List<String> xpathParts = Arrays.asList(xpathProperty.split("/"));
        SelectResult rootElem = trySelectNode(metadataRecord, metadataSchema, xpathParts.get(0));

        Pair<Element, String> result;
        if (rootElem.result instanceof Element) {
            result = findLongestMatch(metadataRecord, metadataSchema, xpathParts);
        } else {
            result = Pair.read(metadataRecord, SLASH_STRING_JOINER.join(xpathParts));
        }
        final Element elementToAttachTo = result.one();
        final Element clonedMetadata = (Element) elementToAttachTo.clone();

        // Creating the element at the xpath location
        // Walk the XPath from the start until the end or the start of a filter
        // expression.
        // Collect element namespace prefix and name, check element exist and
        // create them according to schema definition.
        final XPathParser xpathParser = new XPathParser(new StringReader(clonedMetadata.getQualifiedName()+"/"+result.two()));

        // Start from the root of the metadata document
        Token currentToken = xpathParser.getNextToken();
        Token previousToken = currentToken;

        int depth = 0;
        Element currentNode = clonedMetadata;
        boolean existingElement = true;
        boolean isAttribute = false;
        String currentElementName = "";
        String currentElementNamespacePrefix = "";

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
            if (currentToken.kind == XPathParserLocalConstants.ATTRIBUTE ) {
                isAttribute = true;
            }
            // Match namespace prefix
            if (currentToken.kind == XPathParserLocalConstants.TEXT && previousToken.kind == XPathParserConstants.SLASH) {
                // get element namespace if element is text and previous was /
                // means qualified name only is supported
                currentElementNamespacePrefix = currentToken.image;
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
                    if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
                        Log.debug(Geonet.EDITORADDELEMENT,
                                "Check if " + qualifiedName + " exists in " + currentNode.getName());
                    }


                    Element nodeToCheck = currentNode.getChild(currentElementName,
                            Namespace.getNamespace(metadataSchema.getNS(currentElementNamespacePrefix)));

                    if (nodeToCheck != null) {
                        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
                            Log.debug(Geonet.EDITORADDELEMENT, " > " + qualifiedName + " found");
                        }
                        // Element found, no need to create it, continue walking the xpath.
                        currentNode = nodeToCheck;
                        existingElement &= true;
                    } else {
                        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
                            Log.debug(Geonet.EDITORADDELEMENT, " > add new node " +
                                                               qualifiedName + " inserted in " + currentNode.getName());
                        }

                        if (metadataSchema.getElementValues(qualifiedName, currentNode.getQualifiedName()) != null) {
                            currentNode = addElement(metadataSchema, currentNode, qualifiedName);
                            existingElement = false;
                        } else {
                            // element not in schema so stop!
                            return false;
                    }
                }
                }

                depth ++;
                // Reset current element props
                currentElementName = "";
                currentElementNamespacePrefix = "";
            }

            previousToken = currentToken;
            currentToken = xpathParser.getNextToken();
        }

        // The current node is an existing node or newly created one
        // Insert the XML value
        // TODO: deal with attribute ?
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
                currentNode.setAttribute(previousToken.image, value.getStringValue());
            } else {
                currentNode.setText(value.getStringValue());
            }
        }

        // update worked so now we can update original element...
        elementToAttachTo.removeContent();
        List<Content> toAdd = Lists.newArrayList(clonedMetadata.getContent());
        for (Content content : toAdd) {
            elementToAttachTo.addContent(content.detach());
        }
        return true;
    }

    private static final Joiner SLASH_STRING_JOINER = Joiner.on('/');
    @VisibleForTesting
    protected Pair<Element, String> findLongestMatch(final Element metadataRecord,
                                                     final MetadataSchema metadataSchema,
                                                     final List<String> xpathPropertyParts) {
        BitSet bitSet = new BitSet(xpathPropertyParts.size());
        return findLongestMatch(metadataRecord, metadataRecord, 0, metadataSchema,
                xpathPropertyParts.size() / 2, xpathPropertyParts, bitSet);
    }

    private Pair<Element, String> findLongestMatch(final Element metadataRecord, final Element bestMatch, final int indexOfBestMatch,
                                     final MetadataSchema metadataSchema,  final int nextIndex, final List<String> xpathPropertyParts,
                                     BitSet visited) {

        if (visited.get(nextIndex)) {
            return Pair.read(bestMatch, SLASH_STRING_JOINER.join(xpathPropertyParts.subList(indexOfBestMatch, xpathPropertyParts.size())));
        }
        visited.set(nextIndex);

        // do linear search when for last couple elements of xpath
        if (xpathPropertyParts.size() - nextIndex < 3) {
            for (int i = xpathPropertyParts.size() - 1; i > -1 ; i--) {
                final String xpath = SLASH_STRING_JOINER.join(xpathPropertyParts.subList(0, i));
                SelectResult result = trySelectNode(metadataRecord, metadataSchema, xpath);
                if (result.result instanceof Element) {
                    return Pair.read((Element) result.result, SLASH_STRING_JOINER.join(xpathPropertyParts.subList(i,
                            xpathPropertyParts.size())));
                }
            }
            return Pair.read(bestMatch, SLASH_STRING_JOINER.join(xpathPropertyParts.subList(indexOfBestMatch, xpathPropertyParts.size())));
        } else {
            final SelectResult found = trySelectNode(metadataRecord, metadataSchema, SLASH_STRING_JOINER.join(xpathPropertyParts
                    .subList(0,
                            nextIndex)));
            if (found.result instanceof Element) {
                Element newBest = (Element) found.result;
                int newIndex = nextIndex + ((xpathPropertyParts.size() - nextIndex) / 2);
                return findLongestMatch(metadataRecord, newBest, nextIndex, metadataSchema, newIndex, xpathPropertyParts, visited);
            } else if(!found.error) {
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

    private static class SelectResult {
        private static final SelectResult ERROR = new SelectResult(null, true);

        final Object result;
        final boolean error;

        private SelectResult(Object result, boolean error) {
            this.result = result;
            this.error = error;
        }
        private static SelectResult of(Object result) {
            return new SelectResult(result, false);
        }
    }

    private SelectResult trySelectNode(Element metadataRecord, MetadataSchema metadataSchema, String xpathProperty)  {
        if (xpathProperty.trim().isEmpty()) {
            return SelectResult.of(metadataRecord);
        }

        // Initialize the Xpath with all schema namespaces
        Map<String, String> mapNs = metadataSchema.getSchemaNSWithPrefix();


        try {
            JDOMXPath xpath = new JDOMXPath(xpathProperty);
            xpath.setNamespaceContext(new SimpleNamespaceContext(mapNs));
            // Select the node to update and check it exists
            return SelectResult.of(xpath.selectSingleNode(metadataRecord));
        } catch (JaxenException e) {
            Log.warning(Geonet.EDITORADDELEMENT, "An illegal xpath was used to locate an element: " + xpathProperty);
            return SelectResult.ERROR;
        }
    }

    //--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param el
     * @param qname
     * @return
     */
	private List<Element> getChildren(Element el, String qname) {
		Vector<Element> result = new Vector<Element>();

		@SuppressWarnings("unchecked")
        List<Element> children = el.getChildren();

        for (Element child : children) {
            if (child.getQualifiedName().equals(qname)) {
                result.add(child);
            }
        }
		return result;
	}

    /**
     * Returns the version of a metadata, incrementing it if necessary.
     *
     * @param id
     * @param increment
     * @return
     */
	private synchronized int getVersion(String id, boolean increment) {
		Integer inVer = htVersions.get(id);

		if (inVer == null)
			inVer = 1;

		if (increment)
			inVer = inVer + 1;

		htVersions.put(id, inVer);

		return inVer;
	}

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param sugg
     * @param parent
     * @param element
     * @throws Exception
     */
	private void fillElement(MetadataSchema schema, SchemaSuggestions sugg, Element parent, Element element) throws Exception {
        String parentName = parent.getQualifiedName();
        fillElement(schema, sugg, parentName, element);
    }

    /**
     * TODO javadoc.
     *
     * @param schema    The metadata schema
     * @param sugg  The suggestion configuration for the schema
     * @param parentName  The name of the parent
     * @param element    The element to fill
     * 
     * @throws Exception
     */
    private void fillElement(MetadataSchema schema, SchemaSuggestions sugg, String parentName, Element element) throws Exception {
        String elemName = element.getQualifiedName();
        
        boolean isSimpleElement = schema.isSimpleElement(elemName,parentName);
        
        if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
            Log.debug(Geonet.EDITORFILLELEMENT,"#### Entering fillElement()");
            Log.debug(Geonet.EDITORFILLELEMENT,"#### - elemName = " + elemName);
            Log.debug(Geonet.EDITORFILLELEMENT,"#### - parentName = " + parentName);
            Log.debug(Geonet.EDITORFILLELEMENT,"#### - isSimpleElement(" + elemName + ") = " + isSimpleElement);
        }
        
        
        // Nothing to fill - eg. gco:CharacterString
        if (isSimpleElement) {
            return;
        }
        
        MetadataType type = schema.getTypeInfo(schema.getElementType(elemName, parentName));
        boolean hasSuggestion = sugg.hasSuggestion(elemName, type.getElementList());
//        List<String> elementSuggestion = sugg.getSuggestedElements(elemName);
//        boolean hasSuggestion = elementSuggestion.size() != 0;
        
        
        if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
            Log.debug(Geonet.EDITORFILLELEMENT,"#### - Type:");
            Log.debug(Geonet.EDITORFILLELEMENT,"####   - name = " + type.getName());
            Log.debug(Geonet.EDITORFILLELEMENT,"####   - # attributes = " + type.getAttributeCount());
            Log.debug(Geonet.EDITORFILLELEMENT,"####   - # elements = " + type.getElementCount());
            Log.debug(Geonet.EDITORFILLELEMENT,"####   - # isOrType = " + type.isOrType());
            Log.debug(Geonet.EDITORFILLELEMENT,"####   - type = " + type);
            Log.debug(Geonet.EDITORFILLELEMENT,"#### - Has suggestion = " + hasSuggestion);
        }
        
        
        //-----------------------------------------------------------------------
        //--- handle attributes if mandatory or suggested
        //
        for(int i=0; i<type.getAttributeCount(); i++) {
            MetadataAttribute attr = type.getAttributeAt(i);
            
            if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                Log.debug(Geonet.EDITORFILLELEMENT,"####   - " + i + " attribute = " + attr.name);
                Log.debug(Geonet.EDITORFILLELEMENT,"####     - required = " + attr.required);
                Log.debug(Geonet.EDITORFILLELEMENT,"####     - suggested = "+sugg.isSuggested(elemName, attr.name));
            }
            
            if (attr.required || sugg.isSuggested(elemName, attr.name)) {
                String value = "";
                
                if (attr.defValue != null) {
                    value = attr.defValue;
                    if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                        Log.debug(Geonet.EDITORFILLELEMENT,"####     - value = " + attr.defValue);
                    }
                }
                
                String uname = getUnqualifiedName(attr.name);
                String ns     = getNamespace(attr.name, element, schema);
                String prefix = getPrefix(attr.name);
                if (!prefix.equals(""))
                    element.setAttribute(new Attribute(uname, value, Namespace.getNamespace(prefix,ns)));
                else
                    element.setAttribute(new Attribute(uname, value));
            }
        }
        
        
        //-----------------------------------------------------------------------
        //--- add mandatory children
        //
        //     isOrType if element has substitutes and one of them should be chosen
        if (!type.isOrType()) {
            for(int i=0; i<type.getElementCount(); i++) {
                String childName = type.getElementAt(i);
                boolean childIsMandatory = type.getMinCardinAt(i) > 0;
                boolean childIsSuggested = sugg.isSuggested(elemName, childName);
                
                if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                    Log.debug(Geonet.EDITORFILLELEMENT,"####   - " + i + " element = " + childName);
                    Log.debug(Geonet.EDITORFILLELEMENT,"####     - suggested = " + childIsSuggested);
                    Log.debug(Geonet.EDITORFILLELEMENT,"####     - is mandatory = " + childIsMandatory);
                }
                
                
                
                if (childIsMandatory || childIsSuggested) {
                    
                    MetadataType elemType = schema.getTypeInfo(schema.getElementType(childName, elemName));
                    List<String> childSuggestion = sugg.getSuggestedElements(childName);
										boolean childHasOneSuggestion = sugg.hasSuggestion(childName, elemType.getElementList()) && (CollectionUtils.intersection(elemType.getElementList(),childSuggestion).size()==1);
                    boolean childHasOnlyCharacterStringSuggestion = childSuggestion.size() == 1 && childSuggestion.contains("gco:CharacterString");
                    
                    if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                        Log.debug(Geonet.EDITORFILLELEMENT,"####     - is or type = "+ elemType.isOrType());
                        Log.debug(Geonet.EDITORFILLELEMENT,"####     - has suggestion = "+ childHasOneSuggestion);
                        Log.debug(Geonet.EDITORFILLELEMENT,"####     - elem type list = " + elemType.getElementList());
                        Log.debug(Geonet.EDITORFILLELEMENT,"####     - suggested types list = " + sugg.getSuggestedElements(childName));
                    }
                    
                    //--- There can be 'or' elements with other 'or' elements inside them.
                    //--- In this case we cannot expand the inner 'or' elements so the
                    //--- only way to solve the problem is to avoid the creation of them
                    if (
                        schema.isSimpleElement(elemName, childName) ||  // eg. gco:Decimal
                        !elemType.isOrType() ||                         // eg. gmd:EX_Extent
                        (elemType.isOrType() && (                       // eg. depends on schema-suggestions.xml
                            childHasOneSuggestion ||                    //   expand the only one suggestion - TODO - this needs improvements
                            (childSuggestion.size() == 0 && elemType.getElementList().contains("gco:CharacterString")))
                                                                        //   expand element which have no suggestion
                                                                        // and have a gco:CharacterString substitute.
                                                                        // gco:CharacterString is the default.
                        )
                    ) {
                        // Create the element
                        String name   = getUnqualifiedName(childName);
                        String ns     = getNamespace(childName, element, schema);
                        String prefix = getPrefix(childName);
                        
                        Element child = new Element(name, prefix, ns);
                        
                        // Add it to the element
                        element.addContent(child);
                        
                        if (childHasOnlyCharacterStringSuggestion) {
                            child.addContent(new Element("CharacterString", Namespaces.GCO));
                        }
                        
                        // Continue ....
                        fillElement(schema, sugg, element, child);
                    } else {
                        // Logging some cases to avoid
                        if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                            if (elemType.isOrType()) {
                                 if (elemType.getElementList().contains("gco:CharacterString") 
                                         && !childHasOneSuggestion) {
                                    Log.debug(Geonet.EDITORFILLELEMENT,"####   - (INNER) Requested expansion of an OR element having gco:CharacterString substitute and no suggestion: " + element.getName());
                                 } else {
                                    Log.debug(Geonet.EDITORFILLELEMENT,"####   - WARNING (INNER): requested expansion of an OR element : " +childName);
                                }
                            }
                        }
                    }
                }
            }
        } else if (type.getElementList().contains("gco:CharacterString") && !hasSuggestion) {
            // expand element which have no suggestion
            // and have a gco:CharacterString substitute.
            // gco:CharacterString is the default.
            if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                Log.debug(Geonet.EDITORFILLELEMENT, "####   - Requested expansion of an OR element having gco:CharacterString substitute and no suggestion: " + element.getName());
            }
            Element child = new Element("CharacterString", Namespaces.GCO);
            element.addContent(child);
        } else {
            // TODO: this could be supported if only one suggestion defined for an or element ?
            // It will require to get the proper namespace for the element
            if(Log.isDebugEnabled(Geonet.EDITORFILLELEMENT)) {
                Log.debug(Geonet.EDITORFILLELEMENT, "####   - WARNING : requested expansion of an OR element : " + element.getName());
            }
        }
    }

	//--------------------------------------------------------------------------
	//---
	//--- Tree expansion methods
	//---
	//--------------------------------------------------------------------------

    /**
     * Searches children of container elements for containers.
     *
     * @param chName
     * @param md
     * @param schema
     * @return
     * @throws Exception
     */
	public List<Element> searchChildren(String chName, Element md, String schema) throws Exception	{

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
		String chNS     = getNamespace(chName, md, mdSchema);
		Element container = new Element(chUQname, chPrefix, chNS);
		MetadataType containerType = mdSchema.getTypeInfo(chName);
		for (int k=0;k<containerType.getElementCount();k++) {	
			String elemName = containerType.getElementAt(k);
            if(Log.isDebugEnabled(Geonet.EDITOR))
                Log.debug(Geonet.EDITOR,"		-- Searching for child "+elemName);
			List<Element> elems;
			if (elemName.contains(Edit.RootChild.GROUP)||
					elemName.contains(Edit.RootChild.SEQUENCE)||
					elemName.contains(Edit.RootChild.CHOICE)) {
				elems = searchChildren(elemName,md,schema);
			} else { 
				elems = getChildren(md,elemName);
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
				fillElement(schema,md,container);
				holder.add(container);
			}
		}
		return holder;
	}

    /**
     * Given an unexpanded tree, creates container elements and their children.
     *
     * @param schema
     * @param md
     * @throws Exception
     */
	public void expandElements(String schema, Element md) throws Exception {

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
		String typeName = mdSchema.getElementType(name,parentName);
		MetadataType thisType = mdSchema.getTypeInfo(typeName);

		if (thisType.hasContainers) {
			Vector<Content> holder = new Vector<Content>();
			
			for (int i=0;i<thisType.getElementCount();i++) {
				String chName = thisType.getElementAt(i);
				if (chName.contains(Edit.RootChild.CHOICE)||
						chName.contains(Edit.RootChild.GROUP)||
						chName.contains(Edit.RootChild.SEQUENCE)) {
					List<Element> elems = searchChildren(chName,md,schema);
					if (elems.size() > 0) {
						holder.addAll(elems);
					}
				} else {
					List<Element> chElem = getChildren(md,chName);
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
     * @param md
     * @return
     */
	private Vector<Object> getContainerChildren(Element md) {
		Vector<Object> result = new Vector<Object>();

		@SuppressWarnings("unchecked")
        List<Element> chChilds = md.getChildren();
        for (Element chChild : chChilds) {
            String chName = chChild.getName();
            if (chName.contains(Edit.RootChild.CHOICE) ||
                    chName.contains(Edit.RootChild.GROUP) ||
                    chName.contains(Edit.RootChild.SEQUENCE)) {
                List<Object> moreChChilds = getContainerChildren(chChild);
                result.addAll(moreChChilds);
            }
            else {
                result.add(chChild.clone());
            }
        }
		return result;
	}

    /**
     * Contracts container elements.
     *
     * @param md
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
                if (mdName.contains(Edit.RootChild.CHOICE) ||
                        mdName.contains(Edit.RootChild.GROUP) ||
                        mdName.contains(Edit.RootChild.SEQUENCE)) {
                    if (mdCh.getChildren().size() > 0) {
                        Vector<Object> chChilds = getContainerChildren(mdCh);
                        if (chChilds.size() > 0) {
                            children.addAll(chChilds);
                        }
                    }
                }
                else {
                    children.add(mdCh.clone());
                }
            }
            else {
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
     *
     * @param md
     * @param ref
     * @param parent
     * @return
     * @throws Exception
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
		elem.setAttribute(new Attribute(Edit.Element.Attr.REF, thisRef +""));
		elem.setAttribute(new Attribute(Edit.Element.Attr.PARENT, parent +""));
		elem.setAttribute(new Attribute(Edit.Element.Attr.UUID, md.getQualifiedName()+"_"+UUID.randomUUID().toString()));
		md.addContent(elem);

		return ref;
	}

    /**
     * Finds the ref element with the maximum ref value and returns it.
     *
     * @param md
     * @return
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
     *
     * @param schema
     * @param md
     * @throws Exception
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

    /**
     * TODO javadoc.
     *
     * @param child
     * @return
     */
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
     *
     * @param schema
     * @param md
     * @throws Exception
     */
	public void expandElement(MetadataSchema schema, Element md) throws Exception {
        if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT))
            Log.debug(Geonet.EDITOREXPANDELEMENT,"entering expandElement()");

		String elemName = md.getQualifiedName();
		String parentName = getParentNameFromChild(md);

        if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT)) {
            Log.debug(Geonet.EDITOREXPANDELEMENT,"elemName = " + elemName);
            Log.debug(Geonet.EDITOREXPANDELEMENT,"parentName = " + parentName);
        }

		String elemType = schema.getElementType(elemName,parentName);
        if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT))
            Log.debug(Geonet.EDITOREXPANDELEMENT,"elemType = " + elemType);

		Element elem = md.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
		addValues(schema, elem, elemName, parentName);

		if (schema.isSimpleElement(elemName,parentName))
		{
            if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT))
                Log.debug(Geonet.EDITOREXPANDELEMENT,"is simple element");
			return;
		}
		MetadataType type = schema.getTypeInfo(elemType);
        if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT))
            Log.debug(Geonet.EDITOREXPANDELEMENT,"Type = "+type);

		for (int i=0; i<type.getElementCount(); i++) {
			String childQName = type.getElementAt(i);

            if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT))
                Log.debug(Geonet.EDITOREXPANDELEMENT,"- childName = " + childQName);
			if (childQName == null) continue; // schema extensions cause null types; just skip

			String childName   = getUnqualifiedName(childQName);
			String childPrefix = getPrefix(childQName);
			String childNS     = getNamespace(childQName, md, schema);

            if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT)) {
                Log.debug(Geonet.EDITOREXPANDELEMENT,"- name      = " + childName);
                Log.debug(Geonet.EDITOREXPANDELEMENT,"- prefix    = " + childPrefix);
                Log.debug(Geonet.EDITOREXPANDELEMENT,"- namespace = " + childNS);
            }

			List<?> list = md.getChildren(childName, Namespace.getNamespace(childNS));
			if (list.size() == 0 && !(type.isOrType())) {
                if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT))
                    Log.debug(Geonet.EDITOREXPANDELEMENT,"- no children of this type already present");

				Element newElem = createElement(schema, elemName, childQName, childNS, type.getMinCardinAt(i), type.getMaxCardinAt(i));

				if (i == 0)	insertFirst(md, newElem);
				else {
					String prevQName = type.getElementAt(i-1);
					String prevName = getUnqualifiedName(prevQName);
					String prevNS   = getNamespace(prevQName, md, schema);
					insertLast(md, prevName, prevNS, newElem);
				}
			} else {
                if(Log.isDebugEnabled(Geonet.EDITOREXPANDELEMENT)){
                    Log.debug(Geonet.EDITOREXPANDELEMENT,"- " + list.size() + " children of this type already present");
                    Log.debug(Geonet.EDITOREXPANDELEMENT,"- min cardinality = " + type.getMinCardinAt(i));
                    Log.debug(Geonet.EDITOREXPANDELEMENT,"- max cardinality = " + type.getMaxCardinAt(i));
                }


				for (int j=0; j<list.size(); j++) {
					Element listChild = (Element) list.get(j);
					Element listElem  = listChild.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
					listElem.setAttribute(new Attribute(Edit.Element.Attr.UUID, listChild.getQualifiedName()+"_"+UUID.randomUUID().toString()));
					listElem.setAttribute(new Attribute(Edit.Element.Attr.MIN, ""+type.getMinCardinAt(i)));
					listElem.setAttribute(new Attribute(Edit.Element.Attr.MAX, ""+type.getMaxCardinAt(i)));

					if (j > 0)
						listElem.setAttribute(new Attribute(Edit.Element.Attr.UP, Edit.Value.TRUE));

					if (j<list.size() -1)
						listElem.setAttribute(new Attribute(Edit.Element.Attr.DOWN, Edit.Value.TRUE));

					if (list.size() > type.getMinCardinAt(i))
						listElem.setAttribute(new Attribute(Edit.Element.Attr.DEL, Edit.Value.TRUE));

					if (j < type.getMaxCardinAt(i)-1) 
						listElem.setAttribute(new Attribute(Edit.Element.Attr.ADD, Edit.Value.TRUE));
				}
				if (list.size() < type.getMaxCardinAt(i))
					insertLast(md, childName, childNS, createElement(schema, elemName, childQName, childNS, type.getMinCardinAt(i), type.getMaxCardinAt(i)));
			}
		}
		addAttribs(type, md, schema);
	}

    /**
     * TODO javadoc.
     *
     * @param qname
     * @return
     */
	public String getUnqualifiedName(String qname) {
		int pos = qname.indexOf(':');
		if (pos < 0) return qname;
		else         return qname.substring(pos + 1);
	}

    /**
     * TODO javadoc.
     *
     * @param qname
     * @return
     */
	public String getPrefix(String qname) {
		int pos = qname.indexOf(':');
		if (pos < 0) return "";
		else         return qname.substring(0, pos);
	}

    /**
     * TODO javadoc.
     *
     * @param qname
     * @param md
     * @param schema
     * @return
     */
	public String getNamespace(String qname, Element md, MetadataSchema schema) {
		// check the element first to see whether the namespace is
		// declared locally
		String result = checkNamespaces(qname,md);
		if (result.equals("UNKNOWN")) {

			// find root element, where namespaces *must* be declared
			Element root = md;
			while (root.getParent() != null && root.getParent() instanceof Element) root = (Element)root.getParent();
			result = checkNamespaces(qname,root);
		
			// finally if it isn't on the root element then check the list
			// namespaces we collected as we parsed the schema
			if (result.equals("UNKNOWN")) {
				String prefix = getPrefix(qname);
				if (!prefix.equals("")) {
					result = schema.getNS(prefix);
					if (result == null) result="UNKNOWN";
				} else result="UNKNOWN";
			}
		}
		return result;
	}

    /**
     * TODO javadoc.
     *
     * @param qname
     * @param schema
     * @return
     */
	public String getNamespace(String qname, MetadataSchema schema) {
		// check the list of namespaces we collected as we parsed the schema
		String result;
		String prefix = getPrefix(qname);
		if (!prefix.equals("")) {
			result = schema.getNS(prefix);
			if (result == null) result="UNKNOWN";
		} else result="UNKNOWN";
		return result;
	}

    /**
     * TODO javadoc.
     *
     * @param qname
     * @param md
     * @return
     */
	public String checkNamespaces(String qname, Element md) {
		// get prefix
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

    /**
     * TODO javadoc.
     *
     * @param md
     * @param child
     */
	private void insertFirst(Element md, Element child) {
		Vector<Element> v = new Vector<Element>();
		v.add(child);

		@SuppressWarnings("unchecked")
        List<Element> list = md.getChildren();

        for (Element elem : list) {
            v.add(elem);
        }

		//---

		md.removeContent();

        for (Element aV : v) {
            md.addContent(aV);
        }
	}

    /**
     * TODO javadoc.
     *
     * @param md
     * @param childName
     * @param childNS
     * @param child
     */
	private void insertLast(Element md, String childName, String childNS, Element child) {
		boolean added = false;

		@SuppressWarnings("unchecked")
        List<Element> list = md.getChildren();

		List<Element> v = new ArrayList<Element>();

		for(int i=0; i<list.size(); i++)
		{
			Element el = list.get(i);

			v.add(el);

			if (equal(childName, childNS, el) && !added)
			{
				if (i == list.size() -1)
				{
					v.add(child);
					added = true;
				}
				else
				{
					Element elNext = list.get(i+1);

					if (!equal(el, elNext))
					{
						v.add(child);
						added = true;
					}
				}
			}
		}

		md.removeContent();

        for (Element aV : v) {
            md.addContent(aV);
        }
	}

    /**
     * TODO javadoc.
     *
     * @param childName
     * @param childNS
     * @param el
     * @return
     */
	private boolean equal(String childName, String childNS, Element el) {
		if (Edit.NAMESPACE.getURI().equals(el.getNamespaceURI())) {
            return Edit.RootChild.CHILD.equals(el.getName())
                    && childName.equals(el.getAttributeValue(Edit.ChildElem.Attr.NAME))
                    && childNS.equals(el.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE));
		}
		else
			return childName.equals(el.getName()) && childNS.equals(el.getNamespaceURI());
	}

    /**
     * TODO javadoc.
     *
     * @param el1
     * @param el2
     * @return
     */
	private boolean equal(Element el1, Element el2) {
		String elemNS1 = el1.getNamespaceURI();
		String elemNS2 = el2.getNamespaceURI();

		if (Edit.NAMESPACE.getURI().equals(elemNS1)) {
			if (Edit.NAMESPACE.getURI().equals(elemNS2)) {
				//--- both are geonet:child elements

				if (!Edit.RootChild.CHILD.equals(el1.getName()))
					return false;

				if (!Edit.RootChild.CHILD.equals(el2.getName()))
					return false;

				String name1 = el1.getAttributeValue(Edit.ChildElem.Attr.NAME);
				String name2 = el2.getAttributeValue(Edit.ChildElem.Attr.NAME);

				String ns1 = el1.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE);
				String ns2 = el2.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE);

				return name1.equals(name2) && ns1.equals(ns2);
			}
			else {
				//--- el1 is a geonet:child, el2 is not

				if (!Edit.RootChild.CHILD.equals(el1.getName()))
					return false;

				String name1 = el1.getAttributeValue(Edit.ChildElem.Attr.NAME);
				String ns1   = el1.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE);

				return el2.getName().equals(name1) && el2.getNamespaceURI().equals(ns1);
			}
		}
		else {
			if (Edit.NAMESPACE.getURI().equals(elemNS2)) {
				//--- el2 is a geonet:child, el1 is not

				if (!Edit.RootChild.CHILD.equals(el2.getName()))
					return false;

				String name2 = el2.getAttributeValue(Edit.ChildElem.Attr.NAME);
				String ns2   = el2.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE);

				return el1.getName().equals(name2) && el1.getNamespaceURI().equals(ns2);
			}
			else {
				//--- both not geonet:child elements
				return el1.getName().equals(el2.getName()) && el1.getNamespaceURI().equals(el2.getNamespaceURI());
			}
		}
	}

    /**
     * Returns MetadataType associated with an element.
     *
     * @param mds
     * @param elem
     * @return
     * @throws Exception
     */
	public MetadataType getType(MetadataSchema mds, Element elem) throws Exception {

		String elemName = elem.getQualifiedName();
		String parentName = getParentNameFromChild(elem);

		String elemType = mds.getElementType(elemName,parentName);
		return mds.getTypeInfo(elemType);
	}

    /**
     * Creates a new element for editing - used by Ajax new element addition.
     * @param schema
     * @param child
     * @param parent
     * @return
     * @throws Exception
     */
	public Element createElement(String schema, Element child, Element parent) throws Exception {

		String childQName = child.getQualifiedName();

		MetadataSchema mds = scm.getSchema(schema);
		MetadataType mdt = getType(mds, parent);
		
		int min = -1, max = -1;

		for (int i=0; i<mdt.getElementCount(); i++) {
			if (childQName.equals(mdt.getElementAt(i))) {
				min = mdt.getMinCardinAt(i);
				max = mdt.getMaxCardinAt(i);
			}
		}
		return createElement(mds,parent.getQualifiedName(),child.getQualifiedName(), child.getNamespaceURI(), min, max);
	}

    /**
     * Creates a new element for editing, adding all mandatory subtags.
     *
     * @param schema
     * @param parent
     * @param qname
     * @param childNS
     * @param min
     * @param max
     * @return
     * @throws Exception
     */
	private Element createElement(MetadataSchema schema, String parent, String qname, String childNS, int min, int max) throws Exception {

		Element child = new Element(Edit.RootChild.CHILD, Edit.NAMESPACE);
		SchemaSuggestions mdSugg   = scm.getSchemaSuggestions(schema.getName());
		
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.NAME, getUnqualifiedName(qname)));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.PREFIX, getPrefix(qname)));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.NAMESPACE, childNS));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.UUID, Edit.RootChild.CHILD+"_"+qname+"_"+UUID.randomUUID().toString()));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.MIN, ""+min));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.MAX, ""+max));

		String action = "replace"; // js adds new elements in place of this child
		if (!schema.isSimpleElement(qname,parent)) {
			String elemType = schema.getElementType(qname,parent);

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
				if (type.getElementList().contains("gco:CharacterString") && !useSuggestion) {
                    if(Log.isDebugEnabled(Geonet.EDITOR))
                        Log.debug(Geonet.EDITOR,"OR element having gco:CharacterString substitute and no suggestion: " + qname);

					Element newElem = createElement(schema, qname,
							"gco:CharacterString",
                            "http://www.isotc211.org/2005/gco", 1, 1);
					child.addContent(newElem);
				} else {
					action = "before"; // js adds new elements before this child
					for(int l=0; l<type.getElementCount(); l++) {
						String chElem = type.getElementAt(l);
						if (chElem.contains(Edit.RootChild.CHOICE)) {
							List<String> chElems = recurseOnNestedChoices(schema,chElem,parent);

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
									|| (mdSugg.isSuggested(qname, chElem))){
								// Add all substitute found in the schema or all suggested if suggestion
								createAndAddChoose(child,chElem);
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

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param chElem
     * @param parent
     * @return
     * @throws Exception
     */
	private List<String> recurseOnNestedChoices(MetadataSchema schema,String chElem,String parent) throws Exception {
		List<String> chElems = new ArrayList<String>();
		String elemType = schema.getElementType(chElem,parent);
		MetadataType type = schema.getTypeInfo(elemType);
		for(int l=0; l<type.getElementCount(); l++) {
			String subChElem = type.getElementAt(l);
			if (subChElem.contains(Edit.RootChild.CHOICE)) {
				List<String> subChElems = recurseOnNestedChoices(schema,subChElem,chElem);
				chElems.addAll(subChElems);
			}
			else { chElems.add(subChElem); }
		}
		return chElems;
	}

    /**
     * TODO javadoc.
     *
     * @param child
     * @param chType
     */
	private void createAndAddChoose(Element child,String chType) {
		Element choose = new Element(Edit.ChildElem.Child.CHOOSE, Edit.NAMESPACE);
		choose.setAttribute(new Attribute(Edit.Choose.Attr.NAME, chType));
		child.addContent(choose);
	}

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param elem
     * @param name
     * @param parent
     * @throws Exception
     */
	private void addValues(MetadataSchema schema, Element elem, String name, String parent) throws Exception {
		List<String> values = schema.getElementValues(name,parent);
		if (values != null)
            for (Object value : values) {
                Element text = new Element(Edit.Element.Child.TEXT, Edit.NAMESPACE);
                text.setAttribute(Edit.Attribute.Attr.VALUE, (String) value);

                elem.addContent(text);
            }
	}

    /**
     * TODO javadoc.
     *
     * @param type
     * @param md
     * @param schema
     */
	private void addAttribs(MetadataType type, Element md, MetadataSchema schema) {
		for(int i=0; i<type.getAttributeCount(); i++) {
			MetadataAttribute attr = type.getAttributeAt(i);

			Element attribute = new Element(Edit.RootChild.ATTRIBUTE, Edit.NAMESPACE);

			attribute.setAttribute(new Attribute(Edit.Attribute.Attr.NAME, attr.name));
			//--- add default value (if any)

			if (attr.defValue != null) {
				Element def = new Element(Edit.Attribute.Child.DEFAULT, Edit.NAMESPACE);
				def.setAttribute(Edit.Attribute.Attr.VALUE, attr.defValue);

				attribute.addContent(def);
			}

			for(String value : attr.values) {
                Element text = new Element(Edit.Attribute.Child.TEXT, Edit.NAMESPACE);
				text.setAttribute(Edit.Attribute.Attr.VALUE, value);

				attribute.addContent(text);
			}

			//--- handle 'add' and 'del' attribs

			boolean present;
			String uname = getUnqualifiedName(attr.name);
      String ns     = getNamespace(attr.name, md, schema);
      String prefix = getPrefix(attr.name);
      if (!prefix.equals("")) {
				present = (md.getAttributeValue(uname,Namespace.getNamespace(prefix,ns)) != null);
				if (!present && attr.required && (attr.defValue != null)) { // Add it
					md.setAttribute(new Attribute(uname,attr.defValue,Namespace.getNamespace(prefix,ns)));
				}
			} else {
				present = (md.getAttributeValue(attr.name) != null);
				if (!present && attr.required && (attr.defValue != null)) { // Add it
					md.setAttribute(new Attribute(attr.name,attr.defValue));
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
     * Adds missing namespace (ie. GML) to XML inputs. It should be done by the client side
     * but add a check in here.
     *
     * @param fragment 		The fragment to be checked and processed.
     *
     * @return 				The updated fragment.
     */
    public static String addNamespaceToFragment(String fragment) {
        //add the gml namespace if its missing
        if (fragment.contains("<gml:") && !fragment.contains("xmlns:gml=\"")) {
            if(Log.isDebugEnabled(Geonet.EDITOR))
                Log.debug(Geonet.EDITOR, "  Add missing GML namespace.");
        	fragment = fragment.replaceFirst("<gml:([^ >]+)", "<gml:$1 xmlns:gml=\"http://www.opengis.net/gml\"");
        }
    	return fragment;
    }

  // -- The following methods are used by services that use metadata-edit-embedded so the
	// -- classes know which element to transform
	/**
	 * Tag the element so the metaata-edit-embedded.xsl know which element is the element for display
	 */
    public static void tagForDisplay(Element elem) {
        elem.setAttribute("addedObj","true", Edit.NAMESPACE);
    }
    /**
     * Remove the tag element so the tag does not stay in the actual metadata.
     */
    public static void removeDisplayTag(Element elem) {
        elem.removeAttribute("addedObj", Edit.NAMESPACE);
    }

}
