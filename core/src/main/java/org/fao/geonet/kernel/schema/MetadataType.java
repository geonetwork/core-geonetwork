//==============================================================================
//===
//===   MetadataType
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

import org.fao.geonet.constants.Edit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//==============================================================================

public class MetadataType {
    public boolean hasContainers = false;
    private String name;
    private boolean isOrType;
    private ArrayList<String> alElements = new ArrayList<String>();
    private List<String> alTypes = new ArrayList<String>();
    private List<Integer> alMinCard = new ArrayList<Integer>();
    private List<Integer> alMaxCard = new ArrayList<Integer>();
    private List<MetadataAttribute> alAttribs = new ArrayList<MetadataAttribute>();
    private List<Boolean> alExamineSubs = new ArrayList<Boolean>();

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    MetadataType() {
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public int getElementCount() {
        return alElements.size();
    }

    @SuppressWarnings("unchecked")
    public List<String> getElementList() {
        return (List<String>) alElements.clone();
    }

    //--------------------------------------------------------------------------

    /**
     * Return the component in a given position
     */

    public String getElementAt(int pos) {
        return alElements.get(pos);
    }

    //--------------------------------------------------------------------------

    /**
     * Return the type of element in a given position
     */

    public String getElementTypeAt(int pos) {
        return alTypes.get(pos);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the min cardinality of element in a given pos
     */

    public int getMinCardinAt(int pos) {
        return alMinCard.get(pos);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the max cardinality of element in a given pos
     */

    public int getMaxCardinAt(int pos) {
        return alMaxCard.get(pos);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns true is this type has children in or mode
     */

    public boolean isOrType() {
        return isOrType;
    }

    //--------------------------------------------------------------------------

    void setOrType(boolean yesno) {
        isOrType = yesno;
    }

    //--------------------------------------------------------------------------

    public int getAttributeCount() {
        return alAttribs.size();
    }

    //--------------------------------------------------------------------------

    public MetadataAttribute getAttributeAt(int i) {
        return alAttribs.get(i);
    }

    //--------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public List<String> getAlElements() {return alElements; }

    public List<MetadataAttribute> getAlAttribs() {return alAttribs; }

    //---------------------------------------------------------------------------
    //---
    //--- Package protected API methods
    //---
    //---------------------------------------------------------------------------

    void setName(String name) {
        this.name = name;
    }

    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------

    public String toString() {
        StringBuilder res = new StringBuilder();

        if (isOrType) res.append("IsOrType = TRUE ");
        else res.append("IsOrType = FALSE ");

        for (int i = 0; i < alElements.size(); i++) {
            String comp = getElementAt(i);

            int min = getMinCardinAt(i);
            int max = getMaxCardinAt(i);

            String sMax = (max > 1) ? "n" : String.valueOf(max);

            res.append(comp).append("/").append(min).append("-").append(sMax).append(" ");
        }


        StringBuilder attrs = new StringBuilder();
        for (int i = 0; i < alAttribs.size(); i++) {
            attrs.append("Attribute (").append(i).append(") ").append(getAttributeAt(i).name).append(": ");
            @SuppressWarnings("rawtypes")
            Set<String> alAtts = getAttributeAt(i).values;
            if (alAtts.size() > 0) {
                attrs.append(getAttributeAt(i).values);
                attrs.append(" ");
            }
            attrs.append("Default Value: ").append(getAttributeAt(i).defValue);
            if (getAttributeAt(i).required) attrs.append(" REQUIRED ");
        }
        if (attrs.length() > 0)
            res.append(" Attributes: ").append(attrs);

        return res.toString();
    }

    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------

    void addElementWithType(String name, String elementType, int minCard, int maxCard) {
        addElement(name, elementType, false, minCard, maxCard);
    }

    //---------------------------------------------------------------------------

    void addRefElementWithType(String name, String elementType, int minCard, int maxCard) {
        addElement(name, elementType, true, minCard, maxCard);
    }

    //---------------------------------------------------------------------------

    void addElement(String name, String elementType, Boolean examineElementSubs, int minCard, int maxCard) {

        // Don't add the same element to a type
        if (alElements.contains(name)) {
            if (alElements.indexOf(name) == alTypes.indexOf(elementType)) {
                return;
            }
        }
        alElements.add(name);
        alTypes.add(elementType);
        alExamineSubs.add(examineElementSubs);
        alMinCard.add(minCard);
        alMaxCard.add(maxCard);
        if (name.contains(Edit.RootChild.CHOICE) ||
            name.contains(Edit.RootChild.GROUP) ||
            name.contains(Edit.RootChild.SEQUENCE)) hasContainers = true;
    }

    //---------------------------------------------------------------------------

    void addAttribute(MetadataAttribute ma) {
        alAttribs.add(ma);
    }
}

//==============================================================================

