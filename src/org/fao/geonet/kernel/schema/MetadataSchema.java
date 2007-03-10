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
	private HashMap hmElements = new HashMap();
	private HashMap hmRestric  = new HashMap();
	private HashMap hmTypes    = new HashMap();

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
		return (MetadataType) hmTypes.get(type);
	}

	//---------------------------------------------------------------------------

	public String getElementType(String elem)
	{
		//System.out.println("in getElementType(" + elem + ")"); // DEBUG

		return (String) hmElements.get(elem);
	}

	//---------------------------------------------------------------------------
	/** A simple type is a type that has no children and no attributes (but can
	  * have restrictions on its value)
	  */

	public boolean isSimpleElement(String elem)
	{
		//System.out.println("in isSimpleElement(" + elem + ")"); // DEBUG

		return !hmTypes.containsKey(getElementType(elem));
	}

	//---------------------------------------------------------------------------

	public ArrayList getElementValues(String elem)
	{
		return (ArrayList) hmRestric.get(elem);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package protected API methods
	//---
	//---------------------------------------------------------------------------

	void addElement(String name, String type, ArrayList values)
	{
		//System.out.println("#### adding element " + name); // DEBUG

		hmElements.put(name, type);
		hmRestric .put(name, values);
	}

	//---------------------------------------------------------------------------

	void addType(String name, MetadataType mdt)
	{
		//System.out.println("#### adding type " + name); // DEBUG

		mdt.setName(name);
		hmTypes.put(name, mdt);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Debug methods
	//---
	//---------------------------------------------------------------------------

	public String dump(String type)
	{
		return dump(type, new HashSet());
	}

	//---------------------------------------------------------------------------

	private String dump(String type, HashSet hs)
	{
		StringBuffer sb = new StringBuffer("");

		MetadataType mdt = getTypeInfo(type);

		if (mdt == null)
			throw new IllegalArgumentException("Unknown type : " + type);

		hs.add(type);

		sb.append(type);
		sb.append(" (");

		String sep = mdt.isOrType() ? " | " : ", ";

		for(int i=0; i<mdt.getElementCount(); i++)
		{
			String elem = mdt.getElementAt(i);

			int min = mdt.getMinCardinAt(i);
			int max = mdt.getMaxCardinAt(i);

			sb.append(elem);

			if (min == 0)
			{
				if (max == 1)	sb.append("?");
					else 			sb.append("*");
			}
			else
			{
				if (max == 1)	sb.append("");
					else 			sb.append("+");
			}

			if (i < mdt.getElementCount() -1)
				sb.append(sep);
		}

		sb.append(")\n\n");

		ArrayList al = new ArrayList();

		for(int i=0; i<mdt.getElementCount(); i++)
		{
			String elem = mdt.getElementAt(i);
			type = getElementType(elem);

			if (type == null)
				throw new IllegalArgumentException("Unknown type for elem : " + elem);

			sb.append(elem +" : "+type+"\n");

			if (!isSimpleElement(type))
				al.add(type);
		}

		sb.append("\n");

		for(int i=0; i<al.size(); i++)
		{
			type = (String) al.get(i);

			if (!hs.contains(type))
				sb.append(dump(type, hs));
		}

		return sb.toString();
	}
}

//==============================================================================

