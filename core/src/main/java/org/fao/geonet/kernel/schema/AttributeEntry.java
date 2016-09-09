//==============================================================================
//===
//===   AttributeEntry
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

class AttributeEntry {
    public String name;
    public String type;
    public String unqualifiedName;
    public String namespacePrefix;
    public String defValue;
    public String reference;
    public String form = "unqualified";
    public boolean required = false;

    public ArrayList<String> alValues = new ArrayList<String>();
    public ArrayList<String> alTypes = new ArrayList<String>();

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public AttributeEntry(Element el, Path file, String targetNS, String targetNSPrefix) {
        this(new ElementInfo(el, file, targetNS, targetNSPrefix));
    }

    //---------------------------------------------------------------------------

    public AttributeEntry(ElementInfo ei) {
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
                unqualifiedName = name;
                if (ei.targetNSPrefix != null) {
                    name = ei.targetNSPrefix + ":" + name;
                    namespacePrefix = ei.targetNSPrefix;
                }

                //System.out.println("-- name is "+name);
            } else if (attrName.equals("default") || attrName.equals("fixed")) {
                defValue = at.getValue();
            } else if (attrName.equals("ref")) {
                reference = at.getValue();

                //System.out.println("-- ref is "+reference);
            } else if (attrName.equals("use")) {
                required = "required".equals(at.getValue());
                //System.out.println("-- Required is "+required);
            } else if (attrName.equals("type")) {
                type = ei.element.getAttributeValue(attrName);
            } else if (attrName.equals("form")) {
                form = at.getValue();
            } else {
                Logger.log();
            }
        }
    }

    //---------------------------------------------------------------------------

    private void handleChildren(ElementInfo ei) {
        List<?> children = ei.element.getChildren();
        for (Object aChildren : children) {
            Element elChild = (Element) aChildren;
            String elName = elChild.getName();
            if (elName.equals("simpleType")) {
                SimpleTypeEntry ste = new SimpleTypeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
                for (int j = 0; j < ste.alEnum.size(); j++) {
                    alValues.add(ste.alEnum.get(j));
                }

                for (int j = 0; j < ste.alTypes.size(); j++) {
                    alTypes.add(ste.alTypes.get(j));
                }
            } else if (elName.equals("annotation")) {

            } else {
                Logger.log();
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AttributeEntry name:" + name + ", type:" + type + " [");
        for (int j = 0; j < alValues.size(); j++) {
            sb.append(alValues.get(j) + ",");
        }
        sb.append("], types:[");
        for (int j = 0; j < alTypes.size(); j++) {
            sb.append(alTypes.get(j) + ",");
        }
        sb.append("]");
        return sb.toString();
    }
}

//==============================================================================


