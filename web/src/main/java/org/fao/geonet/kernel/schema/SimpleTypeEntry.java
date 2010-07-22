//==============================================================================
//===
//===   SimpleTypeEntry
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

import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

//==============================================================================

/** This class parses a "SimpleType" element. All attributes are ignored (a warning
  * is logged). Only the "restriction" child is recognized and extracted.
  */

class SimpleTypeEntry
{
	public String name;

	public ArrayList alEnum = new ArrayList();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public SimpleTypeEntry(Element el, String file, String targetNS, String targetNSPrefix)
	{
		this(new ElementInfo(el, file, targetNS, targetNSPrefix));
	}

	//---------------------------------------------------------------------------

	public SimpleTypeEntry(ElementInfo ei)
	{
		handleAttribs(ei);
		handleChildren(ei);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void handleAttribs(ElementInfo ei)
	{
		List attribs = ei.element.getAttributes();
		for(int i=0; i<attribs.size(); i++)
		{
			Attribute at = (Attribute) attribs.get(i);

			String attrName = at.getName();

			if (attrName.equals("name")) {
        name = at.getValue();
        if ((name.indexOf(':') == -1) && (ei.targetNSPrefix != null))
          name = ei.targetNSPrefix + ":" + at.getValue();
      }
			else
				Logger.log("Unknown attribute '"+ attrName +"' in <simpleType> element", ei);
		}
	}

	//---------------------------------------------------------------------------

	private void handleChildren(ElementInfo ei)
	{
		List children = ei.element.getChildren();

		for(int i=0; i<children.size(); i++)
		{
			Element elChild = (Element) children.get(i);
			String  elName  = elChild.getName();

			if (elName.equals("restriction"))
			{
				List restrictions = elChild.getChildren();

				for(int j=0; j<restrictions.size(); j++)
				{
					Element elEnum   = (Element) restrictions.get(j);
					String  elemName = elEnum.getName();

					if (elemName.equals("enumeration"))
						alEnum.add(elEnum.getAttributeValue("value"));

					else if (elemName.equals("minInclusive") || elemName.equals("maxInclusive") ||
								elemName.equals("minExclusive") || elemName.equals("maxExclusive") ||
							   elemName.equals("pattern"))
						//--- we are not interested in type's domain so we skip these specifications
						;

					else
						Logger.log("Unknown child '"+ elEnum.getName() +"' in <restriction> element", ei);
				}
			}

			else if (elName.equals("union"))
				Logger.log("Skipping 'union' child in <simpleType> element '"+ name +"'");

			else if (elName.equals("annotation"))
			        ;
			else
				Logger.log("Unknown child '"+ elName +"' in <simpleType> element ", ei);
		}
	}
}

//==============================================================================


