//==============================================================================
//===
//===   SchemaLoader
//===
//==============================================================================
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

package org.fao.geonet.kernel.schema;

import java.util.*;
import org.jdom.*;

import java.io.File;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Edit;

//==============================================================================

public class SchemaLoader
{
	private Element   elRoot;
	private HashMap   hmElements = new HashMap();
	private HashMap   hmTypes    = new HashMap();
	private HashMap   hmAttrGrp  = new HashMap();
	private HashMap   hmAttrGpEn = new HashMap();
	private HashMap   hmAbsElems = new HashMap();
	private HashMap   hmSubsGrp  = new HashMap();
	private HashMap   hmSubsLink = new HashMap();
	private HashMap   hmSubsNames = new HashMap();
	private HashMap   hmAttribs  = new HashMap();
	private HashMap   hmAllAttrs = new HashMap();
	private HashMap   hmGroups   = new HashMap();
	private HashMap   hmNameSpaces = new HashMap();

	private SchemaSubstitutions ssOverRides;

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

	public MetadataSchema load(String xmlSchemaFile, String schemaId, String xmlSubstitutionsFile) throws Exception
	{
		ssOverRides = new SchemaSubstitutions(xmlSubstitutionsFile);

		if (xmlSchemaFile.startsWith("_")) return new MetadataSchema(new Element("root"));

		//--- PHASE 1 : pre-processing
		//---
		//--- the xml file is parsed and simplified. Xml schema subtrees are
		//--- wrapped in some classes

		ArrayList alElementFiles = loadFile(xmlSchemaFile, new HashSet());

		parseElements(alElementFiles);

		//--- PHASE 2 : resolve abstract elements

		for(Iterator i=hmSubsGrp.keySet().iterator(); i.hasNext();) {
			String elem = (String) i.next();

			ArrayList elements = (ArrayList) hmSubsGrp.get(elem);
			ArrayList subsNames = new ArrayList();
			hmSubsNames.put(elem,subsNames);
			for(int j=0; j<elements.size(); j++) {
				ElementEntry ee = (ElementEntry) elements.get(j);
				if (ee.type == null) {
					ee.type = (String) hmAbsElems.get(elem);

					if (ee.type == null) {
						// If we don't have a type then insert with null and fix
						// when all elements have been added to hmElements
						Logger.log("Type is null for 'element' : "+ee.name+" which is part of substitution group with head element "+elem);
					}
				}

				hmElements.put(ee.name, ee.type);
				subsNames.add(ee.name);
			}
		}

		//--- PHASE 3 : add namespaces and elements

		MetadataSchema mds = new MetadataSchema(elRoot);
		for (int j = 0;j < alElementFiles.size();j++) {
			ElementInfo ei = (ElementInfo) alElementFiles.get(j);
			mds.addNS(ei.targetNSPrefix,ei.targetNS);	
		}

		for(Iterator i=hmElements.keySet().iterator(); i.hasNext();) {
			String elem = (String) i.next();
			String type = (String) hmElements.get(elem);

			// fix any null types by back tracking through substitution links
			// until we get a concrete type or die trying :-)
			if (type == null) {
				Logger.log("Searching for type for element "+elem);
				type = recurseOnSubstitutionLinks(elem);
				if (type == null) {
					System.out.println("WARNING: Cannot find type for " +elem+": assuming string");
					type="string";
				} else {
					Logger.log("-- Recursive search returned "+type+" for element "+elem);
				}
			}

			ArrayList elemRestr = (ArrayList) hmElemRestr.get(elem);
			ArrayList typeRestr = (ArrayList) hmTypeRestr.get(type);

			if (elemRestr == null)
				elemRestr = new ArrayList();

			if (typeRestr != null)
				elemRestr.addAll(typeRestr);

			ArrayList elemSubs = (ArrayList) hmSubsNames.get(elem);
			if (elemSubs == null) elemSubs = new ArrayList();
			String elemSubsLink = (String) hmSubsLink.get(elem);
			if (elemSubsLink == null) elemSubsLink = "";
			mds.addElement(elem, type, elemRestr, elemSubs, elemSubsLink);
		}

		//--- PHASE 4 : resolve references in attribute groups

		for (Iterator i=hmAttrGpEn.values().iterator();i.hasNext();) {
			AttributeGroupEntry age = (AttributeGroupEntry) i.next();
			for (int k=0;k<age.alAttrs.size();k++) {
				AttributeEntry attr = (AttributeEntry)age.alAttrs.get(k);
				if (attr.name != null) hmAllAttrs.put(attr.name,attr);
			}
			ArrayList attrs = resolveNestedAttributeGroups(age);
			hmAttrGrp.put(age.name,attrs);
		}

		//--- PHASE 5 : check attributes to see whether they should be qualified

		HashMap hmAttrChk = new HashMap();
		for(Iterator i=hmAllAttrs.values().iterator(); i.hasNext();) {
			AttributeEntry attr = (AttributeEntry) i.next();
			AttributeEntry attrPrev = (AttributeEntry) hmAttrChk.get(attr.unqualifiedName);
			if (attrPrev != null) {
				attr.form = "qualified"; attrPrev.form = "qualified";
			}
			else {
				hmAttrChk.put(attr.unqualifiedName,attr);
			}
		}

		//--- PHASE 6 : post-processing
		//---
		//--- resolve type inheritance and elements

		ArrayList alTypes = new ArrayList(hmTypes.values());
		for(ListIterator i=alTypes.listIterator(); i.hasNext();) {
			ComplexTypeEntry cte = (ComplexTypeEntry) i.next();

			MetadataType mdt = new MetadataType();

			mdt.setOrType(cte.isOrType);

			//--- resolve element and attribute inheritance from complexContent

			if (cte.complexContent != null) {

				if (cte.complexContent.base != null) {

					//--- add elements
					cte.alElements = resolveInheritance(cte);

					//--- add attribs (if any)
					ArrayList complexContentAttribs = resolveAttributeInheritance(cte);
					for(int j=0; j<complexContentAttribs.size(); j++) {
						AttributeEntry ae = (AttributeEntry) complexContentAttribs.get(j);
						mdt.addAttribute(buildMetadataAttrib(ae));
					}

					//--- if the base type is an ortype then we need to make this an
					//--- or type as well
					ComplexTypeEntry baseCTE = (ComplexTypeEntry)hmTypes.get(cte.complexContent.base);
					if (baseCTE.isOrType) {
						cte.isOrType = true;
						mdt.setOrType(true);
						Logger.log("Setting "+cte.name+" to isOrType");
					}
				} else {
					throw new IllegalArgumentException("base not defined for complexContent in "+cte.name);
				}

			//--- resolve attribute inheritance from simpleContent
			
			} else if (cte.simpleContent != null) {
				ArrayList simpleContentAttribs = resolveAttributeInheritanceFromSimpleContent(cte);
				for(int j=0; j<simpleContentAttribs.size(); j++) {
					AttributeEntry ae = (AttributeEntry) simpleContentAttribs.get(j);
					mdt.addAttribute(buildMetadataAttrib(ae));
				}

			//--- otherwise process the attributes and attribute groups for this type

			} else {
				for(int j=0; j<cte.alAttribs.size(); j++) {
					AttributeEntry ae = (AttributeEntry) cte.alAttribs.get(j);
					mdt.addAttribute(buildMetadataAttrib(ae));
				}
				for (int k=0;k < cte.alAttribGroups.size();k++) {
					String attribGroup = (String)cte.alAttribGroups.get(k);
					ArrayList al = (ArrayList) hmAttrGrp.get(attribGroup);
	
					if (al == null)
						throw new IllegalArgumentException("Attribute group not found : " + attribGroup);
	
					for(int j=0; j<al.size(); j++) {
						AttributeEntry ae = (AttributeEntry) al.get(j);
						mdt.addAttribute(buildMetadataAttrib(ae));
					}
				}
			}
	
			//--- now add the elements belonging to this complex type to the mdt

			for(int j=0; j<cte.alElements.size(); j++) {
				ElementEntry ee = (ElementEntry) cte.alElements.get(j);

// Three situations:
// 1. element is a container element - group, choice or sequence - so recurse 
// and get elements from any containers nested inside this container - 
// we generate a name to use from the cte.name and element position

				if ( ee.groupElem || ee.choiceElem || ee.sequenceElem ) {
					Integer baseNr = j;
					String baseName = cte.name;
					String extension;
					ArrayList elements;
					if (ee.choiceElem) {
						extension = Edit.RootChild.CHOICE;
						elements = ee.alContainerElems;
					} else if (ee.groupElem) {
						extension = Edit.RootChild.GROUP;
						GroupEntry group = (GroupEntry)hmGroups.get(ee.ref);
						elements = group.alElements;
					} else {
						extension = Edit.RootChild.SEQUENCE;
						elements = ee.alContainerElems;
					}
					String type = ee.name = baseName+extension+baseNr;
					ArrayList newCtes = createTypeAndResolveNestedContainers(schemaId,
																						mds,elements,
																						baseName,extension,baseNr);
					if (newCtes.size() != 0) {
						for (int ctCntr = 0;ctCntr < newCtes.size();ctCntr++) {
							ComplexTypeEntry newCte = (ComplexTypeEntry)newCtes.get(ctCntr);
							i.add(newCte); i.previous();
						}
					}
					mds.addElement(ee.name, type, new ArrayList(), new ArrayList(), "");
					mdt.addElementWithType(ee.name, type, ee.min, ee.max);

// 2. element is a reference to a global element so check if abstract or
//    if the type needs to be turned into a choice ie. it has one element 
//    which is the head of a substitution group or a new choice type
//    is created for the element or just add it if none of 
//    the above
				} else if (ee.ref != null) {
					boolean choiceType = (cte.alElements.size() == 1);
					handleRefElement(j,schemaId,cte.name,choiceType,ee,mdt,mds);


// 3. element is a local element so get type or process local complex/simpleType//    and add to the ListIterator if complex
				} else if (ee.name != null) {
					ComplexTypeEntry newCte = handleLocalElement(j,schemaId,cte.name,ee,mdt,mds);
					if (newCte != null) {
						i.add(newCte); i.previous();
					}

				} else {
					throw new IllegalArgumentException("Unknown element type at position "+j+" in complexType "+cte.name);
				}
			}
			mds.addType(cte.name, mdt);
		}

		return mds;
	}
	
