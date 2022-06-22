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
import org.fao.geonet.utils.XmlRequest;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.fao.oaipmh.responses.GetRecordResponse;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

//=============================================================================

public class GetRecordRequest extends AbstractRequest {
    public static final String VERB = "GetRecord";
    private String identifier;
    private String mdPrefix;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public GetRecordRequest(GeonetHttpRequestFactory transport) {
        super(transport);
    }

    public GetRecordRequest(XmlRequest transport) {
        super(transport);
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

    public String getMetadataPrefix() {
        return mdPrefix;
    }

    //---------------------------------------------------------------------------

    public void setMetadataPrefix(String mdPrefix) {
        this.mdPrefix = mdPrefix;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public GetRecordResponse execute() throws IOException, OaiPmhException, JDOMException,
        SAXException, Exception {
        final Map<String, String> params = new HashMap<>();

        params.put("identifier", identifier);
        params.put("metadataPrefix", mdPrefix);

        return new GetRecordResponse(sendRequest(params));
    }

    public String getVerb() {
        return VERB;
    }
}

//=============================================================================

