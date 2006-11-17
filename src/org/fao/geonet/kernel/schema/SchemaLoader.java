//==============================================================================
//===
//===   SchemaLoader
//===
//==============================================================================
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

package org.fao.geonet.kernel.schema;

import java.util.*;
import org.jdom.*;

import java.io.File;
import jeeves.utils.Xml;

//==============================================================================

public class SchemaLoader
{
	private Element   elRoot;
	private HashMap   hmElements = new HashMap();
	private HashMap   hmTypes    = new HashMap();
	private HashMap   hmAttrGrp  = new HashMap();
	private HashMap   hmAbsElems = new HashMap();
	private HashMap   hmSubsGrp  = new HashMap();
	private HashMap   hmSubsLink = new HashMap();
	private HashMap   hmAttribs  = new HashMap();
	private HashMap   hmGroups   = new HashMap();

	private String targetNS;
	private String targetNSPrefix;

	/** Restrictions for simple types (element restriction) */
	private HashMap   hmElemRestr = new HashMap();

	/** Restrictions for simple types (type restriction) */
	private HashMap   hmTypeRestr = new HashMap();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public SchemaLoader() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public MetadataSchema load(String xmlSchemaFile) throws Exception
	{
		if (xmlSchemaFile.startsWith("_")) return new MetadataSchema(new Element("root"));

		//--- PHASE 1 : pre-processing
		//---
		//--- the xml file is parsed and simplified. Xml schema subtrees are
		//--- wrapped in some classes

		ArrayList alElementFiles = loadFile(xmlSchemaFile, new HashSet());

		parseElements(alElementFiles);

		//--- PHASE 2 : resolve abstract elements

		for(Iterator i=hmSubsGrp.keySet().iterator(); i.hasNext();)
		{
			String elem = (String) i.next();

			ArrayList elements = (ArrayList) hmSubsGrp.get(elem);
			for(int j=0; j<elements.size(); j++)
			{
				ElementEntry ee = (ElementEntry) elements.get(j);
				if (ee.type == null)
				{
					ee.type = (String) hmAbsElems.get(elem);

					if (ee.type == null)
						throw new IllegalArgumentException("Type is null for 'element' : " + ee.name);
				}

				hmElements.put(ee.name, ee.type);
			}
		}

		//--- PHASE 3 : add elements

		MetadataSchema mds = new MetadataSchema(elRoot);
		for(Iterator i=hmElements.keySet().iterator(); i.hasNext();)
		{
			String elem = (String) i.next();
			String type = (String) hmElements.get(elem);

			ArrayList elemRestr = (ArrayList) hmElemRestr.get(elem);
			ArrayList typeRestr = (ArrayList) hmTypeRestr.get(type);

			if (elemRestr == null)
				elemRestr = new ArrayList();

			if (typeRestr != null)
				elemRestr.addAll(typeRestr);

			mds.addElement(elem, type, elemRestr);
		}

		//--- PHASE 4 : post-processing
		//---
		//--- resolve abstract types, attribute/substitution groups and other stuff


		for(Iterator i=hmTypes.values().iterator(); i.hasNext();)
		{
			ComplexTypeEntry cte = (ComplexTypeEntry) i.next();

			MetadataType mdt = new MetadataType();

			mdt.setOrType(cte.isOrType);

			//--- generate attribs

			if (cte.attribGroup != null)
			{
				ArrayList al = (ArrayList) hmAttrGrp.get(cte.attribGroup);

				if (al == null)
					throw new IllegalArgumentException("Attribute group not found : " + cte.attribGroup);

				for(int j=0; j<al.size(); j++)
				{
					AttributeEntry ae = (AttributeEntry) al.get(j);

					mdt.addAttribute(buildMetadataAttrib(ae));
				}
			}

			//--- resolve inheritance & add attribs from complexContent

			if (cte.complexContent != null)
			{
				if (cte.complexContent.base != null)
					cte.alElements = resolveInheritance(cte);

				//--- add attribs from complexContent (if any)

				for(int j=0; j<cte.complexContent.alAttribs.size(); j++)
				{
					AttributeEntry ae = (AttributeEntry) cte.complexContent.alAttribs.get(j);

					mdt.addAttribute(buildMetadataAttrib(ae));
				}
			}

			if (cte.groupRef != null)
			{
				GroupEntry ge = (GroupEntry) hmGroups.get(cte.groupRef);

				if (ge == null)
					throw new IllegalArgumentException("Group ref not found for complex type :" +cte.groupRef);

				if (ge.isChoice)
				{
					ElementEntry ee    = (ElementEntry) ge.alElements.get(0);
					ElementEntry eeRef = (ElementEntry) hmElements.get(ee.ref);

					//--- if the ref was not found then we have an abstract element

					if (eeRef == null)
					{
						ArrayList al = (ArrayList) hmSubsGrp.get(ee.ref);

						if (al == null)
							throw new IllegalArgumentException("Abstract elem not found in substGroup : " +ee.ref);

						for(int k=0; k<al.size(); k++)
						{
							ElementEntry eeDer = (ElementEntry) al.get(k);

							mdt.addElement(eeDer.name, ee.min, ee.max);
						}
					}
					else
						throw new IllegalArgumentException("Found not abstract element in choice : " +eeRef.name);
				}
			}

			//--- handle type's elements

			else for(int j=0; j<cte.alElements.size(); j++)
			{
				ElementEntry ee = (ElementEntry) cte.alElements.get(j);

				System.out.println("resolving element " + (ee.name == null ? (" --> " + ee.ref) : ee.name)); // DEBUG

				String type;
				if (ee.ref != null)
				{
					type = (String) hmElements.get(ee.ref);

					System.out.println("- type = " + type); // DEBUG
				}
				else
				{
					if (ee.name == null)
						throw new IllegalArgumentException("Reference and name are null for element : " + ee.name);

					// RGFIX type = "string";
					type = ee.type == null ? "string" : ee.type;

					System.out.println("- type = " + type); // DEBUG

					mds.addElement(ee.name, type, new ArrayList());
				}

				//--- if type is null we have an abstract type

				if (type == null)
				{
					ArrayList al = (ArrayList) hmSubsGrp.get(ee.ref);
					if (al == null)
					{
						al = (ArrayList) hmSubsLink.get(ee.ref);

						/* RGFIX: found singleton subst-group with only one abstract element
						if (al == null)
							throw new IllegalArgumentException("Reference not found inside subst-group : "+ee.ref);

						mdt.addElement(ee.ref, ee.min, ee.max);
						 */
						if (al != null)
							mdt.addElement(ee.ref, ee.min, ee.max);
					}
					else
					{
						mdt.setOrType(al.size() > 1);

						for(int k=0; k<al.size(); k++)
						{
							ee = (ElementEntry) al.get(k);
							mdt.addElement(ee.name, ee.min, ee.max);
						}
					}
				}
				else
				{
					String elemName = (ee.ref == null) ? ee.name : ee.ref;
					mdt.addElement(elemName, ee.min, ee.max);
				}
			}
			mds.addType(cte.name, mdt);
		}

		return mds;
	}

