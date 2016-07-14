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

import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.domain.ISODate;
import org.fao.oaipmh.OaiPmh;
import org.jdom.Attribute;
import org.jdom.Element;

//=============================================================================

public abstract class AbstractResponse {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    private Element response;

    //---------------------------------------------------------------------------
    private ISODate responseDate;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    private Map<String, String> request = new HashMap<String, String>();

    public AbstractResponse() {
        responseDate = new ISODate();
    }

    public AbstractResponse(Element response) {
        this.response = response;
        build(response);
    }

    //---------------------------------------------------------------------------

    public Element getResponse() {
        return response;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Protected methods
    //---
    //---------------------------------------------------------------------------

    public ISODate getResponseDate() {
        return responseDate;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public void setResponseDate(ISODate date) {
        responseDate = date;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public abstract Element toXml();

    protected void add(Element parent, String name, String value) {
        parent.addContent(new Element(name, OaiPmh.Namespaces.OAI_PMH).setText(value));
    }

    private void build(Element response) {
        //--- save response date

        responseDate = new ISODate(response.getChildText("responseDate", OaiPmh.Namespaces.OAI_PMH));

        //--- save request parameters

        Element req = response.getChild("request", OaiPmh.Namespaces.OAI_PMH);

        for (Object o : req.getAttributes()) {
            Attribute attr = (Attribute) o;
            request.put(attr.getName(), attr.getValue());
        }
    }
}

//=============================================================================

