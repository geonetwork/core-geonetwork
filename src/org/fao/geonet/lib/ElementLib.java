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

package org.fao.geonet.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jdom.Element;

//=============================================================================

public class ElementLib
{
	//-----------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//-----------------------------------------------------------------------------

	public Set<String> getIds(Element elem)
	{
		HashSet<String> hs = new HashSet<String>();

		for (Object child : elem.getChildren())
			hs.add(((Element) child).getChildText("id"));

		return hs;
	}

	//-----------------------------------------------------------------------------

	public Element pruneChildren(Element elem, Set<String> ids)
	{
		ArrayList<Element> alToPrune = new ArrayList<Element>();

		//--- collect elements to prune

		for (Object obj : elem.getChildren())
		{
			Element child = (Element) obj;
			String id = child.getChildText("id");

			if (!ids.contains(id))
				alToPrune.add(child);
		}

		//--- remove collected elements

		for (Element child : alToPrune)
			child.detach();

		return elem;
	}

	//-----------------------------------------------------------------------------

	public void add(Element el,String name, Object value)
	{
		if (value != null)
			el.addContent(new Element(name).setText(value.toString()));
	}
}

//=============================================================================

