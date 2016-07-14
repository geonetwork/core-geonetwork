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

import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//==============================================================================

class SimpleContentEntry {
    public String base;

    public ArrayList<AttributeEntry> alAttribs = new ArrayList<AttributeEntry>();
    public List<String> alAttribGroups = new ArrayList<String>();
    public boolean restriction = false;


    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public SimpleContentEntry(Element el, Path file, String targetNS, String targetNSPrefix) {
        this(new ElementInfo(el, file, targetNS, targetNSPrefix));
    }

    //---------------------------------------------------------------------------

    public SimpleContentEntry(ElementInfo ei) {
        handleAttribs(ei);
        handleChildren(ei);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void handleAttribs(ElementInfo ei) {
        // TODO; handle attributes
    }

    //---------------------------------------------------------------------------

    private void handleChildren(ElementInfo ei) {
        @SuppressWarnings("unchecked")
        List<Element> children = ei.element.getChildren();

        for (Element elChild : children) {

            if (elChild.getName().equals("extension")) {
                handleExtension(elChild, ei);
            } else if (elChild.getName().equals("restriction")) {
                handleExtension(elChild, ei);
            } else {
                Logger.log();
            }
        }
    }

    //---------------------------------------------------------------------------

    private void handleExtension(Element el, ElementInfo ei) {
        restriction = false;
        base = el.getAttributeValue("base");
        @SuppressWarnings("unchecked")
        List<Element> extension = el.getChildren();
        for (Element elExt : extension) {
            String elName = elExt.getName();

            if (elName.equals("attribute")) {
                alAttribs.add(new AttributeEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elName.equals("attributeGroup")) {
                String attribGroup = elExt.getAttributeValue("ref");

                if (attribGroup == null) {
                    throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of SimpleContent with extension base " + base);
                }

                alAttribGroups.add(attribGroup);

            } else {
                Logger.log();
            }

        }
    }
}

