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

import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.util.Lib;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Map;

//import org.fao.oaipmh.util.Xml;

//=============================================================================

public abstract class AbstractRequest {
    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    private final XmlRequest transport;
    private Path schemaPath;

    public AbstractRequest(GeonetHttpRequestFactory transport) {
        this.transport = transport.createXmlRequest();
    }

    public AbstractRequest(XmlRequest transport) {
        this.transport = transport;
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    /**
     * @return the schemaPath
     */
    public Path getSchemaPath() {
        return schemaPath;
    }


    /**
     * @param schemaPath the schemaPath to set
     */
    public void setSchemaPath(Path schemaPath) {
        this.schemaPath = schemaPath;
    }

    /**
     * @return the transport
     */
    public XmlRequest getTransport() {
        return transport;
    }


    public abstract String getVerb();

    public abstract AbstractResponse execute() throws Exception;

    //---------------------------------------------------------------------------
    //---
    //--- Protected methods
    //---
    //---------------------------------------------------------------------------

    protected Element sendRequest(Map<String, String> params) throws Exception {
        transport.clearParams();

        for (Map.Entry<String, String> param : params.entrySet()) {
            transport.addParam(param.getKey(), param.getValue());
        }

        transport.addParam("verb", getVerb());

        Element response = transport.execute();

        if (!Lib.isRootValid(response)) {
            throw new Exception("Response is not in OAI-PMH format");
        }

        //--- validate the result
        try {
            Xml.validate(response);
        } catch (XSDValidationErrorEx e) {
            response.addContent(new Element("error").setText(e.getMessage()));
        }

        //--- raises an exception if the case
        OaiPmhException.unmarshal(response);

        return response;
    }
}

//=============================================================================


