//==============================================================================
//===
//===   AttributeGroupEntry
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//==============================================================================

class AttributeGroupEntry {
    public String name;
    public String ref;

    public ArrayList<AttributeEntry> alAttrs = new ArrayList<AttributeEntry>();
    public ArrayList<AttributeGroupEntry> alAttrGrps = new ArrayList<AttributeGroupEntry>();

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public AttributeGroupEntry(Element el, Path file, String targetNS, String targetNSPrefix) {
        this(new ElementInfo(el, file, targetNS, targetNSPrefix));
    }

    //---------------------------------------------------------------------------

    public AttributeGroupEntry(ElementInfo ei) {
        handleAttribs(ei);
        handleChildren(ei);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void handleAttribs(ElementInfo ei) {
        @SuppressWarnings("unchecked")
        List<Attribute> attribs = ei.element.getAttributes();

        for (Attribute at : attribs) {

            String attrName = at.getName();
            if (attrName.equals("name")) {
                name = at.getValue();
                if (ei.targetNSPrefix != null) {
                    name = ei.targetNSPrefix + ":" + name;
                }

                //System.out.println("-- name is "+name);
            } else if (attrName.equals("ref")) {
                ref = at.getValue();

                //System.out.println("-- ref is "+ref);
            } else {
                Logger.log();
            }
        }
    }

    //---------------------------------------------------------------------------

    private void handleChildren(ElementInfo ei) {
        @SuppressWarnings("unchecked")
        List<Element> children = ei.element.getChildren();

        for (Element elChild : children) {
            String elName = elChild.getName();

            if (elName.equals("attribute")) {
                AttributeEntry at = new AttributeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
                alAttrs.add(at);
            } else if (elName.equals("attributeGroup")) {
                AttributeGroupEntry age = new AttributeGroupEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
                alAttrGrps.add(age);
            } else if (elName.equals("annotation")) {

            } else {
                Logger.log();
            }
        }
    }
}

//==============================================================================