	//---------------------------------------------------------------------------
	//---
	//--- PHASE 1 : Schema loading
	//---
	//---------------------------------------------------------------------------

	/** Loads the xml-schema file, removes annotations and resolve imports/includes */

	private ArrayList loadFile(String xmlSchemaFile, HashSet loadedFiles) throws Exception
	{
		loadedFiles.add(new File(xmlSchemaFile).getCanonicalPath());
		Logger.log("Added : "+ new File(xmlSchemaFile).getCanonicalPath());

		String path = new File(xmlSchemaFile).getParent() + "/";

		//--- load xml-schema

		elRoot = Xml.loadFile(xmlSchemaFile);

		// change target namespace
		String oldtargetNS       = targetNS;
		String oldtargetNSPrefix = targetNSPrefix;
		targetNS = elRoot.getAttributeValue("targetNamespace");
		targetNSPrefix = null;
		if (targetNS != null)
		{
			for (Iterator i = elRoot.getAdditionalNamespaces().iterator(); i.hasNext(); )
			{
				Namespace ns = (Namespace)i.next();
				if (targetNS.equals(ns.getURI()))
				{
					targetNSPrefix = ns.getPrefix();
					break;
				}
			}
			if ("".equals(targetNSPrefix)) targetNSPrefix = null;
		}
		List children = elRoot.getChildren();

		//--- collect elements into an array because we have to add elements
		//--- when we encounter the "import" element

		ArrayList alElementFiles = new ArrayList();

		for(int i=0; i<children.size(); i++)
		{
			Element elChild = (Element) children.get(i);
			String  name    = elChild.getName();

			if (name.equals("annotation"))
				;

			else if (name.equals("import") || name.equals("include"))
			{
				String schemaLoc = elChild.getAttributeValue("schemaLocation");

				//--- we must prevent imports from the web

				if (schemaLoc.startsWith("http:"))
				{
					int lastSlash = schemaLoc.lastIndexOf("/");
					schemaLoc = schemaLoc.substring(lastSlash +1);
				}
				if (!loadedFiles.contains(new File(path + schemaLoc).getCanonicalPath()))
					alElementFiles.addAll(loadFile(path + schemaLoc, loadedFiles));
			}
			else
				alElementFiles.add(new ElementInfo(elChild, xmlSchemaFile, targetNS, targetNSPrefix));
		}
		// restore target namespace
		targetNS       = oldtargetNS;
		targetNSPrefix = oldtargetNSPrefix;

		return alElementFiles;
	}

