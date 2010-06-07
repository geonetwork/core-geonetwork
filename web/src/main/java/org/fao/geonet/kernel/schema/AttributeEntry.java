//==============================================================================
//===
//===   AttributeEntry
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

class AttributeEntry
{
	public String  name;
	public String  unqualifiedName;
	public String  namespacePrefix;
	public String  defValue;
	public String  reference;
	public String  referenceNS;
	public String	 form = "unqualified";
	public boolean required = false;

	public ArrayList alValues = new ArrayList();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public AttributeEntry(Element el, String file, String targetNS, String targetNSPrefix)
	{
		this(new ElementInfo(el, file, targetNS, targetNSPrefix));
	}

	//---------------------------------------------------------------------------

	public AttributeEntry(ElementInfo ei)
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
				unqualifiedName = name;
				if (ei.targetNSPrefix != null) {
					name = ei.targetNSPrefix+":"+name;
					namespacePrefix = ei.targetNSPrefix;
				}
		
				//System.out.println("-- name is "+name);
			}
			else if (attrName.equals("default")||attrName.equals("fixed"))
				defValue = at.getValue();

			else if (attrName.equals("ref")) {
				reference = at.getValue();

				//System.out.println("-- ref is "+reference);
			}

			else if (attrName.equals("use")) {
				required = "required".equals(at.getValue());
				//System.out.println("-- Required is "+required);
			}

			else if (attrName.equals("type"))
				; //Logger.log("Skipping 'type' attribute in <attribute> element '"+ name +"'");

			else if (attrName.equals("form"))
				form = at.getValue();

			else
				Logger.log("Unknown attribute '"+ attrName +"' in <attribute> element '"+ name +"'", ei);
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

			if (elName.equals("simpleType"))
			{
				SimpleTypeEntry ste = new SimpleTypeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);

				for(int j=0; j<ste.alEnum.size(); j++)
					alValues.add(ste.alEnum.get(j));
			}

			else if (elName.equals("annotation"))
				;

			else
				Logger.log("Unknown child '"+ elName +"' in <attribute> element '"+ name +"'", ei);
		}
	}
}

//==============================================================================


