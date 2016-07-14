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

package org.fao.oaipmh.exceptions;

import java.util.Map;

import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.util.Lib;
import org.jdom.Element;

//=============================================================================

public class OaiPmhException extends Exception {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    protected static final String BAD_ARGUMENT = "badArgument";
    protected static final String BAD_RESUMPTION_TOKEN = "badResumptionToken";

    //---------------------------------------------------------------------------
    protected static final String BAD_VERB = "badVerb";

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    protected static final String CANNOT_DISSEMINATE_FORMAT = "cannotDisseminateFormat";
    protected static final String ID_DOES_NOT_EXIST = "idDoesNotExist";

    //---------------------------------------------------------------------------
    protected static final String NO_RECORDS_MATCH = "noRecordsMatch";

    //---------------------------------------------------------------------------
    //---
    //--- Static marshalling methods
    //---
    //---------------------------------------------------------------------------
    protected static final String NO_METADATA_FORMATS = "noMetadataFormats";

    //---------------------------------------------------------------------------
    protected static final String NO_SET_HIERARCHY = "noSetHierarchy";

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    /**
     *
     */
    private static final long serialVersionUID = -370658366099038395L;
    private String code;
    private Element response;
    public OaiPmhException(String code, String message) {
        this(code, message, null);
    }
    public OaiPmhException(String code, String message, Element response) {
        super(message);

        this.code = code;
        this.response = response;
    }

    public static Element marshal(OaiPmhException e, String reqUrl, Map<String, String> reqParams) {
        Element err = new Element("error", OaiPmh.Namespaces.OAI_PMH);

        err.setText(e.getMessage());
        err.setAttribute("code", e.getCode());

        Element root = Lib.createOaiRoot(reqUrl, reqParams, err);

        return root;
    }

    public static void unmarshal(Element response) throws OaiPmhException {
        Element error = response.getChild("error", OaiPmh.Namespaces.OAI_PMH);

        //--- no errors: ok, skip
        if (error == null)
            return;

        String code = error.getAttributeValue("code");
        String msg = error.getText();

        if (code.equals(BAD_ARGUMENT))
            throw new BadArgumentException(msg, response);

        if (code.equals(BAD_RESUMPTION_TOKEN))
            throw new BadResumptionTokenException(msg, response);

        if (code.equals(BAD_VERB))
            throw new BadVerbException(msg, response);

        if (code.equals(CANNOT_DISSEMINATE_FORMAT))
            throw new CannotDisseminateFormatException(msg, response);

        if (code.equals(ID_DOES_NOT_EXIST))
            throw new IdDoesNotExistException(msg, response);

        if (code.equals(NO_RECORDS_MATCH))
            throw new NoRecordsMatchException(msg, response);

        if (code.equals(NO_METADATA_FORMATS))
            throw new NoMetadataFormatsException(msg, response);

        if (code.equals(NO_SET_HIERARCHY))
            throw new NoSetHierarchyException(msg, response);

        //--- we should not get here
        throw new RuntimeException("Unknown error code : " + code);
    }

    public String getCode() {
        return code;
    }

    //---------------------------------------------------------------------------

    public Element getResponse() {
        return response;
    }

    public String toString() {
        return getClass().getSimpleName() + ": code=" + code + ", message=" + getMessage();
    }
}

//=============================================================================

