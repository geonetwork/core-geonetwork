//==============================================================================
//===
//===   GroupEntry
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
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

//==============================================================================

class GroupEntry
{
	public String  name;
	public boolean isChoice;

	public ArrayList alElements = new ArrayList();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GroupEntry(ElementInfo ei)
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
        if ((name.indexOf(":") == -1) && (ei.targetNSPrefix != null))
          name = ei.targetNSPrefix + ":" + at.getValue();
      }
			else
				Logger.log("Unknown attribute '"+ attrName +"' in <group> element", ei);
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

			if (elName.equals("sequence"))
			{
				List sequence = elChild.getChildren();

				for(int j=0; j<sequence.size(); j++)
				{
					Element elElem = (Element) sequence.get(j);

					if (elElem.getName().equals("choice"))
						handleChoice(elElem, ei);

					else
						Logger.log("Unknown child '"+ elElem.getName() +"' in <sequence> element", ei);
				}
			}

			else if (elName.equals("annotation"))
				;

			else
				Logger.log("Unknown child '"+ elName +"' in <group> element", ei);
		}
	}

	//---------------------------------------------------------------------------

	private void handleChoice(Element el, ElementInfo ei)
	{
		isChoice = true;

		int min = 1;
		int max = 1;

		List attribs = el.getAttributes();

		for(int i=0; i<attribs.size(); i++)
		{
			Attribute at = (Attribute) attribs.get(i);

			String attrName = at.getName();

			if (attrName.equals("minOccurs"))
				min = Integer.parseInt(at.getValue());

			else if (attrName.equals("maxOccurs"))
			{
				String value = at.getValue();

				if (value.equals("unbounded")) 	max = 10000;
					else									max = Integer.parseInt(value);
			}

			else
				Logger.log("Unknown attribute '"+ attrName +"' in <sequence/choice> element", ei);
		}

		//--- handle choice's children

		List children = el.getChildren();

		for(int i=0; i<children.size(); i++)
		{
			Element elChild = (Element) children.get(i);
			String  elName  = elChild.getName();

			if (elName.equals("element"))
			{
				ElementEntry ee = new ElementEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
				ee.min = min;
				ee.max = max;

				alElements.add(ee);
			}

			else if (elName.equals("annotation"))
				;

			else
				Logger.log("Unknown child '"+ elName +"' in <sequence/choice> element", ei);
		}
	}
}

//==============================================================================


