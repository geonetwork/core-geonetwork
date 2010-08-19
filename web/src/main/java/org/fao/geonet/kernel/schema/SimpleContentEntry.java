//==============================================================================
//===
//===   SimpleContentEntry
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

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

//==============================================================================

class SimpleContentEntry
{
	public String base;

	public ArrayList alElements = new ArrayList();
	public ArrayList alAttribs  = new ArrayList();
	public ArrayList alAttribGroups = new ArrayList();
	public boolean restriction = false;


	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public SimpleContentEntry(Element el, String file, String targetNS, String targetNSPrefix)
	{
		this(new ElementInfo(el, file, targetNS, targetNSPrefix));
	}

	//---------------------------------------------------------------------------

	public SimpleContentEntry(ElementInfo ei)
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

			// TODO; handle attributes
			
			Logger.log("Unknown attribute '"+ attrName +"' in <simpleContent> element", ei);
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

			if (elChild.getName().equals("extension"))
				handleExtension(elChild, ei);
			else if (elChild.getName().equals("restriction"))
				handleExtension(elChild, ei);

			else
				Logger.log("Unknown child '"+ elName +"' in <simpleContent> element", ei);
		}
	}

	//---------------------------------------------------------------------------

	private void handleExtension(Element el, ElementInfo ei)
	{
		restriction = false;
		base = el.getAttributeValue("base");
		List extension = el.getChildren();
		for(int j=0; j<extension.size(); j++)
		{
			Element elExt = (Element) extension.get(j);
			String  elName= elExt.getName();

			if (elName.equals("attribute"))
				alAttribs.add(new AttributeEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
			else if (elName.equals("attributeGroup")) {
				String attribGroup = elExt.getAttributeValue("ref");

				if (attribGroup == null)
					throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of SimpleContent with extension base "+base);
					
				alAttribGroups.add(attribGroup);

			}


			else
				Logger.log("Unknown child '"+ elName +"' in <restriction> element", ei);

		}
	}

	//---------------------------------------------------------------------------

	private void handleRestriction(Element el, ElementInfo ei)
	{
		restriction = true;
		base = el.getAttributeValue("base");
		
		List attribs = el.getAttributes();

		for(int i=0; i<attribs.size(); i++)
		{
			Attribute at = (Attribute) attribs.get(i);

			String attrName = at.getName();

			Logger.log("Unknown attribute '"+ attrName +"' in <restriction> element", ei);
		}

		//--- handle children

		List children = el.getChildren();

		for(int i=0; i<children.size(); i++)
		{
			Element elRes = (Element) children.get(i);
			String  elName= elRes.getName();

			if (elName.equals("attribute"))
				alAttribs.add(new AttributeEntry(elRes, ei.file, ei.targetNS, ei.targetNSPrefix));
			else if (elName.equals("attributeGroup")) {
				String attribGroup = elRes.getAttributeValue("ref");

				if (attribGroup == null)
					throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of SimpleContent with restriction base "+base);
					
				alAttribGroups.add(attribGroup);

			}


			else
				Logger.log("Unknown child '"+ elName +"' in <restriction> element", ei);

		}
	}
}

//==============================================================================

