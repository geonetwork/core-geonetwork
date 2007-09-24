//==============================================================================
//===
//===   MetadataSchema
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

import org.jdom.Element;

//==============================================================================

public class MetadataSchema
{
	private Map<String,List<String>> hmElements = new HashMap<String,List<String>>();
	private Map<String,List<List>> hmRestric  = new HashMap<String,List<List>>();
	private HashMap hmTypes    = new HashMap();
	private HashMap hmSubs		 = new HashMap();
	private HashMap hmSubsLink = new HashMap();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	MetadataSchema(Element root) {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public MetadataType getTypeInfo(String type)
	{
		Logger.log("metadataSchema: Asking for type "+type);
		if (hmTypes.get(type) == null) return new MetadataType();
		else return (MetadataType) hmTypes.get(type);
	}

	//---------------------------------------------------------------------------

	public String getElementType(String elem,String parent) throws Exception
	{
		// two cases here - if we have just one element (or a substitute) with 
		// this name then return its type

	  Logger.log("metadataSchema: Asking for element "+elem+" parent "+parent);
		List<String> childType = hmElements.get(elem);
		if (childType == null) {
			// Check and see whether we can substitute another element from the
			// substitution link 
			elem = (String) hmSubsLink.get(elem);
	  	Logger.log(" -- substitute "+elem);
			childType = hmElements.get(elem);
			if (childType == null) 
				throw new IllegalArgumentException("Mismatch between schema and xml: No type for 'element' : "+elem+" with parent "+parent);
		}
		if (childType.size() == 1) return childType.get(0);

		Logger.log("-- Multiple elements so moving to parent");
		// OTHERWISE get the type by examining the parent:
		// for each parent with that name parent
		// 1. retrieve its mdt 
		List<String> exType = hmElements.get(parent);
		Iterator i = exType.iterator();
		while (i.hasNext()) { 
		// 2. search that mdt for the element names elem
			String type = (String)i.next();
			MetadataType mdt = getTypeInfo(type);
			for (int k = 0;k < mdt.getElementCount();k++) {
				String elemTest = mdt.getElementAt(k);
		// 3. return the type name of that element
				if (elem.equals(elemTest)) return mdt.getElementTypeAt(k);
			}
		}

		Logger.log("ERROR: could not find type for element "+elem+" with parent "+parent);
		return null;
	}

	//---------------------------------------------------------------------------
	/** A simple type is a type that has no children and no attributes (but can
	  * have restrictions on its value)
	  */

	public boolean isSimpleElement(String elem,String parent) throws Exception
	{
		String type = getElementType(elem,parent);
		if (type == null) return false;
		else return !hmTypes.containsKey(type);
	}

	//---------------------------------------------------------------------------

	public ArrayList getElementSubs(String elem)
	{
		return((ArrayList)hmSubs.get(elem));
	}

	//---------------------------------------------------------------------------

	public ArrayList getElementValues(String elem,String parent) throws Exception
	{

		String type = getElementType(elem,parent);
		String restricName = elem;
		if (type != null) restricName = restricName+"+"+type;

		// two cases here - if we have just one element with this name 
		// then return its values
		List<List> childValues = hmRestric.get(restricName);
		if (childValues == null) return null;
		if (childValues.size() == 1) return (ArrayList)childValues.get(0);

		// OTHERWISE we don't know what to do so return the first one anyway! This
		// should not happen....
		Logger.log("WARNING: returning first set of values for element "+elem+" this should not happen and it may not be correct.....check logs for VALUESCLASH statements and fix schema");
		return (ArrayList)childValues.get(0);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package protected API methods
	//---
	//---------------------------------------------------------------------------

	void addElement(String name, String type, ArrayList alValues, ArrayList alSubs, String subLink)
	{
		// first just add the subs - because these are for global elements we 
		// never have a clash because global elements are all in the same scope
		// and are thus unique
		if (alSubs != null && alSubs.size() > 0) hmSubs.put(name,alSubs);
		if (subLink != null && subLink.length() > 0) hmSubsLink.put(name,subLink);

		List<String> exType = hmElements.get(name);

		// it's already there but the type has been added already
		if (exType != null && exType.contains(type)) return; 

		// it's already there but doesn't have this type 
		if (exType != null && !(exType.contains(type))) { 
			Logger.log("CLASH: trying to add "+name+" with type "+type+": already exists with type: "+exType+" - adding overflows and code to cope");


		// it's not there so add a new list
		} else {
			hmElements.put(name, exType = new ArrayList<String>());
		}
		exType.add(type);

		String restricName = name;
		if (type != null) restricName = name+"+"+type;

		// it's already there
		List<List> exValues = hmRestric.get(restricName);
		if (exValues != null) {
			Logger.log("VALUESCLASH: trying to add "+restricName+" with values "+alValues+": already exists with values "+exValues+" - this should not happen");

		// it's not there so add a new list of lists
		} else {
			hmRestric .put(restricName, exValues = new ArrayList<List>());
		}
		exValues.add(alValues);
	}

	//---------------------------------------------------------------------------

	void addType(String name, MetadataType mdt)
	{
		mdt.setName(name);
		hmTypes.put(name, mdt);
	}

}

//==============================================================================