	//---------------------------------------------------------------------------
	//---
	//--- PHASE 2 : Parse elements building intermediate data structures
	//---
	//---------------------------------------------------------------------------

	private void parseElements(ArrayList alElementFiles) throws JDOMException
	{
		//--- clear some structures

		hmElements .clear();
		hmTypes    .clear();
		hmAttrGrp  .clear();
		hmAbsElems .clear();
		hmSubsGrp  .clear();
		hmSubsLink .clear();
		hmElemRestr.clear();
		hmTypeRestr.clear();
		hmAttribs  .clear();
		hmGroups   .clear();

		for(int i=0; i<alElementFiles.size(); i++)
		{
			ElementInfo ei = (ElementInfo) alElementFiles.get(i);

			Element elChild = ei.element;
			String  name    = elChild.getName();

			if (name.equals("element"))
				buildGlobalElement(ei);

			else if (name.equals("complexType"))
				buildComplexType(ei);

			else if (name.equals("simpleType"))
				buildSimpleType(ei);

			else if (name.equals("attribute"))
				buildGlobalAttrib(ei);

			else if (name.equals("group"))
				buildGlobalGroup(ei);

			else if (name.equals("attributeGroup"))
				buildAttributeGroup(ei);

			else
				Logger.log("Unknown global element : " + elChild.getName(), ei);
		}
	}

	//---------------------------------------------------------------------------

	private void buildGlobalElement(ElementInfo ei)
	{
		ElementEntry ee = new ElementEntry(ei);
		if (ee.name == null)
			throw new IllegalArgumentException("Name is null for element : " + ee.name);

		System.out.println("building global element " + ee.name); // DEBUG

		if (ee.substGroup != null)
		{
			ArrayList al = (ArrayList) hmSubsGrp.get(ee.substGroup);

			if (al == null)
			{
				al = new ArrayList();
				hmSubsGrp.put(ee.substGroup, al);
			}
			al.add(ee);
			hmSubsLink.put(ee.name, al);
		}
		if (ee.abstrElem)
		{
			if (hmAbsElems.containsKey(ee.name))
				throw new IllegalArgumentException("Namespace collision for : " + ee.name);
			hmAbsElems.put(ee.name, ee.type);

			System.out.println("- abstract element"); // DEBUG

			return;
		}
		if (ee.complexType != null)
		{
			String type = ee.name+"#I";
			ee.complexType.name = type;
			ee.type = type;
			if (hmElements.containsKey(ee.name))
				throw new IllegalArgumentException("Namespace collision for : " + ee.name);

			hmElements.put(ee.name, type);
			hmTypes.put(type, ee.complexType);

			System.out.println("- complex type"); // DEBUG
		}
		else if (ee.simpleType != null)
		{
			if (ee.type == null)
				throw new IllegalArgumentException("Type is null for element : " + ee.name);

			if (hmElements.containsKey(ee.name))
				throw new IllegalArgumentException("Namespace collision for : " + ee.name);

			hmElements .put(ee.name, ee.type);
			hmElemRestr.put(ee.name, ee.simpleType.alEnum);

			System.out.println("- simple type"); // DEBUG
		}
		else
		{
			hmElements.put(ee.name, ee.type);

			System.out.println("- element of : "+ee.type); // DEBUG
		}
	}

