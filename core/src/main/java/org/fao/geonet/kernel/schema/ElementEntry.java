//==============================================================================
//===
//===   ElementEntry
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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.jdom.Attribute;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//==============================================================================

class ElementEntry {
    public String name;
    public String ns;
    public String type;
    public int min = 1;
    public int max = 1;
    public String substGroup;
    public String substGroupNS;
    public String ref;
    public boolean abstrElem;
    public boolean choiceElem;
    public boolean groupElem;
    public boolean sequenceElem;
    public ArrayList<ElementEntry> alContainerElems = new ArrayList<ElementEntry>();

    public ComplexTypeEntry complexType;
    public SimpleTypeEntry simpleType;

    //---------------------------------------------------------------------------
    //---
    //--- Constructor - this class handles both <element> and <choice> entries
    //---
    //---------------------------------------------------------------------------

    private ElementEntry() {
    }

    //---------------------------------------------------------------------------

    public ElementEntry(Element el, Path file, String targetNS, String targetNSPrefix) {
        this(new ElementInfo(el, file, targetNS, targetNSPrefix));
    }

    //---------------------------------------------------------------------------

    public ElementEntry(ElementInfo ei) {
        ns = ei.targetNS;
        handleAttribs(ei);
        if (choiceElem) handleContainerChildren(ei, alContainerElems);
            // if (groupElem) no need to do anything - read elements from group later
        else if (sequenceElem) handleContainerChildren(ei, alContainerElems);
        else handleChildren(ei);
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public ElementEntry copy() {
        ElementEntry ee = new ElementEntry();

        ee.name = name;
        ee.ns = ns;
        ee.type = type;
        ee.min = min;
        ee.max = max;
        ee.substGroup = substGroup;
        ee.substGroupNS = substGroupNS;
        ee.ref = ref;
        ee.abstrElem = abstrElem;

        ee.complexType = complexType;
        ee.simpleType = simpleType;
        ee.choiceElem = choiceElem;
        ee.groupElem = groupElem;
        ee.sequenceElem = sequenceElem;
        ee.alContainerElems = alContainerElems;

        return ee;
    }

    public String toString() {
        return "ElementEntry name: " + name + " ref: " + ref + " type: " + type + " abstract: " + abstrElem + " choice: " + choiceElem + " group: " + groupElem + " sequence: " + sequenceElem;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void handleAttribs(ElementInfo ei) {
        if (ei.element.getName().equals("choice")) choiceElem = true;
        else if (ei.element.getName().equals("group")) groupElem = true;
        else if (ei.element.getName().equals("sequence")) sequenceElem = true;

        @SuppressWarnings("unchecked")
        List<Attribute> attrs = ei.element.getAttributes();

        for (Attribute at : attrs) {
            String attrName = at.getName();
            String value = at.getValue();

            if (attrName.equals("name")) {
                name = at.getValue();
                if ((name.indexOf(':') == -1) && (ei.targetNSPrefix != null)) {
                    name = ei.targetNSPrefix + ":" + at.getValue();
                }
                // System.out.println("Doing Element "+name);
            } else if (attrName.equals("type")) {
                type = value;
            } else if (attrName.equals("ref")) {
                ref = value;
            } else if (attrName.equals("substitutionGroup")) {
                substGroup = value;
            } else if (attrName.equals("abstract")) {
                abstrElem = "true".equals(value);
            } else if (attrName.equals("minOccurs")) {
                min = Integer.parseInt(value);
            } else if (attrName.equals("maxOccurs")) {
                if (value.equals("unbounded")) {
                    max = 10000;
                } else {
                    max = Integer.parseInt(value);
                }
            } else {
// TODO:
//                if (choiceElem) {
//                    Logger.log();
//                }
//                else if (groupElem) {
//                    Logger.log();
//                }
//                else if (sequenceElem) {
//                    Logger.log();
//                }
//                else {
//                    Logger.log();
//                }

            }
        }
    }

    //---------------------------------------------------------------------------

    private void handleChildren(ElementInfo ei) {
        @SuppressWarnings("unchecked")
        List<Element> children = ei.element.getChildren();

        for (Element elChild : children) {
            String elName = elChild.getName();

            if (elName.equals("complexType")) {
                complexType = new ComplexTypeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
            } else if (elName.equals("simpleType")) {
                simpleType = new SimpleTypeEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix);
                if (simpleType.name == null) {
                    simpleType.name = name + "HASHS";
                }
            } else if (elName.equals("key")) {
                Logger.log();
            } else if (elName.equals("annotation")) {

            } else {
                Logger.log();
            }
        }
    }

    //---------------------------------------------------------------------------

    private void handleContainerChildren(ElementInfo ei, ArrayList<ElementEntry> elements) {
        @SuppressWarnings("unchecked")
        List<Element> children = ei.element.getChildren();

        for (Element elChild : children) {

            if (groupElem) {
                Log.warning(Geonet.SCHEMA_MANAGER, "WARNING found element children for group in element " + name + " " + ref);
            }
            String elName = elChild.getName();

            if (elName.equals("annotation")) {
                @SuppressWarnings("unchecked")
                List<Element> appinfo = elChild.getChildren();
                for (Element elElem : appinfo) {
                    if (elElem.getName().equals("appinfo")) {
                        name = elElem.getText();
                    }
                }
            } else if (elName.equals("element") || elName.equals("choice") || elName.equals("sequence") || elName.equals("group")) {
                elements.add(new ElementEntry(elChild, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else {
                Logger.log();
            }
        }
    }
}

//==============================================================================

