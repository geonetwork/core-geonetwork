//==============================================================================
//===
//===   GroupEntry
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

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

//==============================================================================

class GroupEntry
{
	public String  name;
    public ArrayList<ElementEntry> alElements = new ArrayList<ElementEntry>();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GroupEntry(Element el, String file, String targetNS, String targetNSPrefix)
	{
		this(new ElementInfo(el, file, targetNS, targetNSPrefix));
	}

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

        for (Object attrib : attribs) {
            Attribute at = (Attribute) attrib;

            String attrName = at.getName();
            if (attrName.equals("name")) {
                name = at.getValue();
                if ((name.indexOf(':') == -1) && (ei.targetNSPrefix != null)) {
                    name = ei.targetNSPrefix + ":" + at.getValue();
                }
            }
            else {
                Logger.log();
            }
        }
	}

	//---------------------------------------------------------------------------

	private void handleChildren(ElementInfo ei)
	{
		List children = ei.element.getChildren();

        for (Object aChildren : children) {
            Element elChild = (Element) aChildren;
            String elName = elChild.getName();

            if (elName.equals("sequence")) {
                List sequence = elChild.getChildren();

                for (Object aSequence : sequence) {
                    Element elElem = (Element) aSequence;

                    if (elElem.getName().equals("choice") || elElem.getName().equals("element") || elElem.getName().equals("group") || elElem.getName().equals("sequence")) {
                        alElements.add(new ElementEntry(elElem, ei.file, ei.targetNS, ei.targetNSPrefix));
                    }

                    else {
                        Logger.log();
                    }
                }
            }
            else if (elName.equals("choice")) {
                alElements.add(new ElementEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix));
            }

            else if (elName.equals("annotation")) {
                
            }

            else {
                Logger.log();
            }
        }
	}

}

//==============================================================================


