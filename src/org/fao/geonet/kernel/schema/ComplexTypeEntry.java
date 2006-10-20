//==============================================================================
//===
//===   ComplexTypeEntry
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.schema;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

//==============================================================================

class ComplexTypeEntry
{
	public String  name;
	public String  attribGroup;
	public String  groupRef;
	public boolean isOrType;

	public ComplexContentEntry complexContent;

	public ArrayList alElements = new ArrayList();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	private ComplexTypeEntry() {}

	//---------------------------------------------------------------------------

	public ComplexTypeEntry(Element el, String file, String targetNS, String targetNSPrefix)
	{
		this(new ElementInfo(el, file, targetNS, targetNSPrefix));
	}

	//---------------------------------------------------------------------------

	public ComplexTypeEntry(ElementInfo ei)
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

			if (attrName.equals("name"))
				name = (ei.targetNSPrefix == null) ? at.getValue() : ei.targetNSPrefix + ":" + at.getValue();
			
			else if (attrName.equals("mixed"))
				Logger.log("Skipping 'mixed' attribute in <complexType> element '"+ name +"'");

			else
				Logger.log("Unknown attribute '"+ attrName +"' in <complexType> element", ei);
		}
	}

	//---------------------------------------------------------------------------

	private void handleChildren(ElementInfo ei)
	{
		List children = ei.element.getChildren();

		for(int i=0; i<children.size(); i++)
		{
			Element elChild = (Element) children.get(i);
			String  elName    = elChild.getName();

			if (elName.equals("sequence") || elName.equals("choice"))
			{
				isOrType = elName.equals("choice");

				List sequence = elChild.getChildren();

				for(int j=0; j<sequence.size(); j++)
				{
					Element elElem = (Element) sequence.get(j);

					if (elElem.getName().equals("element"))
						alElements.add(new ElementEntry(elElem, ei.file, ei.targetNS, ei.targetNSPrefix));

					else if (elElem.getName().equals("group"))
					{
						isOrType = false;
						groupRef = elElem.getAttributeValue("ref");

						if (groupRef == null)
							throw new IllegalArgumentException("Found 'group' element without 'ref' attrib in complexType : " +elName);
					}

					else
						Logger.log("Unknown child '"+ elElem.getName() +"' in <sequence|choice> element", ei);
				}
			}

			else if (elName.equals("attributeGroup"))
			{
				attribGroup = elChild.getAttributeValue("ref");

				if (attribGroup == null)
					throw new IllegalArgumentException("'ref' is null for <attributeGroup> in complexType : " + name);
			}

			else if (elName.equals("complexContent"))
				complexContent = new ComplexContentEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);

			else if (elName.equals("annotation"))
				;

			else
				Logger.log("Unknown child '"+ elName +"' in <complexType> element", ei);
		}
	}
}

//==============================================================================

