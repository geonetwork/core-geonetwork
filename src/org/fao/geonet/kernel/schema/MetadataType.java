//==============================================================================
//===
//===   MetadataType
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

import java.util.ArrayList;

//==============================================================================

public class MetadataType
{
	private String  name;
	private boolean isOrType;

	private ArrayList alElements = new ArrayList();
	private ArrayList alMinCard  = new ArrayList();
	private ArrayList alMaxCard  = new ArrayList();
	private ArrayList alAttribs  = new ArrayList();

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

	//--------------------------------------------------------------------------
	/** Return the component in a given position */

	public String getElementAt(int pos)
	{
		return (String) alElements.get(pos);
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

		for(int i=0; i<alElements.size(); i++)
		{
			String comp = getElementAt(i);

			int    min  = getMinCardinAt(i);
			int    max  = getMaxCardinAt(i);

			String sMax = (max>1) ? "n" : max +"";

			res += comp + "/" + min+ "-" + sMax + " ";
		}

		return res;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package protected API methods
	//---
	//---------------------------------------------------------------------------

	void addElement(String name, int minCard, int maxCard)
	{
		alElements.add(name);

		alMinCard.add(new Integer(minCard));
		alMaxCard.add(new Integer(maxCard));
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

