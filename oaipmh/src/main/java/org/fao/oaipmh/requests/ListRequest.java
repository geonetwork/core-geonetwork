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

package org.fao.oaipmh.requests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.fao.oaipmh.responses.ResumptionToken;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

//=============================================================================

public abstract class ListRequest extends AbstractRequest {

    protected String resumpToken;
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public ListRequest(GeonetHttpRequestFactory transport) {
        super(transport);
    }

    //---------------------------------------------------------------------------

    public String getResumptionToken() {
        return resumpToken;
    }

    //---------------------------------------------------------------------------

    public void setResumptionToken(String token) {
        resumpToken = token;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public Element resume(ResumptionToken token) throws IOException, OaiPmhException,
        JDOMException, SAXException, Exception {
        Map<String, String> params = new HashMap<String, String>();

        params.put("resumptionToken", token.getToken());

        return sendRequest(params);
    }
}

//=============================================================================

