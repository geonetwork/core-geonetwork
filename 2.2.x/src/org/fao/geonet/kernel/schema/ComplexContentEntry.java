//==============================================================================
//===
//===   ComplexContentEntry
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
import java.io.*;

import org.jdom.Attribute;
import org.jdom.Element;

//==============================================================================

class ComplexContentEntry
{
	public String base;
	public ArrayList alAttribGroups = new ArrayList();
	public ArrayList alElements = new ArrayList();
	public ArrayList alAttribs  = new ArrayList();
	boolean restriction = false;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ComplexContentEntry(Element el, String file, String targetNS, String targetNSPrefix)
	{
		this(new ElementInfo(el, file, targetNS, targetNSPrefix));
	}

	//---------------------------------------------------------------------------

	public ComplexContentEntry(ElementInfo ei)
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

			if (attrName.equals("mixed"))
				Logger.log("Skipping 'mixed' attribute in <complexContent> element");
			else
				Logger.log("Unknown attribute '"+ attrName +"' in <complexContent> element", ei);
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

			else if (elChild.getName().equals("restriction")) {
				handleRestriction(elChild, ei);
				restriction = true;
			}

			else
				Logger.log("Unknown child '"+ elName +"' in <complexContent> element", ei);
		}
	}

	//---------------------------------------------------------------------------

	private void handleExtension(Element el, ElementInfo ei)
	{
		base = el.getAttributeValue("base");

		List extension = el.getChildren();

		for(int j=0; j<extension.size(); j++)
		{
			Element elExt = (Element) extension.get(j);

			if (elExt.getName().equals("sequence")) {
				List sequence = elExt.getChildren();

				for(int k=0; k<sequence.size(); k++)
				{
					Element elSeq = (Element) sequence.get(k);

					if (elSeq.getName().equals("element") || elSeq.getName().equals("choice") || elSeq.getName().equals("group") || elSeq.getName().equals("sequence")) 
						alElements.add(new ElementEntry(elSeq, ei.file, ei.targetNS, ei.targetNSPrefix));
					
					else
						Logger.log("Unknown child '"+ elSeq.getName() +"' in <sequence> element"+ei);
				}
			}
			else if (elExt.getName().equals("group")) {
				alElements.add(new ElementEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
			}
			else if (elExt.getName().equals("choice")) {
				alElements.add(new ElementEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
			}
			else if (elExt.getName().equals("attribute"))
				alAttribs.add(new AttributeEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
			else if (elExt.getName().equals("attributeGroup"))
      {
				String attribGroup = elExt.getAttributeValue("ref");

        if (attribGroup == null)
          throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of ComplexContent with extension base "+base);
				alAttribGroups.add(attribGroup);
      }


			else
				Logger.log("Unknown child '"+ elExt.getName() +"' in <extension> element "+ei);
		}
	}

	//---------------------------------------------------------------------------

	private void handleRestriction(Element el, ElementInfo ei)
	{
		base = el.getAttributeValue("base");

		//--- handle children

		List restriction = el.getChildren();

		for(int i=0; i<restriction.size(); i++)
		{
			Element elRes = (Element) restriction.get(i);
			String  elName= elRes.getName();

			if (elRes.getName().equals("sequence"))
			{
				List sequence = elRes.getChildren();

				for(int k=0; k<sequence.size(); k++)
				{
					Element elSeq = (Element) sequence.get(k);

					if (elSeq.getName().equals("element") || elSeq.getName().equals("choice") || elSeq.getName().equals("group") || elSeq.getName().equals("sequence")) 
						alElements.add(new ElementEntry(elSeq, ei.file, ei.targetNS, ei.targetNSPrefix));
					else
						Logger.log("Unknown child '"+ elSeq.getName() +"' in <sequence> element"+ei);
				}
			}

			else if (elRes.getName().equals("group")) {
				alElements.add(new ElementEntry(elRes, ei.file, ei.targetNS, ei.targetNSPrefix));
			}

			else if (elName.equals("attribute"))
				alAttribs.add(new AttributeEntry(elRes, ei.file, ei.targetNS, ei.targetNSPrefix));

			else if (elName.equals("attributeGroup"))
      {
				String attribGroup = elRes.getAttributeValue("ref");

        if (attribGroup == null)
          throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of ComplexContent with restriction base "+base);
				alAttribGroups.add(attribGroup);
      }

			else
				Logger.log("Unknown child '"+ elName +"' in <restriction> element",ei);

		}
	}
}

//==============================================================================


