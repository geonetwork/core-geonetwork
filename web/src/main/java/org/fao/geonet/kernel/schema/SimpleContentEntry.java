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

    public List<AttributeEntry> alAttribs  = new ArrayList<AttributeEntry>();
	public List<String> alAttribGroups = new ArrayList<String>();
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
            // TODO; handle attributes
	}

	//---------------------------------------------------------------------------

	private void handleChildren(ElementInfo ei)
	{
		List children = ei.element.getChildren();

        for (Object aChildren : children) {
            Element elChild = (Element) aChildren;

            if (elChild.getName().equals("extension")) {
                handleExtension(elChild, ei);
            }
            else if (elChild.getName().equals("restriction")) {
                handleExtension(elChild, ei);
            }

            else {
                Logger.log();
            }
        }
	}

	//---------------------------------------------------------------------------

	private void handleExtension(Element el, ElementInfo ei)
	{
		restriction = false;
		base = el.getAttributeValue("base");
		List extension = el.getChildren();
        for (Object anExtension : extension) {
            Element elExt = (Element) anExtension;
            String elName = elExt.getName();

            if (elName.equals("attribute")) {
                alAttribs.add(new AttributeEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
            }
            else if (elName.equals("attributeGroup")) {
                String attribGroup = elExt.getAttributeValue("ref");

                if (attribGroup == null) {
                    throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of SimpleContent with extension base " + base);
                }

                alAttribGroups.add(attribGroup);

            }


            else {
                Logger.log();
            }

        }
	}

	//---------------------------------------------------------------------------

	private void handleRestriction(Element el, ElementInfo ei)
	{
		restriction = true;
		base = el.getAttributeValue("base");
		
		List attribs = el.getAttributes();

        for (Object attrib : attribs) {
            Attribute at = (Attribute) attrib;

            String attrName = at.getName();

            Logger.log();
        }

		//--- handle children

		List children = el.getChildren();

        for (Object aChildren : children) {
            Element elRes = (Element) aChildren;
            String elName = elRes.getName();

            if (elName.equals("attribute")) {
                alAttribs.add(new AttributeEntry(elRes, ei.file, ei.targetNS, ei.targetNSPrefix));
            }
            else if (elName.equals("attributeGroup")) {
                String attribGroup = elRes.getAttributeValue("ref");

                if (attribGroup == null) {
                    throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of SimpleContent with restriction base " + base);
                }

                alAttribGroups.add(attribGroup);

            }


            else {
                Logger.log();
            }

        }
	}
}

//==============================================================================

