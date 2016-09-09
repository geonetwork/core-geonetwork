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
import org.jdom.Element;

//=============================================================================

public class Record {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    private Header header;

    //---------------------------------------------------------------------------
    private Element metadata;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    private List<Element> abouts = new ArrayList<Element>();

    public Record() {
    }

    //---------------------------------------------------------------------------

    public Record(Element record) {
        build(record);
    }

    //---------------------------------------------------------------------------

    public Header getHeader() {
        return header;
    }

    //---------------------------------------------------------------------------

    public void setHeader(Header header) {
        this.header = header;
    }

    //---------------------------------------------------------------------------

    public Element getMetadata() {
        return metadata;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public void setMetadata(Element metadata) {
        this.metadata = metadata;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public Iterator<Element> getAbouts() {
        return abouts.iterator();
    }

    public Element toXml() {
        Element rec = new Element("record", OaiPmh.Namespaces.OAI_PMH);
        Element md = new Element("metadata", OaiPmh.Namespaces.OAI_PMH);

        rec.addContent(header.toXml());
        rec.addContent(md);
        md.addContent((Element) metadata.clone());

        for (Element about : abouts)
            rec.addContent((Element) about.clone());

        return rec;
    }

    @SuppressWarnings("unchecked")
    private void build(Element record) {
        Element header = record.getChild("header", OaiPmh.Namespaces.OAI_PMH);
        Element mdata = record.getChild("metadata", OaiPmh.Namespaces.OAI_PMH);

        //--- store header

        this.header = new Header(header);

        //--- store metadata

        List<Element> list = mdata.getChildren();

        if (list.size() != 0)
            metadata = list.get(0);

        //--- add about information

        for (Element e : (List<Element>) record.getChildren("about", OaiPmh.Namespaces.OAI_PMH))
            abouts.add(e);
    }
}

//=============================================================================

