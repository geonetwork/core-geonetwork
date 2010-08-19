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

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

//==============================================================================

/** This class parses a "SimpleType" element. All attributes are ignored (a warning
  * is logged). Only the "restriction" child is recognized and extracted.
  */

class SimpleTypeEntry
{
	public String name;

	public List alEnum = new ArrayList();

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

            if (elName.equals("restriction")) {
                List restrictions = elChild.getChildren();

                for (Object restriction : restrictions) {
                    Element elEnum = (Element) restriction;
                    String elemName = elEnum.getName();

                    if (elemName.equals("enumeration")) {
                        alEnum.add(elEnum.getAttributeValue("value"));
                    }

                    else if (elemName.equals("minInclusive") || elemName.equals("maxInclusive") ||
                            elemName.equals("minExclusive") || elemName.equals("maxExclusive") ||
                            elemName.equals("pattern"))
                    //--- we are not interested in type's domain so we skip these specifications
                    {

                    }

                    else {
                        Logger.log();
                    }
                }
            }

            else if (elName.equals("union")) {
                Logger.log();
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


