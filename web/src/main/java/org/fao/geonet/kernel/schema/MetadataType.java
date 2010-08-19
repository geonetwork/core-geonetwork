//==============================================================================
//===
//===   MetadataType
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.schema;

import org.fao.geonet.constants.Edit;

import java.util.ArrayList;

//==============================================================================

public class MetadataType
{
	private String  name;
	private boolean isOrType;
	public boolean hasContainers = false;

	private ArrayList alElements   = new ArrayList();
	private ArrayList alTypes      = new ArrayList();
	private ArrayList alMinCard    = new ArrayList();
	private ArrayList alMaxCard    = new ArrayList();
	private ArrayList alAttribs    = new ArrayList();
	private ArrayList alExamineSubs  = new ArrayList();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	MetadataType() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public int getElementCount() { return alElements.size(); }

	public void resetElementAt(int pos,String name,String type) {
		alElements.set(pos,name);
		alTypes.set(pos,type);
	}

	public ArrayList getElementList() {
		return (ArrayList) alElements.clone();
	}

	//--------------------------------------------------------------------------
	/** Return the component in a given position */

	public String getElementAt(int pos)
	{
		return (String) alElements.get(pos);
	}

	//--------------------------------------------------------------------------
	/** Return the type of element in a given position */

	public String getElementTypeAt(int pos)
	{
		return (String) alTypes.get(pos);
	}

	//--------------------------------------------------------------------------
	/** Return true if subs for element in given position need to be examined, 
	 *  false otherwise */

	public Boolean examineSubs(int pos)
	{
		return (Boolean) alExamineSubs.get(pos);
	}

	//--------------------------------------------------------------------------
	/** Returns the min cardinality of element in a given pos */

	public int getMinCardinAt(int pos)
	{
		return ((Integer) alMinCard.get(pos)).intValue();
	}

	//--------------------------------------------------------------------------
	/** Returns the max cardinality of element in a given pos */

	public int getMaxCardinAt(int pos)
	{
		return ((Integer) alMaxCard.get(pos)).intValue();
	}

	//--------------------------------------------------------------------------
	/** Returns true is this type has children in or mode */

	public boolean isOrType() { return isOrType; }

	//--------------------------------------------------------------------------

	public int getAttributeCount() { return alAttribs.size(); }

	//--------------------------------------------------------------------------

	public MetadataAttribute getAttributeAt(int i)
	{
		return (MetadataAttribute) alAttribs.get(i);
	}

	//--------------------------------------------------------------------------

	public String getName() { return name; }

	//--------------------------------------------------------------------------

	public String toString()
	{
		String res = "";

		if (isOrType) res += "IsOrType = TRUE ";
		else res += "IsOrType = FALSE ";

		for(int i=0; i<alElements.size(); i++)
		{
			String comp = getElementAt(i);

			int    min  = getMinCardinAt(i);
			int    max  = getMaxCardinAt(i);

			String sMax = (max>1) ? "n" : max +"";

			res += comp + "/" + min+ "-" + sMax + " ";
		}

	
		String attrs = "";
		for(int i=0; i<alAttribs.size(); i++)
		{
			attrs += "Attribute ("+i+") "+getAttributeAt(i).name+": ";
			ArrayList alAtts = getAttributeAt(i).values;
			if (alAtts.size() > 0) {
				attrs += getAttributeAt(i).values.toString();
				attrs += " ";
			}
			attrs += "Default Value: "+getAttributeAt(i).defValue;
			if (getAttributeAt(i).required) attrs += " REQUIRED ";
		}
		if (attrs.length() > 0)
			res += " Attributes: "+attrs;

		return res;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package protected API methods
	//---
	//---------------------------------------------------------------------------

	void addElementWithType(String name, String elementType, int minCard, int maxCard)
	{
		addElement(name,elementType,false,minCard,maxCard);
	}

	//---------------------------------------------------------------------------

	void addElementWithNoType(String name, int minCard, int maxCard)
	{
		addElement(name,null,false,minCard,maxCard);
	}

	//---------------------------------------------------------------------------
	
	void addRefElementWithType(String name, String elementType, int minCard, int maxCard)
	{
		addElement(name,elementType,true,minCard,maxCard);
	}

	//---------------------------------------------------------------------------

	void addRefElementWithNoType(String name, int minCard, int maxCard)
	{
		addElement(name,null,true,minCard,maxCard);
	}

	//---------------------------------------------------------------------------

	void addElement(String name, String elementType, Boolean examineElementSubs, int minCard, int maxCard)
	{

		// Don't add the same element to a type 
		if (alElements.contains(name)) {
			if (alElements.indexOf(name) == alTypes.indexOf(elementType)) {
				return;
			}
		}
		alElements.add(name);
		alTypes.add(elementType);
		alExamineSubs.add(examineElementSubs);
		alMinCard.add(new Integer(minCard));
		alMaxCard.add(new Integer(maxCard));
		if (name.contains(Edit.RootChild.CHOICE)||
				name.contains(Edit.RootChild.GROUP)||
				name.contains(Edit.RootChild.SEQUENCE)) hasContainers = true;
	}

	//---------------------------------------------------------------------------

	void addAttribute(MetadataAttribute ma)
	{
		alAttribs.add(ma);
	}

	//---------------------------------------------------------------------------

	void setName(String name)
	{
		this.name = name;
	}

	//---------------------------------------------------------------------------

	void setOrType(boolean yesno)
	{
		isOrType = yesno;
	}
}

//==============================================================================

