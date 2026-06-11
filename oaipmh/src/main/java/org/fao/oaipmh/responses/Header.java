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

import org.fao.geonet.domain.ISODate;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.util.Lib;
import org.jdom.Element;

//=============================================================================

public class Header {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    private String identifier;

    //---------------------------------------------------------------------------
    private ISODate dateStamp;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    private boolean deleted;
    private List<String> sets = new ArrayList<String>();

    public Header() {
    }

    //---------------------------------------------------------------------------

    public Header(Element header) {
        build(header);
    }

    //---------------------------------------------------------------------------

    public String getIdentifier() {
        return identifier;
    }

    //---------------------------------------------------------------------------

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    //---------------------------------------------------------------------------

    public ISODate getDateStamp() {
        return dateStamp;
    }

    //---------------------------------------------------------------------------

    public void setDateStamp(ISODate dateStamp) {
        this.dateStamp = dateStamp;
    }

    //---------------------------------------------------------------------------

    public boolean isDeleted() {
        return deleted;
    }

    //---------------------------------------------------------------------------

    public void setDeleted(boolean yesno) {
        deleted = yesno;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public Iterator<String> getSets() {
        return sets.iterator();
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public void clearSets() {
        sets.clear();
    }

    public void addSet(String set) {
        sets.add(set);
    }

    public Element toXml() {
        Element header = new Element("header", OaiPmh.Namespaces.OAI_PMH);

        Lib.add(header, "identifier", identifier);
        Lib.add(header, "datestamp", dateStamp.getDateAndTime());

        for (String set : sets)
            Lib.add(header, "setSpec", set);

        if (deleted)
            header.setAttribute("status", "deleted");

        return header;
    }

    private void build(Element header) {
        Element ident = header.getChild("identifier", OaiPmh.Namespaces.OAI_PMH);
        Element date = header.getChild("datestamp", OaiPmh.Namespaces.OAI_PMH);
        String status = header.getAttributeValue("status");

        //--- store identifier & dateStamp

        identifier = ident.getText();
        dateStamp = new ISODate(date.getText());
        deleted = (status != null);

        //--- add set information

        for (Object o : header.getChildren("setSpec", OaiPmh.Namespaces.OAI_PMH)) {
            Element set = (Element) o;
            sets.add(set.getText());
        }
    }
}

//=============================================================================

