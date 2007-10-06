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

import java.util.*;
import org.fao.geonet.kernel.schema.*;
import org.jdom.*;
import jeeves.utils.Xml;

import java.io.File;
import org.fao.geonet.constants.Edit;

//=============================================================================

public class EditLib
{
	private DataManager dataMan;
	private Hashtable   htVersions   = new Hashtable(1000);
	private Hashtable   htSchemas    = new Hashtable();
	private Hashtable   htSchemaDirs = new Hashtable();
	private Hashtable   htSchemaSugg = new Hashtable();

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	/** Init structures
	  */

	public EditLib(DataManager dataMan)
	{
		this.dataMan = dataMan;

		htVersions.clear();
		htSchemas .clear();
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/** Loads the metadata schema from disk and adds it to the pool
	  */

	public void addSchema(String id, String xmlSchemaFile, String xmlSuggestFile) throws Exception
	{
		String path = new File(xmlSchemaFile).getParent() +"/";

		htSchemas   .put(id, new SchemaLoader().load(xmlSchemaFile));
		htSchemaDirs.put(id, path);
		htSchemaSugg.put(id, new SchemaSuggestions(xmlSuggestFile));
	}

	//--------------------------------------------------------------------------

	public MetadataSchema getSchema(String name)
	{
		MetadataSchema schema = (MetadataSchema) htSchemas.get(name);

		if (schema == null)
			throw new IllegalArgumentException("Schema not registered : " + name);

		return schema;
	}

	//--------------------------------------------------------------------------

	public String getSchemaDir(String name)
	{
		String dir = (String) htSchemaDirs.get(name);

		if (dir == null)
			throw new IllegalArgumentException("Schema not registered : " + name);

		return dir;
	}

	//--------------------------------------------------------------------------

	public Set<String> getSchemas()
	{
		return htSchemas.keySet();
	}

	//--------------------------------------------------------------------------

	public boolean existsSchema(String name)
	{
		return htSchemas.containsKey(name);
	}

	//--------------------------------------------------------------------------
	/** Expands a metadata adding all information needed for editing.
	  */

	public String addEditingInfo(String schema, String id, Element md) throws Exception
	{
		String version = getVersion(id, true) +"";

		enumerateTree(md, 1);
		expandTree(schema, getSchema(schema), md);

		return version;
	}

	//--------------------------------------------------------------------------

	public void enumerateTree(Element md)
	{
		enumerateTree(md, 1);
	}

	//--------------------------------------------------------------------------

	public String getVersion(String id)
	{
		return Integer.toString(getVersion(id, false));
	}

	//--------------------------------------------------------------------------

	public String getNewVersion(String id)
	{
		return Integer.toString(getVersion(id, true));
	}

	//--------------------------------------------------------------------------
	/** Given an element, creates all mandatory sub-elements. The given element
	  * should be empty.
	  */

	public void fillElement(String schema, Element parent, Element md) throws Exception
	{
		fillElement(getSchema(schema), getSchemaSuggestions(schema), parent, md);
	}

	//--------------------------------------------------------------------------
	/** Given an expanded tree, removes all info added for editing
	  */

	public void removeEditingInfo(Element md)
	{
		//--- purge children

		List list = md.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element child = (Element) list.get(i);

			if (!Edit.NS_PREFIX.equals(child.getNamespacePrefix()))
				removeEditingInfo(child);
			else
			{
				child.detach();
				i--;
			}
		}
	}

	//--------------------------------------------------------------------------
	/** Returns the element at a given reference.
	  * @param md the metadata element expanded with editing info
	  * @param ref the element position in a pre-order visit
	  */

	public Element findElement(Element md, String ref)
	{
		Element elem = md.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);

		if (ref.equals(elem.getAttributeValue(Edit.Element.Attr.REF)))
			 return md;

		//--- search on children

		List list = md.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element child = (Element) list.get(i);

