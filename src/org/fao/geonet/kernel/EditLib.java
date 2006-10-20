//==============================================================================
//===
//=== EditLib
//===
//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.kernel.schema.MetadataAttribute;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataType;
import org.fao.geonet.kernel.schema.SchemaLoader;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

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

	public Iterator getSchemas()
	{
		return htSchemas.keySet().iterator();
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

	public void fillElement(String schema, Element md)
	{
		fillElement(getSchema(schema), getSchemaSuggestions(schema), md);
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

	public Element addElement(String schema, Element el, String name)
	{
		Element child = new Element(name);

		MetadataSchema    mdSchema = getSchema(schema);
		SchemaSuggestions mdSugg   = getSchemaSuggestions(schema);

		String typeName = mdSchema.getElementType(el.getName());

		MetadataType type = mdSchema.getTypeInfo(typeName);

		//--- collect all children, adding the new one at the end of the others

		Vector children = new Vector();

		for(int i=0; i<type.getElementCount(); i++)
		{
			List list = getChildren(el, type.getElementAt(i));

			for(int j=0; j<list.size(); j++)
				children.add(list.get(j));

			if (name.equals(type.getElementAt(i)))
				children.add(child);
		}

		//--- readd collected children to element to assure a correct position
		//--- for the new added one

		el.removeContent();

		for(int i=0; i<children.size(); i++)
			el.addContent((Element) children.get(i));

		//--- add proper namespace (or at least try to do it)

		if (!el.getNamespacePrefix().equals(""))
			child.setNamespace(el.getNamespace());
		else
		{
			List nsList = el.getAdditionalNamespaces();

			if (nsList.size() != 0)
				child.setNamespace((Namespace) nsList.get(0));
		}

		//--- add mandatory sub-tags
		fillElement(mdSchema, mdSugg, child);

		return child;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private List getChildren(Element el, String name)
	{
		Vector result = new Vector();

		List children = el.getChildren();

		for(int i=0; i<children.size(); i++)
		{
			Element child = (Element) children.get(i);

			if (child.getName().equals(name))
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

	private void fillElement(MetadataSchema schema, SchemaSuggestions sugg, Element md)
	{
		String elemName = md.getName();

		if (schema.isSimpleElement(elemName))
			return;

		MetadataType type = schema.getTypeInfo(schema.getElementType(elemName));

		//-----------------------------------------------------------------------
		//--- handle attributes

		for(int i=0; i<type.getAttributeCount(); i++)
		{
			MetadataAttribute attr = type.getAttributeAt(i);

			if (attr.required)
			{
				String value = "";

				if (attr.defValue != null)
					value = attr.defValue;

				md.setAttribute(new Attribute(attr.name, value));
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

				if (minCard > 0 || sugg.isSuggested(elemName, childName))
				{
					MetadataType elemType = schema.getTypeInfo(schema.getElementType(childName));

					//--- There can be 'or' elements with other 'or' elements inside them.
					//--- In this case we cannot expand the inner 'or' elements so the
					//--- only way to solve the problem is to avoid the creation of them

					if (schema.isSimpleElement(childName) || !elemType.isOrType())
					{
						Element child = new Element(childName, md.getNamespace());

						md.addContent(child);
						fillElement(schema, sugg, child);
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
	/** Adds editing information to a single element
	  */

	private void expandElement(String schemaName, MetadataSchema schema, Element md) throws Exception
	{
		String elemName = md.getQualifiedName(); // RGFIX: was: getname
		String elemType = schema.getElementType(elemName);

		System.out.println("elemName = " + elemName); // DEBUG
		
		Element elem = md.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
//		addValues(schema, elem, elemName); // RGFIX
		
		if (schema.isSimpleElement(elemName))
			return;

		MetadataType type = schema.getTypeInfo(elemType);
		
		if (!type.isOrType())
		{
			for(int i=0; i<type.getElementCount(); i++)
			{
				String childName = type.getElementAt(i);
				
				System.out.println("- childName = " + childName); // DEBUG
				
				if (childName == null) continue; // schema extensions cause null types; just skip
				
				List list = md.getChildren();
				if (list.size() == 0)
				{
					Element newElem = createElement(schemaName, schema, childName);

					if (i == 0)	insertFirst(md, newElem);
						else		insertLast(md, type.getElementAt(i-1), newElem);
				}
				else
				{
					for(int j=0; j<list.size(); j++)
					{
						Element listChild = (Element) list.get(j);
						Element listElem  = listChild.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);

						if (j>0)
							listElem.setAttribute(new Attribute(Edit.Element.Attr.UP, Edit.Value.TRUE));

						if (j<list.size() -1)
							listElem.setAttribute(new Attribute(Edit.Element.Attr.DOWN, Edit.Value.TRUE));

						if (list.size() > type.getMinCardinAt(i))
							listElem.setAttribute(new Attribute(Edit.Element.Attr.DEL, Edit.Value.TRUE));
					}

					if (list.size() < type.getMaxCardinAt(i))
						insertLast(md, childName, createElement(schemaName, schema, childName));
				}
			}
		}
		addAttribs(type, md);
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

	private void insertLast(Element md, String childName, Element child)
	{
		boolean added = false;

		List list = md.getChildren();

		Vector v = new Vector();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);

			v.add(el);

			if (equal(childName, el) && !added)
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

	private boolean equal(String childName, Element el)
	{
		if (Edit.NS_PREFIX.equals(el.getNamespacePrefix()))
		{
			if (Edit.RootChild.CHILD.equals(el.getName()))
				return childName.equals(el.getAttributeValue(Edit.ChildElem.Attr.NAME));
			else
				return false;
		}
		else
			return childName.equals(el.getName());
	}

	//--------------------------------------------------------------------------

	private boolean equal(Element el1, Element el2)
	{
		String ns1 = el1.getNamespacePrefix();
		String ns2 = el2.getNamespacePrefix();

		if (Edit.NS_PREFIX.equals(ns1))
		{
			if (Edit.NS_PREFIX.equals(ns2))
			{
				//--- el1 has namespace, el2 has namespace

				if (!Edit.RootChild.CHILD.equals(el1.getName()))
					return false;

				if (!Edit.RootChild.CHILD.equals(el2.getName()))
					return false;

				String name1 = el1.getAttributeValue(Edit.ChildElem.Attr.NAME);
				String name2 = el2.getAttributeValue(Edit.ChildElem.Attr.NAME);

				return name1.equals(name2);
			}
			else
			{
				//--- el1 has namespace, el2 not

				if (!Edit.RootChild.CHILD.equals(el1.getName()))
					return false;

				String name1 = el1.getAttributeValue(Edit.ChildElem.Attr.NAME);

				return el2.getName().equals(name1);
			}
		}
		else
		{
			if (Edit.NS_PREFIX.equals(ns2))
			{
				//--- el1 has no namespace, el2 yes

				if (!Edit.RootChild.CHILD.equals(el2.getName()))
					return false;

				String name2 = el2.getAttributeValue(Edit.ChildElem.Attr.NAME);

				return el1.getName().equals(name2);
			}
			else
			{
				//--- el1 has no namespace, el2 neither

				return el1.getName().equals(el2.getName());
			}
		}
	}

	//--------------------------------------------------------------------------
	/** Create a new element for editing, adding all mandatory subtags
	  */

	private Element createElement(String schemaName, MetadataSchema schema, String name) throws Exception
	{
		Element child = new Element(Edit.RootChild.CHILD, Edit.NAMESPACE);
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.NAME, name));

		String prefix    = "";
		String namespace = "";

		//FIXME: Cacciavitata

		if (schemaName.equals("dublin-core") && !name.equals("simpledc"))
		{
			prefix    = "dc";
			namespace = "http://purl.org/dc/elements/1.1/";
		}

		child.setAttribute(new Attribute(Edit.ChildElem.Attr.PREFIX,    prefix));
		child.setAttribute(new Attribute(Edit.ChildElem.Attr.NAMESPACE, namespace));


		if (!schema.isSimpleElement(name))
		{
			String elemType = schema.getElementType(name);

			MetadataType type = schema.getTypeInfo(elemType);

			if (type.isOrType())
				for(int l=0; l<type.getElementCount(); l++)
				{
					Element choose = new Element(Edit.ChildElem.Child.CHOOSE, Edit.NAMESPACE);
					choose.setAttribute(new Attribute(Edit.Choose.Attr.NAME, type.getElementAt(l)));

					child.addContent(choose);
				}
		}

		return child;
	}

	//--------------------------------------------------------------------------

	private void addValues(MetadataSchema schema, Element elem, String name)
	{
		List values = schema.getElementValues(name);
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

			attribute.setAttribute(new Attribute(Edit.Attribute.Attr.NAME, attr.name));

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

