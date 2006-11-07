//==============================================================================
//===
//===   ElementEntry
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
import java.util.StringTokenizer;

import org.jdom.Attribute;
import org.jdom.Element;

//==============================================================================

class ElementEntry
{
	public String  name;
	public String  ns;
	public String  type;
	public int     min = 1;
	public int     max = 1;
	public String  substGroup;
	public String  substGroupNS;
	public String  ref;
	public boolean abstrElem;

	public ComplexTypeEntry complexType;
	public SimpleTypeEntry  simpleType;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	private ElementEntry() {}

	//---------------------------------------------------------------------------

	public ElementEntry(Element el, String file, String targetNS, String targetNSPrefix)
	{
		this(new ElementInfo(el, file, targetNS, targetNSPrefix));
	}

	//---------------------------------------------------------------------------

	public ElementEntry(ElementInfo ei)
	{
		ns = ei.targetNS;
		handleAttribs(ei);
		handleChildren(ei);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public ElementEntry copy()
	{
		ElementEntry ee = new ElementEntry();

		ee.name        = name;
		ee.ns          = ns;
		ee.type        = type;
		ee.min         = min;
		ee.max         = max;
		ee.substGroup  = substGroup;
		ee.substGroupNS= substGroupNS;
		ee.ref         = ref;
		ee.abstrElem   = abstrElem;

		ee.complexType = complexType;
		ee.simpleType  = simpleType;

		return ee;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void handleAttribs(ElementInfo ei)
	{
		List attrs = ei.element.getAttributes();

		for(int i=0; i<attrs.size(); i++)
		{
			Attribute attr = (Attribute) attrs.get(i);

			String name = attr.getName();
			String value= attr.getValue();

			if (name.equals("name"))
				this.name = (ei.targetNSPrefix == null) ? value : ei.targetNSPrefix + ":" + value;
			
			else if (name.equals("type"))
				type = value;

			else if (name.equals("ref"))
				ref = value;

			else if (name.equals("substitutionGroup"))
				substGroup = value;

			else if (name.equals("abstract"))
				abstrElem = "true".equals(value);

			else if (name.equals("minOccurs"))
				min = Integer.parseInt(value);

			else if (name.equals("maxOccurs"))
			{
				if (value.equals("unbounded")) 	max = 10000;
					else									max = Integer.parseInt(value);
			}

			else
				Logger.log("Unknown attribute '" +attr.getName()+ "'in <element> element", ei);
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

			if (elName.equals("complexType"))
				complexType = new ComplexTypeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);

			else if (elName.equals("simpleType"))
			{
				simpleType = new SimpleTypeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
			}
			else if (elName.equals("key"))
				Logger.log("Skipping 'key' child in <element> element '"+ name +"'");

			else
				Logger.log("Unknown child '" +elName+ "' in <element> element", ei);
		}
	}
}

//==============================================================================

