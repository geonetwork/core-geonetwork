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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import jeeves.utils.Xml;
import org.jdom.Element;

//=============================================================================

public class SchemaSuggestions
{
	private Hashtable htFields = new Hashtable();

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public SchemaSuggestions(String xmlSuggestFile) throws Exception
	{
		Element sugg = Xml.loadFile(xmlSuggestFile);

		List list = sugg.getChildren();

		for(Iterator i=list.iterator(); i.hasNext();)
		{
			Element el = (Element) i.next();

			if (el.getName().equals("field"))
				htFields.put(el.getAttributeValue("name"), el);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public boolean isSuggested(String parent, String child)
	{
		Element el = (Element) htFields.get(parent);

		if (el == null)
			return false;

		List list = el.getChildren();

		for(Iterator i=list.iterator(); i.hasNext();)
		{
			el = (Element) i.next();

			if (el.getName().equals("suggest"))
			{
				String name = el.getAttributeValue("name");

				if (child.equals(name))
					return true;
			}
		}

		return false;
	}
}

//=============================================================================

