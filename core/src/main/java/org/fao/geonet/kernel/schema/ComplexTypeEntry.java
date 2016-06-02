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

class ComplexTypeEntry extends BaseHandler {
    public String name;
    public boolean isOrType = false;
    public boolean isAbstract;

    public ComplexContentEntry complexContent;
    public SimpleContentEntry simpleContent;

    public List<ElementEntry> alElements = new ArrayList<ElementEntry>();
    public List<AttributeEntry> alAttribs = new ArrayList<AttributeEntry>();
    public List<String> alAttribGroups = new ArrayList<String>();

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public ComplexTypeEntry(Element el, Path file, String targetNS, String targetNSPrefix) {
        this(new ElementInfo(el, file, targetNS, targetNSPrefix));
    }

    //---------------------------------------------------------------------------

    public ComplexTypeEntry(ElementInfo ei) {
        handleAttribs(ei);
        handleChildren(ei);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void handleAttribs(ElementInfo ei) {
        List<?> attribs = ei.element.getAttributes();

        for (Object attrib : attribs) {
            Attribute at = (Attribute) attrib;

            String attrName = at.getName();
            if (attrName.equals("name")) {
                name = at.getValue();
                if ((name.indexOf(':') == -1) && (ei.targetNSPrefix != null)) {
                    name = ei.targetNSPrefix + ":" + at.getValue();
                }
            } else if (attrName.equals("abstract")) {
                String abValue = at.getValue();
                isAbstract = abValue.equals("true");
            }
// TODO:
//            else if (attrName.equals("mixed")) {
//                Logger.log();
//            }
//            else {
//                Logger.log();
//            }

        }
    }

    //---------------------------------------------------------------------------

    private void handleChildren(ElementInfo ei) {
        List<?> children = ei.element.getChildren();

        for (Object aChildren : children) {
            Element elChild = (Element) aChildren;
            String elName = elChild.getName();
            boolean first = true;

            if (elName.equals("sequence") || elName.equals("choice")) {
                isOrType = elName.equals("choice") && (first);

                List<?> sequence = elChild.getChildren();

                for (Object aSequence : sequence) {
                    Element elElem = (Element) aSequence;

                    if (isChoiceOrElementOrGroupOrSequence(elElem)) {
                        alElements.add(new ElementEntry(elElem, ei.file, ei.targetNS, ei.targetNSPrefix));
                    } else {
                        Logger.log();
                    }
                }
            } else if (elName.equals("group")) {
                first = false;
                isOrType = false;
                alElements.add(new ElementEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elName.equals("attribute")) {
                first = false;
                alAttribs.add(new AttributeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elName.equals("attributeGroup")) {
                first = false;
                String attribGroup = elChild.getAttributeValue("ref");

                if (attribGroup == null) {
                    throw new IllegalArgumentException("'ref' is null for <attributeGroup> in complexType : " + name);
                }

                alAttribGroups.add(attribGroup);
            } else if (elName.equals("complexContent")) {
                first = false;
                complexContent = new ComplexContentEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
            } else if (elName.equals("simpleContent")) {
                first = false;
                simpleContent = new SimpleContentEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
            } else if (elName.equals("annotation")) {

            } else {
                Logger.log();
            }
        }
    }

}

//==============================================================================


