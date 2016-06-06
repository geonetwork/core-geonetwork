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

import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//==============================================================================

class ComplexContentEntry extends BaseHandler {
    public String base;
    public ArrayList<String> alAttribGroups = new ArrayList<String>();
    public ArrayList<ElementEntry> alElements = new ArrayList<ElementEntry>();
    public ArrayList<AttributeEntry> alAttribs = new ArrayList<AttributeEntry>();
    boolean restriction = false;

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public ComplexContentEntry(Element el, Path file, String targetNS, String targetNSPrefix) {
        this(new ElementInfo(el, file, targetNS, targetNSPrefix));
    }

    //---------------------------------------------------------------------------

    public ComplexContentEntry(ElementInfo ei) {
        handleAttribs(ei);
        handleChildren(ei);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void handleAttribs(ElementInfo ei) {
//		@SuppressWarnings("unchecked")
//        List<Attribute> attribs = ei.element.getAttributes();
//
//        for (Attribute at : attribs) {
//            String attrName = at.getName();
//  TODO:
//            if (attrName.equals("mixed")) {
//                Logger.log();
//            }
//            else {
//                Logger.log();
//            }
//        }
    }

    //---------------------------------------------------------------------------

    private void handleChildren(ElementInfo ei) {
        @SuppressWarnings("unchecked")
        List<Element> children = ei.element.getChildren();

        for (Element elChild : children) {
            if (elChild.getName().equals("extension")) {
                handleExtension(elChild, ei);
            } else if (elChild.getName().equals("restriction")) {
                handleRestriction(elChild, ei);
                restriction = true;
            } else {
                Logger.log();
            }
        }
    }

    //---------------------------------------------------------------------------

    private void handleExtension(Element el, ElementInfo ei) {
        base = el.getAttributeValue("base");

        @SuppressWarnings("unchecked")
        List<Element> extension = el.getChildren();

        for (Element elExt : extension) {
            if (elExt.getName().equals("sequence")) {
                @SuppressWarnings("unchecked")
                List<Element> sequence = elExt.getChildren();

                for (Element elSeq : sequence) {
                    if (isChoiceOrElementOrGroupOrSequence(elSeq)) {
                        alElements.add(new ElementEntry(elSeq, ei.file, ei.targetNS, ei.targetNSPrefix));
                    } else {
                        Logger.log();
                    }
                }
            } else if (elExt.getName().equals("group")) {
                alElements.add(new ElementEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elExt.getName().equals("choice")) {
                alElements.add(new ElementEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elExt.getName().equals("attribute")) {
                alAttribs.add(new AttributeEntry(elExt, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elExt.getName().equals("attributeGroup")) {
                String attribGroup = elExt.getAttributeValue("ref");

                if (attribGroup == null) {
                    throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of ComplexContent with extension base " + base);
                }
                alAttribGroups.add(attribGroup);
            } else {
                Logger.log();
            }
        }
    }

    //---------------------------------------------------------------------------

    private void handleRestriction(Element el, ElementInfo ei) {
        base = el.getAttributeValue("base");

        //--- handle children

        @SuppressWarnings("unchecked")
        List<Element> restriction = el.getChildren();

        for (Element elRes : restriction) {
            String elName = elRes.getName();

            if (elRes.getName().equals("sequence")) {
                handleSequence(elRes, alElements, ei);
            } else if (elRes.getName().equals("group")) {
                alElements.add(new ElementEntry(elRes, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elName.equals("attribute")) {
                alAttribs.add(new AttributeEntry(elRes, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else if (elName.equals("attributeGroup")) {
                String attribGroup = elRes.getAttributeValue("ref");

                if (attribGroup == null) {
                    throw new IllegalArgumentException("'ref' is null for element in <attributeGroup> of ComplexContent with restriction base " + base);
                }
                alAttribGroups.add(attribGroup);
            } else {
                Logger.log();
            }

        }
    }
}

//==============================================================================