	//---------------------------------------------------------------------------
	//---
	//--- Recurse on substitution links until we get a type that we can use
	//---
	//---------------------------------------------------------------------------
	private String recurseOnSubstitutionLinks(String elemName) {
		String elemLinkName = (String) hmSubsLink.get(elemName);
		if (elemLinkName != null) {
			String elemLinkType = (String) hmElements.get(elemLinkName);
			if (elemLinkType != null) return elemLinkType; // found concrete type!
			else recurseOnSubstitutionLinks(elemLinkName); // keep trying
		}
		return null; // Cannot find a type so return null
	}

	//---------------------------------------------------------------------------
	//---
	//--- Build a local element into the MetadataType and Schema
	//---
	//---------------------------------------------------------------------------
	private ComplexTypeEntry handleLocalElement(Integer elementNr,String schemaId,String baseName,ElementEntry ee,MetadataType mdt,MetadataSchema mds) {

		ComplexTypeEntry cteInt = null;
		ArrayList elemRestr = new ArrayList();

		if (ee.type == null) {
			if (ee.complexType != null) {
				cteInt = ee.complexType;
				ee.type = cteInt.name = ee.name+"HSI"+elementNr+
												getUnqualifiedName(baseName);
			} else if (ee.simpleType != null) {
				ee.type = "string";
				if (ee.simpleType.alEnum != null) // add enumerations if any
					elemRestr.add(ee.simpleType.alEnum);
			} else {
				System.out.println("WARNING: Could not find type for "+ee.name+" - assuming string");
				ee.type = "string";
			} 
		}

		mds.addElement(ee.name, ee.type, elemRestr, new ArrayList(), "");
		mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);

