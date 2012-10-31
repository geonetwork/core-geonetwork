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

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.schema.MetadataAttribute;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataType;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * TODO javadoc.
 *
 */
public class EditLib {
    private Hashtable<String, Integer> htVersions   = new Hashtable<String, Integer>(1000);
	private SchemaManager scm;

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
            Log.debug(Geonet.EDITOR,"MD before editing infomation:\n" + jeeves.utils.Xml.getString(md));
		enumerateTree(md,id,parent);
		expandTree(scm.getSchema(schema), md);
        if(Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR,"MD after editing infomation:\n" + jeeves.utils.Xml.getString(md));
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
     * TODO javadoc.
     *
     * @param schema
     * @param parentName
     * @param md
     * @throws Exception
     */
	public void fillElement(String schema, String parentName, Element md) throws Exception {
		fillElement(scm.getSchema(schema), scm.getSchemaSuggestions(schema), parentName, md);
	}

    /**
     * Given an expanded tree, removes all info added for editing and replaces choice_elements with their children.
     *
     * @param md
     */
	public void removeEditingInfo(Element md) {
		//--- purge geonet: attributes

		List listAtts = md.getAttributes();
		for (int i=0; i<listAtts.size(); i++) {
			Attribute attr = (Attribute) listAtts.get(i);
			if (Edit.NS_PREFIX.equals(attr.getNamespacePrefix())) {
				attr.detach();
				i--;
			}
		}

		//--- purge geonet: children
		List list = md.getChildren();
		for (int i=0; i<list.size(); i++) {
			Element child = (Element) list.get(i);
			if (!Edit.NS_PREFIX.equals(child.getNamespacePrefix()))
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

		List list = md.getChildren();

        for (Object aList : list) {
            Element child = (Element) aList;

            if (!Edit.NS_PREFIX.equals(child.getNamespacePrefix())) {
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
     * @param schema
     * @param el
     * @param qname
     * @return
     * @throws Exception
     */
	public Element addElement(String schema, Element el, String qname) throws Exception {
        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)){
            Log.debug(Geonet.EDITORADDELEMENT,"#### in addElement()");
            Log.debug(Geonet.EDITORADDELEMENT,"#### - parent = " + el.getName());
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child qname = " + qname);
        }
		String name   = getUnqualifiedName(qname);
		String ns     = getNamespace(qname, el, scm.getSchema(schema));
		String prefix = getPrefix(qname);
		String parentName = getParentNameFromChild(el);

        if(Log.isDebugEnabled(Geonet.EDITORADDELEMENT)) {
            Log.debug(Geonet.EDITORADDELEMENT,"#### - parent name for type retrieval = " + parentName);
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child name = " + name);
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child namespace = " + ns);
            Log.debug(Geonet.EDITORADDELEMENT,"#### - child prefix = " + prefix);
        }
		List childS = el.getChildren();
		if (childS.size() > 0) {
			Element elChildS = (Element)childS.get(0);
			Log.debug(Geonet.EDITORADDELEMENT,"#### 	- parents first child = " + elChildS.getName());
		}

		Element child = new Element(name, prefix, ns);

		MetadataSchema    mdSchema = scm.getSchema(schema);
		SchemaSuggestions mdSugg   = scm.getSchemaSuggestions(schema);

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
            Log.error("EditLib : Error parsing XML fragment, ", e.toString());
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

		List children = el.getChildren();

        for (Object aChildren : children) {
            Element child = (Element) aChildren;

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
     * @param md
     * @throws Exception
     */
	private void fillElement(MetadataSchema schema, SchemaSuggestions sugg, Element parent, Element md) throws Exception {
        if(Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR,"#### entering fillElement()");
		String parentName = parent.getQualifiedName();
		fillElement(schema,sugg,parentName,md);
	}

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param sugg
     * @param parentName
     * @param md
     * @throws Exception
     */
	private void fillElement(MetadataSchema schema, SchemaSuggestions sugg, String parentName, Element md) throws Exception {
        if(Log.isDebugEnabled(Geonet.EDITOR))
            Log.debug(Geonet.EDITOR,"#### entering fillElement()");
		String elemName = md.getQualifiedName();

        if(Log.isDebugEnabled(Geonet.EDITOR)) {
            Log.debug(Geonet.EDITOR,"#### - elemName = " + elemName);
            Log.debug(Geonet.EDITOR,"#### - parentName = " + parentName);
            Log.debug(Geonet.EDITOR,"#### - isSimpleElement(" + elemName + ") = " + schema.isSimpleElement(elemName,parentName));
        }

		if (schema.isSimpleElement(elemName,parentName))
			return;

		MetadataType type = schema.getTypeInfo(schema.getElementType(elemName,parentName));
		boolean useSuggestion = sugg.hasSuggestion(elemName, type.getElementList());

        if(Log.isDebugEnabled(Geonet.EDITOR)) {
            Log.debug(Geonet.EDITOR,"#### - type:");
            Log.debug(Geonet.EDITOR,"####   - name = " + type.getName());
            Log.debug(Geonet.EDITOR,"####   - # attributes = " + type.getAttributeCount());
            Log.debug(Geonet.EDITOR,"####   - # elements = " + type.getElementCount());
            Log.debug(Geonet.EDITOR,"####   - # isOrType = " + type.isOrType());
            Log.debug(Geonet.EDITOR,"####   - type = " + type);
        }

		//-----------------------------------------------------------------------
		//--- handle attributes

		for(int i=0; i<type.getAttributeCount(); i++)
		{
			MetadataAttribute attr = type.getAttributeAt(i);

            if(Log.isDebugEnabled(Geonet.EDITOR)) {
                Log.debug(Geonet.EDITOR,"####   - " + i + " attribute = " + attr.name);
                Log.debug(Geonet.EDITOR,"####     - required = " + attr.required);
                Log.debug(Geonet.EDITOR,"####     - suggested = "+sugg.isSuggested(elemName, attr.name));
            }
			if (attr.required || sugg.isSuggested(elemName, attr.name))
			{
				String value = "";

				if (attr.defValue != null) {
					value = attr.defValue;
                    if(Log.isDebugEnabled(Geonet.EDITOR))
                        Log.debug(Geonet.EDITOR,"####     - value = " + attr.defValue);
				}

				String uname = getUnqualifiedName(attr.name);
				String ns     = getNamespace(attr.name, md, schema);
				String prefix = getPrefix(attr.name);
				if (!prefix.equals(""))
					md.setAttribute(new Attribute(uname, value, Namespace.getNamespace(prefix,ns)));
				else
					md.setAttribute(new Attribute(uname, value));
			}
		}

		//-----------------------------------------------------------------------
		//--- add mandatory children

		if (!type.isOrType()) {
			for(int i=0; i<type.getElementCount(); i++) {
				int    minCard   = type.getMinCardinAt(i);
				String childName = type.getElementAt(i);
				boolean hasSuggestion = sugg.hasSuggestion(childName, type.getElementList());

                if(Log.isDebugEnabled(Geonet.EDITOR)) {
                    Log.debug(Geonet.EDITOR,"####   - " + i + " element = " + childName);
                    Log.debug(Geonet.EDITOR,"####     - suggested = "+sugg.isSuggested(elemName, childName));
                    Log.debug(Geonet.EDITOR,"####     - has suggestion = "+hasSuggestion );
                }

                if (minCard > 0 || sugg.isSuggested(elemName, childName)) {
					MetadataType elemType = schema.getTypeInfo(schema.getElementType(childName,elemName));

					//--- There can be 'or' elements with other 'or' elements inside them.
					//--- In this case we cannot expand the inner 'or' elements so the
					//--- only way to solve the problem is to avoid the creation of them

					if (
							(schema.isSimpleElement(elemName, childName) || !elemType.isOrType()) ||
							(elemType.isOrType() && elemType.getElementList().contains("gco:CharacterString") && !hasSuggestion)
						) {
						String name   = getUnqualifiedName(childName);
						String ns     = getNamespace(childName, md, schema);
						String prefix = getPrefix(childName);

						Element child = new Element(name, prefix, ns);

						md.addContent(child);
						fillElement(schema, sugg, md, child);
					} else {
						if (elemType.isOrType()) {
							if (elemType.getElementList().contains("gco:CharacterString") && !hasSuggestion) {
                                if(Log.isDebugEnabled(Geonet.EDITOR))
                                    Log.debug(Geonet.EDITOR,"####   - (INNER) Requested expansion of an OR element having gco:CharacterString substitute and no suggestion: " + md.getName());
							} else {
                                if(Log.isDebugEnabled(Geonet.EDITOR))
                                    Log.debug(Geonet.EDITOR,"####   - WARNING (INNER): requested expansion of an OR element : " +childName);
                            }
						}
					}
				}
			}
		}
		else if (type.getElementList().contains("gco:CharacterString") && !useSuggestion) {
			// Here we could probably expand element having one and only one suggestion for 
			// an or element - then we force to expand that only one suggestion ? 
            if(Log.isDebugEnabled(Geonet.EDITOR))
                Log.debug(Geonet.EDITOR,"####   - Requested expansion of an OR element having gco:CharacterString substitute and no suggestion: " + md.getName());
			Element child = new Element("CharacterString", "gco", "http://www.isotc211.org/2005/gco");
			md.addContent(child);
		} else {
            if(Log.isDebugEnabled(Geonet.EDITOR))
                Log.debug(Geonet.EDITOR,"####   - WARNING : requested expansion of an OR element : " +md.getName());
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
		
		List childs = md.getChildren();
        for (Object child : childs) {
            expandElements(schema, (Element) child);
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

		List chChilds = md.getChildren();
        for (Object chChild1 : chChilds) {
            Element chChild = (Element) chChild1;
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
		List childs = md.getContent();
        for (Object obj : childs) {
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

		List list = md.getChildren();

        for (Object aList : list) {
            Element child = (Element) aList;
            if (!Edit.NS_PREFIX.equals(child.getNamespacePrefix())) {
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
		Iterator mdIt = md.getDescendants(new ElementFilter("element"));
		while (mdIt.hasNext()) {
			Element elem = (Element)mdIt.next();
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

		List list = md.getChildren();

        for (Object aList : list) {
            Element child = (Element) aList;

            if (!Edit.NS_PREFIX.equals(child.getNamespacePrefix())) {
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

			List list = md.getChildren(childName, Namespace.getNamespace(childNS));
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

		List list = md.getChildren();

        for (Object aList : list) {
            v.add((Element) aList);
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

		List list = md.getChildren();

		Vector<Element> v = new Vector<Element>();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);

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
					Element elNext = (Element) list.get(i+1);

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
		if (Edit.NS_URI.equals(el.getNamespaceURI())) {
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

		if (Edit.NS_URI.equals(elemNS1)) {
			if (Edit.NS_URI.equals(elemNS2)) {
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
			if (Edit.NS_URI.equals(elemNS2)) {
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
		List values = schema.getElementValues(name,parent);
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

			for(int j=0; j<attr.values.size(); j++) {
				Element text = new Element(Edit.Attribute.Child.TEXT, Edit.NAMESPACE);
				text.setAttribute(Edit.Attribute.Attr.VALUE, (String) attr.values.get(j));

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