			if (!Edit.NS_PREFIX.equals(child.getNamespacePrefix()))
			{
				child = findElement(child, ref);

				if (child != null)
					return child;
			}
		}

		return null;
	}

	//--------------------------------------------------------------------------

	public Element addElement(String schema, Element el, String qname) throws Exception
	{
//		System.out.println("#### in addElement()"); // DEBUG

//		System.out.println("#### - parent = " + el.getName()); // DEBUG
//		System.out.println("#### - child qname = " + qname); // DEBUG

		String name   = getUnqualifiedName(qname);
		String ns     = getNamespace(qname, el);
		String prefix = getPrefix(qname);
		String parentName = getParentNameFromChild(el);


		// System.out.println("#### - parent name = " + parentName); // DEBUG
		// System.out.println("#### - child name = " + name); // DEBUG
		// System.out.println("#### - child namespace = " + ns); // DEBUG
		// System.out.println("#### - child prefix = " + prefix); // DEBUG
		List childS = el.getChildren();
		if (childS.size() > 0) {
			Element elChildS = (Element)childS.get(0);
			// System.out.println("#### - child text = " + elChildS.getName()); // DEBUG
		}


		Element child = new Element(name, prefix, ns);

		MetadataSchema    mdSchema = getSchema(schema);
		SchemaSuggestions mdSugg   = getSchemaSuggestions(schema);

		String typeName = mdSchema.getElementType(el.getQualifiedName(),parentName);

		// System.out.println("#### - type name = " + typeName); // DEBUG

 		MetadataType type = mdSchema.getTypeInfo(typeName);

		// System.out.println("#### - metadata tpe = " + type); // DEBUG

		//--- collect all children, adding the new one at the end of the others

		Vector children = new Vector();

		for(int i=0; i<type.getElementCount(); i++)
		{
			List list = getChildren(el, type.getElementAt(i));

			// System.out.println("####   - children of type " + type.getElementAt(i) + " list size = " + list.size()); // DEBUG

			for(int j=0; j<list.size(); j++)
				children.add(list.get(j));

			if (qname.equals(type.getElementAt(i)))
				children.add(child);
		}
		//--- read collected children to element to assure a correct position
		//--- for the new added one

		el.removeContent();

		for(int i=0; i<children.size(); i++)
			el.addContent((Element) children.get(i));

		//--- add mandatory sub-tags
		fillElement(mdSchema, mdSugg, el, child);

		return child;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private List getChildren(Element el, String qname)
	{
		Vector result = new Vector();

		List children = el.getChildren();

		for(int i=0; i<children.size(); i++)
		{
			Element child = (Element) children.get(i);

			if (child.getQualifiedName().equals(qname))
				result.add(child);
		}
		return result;
	}

	//--------------------------------------------------------------------------

	/** Returns the version of a metadata, incrementing it if necessary
	  */

	private synchronized int getVersion(String id, boolean increment)
	{
		Integer inVer = (Integer) htVersions.get(id);

		if (inVer == null)
			inVer = new Integer(1);

		if (increment)
			inVer = new Integer(inVer.intValue() +1);

		htVersions.put(id, inVer);

		return inVer.intValue();
	}

	//--------------------------------------------------------------------------

	private void fillElement(MetadataSchema schema, SchemaSuggestions sugg,
														Element parent, Element md) throws Exception
	{
		//System.out.println("#### entering fillElement()"); // DEBUG

		String elemName = md.getQualifiedName();
		String parentName = parent.getQualifiedName();

		//System.out.println("#### - elemName = " + elemName); // DEBUG
		//System.out.println("#### - isSimpleElement(" + elemName + ") = " + schema.isSimpleElement(elemName,parentName)); // DEBUG

		if (schema.isSimpleElement(elemName,parentName))
			return;

		MetadataType type = schema.getTypeInfo(schema.getElementType(elemName,parentName));

		//System.out.println("#### - type:"); // DEBUG
		//System.out.println("####   - name = " + type.getName()); // DEBUG
		//System.out.println("####   - # attributes = " + type.getAttributeCount()); // DEBUG
		//System.out.println("####   - # elements = " + type.getElementCount()); // DEBUG
		//System.out.println("####   - # isOrType = " + type.isOrType()); // DEBUG

		//-----------------------------------------------------------------------
		//--- handle attributes

		for(int i=0; i<type.getAttributeCount(); i++)
		{
			MetadataAttribute attr = type.getAttributeAt(i);

			//System.out.println("####   - " + i + " attribute = " + attr.name); // DEBUG
			//System.out.println("####     - required = " + attr.required); // DEBUG

			if (attr.required)
			{
				String value = "";

				if (attr.defValue != null)
					value = attr.defValue;

				String uname = getUnqualifiedName(attr.name);
				String ns     = getNamespace(attr.name, md);
				String prefix = getPrefix(attr.name);
				// if the prefix is gml then we create the attribute with the
				// appropriate namespace because gml types include ref'd attributes
				// like gml:id - the others such as gmd etc don't include refs but
				// define types that include attributes without namespace prefixes
				if (prefix.equals("gml"))
					md.setAttribute(new Attribute(uname, value, Namespace.getNamespace(prefix,ns)));
				else
					md.setAttribute(new Attribute(uname, value));
			}
		}

		//-----------------------------------------------------------------------
		//--- add mandatory children

		if (!type.isOrType())
		{
			for(int i=0; i<type.getElementCount(); i++)
			{
				int    minCard   = type.getMinCardinAt(i);
				String childName = type.getElementAt(i);

				// Now if the element has subs examine them and substitute the
				// correct element based on the namespace of the root element
				if (type.examineSubs(i)) {
					childName  = getProfileSubstituteName(schema,childName,md);
					//System.out.println("- childName has substituted = " + childName);
				}

				if (minCard > 0 || sugg.isSuggested(elemName, childName))
				{
					MetadataType elemType = schema.getTypeInfo(schema.getElementType(childName,elemName));

					//--- There can be 'or' elements with other 'or' elements inside them.
					//--- In this case we cannot expand the inner 'or' elements so the
					//--- only way to solve the problem is to avoid the creation of them

					if (schema.isSimpleElement(elemName, childName) || !elemType.isOrType())
					{
						String name   = getUnqualifiedName(childName);
						String ns     = getNamespace(childName, md);
						String prefix = getPrefix(childName);

						Element child = new Element(name, prefix, ns);

						md.addContent(child);
						fillElement(schema, sugg, md, child);
					}
				}
			}
		}
		else
		{
			System.out.println("WARNING : requested expansion of an OR element : " +md.getName());
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Tree expansion methods
	//---
	//--------------------------------------------------------------------------

	/** Does a pre-order visit enumerating each node
	  */

	private int enumerateTree(Element md, int ref)
	{
		Element elem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
		elem.setAttribute(new Attribute(Edit.Element.Attr.REF, ref +""));

		List list = md.getChildren();

		for(int i=0; i<list.size(); i++)
			ref = enumerateTree((Element) list.get(i), ref +1);

		md.addContent(elem);

		return ref;
	}

	//--------------------------------------------------------------------------
	/** Given a metadata, does a recursive scan adding information for editing
	  */

	private void expandTree(String schemaName, MetadataSchema schema, Element md) throws Exception
	{
		expandElement(schemaName, schema, md);

		List list = md.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element child = (Element) list.get(i);

			if (!Edit.NS_PREFIX.equals(child.getNamespacePrefix()))
				expandTree(schemaName, schema, child);
		}
	}

	//--------------------------------------------------------------------------

	private String getParentNameFromChild(Element child) {
	  String childName = child.getQualifiedName();
		String parentName = "root";
		Element parent = child.getParentElement();
		if (parent != null) {
			parentName = parent.getQualifiedName();
		}
		return parentName;
	}

	//--------------------------------------------------------------------------
	/** Checks substitutions for element and returns substitute based on
	 *  namespace prefix of the root - NOTE: this code is to support
   *  profiles of ISO19139 which should have a different root element
   *  namespace - so no subs are done if this is vanilla iso19139 ie. gmd
   *  root namespace.
	 */
	private String getProfileSubstituteName(MetadataSchema schema,
																String childQName, Element md)
	{
		ArrayList subsNames = schema.getElementSubs(childQName);
		if (subsNames == null) return childQName;

		// find root element, where namespaces *must* be declared
		Element root = md;
		while (root.getParent() != null && root.getParent() instanceof Element) root = (Element)root.getParent();
		String prefix = root.getNamespacePrefix();
		if (prefix.equals("gmd")) return childQName;

		for (int i = 0;i < subsNames.size();i++) {
			 String sub = (String) subsNames.get(i);
			if (getPrefix(sub).equals(prefix)) return sub;
		}

		return childQName;

	}

	//--------------------------------------------------------------------------
	/** Replace CHOICE_ELEMENTs with their children by enumerating the tree
	 */
	public void replaceChoiceElements(Element md) throws Exception
	{
		enumerateTree(md);
		removeEditingInfo(md);
	}


	//--------------------------------------------------------------------------
	/** Adds editing information to a single element
	  */

	private void expandElement(String schemaName, MetadataSchema schema, Element md) throws Exception
	{
		// System.out.println("entering expandElement()"); // DEBUG

		String elemName = md.getQualifiedName();
		String parentName = getParentNameFromChild(md);

		// System.out.println("elemName = " + elemName); // DEBUG
		// System.out.println("parentName = " + parentName); // DEBUG

		String elemType = schema.getElementType(elemName,parentName);
		// System.out.println("elemType = " + elemType); // DEBUG

		Element elem = md.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
		addValues(schema, elem, elemName, parentName);

		if (schema.isSimpleElement(elemName,parentName))
		{
			// System.out.println("is simple element"); // DEBUG
			return;
		}
		MetadataType type = schema.getTypeInfo(elemType);

		boolean postProcessChoiceElements = false;

		if (!type.isOrType())
		{
			for(int i=0; i<type.getElementCount(); i++)
			{
				String childQName = type.getElementAt(i);

				// System.out.println("- childName = " + childQName); // DEBUG
				if (childQName == null) continue; // schema extensions cause null types; just skip

				// Now if the element has subs examine them and substitute the
				// correct element based on the namespace of the root element
				if (type.examineSubs(i)) {
					childQName  = getProfileSubstituteName(schema,childQName,md);
					// System.out.println("- childName has substituted = " + childQName);
				}

				String childName   = getUnqualifiedName(childQName);
				String childPrefix = getPrefix(childQName);
				String childNS     = getNamespace(childQName, md);

				// System.out.println("- name      = " + childName); // DEBUG
				// System.out.println("- prefix    = " + childPrefix); // DEBUG
				// System.out.println("- namespace = " + childNS); // DEBUG

				List list = md.getChildren(childName, Namespace.getNamespace(childNS));
				if (list.size() == 0)
				{
					// System.out.println("- no children of this type already present"); // DEBUG

					Element newElem = createElement(schemaName, schema, elemName, childQName, childPrefix, childNS);
						if (i == 0)	insertFirst(md, newElem);
						else
						{
							String prevQName = type.getElementAt(i-1);
							String prevName = getUnqualifiedName(prevQName);
							String prevNS   = getNamespace(prevQName, md);
							insertLast(md, prevName, prevNS, newElem);
						}
				}
				else
				{
					// System.out.println("- " + list.size() + " children of this type already present"); // DEBUG
					// System.out.println("- min cardinality = " + type.getMinCardinAt(i)); // DEBUG
					// System.out.println("- max cardinality = " + type.getMaxCardinAt(i)); // DEBUG

					for(int j=0; j<list.size(); j++)
					{
						Element listChild = (Element) list.get(j);

						// IF this is a CHOICE_ELEMENT then we will replace it afterwards
						// with its children
						postProcessChoiceElements = true;
						Element listElem  = listChild.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);

						if (j>0)
							listElem.setAttribute(new Attribute(Edit.Element.Attr.UP, Edit.Value.TRUE));

						if (j<list.size() -1)
							listElem.setAttribute(new Attribute(Edit.Element.Attr.DOWN, Edit.Value.TRUE));

						if (list.size() > type.getMinCardinAt(i))
							listElem.setAttribute(new Attribute(Edit.Element.Attr.DEL, Edit.Value.TRUE));
					}
					if (list.size() < type.getMaxCardinAt(i))
						insertLast(md, childName, childNS, createElement(schemaName, schema, elemName, childQName, childPrefix, childNS));
				}

			}

// Deals with CHOICE_ELEMENTs by replacing them with their children
// FIXME: must also copy up the geonet:element child so that minOccurs/maxOccurs
// are properly carried - this isn't a problem yet
			if (postProcessChoiceElements) {
				List list = md.getChildren();
				for (int i = 0;i < list.size();i++) {
					Element listChild = (Element) list.get(i);
					if (listChild.getName().indexOf("CHOICE_ELEMENT") != -1) {
						// System.out.println("###### Replacing "+listChild.getName());
						List children = listChild.getChildren();
						Element child = (Element)children.get(0);
						list.set(i,child.detach());
					}
				}
			}
		}
		addAttribs(type, md);
	}

	//--------------------------------------------------------------------------

	public String getUnqualifiedName(String qname)
	{
		int pos = qname.indexOf(":");
		if (pos < 0) return qname;
		else         return qname.substring(pos + 1);
	}

	//--------------------------------------------------------------------------

	public String getPrefix(String qname)
	{
		int pos = qname.indexOf(":");
		if (pos < 0) return "";
		else         return qname.substring(0, pos);
	}

	//--------------------------------------------------------------------------

	public String getNamespace(String qname, Element md)
	{
		// check the element first to see whether the namespace is
		// declared locally
		String result = checkNamespaces(qname,md);
		if (result.equals("UNKNOWN")) {

			// find root element, where namespaces *must* be declared
			Element root = md;
			while (root.getParent() != null && root.getParent() instanceof Element) root = (Element)root.getParent();
			result = checkNamespaces(qname,root);
		}
		return result;
	}

	//--------------------------------------------------------------------------

	public String checkNamespaces(String qname, Element md)
	{
		// get prefix
		String prefix = getPrefix(qname);

		// loop on namespaces to fine the one corresponding to prefix
		Namespace rns = md.getNamespace();
		if (prefix.equals(rns.getPrefix())) return rns.getURI();
		for (Iterator i = md.getAdditionalNamespaces().iterator(); i.hasNext(); )
		{
			Namespace ns = (Namespace)i.next();
			if (prefix.equals(ns.getPrefix())) return ns.getURI();
		}
		return "UNKNOWN";
	}

	//--------------------------------------------------------------------------

	private void insertFirst(Element md, Element child)
	{
		Vector v = new Vector();
		v.add(child);

		List list = md.getChildren();

		for(int i=0; i<list.size(); i++)
			v.add((Element) list.get(i));

		//---

		md.removeContent();

		for(int i=0; i<v.size(); i++)
			md.addContent((Element) v.get(i));
	}

	//--------------------------------------------------------------------------

	private void insertLast(Element md, String childName, String childNS, Element child)
	{
		boolean added = false;

		List list = md.getChildren();

		Vector v = new Vector();

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

		for(int i=0; i<v.size(); i++)
			md.addContent((Element) v.get(i));
	}

	//--------------------------------------------------------------------------

	private boolean equal(String childName, String childNS, Element el)
	{
		if (Edit.NS_URI.equals(el.getNamespaceURI()))
		{
			if (Edit.RootChild.CHILD.equals(el.getName()))
				return childName.equals(el.getAttributeValue(Edit.ChildElem.Attr.NAME)) &&
						 childNS.equals(el.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE));
			else
				return false;
		}
		else
			return childName.equals(el.getName()) && childNS.equals(el.getNamespaceURI());
	}

	//--------------------------------------------------------------------------

	private boolean equal(Element el1, Element el2)
	{
		String elemNS1 = el1.getNamespaceURI();
		String elemNS2 = el2.getNamespaceURI();

		if (Edit.NS_URI.equals(elemNS1))
		{
			if (Edit.NS_URI.equals(elemNS2))
			{
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
			else
			{
				//--- el1 is a geonet:child, el2 is not

				if (!Edit.RootChild.CHILD.equals(el1.getName()))
					return false;

				String name1 = el1.getAttributeValue(Edit.ChildElem.Attr.NAME);
				String ns1   = el1.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE);

				return el2.getName().equals(name1) && el2.getNamespaceURI().equals(ns1);
			}
		}
		else
		{
			if (Edit.NS_URI.equals(elemNS2))
			{
				//--- el2 is a geonet:child, el1 is not

				if (!Edit.RootChild.CHILD.equals(el2.getName()))
					return false;

				String name2 = el2.getAttributeValue(Edit.ChildElem.Attr.NAME);
				String ns2   = el2.getAttributeValue(Edit.ChildElem.Attr.NAMESPACE);

				return el1.getName().equals(name2) && el1.getNamespaceURI().equals(ns2);
			}
			else
			{
				//--- both not geonet:child elements

				return el1.getName().equals(el2.getName()) && el1.getNamespaceURI().equals(el2.getNamespaceURI());
			}
		}
	}

	//--------------------------------------------------------------------------
	/** Create a new element for editing, adding all mandatory subtags
	  */

	private Element createElement(String schemaName, MetadataSchema schema, String parent, String qname, String childPrefix, String childNS) throws Exception
	{
		Element child = new Element(Edit.RootChild.CHILD, Edit.NAMESPACE);
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.NAME, getUnqualifiedName(qname)));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.PREFIX,   getPrefix(qname)));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.NAMESPACE, childNS));

		if (!schema.isSimpleElement(qname,parent))
		{
			String elemType = schema.getElementType(qname,parent);

			MetadataType type = schema.getTypeInfo(elemType);

			if (type.isOrType())
				for(int l=0; l<type.getElementCount(); l++)
				{
					Element choose = new Element(Edit.ChildElem.Child.CHOOSE, Edit.NAMESPACE);
					String chType = type.getElementAt(l);
					//String prefix = getPrefix(chType);
					//choose.setAttribute(new Attribute(Edit.Choose.Attr.NAME, getUnqualifiedName(chType)));
					choose.setAttribute(new Attribute(Edit.Choose.Attr.NAME, chType));
					//if (!prefix.equals(""))
					//	choose.setAttribute(new Attribute(Edit.Choose.Attr.PREFIX,prefix));

					child.addContent(choose);
				}
		}

		return child;
	}

	//--------------------------------------------------------------------------

	private void addValues(MetadataSchema schema, Element elem, String name, String parent) throws Exception
	{
		List values = schema.getElementValues(name,parent);
		if (values != null)
			for(int i=0; i<values.size(); i++)
			{
				Element text  = new Element(Edit.Element.Child.TEXT, Edit.NAMESPACE);
				text.setAttribute(Edit.Attribute.Attr.VALUE, (String) values.get(i));

				elem.addContent(text);
			}
	}

	//--------------------------------------------------------------------------

	private void addAttribs(MetadataType type, Element md)
	{
		for(int i=0; i<type.getAttributeCount(); i++)
		{
			MetadataAttribute attr = type.getAttributeAt(i);

			Element attribute = new Element(Edit.RootChild.ATTRIBUTE, Edit.NAMESPACE);

			//String name = getUnqualifiedName(attr.name);
			//String prefix = getPrefix(attr.name);

			attribute.setAttribute(new Attribute(Edit.Attribute.Attr.NAME, attr.name));
			//if (prefix != null && !prefix.equals(""))
			//	attribute.setAttribute(new Attribute(Edit.Attribute.Attr.PREFIX, prefix));

			//--- add default value (if any)

			if (attr.defValue != null)
			{
				Element def = new Element(Edit.Attribute.Child.DEFAULT, Edit.NAMESPACE);
				def.setAttribute(Edit.Attribute.Attr.VALUE, attr.defValue);

				attribute.addContent(def);
			}

			for(int j=0; j<attr.values.size(); j++)
			{
				Element text = new Element(Edit.Attribute.Child.TEXT, Edit.NAMESPACE);
				text.setAttribute(Edit.Attribute.Attr.VALUE, (String) attr.values.get(j));

				attribute.addContent(text);
			}

			//--- handle 'add' and 'del' attribs

			boolean present = (md.getAttributeValue(attr.name) != null);

			if (!present)
				attribute.setAttribute(new Attribute(Edit.Attribute.Attr.ADD, Edit.Value.TRUE));

			else if (!attr.required)
				attribute.setAttribute(new Attribute(Edit.Attribute.Attr.DEL, Edit.Value.TRUE));

			md.addContent(attribute);
		}
	}

	//--------------------------------------------------------------------------

	private SchemaSuggestions getSchemaSuggestions(String name)
	{
		SchemaSuggestions sugg = (SchemaSuggestions) htSchemaSugg.get(name);

		if (sugg == null)
			throw new IllegalArgumentException("Schema suggestions not registered : " + name);

		return sugg;
	}
}

//=============================================================================

