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

package org.fao.geonet.csw.common.requests;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

//=============================================================================

/**
 * Params: - elementSetName (0..1) Can be 'brief', 'summary', 'full'. Default is 'summary' - id
 * (1..n)
 */

public class GetRecordByIdRequest extends CatalogRequest {
    private ElementSetName setName;

    private List<String> alIds = new ArrayList<String>();

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------


    public GetRecordByIdRequest(ServiceContext context) {
        super(context);
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public void setElementSetName(ElementSetName name) {
        setName = name;
    }

    //---------------------------------------------------------------------------

    public void addId(String id) {
        alIds.add(id);
    }

    //---------------------------------------------------------------------------

    public void clearIds() {
        alIds.clear();
    }

    //---------------------------------------------------------------------------
    //---
    //--- Protected methods
    //---
    //---------------------------------------------------------------------------

    protected String getRequestName() {
        return "GetRecordById";
    }

    //---------------------------------------------------------------------------

    protected void setupGetParams() {
        addParam("request", getRequestName());
        addParam("service", Csw.SERVICE);
        addParam("version", getServerVersion());
        // heikki doeleman: set outputSchema as per CSW 2.0.2 specification 07-45 section 7.4:
        if (outputSchema != null) {
            addParam("outputSchema", outputSchema);
        }
        if (setName != null) {
            addParam("elementSetName", setName);
        }
        fill("id", alIds);
    }

    //---------------------------------------------------------------------------

    protected Element getPostParams() {
        Element params = new Element(getRequestName(), Csw.NAMESPACE_CSW);

        //--- 'service' and 'version' are common mandatory attributes
        params.setAttribute("service", Csw.SERVICE);
        params.setAttribute("version", getServerVersion());

        // heikki doeleman: set outputSchema as per CSW 2.0.2 specification 07-45 section 7.4:
        if (outputSchema != null) {
            params.setAttribute("outputSchema", outputSchema);
        }

        fill(params, "Id", alIds);

        if (setName != null) {
            Element elem = new Element("ElementSetName", Csw.NAMESPACE_CSW);
            elem.setText(setName.toString());

            params.addContent(elem);
        }
        return params;
    }
}

//=============================================================================