	//---------------------------------------------------------------------------

	private void buildComplexType(ElementInfo ei)
	{
		ComplexTypeEntry ct = new ComplexTypeEntry(ei);
		if (hmTypes.containsKey(ct.name))
			throw new IllegalArgumentException("Namespace collision for : " + ct.name);

		hmTypes.put(ct.name, ct);
	}

	//---------------------------------------------------------------------------

	private void buildSimpleType(ElementInfo ei)
	{
		SimpleTypeEntry  st = new SimpleTypeEntry(ei);
		if (hmTypeRestr.containsKey(st.name))
			throw new IllegalArgumentException("Namespace collision for : " + st.name);

		hmTypeRestr.put(st.name, st.alEnum);
	}

	//---------------------------------------------------------------------------

	private void buildGlobalAttrib(ElementInfo ei)
	{
		AttributeEntry at = new AttributeEntry(ei);
		if (hmAttribs.containsKey(at.name))
			throw new IllegalArgumentException("Namespace collision for : " + at.name);

		hmAttribs.put(at.name, at);
	}

	//---------------------------------------------------------------------------

	private void buildGlobalGroup(ElementInfo ei)
	{
		GroupEntry ge = new GroupEntry(ei);
		if (hmGroups.containsKey(ge.name))
			throw new IllegalArgumentException("Namespace collision for : " + ge.name);

		hmGroups.put(ge.name, ge);
	}

	//---------------------------------------------------------------------------

	private void buildAttributeGroup(ElementInfo ei)
	{
		String name = ei.element.getAttributeValue("name");
		if (ei.targetNSPrefix != null) name = ei.targetNSPrefix + ":" + name;
		ArrayList al = (ArrayList) hmAttrGrp.get(name);

		if (al == null)
		{
			al = new ArrayList();
			hmAttrGrp.put(name, al);
		}

		List children = ei.element.getChildren();

		for(int i=0; i<children.size(); i++)
		{
			Element elChild = (Element) children.get(i);

			if (elChild.getName().equals("attribute"))
				al.add(new AttributeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix));

			else
				Logger.log("Unknown child in 'attributeGroup' : " + elChild.getName(), ei);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//---
	//---
	//---------------------------------------------------------------------------

	private ArrayList resolveInheritance(ComplexTypeEntry cte)
	{
		if (cte.complexContent == null)
			return cte.alElements;

		String baseType = cte.complexContent.base;
		ComplexTypeEntry baseCTE = (ComplexTypeEntry) hmTypes.get(baseType);
		if (baseCTE == null)
			throw new IllegalArgumentException("Base type not found for : " + baseType);

		ArrayList result = new ArrayList(resolveInheritance(baseCTE));

		ArrayList al = cte.complexContent.alElements;

		for(int i=0; i<al.size(); i++)
			result.add(al.get(i));

		return result;
	}

	//---------------------------------------------------------------------------

	private MetadataAttribute buildMetadataAttrib(AttributeEntry ae)
	{
		String name = ae.name;
		String ref  = ae.reference;

		if (ref != null)
		{
			ae = (AttributeEntry) hmAttribs.get(ref);

			if (ae == null)
				throw new IllegalArgumentException("Reference '"+ref+"' not found for attrib : " +name);
		}

		MetadataAttribute ma = new MetadataAttribute();

		ma.name     = ae.name;
		ma.defValue = ae.defValue;
		ma.required = ae.required;

		for(int k=0; k<ae.alValues.size(); k++)
			ma.values.add(ae.alValues.get(k));

		return ma;
	}
}

//==============================================================================

class ElementInfo
{
	public Element element;
	public String  file;
	public String  targetNS;
	public String  targetNSPrefix;

	//---------------------------------------------------------------------------

	public ElementInfo(Element e, String f, String tns, String tnsp)
	{
		element        = e;
		file           = f;
		targetNS       = tns;
		targetNSPrefix = tnsp;
	}
}

//==============================================================================

