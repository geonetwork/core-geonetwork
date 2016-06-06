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

import org.jdom.Element;
import org.jdom.Namespace;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//==============================================================================

/**
 * This class parses a "SimpleType" element. All attributes are ignored (a warning is logged). Only
 * the "restriction" child is recognized and extracted.
 */

class SimpleTypeEntry extends BaseHandler {
    public String name;
    public List<String> alTypes = new ArrayList<String>();
    public List<String> alEnum = new ArrayList<String>();

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public SimpleTypeEntry(Element el, Path file, String targetNS, String targetNSPrefix) {
        this(new ElementInfo(el, file, targetNS, targetNSPrefix));
    }

    //---------------------------------------------------------------------------

    public SimpleTypeEntry(ElementInfo ei) {
        name = handleAttribs(ei, null);
        handleChildren(ei);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SimpleTypeEntry name:" + name + " Enums:[");
        for (int j = 0; j < alEnum.size(); j++) {
            sb.append(alEnum.get(j) + ", ");
        }
        sb.append("], types:[");
        for (int j = 0; j < alTypes.size(); j++) {
            sb.append(alTypes.get(j) + ", ");
        }
        sb.append("]");
        return sb.toString();
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void handleChildren(ElementInfo ei) {
        List<?> children = ei.element.getChildren();

        for (Object aChildren : children) {
            Element elChild = (Element) aChildren;
            String elName = elChild.getName();

            if (elName.equals("restriction")) {
                List<?> restrictions = elChild.getChildren();

                for (Object restriction : restrictions) {
                    Element elEnum = (Element) restriction;
                    String elemName = elEnum.getName();

                    if (elemName.equals("enumeration")) {
                        alEnum.add(elEnum.getAttributeValue("value"));
                    } else if (elemName.equals("minInclusive") || elemName.equals("maxInclusive") ||
                        elemName.equals("minExclusive") || elemName.equals("maxExclusive") ||
                        elemName.equals("pattern"))
                    //--- we are not interested in type's domain so we skip these specifications
                    {

                    } else {
                        Logger.log();
                    }
                }
            } else if (elName.equals("union")) {
                List<?> simpleTypes = elChild.getChildren("simpleType", Namespace.getNamespace("http://www.w3.org/2001/XMLSchema"));
                // Load enumeration of union of simpleType (eg. gml:TimeUnitType, gml:NilReasonEnumeration)
                if (simpleTypes.size() > 0) {
                    for (Object st : simpleTypes) {
                        Element stEl = (Element) st;
                        List<?> restrictions = stEl.getChildren("restriction", Namespace.getNamespace("http://www.w3.org/2001/XMLSchema"));

                        for (Object r : restrictions) {
                            Element rEl = (Element) r;
                            List<?> enumerationList = rEl.getChildren("enumeration", Namespace.getNamespace("http://www.w3.org/2001/XMLSchema"));
                            for (Object e : enumerationList) {
                                Element elEnum = (Element) e;
                                String v = elEnum.getAttributeValue("value");
                                alEnum.add(v);
                            }
                        }
                    }
                    // TODO : Optional pattern restriction
                } else {
                    // List of member types are loaded in order to retrieve type enumeration list when available (eg. gco:nilReason).
                    String memberTypes = elChild.getAttributeValue("memberTypes");
                    // TODO : Probably member types should be handled as geonet:choose style complexElement
                    if (memberTypes != null) {
                        String[] types = memberTypes.split(" ");
                        for (String type : types) {
                            alTypes.add(type);
                        }
                    }
                }
            } else if (elName.equals("annotation")) {

            } else {
                Logger.log();
            }
        }

    }
}

//==============================================================================


