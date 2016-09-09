//=============================================================================
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

package org.fao.oaipmh.responses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.util.Lib;
import org.jdom.Element;

//=============================================================================

public class SetInfo {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    private String spec;

    //---------------------------------------------------------------------------
    private String name;

    //---------------------------------------------------------------------------
    private List<Element> descriptions = new ArrayList<Element>();

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public SetInfo() {
    }

    public SetInfo(String spec, String name) {
        this.spec = spec;
        this.name = name;
    }

    //---------------------------------------------------------------------------

    public SetInfo(Element set) {
        build(set);
    }

    //---------------------------------------------------------------------------

    public String getSpec() {
        return spec;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public Iterator<Element> getDescriptions() {
        return descriptions.iterator();
    }

    public Element toXml() {
        Element set = new Element("set", OaiPmh.Namespaces.OAI_PMH);

        Lib.add(set, "setSpec", spec);
        Lib.add(set, "setName", name);

        for (Element descr : descriptions)
            set.addContent((Element) descr.clone());

        return set;
    }

    private void build(Element set) {
        spec = set.getChildText("setSpec", OaiPmh.Namespaces.OAI_PMH);
        name = set.getChildText("setName", OaiPmh.Namespaces.OAI_PMH);

        //--- add description information

        for (Object o : set.getChildren("setDescription", OaiPmh.Namespaces.OAI_PMH))
            descriptions.add((Element) o);
    }
}

//=============================================================================

