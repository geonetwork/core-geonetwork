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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.fao.oaipmh.requests.ListRequest;
import org.fao.oaipmh.requests.ListSetsRequest;
import org.fao.oaipmh.responses.SetInfo;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

//=============================================================================

public class ListSetsResponse extends ListResponse {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    private List<SetInfo> sets = new ArrayList<SetInfo>();

    //---------------------------------------------------------------------------

    public ListSetsResponse() {
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public ListSetsResponse(ListRequest lr, Element response) {
        super(lr, response);
    }

    public void clearSets() {
        sets.clear();
    }

    //---------------------------------------------------------------------------

    public int getSize() {
        return sets.size();
    }

    //---------------------------------------------------------------------------

    public void addSet(SetInfo si) {
        sets.add(si);
    }

    //---------------------------------------------------------------------------

    public SetInfo next() throws IOException, OaiPmhException, JDOMException, SAXException, Exception {
        return (SetInfo) super.next();
    }

    //---------------------------------------------------------------------------
    //---
    //--- Protected methods
    //---
    //---------------------------------------------------------------------------

    public Element toXml() {
        Element root = new Element(ListSetsRequest.VERB, OaiPmh.Namespaces.OAI_PMH);

        for (SetInfo si : sets)
            root.addContent(si.toXml());

        ResumptionToken token = getResumptionToken();

        if (token != null)
            root.addContent(token.toXml());

        return root;
    }

    //---------------------------------------------------------------------------

    protected Object createObject(Element object) {
        return new SetInfo(object);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    protected String getListElementName() {
        return "set";
    }


}

//=============================================================================