		return(cteInt);

	}

	//---------------------------------------------------------------------------
	//---
	//--- Return list of substitutes if we want to override those derived
	//--- from the schema XSDs - this is schema dependent and defined in 
	//--- the schema-substitutes files and is used for elements such as 
	//--- gco:CharacterString in the iso19139 schema
	//--- 
	//--- returns null if there are no user defined substitutes, 
	//---     OR  an empty list if removal of all schema substitutes is required 
	//---     OR  a list of ElementEntry objects to use as substitutes
	//---
	//---------------------------------------------------------------------------
	private ArrayList getOverRideSubstitutes(String elementName) {

		ArrayList subs = (ArrayList)hmSubsGrp.get(elementName);
		ArrayList ssOs = ssOverRides.getSubstitutes(elementName);
		if (ssOs != null && subs != null) {
			ArrayList results = new ArrayList();
			ArrayList validSubs = (ArrayList)hmSubsNames.get(elementName);
			for (int i = 0;i < ssOs.size();i++) {
				String altSub = (String)ssOs.get(i); 
				if (validSubs != null && !validSubs.contains(altSub)) {
					System.out.println("WARNING: schema-substitutions.xml specified "+altSub+" for element "+elementName+" but the schema does not define this as a valid substitute");
				}
				for (int k = 0;k < subs.size();k++) {
					ElementEntry ee = (ElementEntry)subs.get(k);
					if (ee.name.equals(altSub)) {
						results.add(ee);
					}
				}
			}
			if (results.size() == 0 && validSubs != null) {
				System.out.println("WARNING: schema-substitutions.xml has wiped out XSD substitution list for "+elementName);
			}
			return results;
		}
		return null;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Build a reference to a global element into the MetadataType and Schema
	//---
	//---------------------------------------------------------------------------
	private void handleRefElement(Integer elementNr,String schemaId,String baseName,boolean choiceType,ElementEntry ee,MetadataType mdt,MetadataSchema mds) {

		String type = (String) hmElements.get(ee.ref);
		boolean isAbstract = hmAbsElems.containsKey(ee.ref);

		// If we have user specified substitutions then use them otherwise
		// use those from the schema
		boolean doSubs = true;
		ArrayList al = getOverRideSubstitutes(ee.ref);
		if (al == null) al = (ArrayList)hmSubsGrp.get(ee.ref);
		else doSubs = false;

		if ((al != null && al.size() > 0) || isAbstract ) {
			if (choiceType) {
			// The complex type has only one element then make it a choice type if
			// there are concrete elements in the substitution group
				Integer elementsAdded = assembleChoiceElements(mdt,al,doSubs);
				if (!isAbstract && doSubs) {
					/* 
					 * Control of substitution lists is via the schema-substitutions.xml
					 * file because some profiles do not mandate substitutions of this
					 * kind eg. wmo
					 *
					if (elementsAdded == 1 && 
							(getPrefix(mdt.getElementAt(0)).equals(getProfile(schemaId))) && 
											schemaId.startsWith("iso19139")) { 
						Logger.log("Sticking with "+mdt.toString()+" for "+ee.ref);
					} else { 
					 * 
					 */
						mdt.addRefElementWithType(ee.ref,type,ee.min,ee.max);
						elementsAdded++;
					/*}*/
				}
				mdt.setOrType(elementsAdded > 1);
			} else {
			// The complex type has real elements and/or attributes so make a new 
			// choice element with type and replace this element with it 
				MetadataType mdtc = new MetadataType();
				Integer elementsAdded = assembleChoiceElements(mdtc,al,doSubs);
				if (!isAbstract && doSubs) {
					mdtc.addRefElementWithType(ee.ref,ee.type,ee.min,ee.max);
					elementsAdded++;
				}
				mdtc.setOrType(elementsAdded > 1);
				type = ee.ref+Edit.RootChild.CHOICE+elementNr;
				String name = type;
				mds.addType(type,mdtc);
				mds.addElement(name,type,new ArrayList(),new ArrayList(), "");
				mdt.addElementWithType(name,type,ee.min,ee.max);
			}
		} else if (!isAbstract) {
			mdt.addRefElementWithType(ee.ref,type,ee.min,ee.max);
		} else {
			System.out.println("WARNING: element "+ee.ref+" from "+baseName+" has fallen through the logic (abstract: "+isAbstract+") - ignoring");
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Recurse on attributeGroups to build a list of AttributeEntry objects
	//---
	//---------------------------------------------------------------------------
	private ArrayList resolveNestedAttributeGroups(AttributeGroupEntry age) {
		ArrayList attrs = new ArrayList();

		if (age.alAttrGrps.size() > 0) {
			for (int i=0;i<age.alAttrGrps.size();i++) {
				AttributeGroupEntry ageInternal = 
								(AttributeGroupEntry)age.alAttrGrps.get(i);
				AttributeGroupEntry ageRef = 
								(AttributeGroupEntry)hmAttrGpEn.get(ageInternal.ref);
				if (ageRef == null) 
					throw new IllegalArgumentException
							("ERROR: cannot find attributeGroup with ref "+ageInternal.ref);
				attrs.addAll(resolveNestedAttributeGroups(ageRef));
			}
		}
		attrs.addAll(age.alAttrs);
		return attrs;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Descend recursively to deal with nested containers 
	//---
	//---------------------------------------------------------------------------
	private ArrayList createTypeAndResolveNestedContainers(String schemaId, 
														MetadataSchema mds,ArrayList al,String baseName,
														String extension,Integer baseNr) {

		ArrayList complexTypes = new ArrayList();

		Integer oldBaseNr = baseNr;
		if (al == null) return complexTypes;
		MetadataType mdt = new MetadataType();
		if (extension.contains(Edit.RootChild.CHOICE)) mdt.setOrType(true);
		for(int k=0; k<al.size(); k++)
		{
			ElementEntry ee = (ElementEntry) al.get(k);
			baseNr++;

			// CHOICE
			if (ee.choiceElem) {
				String newExtension = Edit.RootChild.CHOICE;
				ArrayList newCtes = createTypeAndResolveNestedContainers(schemaId,mds,ee.alContainerElems,baseName,newExtension,baseNr);
				if (newCtes.size() > 0) complexTypes.addAll(newCtes);
				ee.name = ee.type = baseName+newExtension+baseNr;
				mds.addElement(ee.name,ee.type,new ArrayList(),new ArrayList(), "");
				mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);

			// GROUP
			} else if (ee.groupElem) {
				String newExtension = Edit.RootChild.GROUP;
				if (ee.ref != null) {
					GroupEntry group = (GroupEntry) hmGroups.get(ee.ref);
					ArrayList alGroupElements = group.alElements;
					ArrayList newCtes = createTypeAndResolveNestedContainers(schemaId,mds,alGroupElements,baseName,newExtension,baseNr);
					if (newCtes.size() > 0) complexTypes.addAll(newCtes);
					ee.name = ee.type = baseName+newExtension+baseNr;
					mds.addElement(ee.name,ee.type,new ArrayList(),new ArrayList(), "");
					mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);
				} else {
					System.out.println("WARNING: group element ref is NULL in "+baseName+extension+baseNr);
				}

			// SEQUENCE
			} else if (ee.sequenceElem) {
				String newExtension = Edit.RootChild.SEQUENCE;
				ArrayList newCtes = createTypeAndResolveNestedContainers(schemaId,mds,ee.alContainerElems,baseName,newExtension,baseNr);
				if (newCtes.size() > 0) complexTypes.addAll(newCtes);
				ee.name = ee.type = baseName+newExtension+baseNr;
				mds.addElement(ee.name,ee.type,new ArrayList(),new ArrayList(), "");
				mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);

			// ELEMENT 
			} else {
				if (ee.name != null) {
					ComplexTypeEntry newCte = handleLocalElement(k,schemaId,baseName,ee,mdt,mds);
					if (newCte != null) complexTypes.add(newCte);
				}
				else {
					handleRefElement(k,schemaId,baseName,false,ee,mdt,mds);
				}
			}
		}
		mds.addType(baseName+extension+oldBaseNr,mdt);
		return complexTypes;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Descend recursively to deal with abstract elements
	//---
	//---------------------------------------------------------------------------
	private int assembleChoiceElements(MetadataType mdt,ArrayList al,boolean doSubs) {

		int number = 0;
		if (al == null) return number;
		for(int k=0; k<al.size(); k++)
		{
			ElementEntry ee = (ElementEntry) al.get(k);
			if (ee.abstrElem) {
				Integer numberRecursed = assembleChoiceElements(mdt,(ArrayList) hmSubsGrp.get(ee.name),doSubs);
				number = number + numberRecursed;
			} else {
				number++;
				mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);
				// Also add any elements that substitute for this one so that we can 
				// complete the list of choices if required 
				if (doSubs) {
					ArrayList elemSubs = (ArrayList)hmSubsGrp.get(ee.name);
					if (elemSubs != null) {
						for (int j = 0;j < elemSubs.size();j++) {
							ElementEntry eeSub = (ElementEntry)elemSubs.get(j);
							mdt.addElementWithType(eeSub.name,eeSub.type,eeSub.min,eeSub.max);
							number++;
						}
					}
				}
			}
		}
		return number;
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

		String path = new File(xmlSchemaFile).getParent() + "/";

		//--- load xml-schema

		elRoot = Xml.loadFile(xmlSchemaFile);

		// change target namespace
		String oldtargetNS       = targetNS;
		String oldtargetNSPrefix = targetNSPrefix;
		targetNS = elRoot.getAttributeValue("targetNamespace");
		targetNSPrefix = null;


		if (targetNS != null) {
			for (Iterator i = elRoot.getAdditionalNamespaces().iterator(); i.hasNext(); ) {
				Namespace ns = (Namespace)i.next();
				if (targetNS.equals(ns.getURI())) {
					targetNSPrefix = ns.getPrefix();
					break;
				}
			}
			if ("".equals(targetNSPrefix)) targetNSPrefix = null;
		}
		// This is a bug in jdom - seems that if the namespace prefix is xml: and 
		// namespace is as shown in the if statement then getAdditionalNamespaces 
		// doesn't return the namespaces and we can't get a prefix - this fix gets
		// around that bug
		if (xmlSchemaFile.contains("xml.xsd") && targetNS.equals("http://www.w3.org/XML/1998/namespace")) targetNSPrefix="xml";

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
		hmAllAttrs .clear();
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
				buildGlobalAttributeGroup(ei);

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


		if (ee.substGroup != null)
		{
			ArrayList al = (ArrayList) hmSubsGrp.get(ee.substGroup);

			if (al == null) {
				al = new ArrayList();
				hmSubsGrp.put(ee.substGroup, al);
			}
			al.add(ee);

			if (hmSubsLink.get(ee.name) != null) {
				throw new IllegalArgumentException("Substitution link collision for : "+ee.name+" link to "+ee.substGroup);
			} else {
				hmSubsLink.put(ee.name,ee.substGroup);
			}
		}
		if (ee.abstrElem)
		{
			if (hmAbsElems.containsKey(ee.name))
				throw new IllegalArgumentException("Namespace collision for : " + ee.name);
			hmAbsElems.put(ee.name, ee.type);


			return;
		}
		if (ee.complexType != null)
		{
			String type = ee.name+"HSI";
			ee.complexType.name = type;
			ee.type = type;
			if (hmElements.containsKey(ee.name))
				throw new IllegalArgumentException("Namespace collision for : " + ee.name);

			hmElements.put(ee.name, type);
			hmTypes.put(type, ee.complexType);

		}
		else if (ee.simpleType != null)
		{
			String type = ee.name;
			if (hmElements.containsKey(ee.name))
				throw new IllegalArgumentException("Namespace collision for : " + ee.name);
			ee.type = "string";
			hmElements .put(ee.name, ee.type);
			hmElemRestr.put(ee.name, ee.simpleType.alEnum);

		}
		else
		{
			if (ee.type == null && ee.substGroup == null) {
				System.out.println("WARNING: "+ee.name+" is a global element without a type - assuming a string");
				ee.type ="string";
			}
			hmElements.put(ee.name, ee.type);

		}
		if (ee.name.contains("SensorML")) {
			Logger.log("SensorML element detected "+ee.name+" "+ee.complexType.name);
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
		hmAllAttrs.put(at.name, at);
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

	private void buildGlobalAttributeGroup(ElementInfo ei)
	{

		AttributeGroupEntry age = new AttributeGroupEntry(ei);
		if (hmAttrGpEn.containsKey(age.name))
			throw new IllegalArgumentException("Namespace collision for : " + age.name);
		hmAttrGpEn.put(age.name, age);

	}

	//---------------------------------------------------------------------------
	//---
	//--- Add in attributes from complexType with SimpleContent that restricts
	//--- or extends a base type (if any)
	//---
	//---------------------------------------------------------------------------

	private ArrayList resolveAttributeInheritanceFromSimpleContent(ComplexTypeEntry cte)
	{
		ArrayList result = new ArrayList();

		if (cte.simpleContent == null) {
			throw new IllegalArgumentException("SimpleContent must be present in base type of the SimpleContent in "+cte.name);
		} else {

			// recurse if we need to follow the base type
			
			String baseType = cte.simpleContent.base;
			ComplexTypeEntry baseCTE = (ComplexTypeEntry) hmTypes.get(baseType);
			if (baseCTE != null)
				result = new ArrayList(resolveAttributeInheritanceFromSimpleContent(baseCTE));
	
			// if the base type was a restriction then replace the attributes we got
			// from the restriction with these
			if (cte.simpleContent.restriction) {
				ArrayList adds = (ArrayList)cte.simpleContent.alAttribs.clone();
				for (int i = 0;i < result.size();i++) {
					AttributeEntry attrib = (AttributeEntry)result.get(i);
					for (int j = 0;j < adds.size();j++) {
						AttributeEntry attribOther = (AttributeEntry)adds.get(j);
						boolean eqAttrib = eqAttribs(attribOther,attrib);
						if (eqAttrib) {
							result.set(i,attribOther);	
						} 
					}
				}
			}
			// otherwise base type was an extension so add the attributes we got
			// from the extension to these
			else 
				result.addAll((ArrayList)cte.simpleContent.alAttribs.clone());
		
			// No one seems clear on what to do with attributeGroups so treat them
			// as an extension
			if (cte.simpleContent.alAttribGroups != null) {
				for (int k=0;k<cte.simpleContent.alAttribGroups.size();k++) {
					String attribGroup = (String)cte.simpleContent.alAttribGroups.get(k);
					ArrayList al = (ArrayList) hmAttrGrp.get(attribGroup);

					if (al == null)
						throw new IllegalArgumentException("Attribute group not found : " + attribGroup);

					for(int j=0; j<al.size(); j++) 
						result.add(al.get(j));
				}
			}
		}


		return result;
	}

	/** function to test whether two AttributeEntry objects have the same name
	 */
	boolean eqAttribs(AttributeEntry attribOther,AttributeEntry attrib) {
		if (attribOther.name != null) {
			if (attrib.name != null) {
				if (attribOther.name.equals(attrib.name)) return true;
			} else {
				if (attribOther.name.equals(attrib.reference)) return true;
			}
		} else {
			if (attrib.name != null) {
				if (attribOther.reference.equals(attrib.name)) return true;
			} else {
				if (attribOther.reference.equals(attrib.reference)) return true;
			}
		}
		return false;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add in attributes from complexType with ComplexContent that restricts
	//--- or extends a base type (if any)
	//---
	//---------------------------------------------------------------------------

	private ArrayList resolveAttributeInheritance(ComplexTypeEntry cte)
	{

		if (cte.complexContent == null)
			return cte.alAttribs;
		
		String baseType = cte.complexContent.base;
		ComplexTypeEntry baseCTE = (ComplexTypeEntry) hmTypes.get(baseType);
		if (baseCTE == null)
			throw new IllegalArgumentException("Base type not found for : " + baseType);

		ArrayList result = new ArrayList(resolveAttributeInheritance(baseCTE));

		// if the base type was a restriction then replace the attributes we got
		// from the restriction with these

		if (cte.complexContent.restriction) {
			ArrayList adds = (ArrayList)cte.complexContent.alAttribs;
			for (int i = 0;i < result.size();i++) {
				AttributeEntry attrib = (AttributeEntry)result.get(i);
				for (int j = 0;j < adds.size();j++) {
					AttributeEntry attribOther = (AttributeEntry)adds.get(j);
					boolean eqAttrib = eqAttribs(attribOther,attrib);
					if (eqAttrib) {
						result.set(i,attribOther);	
					} 
				}
			}
		}
		// otherwise base type was an extension so add the attributes we got
		// from the extension to these
		else { 
			result.addAll((ArrayList)cte.complexContent.alAttribs);
			if (cte.complexContent.alAttribGroups != null) {
				for (int k=0;k<cte.complexContent.alAttribGroups.size();k++) {
					String attribGroup = (String)cte.complexContent.alAttribGroups.get(k);					ArrayList al = (ArrayList) hmAttrGrp.get(attribGroup);
					if (al == null) 
						throw new IllegalArgumentException("Attribute group not found : " + attribGroup);
					for(int j=0; j<al.size(); j++)
						result.add(al.get(j));
				}
			}
		}

		// No one seems clear on what to do with attributeGroups so treat them
		// as an extension
		if (baseCTE.alAttribGroups != null) {
			for (int k=0;k<baseCTE.alAttribGroups.size();k++) {
				String attribGroup = (String)baseCTE.alAttribGroups.get(k);
				ArrayList al = (ArrayList) hmAttrGrp.get(attribGroup);

				if (al == null)
					throw new IllegalArgumentException("Attribute group not found : " + attribGroup);

				for(int j=0; j<al.size(); j++) 
					result.add(al.get(j));
			}
		}

		return result;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add in elements to complexType that come from base type (if any)
	//---
	//---------------------------------------------------------------------------

	private ArrayList resolveInheritance(ComplexTypeEntry cte)
	{
		if (cte == null || cte.complexContent == null)
			return cte.alElements;

		String baseType = cte.complexContent.base;
		ComplexTypeEntry baseCTE = (ComplexTypeEntry) hmTypes.get(baseType);
		if (baseCTE == null)
			throw new IllegalArgumentException("Base type not found for : " + baseType);

		// skip over the elements in the base type of a restricted complex type 
		// by ending the recursion
		ArrayList result = new ArrayList();
		if (!cte.complexContent.restriction)
		 result = new ArrayList(resolveInheritance(baseCTE));

		result.addAll((ArrayList)cte.complexContent.alElements);

		return result;
	}

	//---------------------------------------------------------------------------

	private MetadataAttribute buildMetadataAttrib(AttributeEntry ae)
	{
		String name = ae.name;
		String ref  = ae.reference;
		String value = ae.defValue;
		boolean overRequired = ae.required;

		MetadataAttribute ma = new MetadataAttribute();

		if (ref != null) {
			ae = (AttributeEntry) hmAttribs.get(ref);
			if (ae == null)
				throw new IllegalArgumentException("Reference '"+ref+"' not found for attrib : " +name);
		} 

		if (ref != null && ref.contains(":"))
			ma.name = ref;
		else
			ma.name = ae.unqualifiedName;

		if (value != null) 
			ma.defValue = value;
		else
			ma.defValue = ae.defValue;

		ma.required = overRequired;

		for(int k=0; k<ae.alValues.size(); k++)
			ma.values.add(ae.alValues.get(k));

		return ma;
	}

	//---------------------------------------------------------------------------

	private String getPrefix(String qname) {
		int pos = qname.indexOf(":");
		if (pos < 0) return "";
		else         return qname.substring(0, pos);
	}

	//--------------------------------------------------------------------------
	
	public String getUnqualifiedName(String qname) {
		int pos = qname.indexOf(":");
		if (pos < 0) return qname;
		else         return qname.substring(pos + 1);
	}

	//---------------------------------------------------------------------------

	private String getProfile(String name) {
		int pos = name.indexOf(".");
		if (pos < 0) return "";
		else         return name.substring(pos+1);
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

