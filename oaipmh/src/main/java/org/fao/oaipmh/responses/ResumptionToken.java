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

import org.fao.geonet.domain.ISODate;
import org.fao.oaipmh.OaiPmh;
import org.jdom.Element;

//=============================================================================

public class ResumptionToken {
    private String token;
    private ISODate expirDate;
    private Integer listSize;
    private Integer cursor;

    /**
     * Default constructor. Builds a ResumptionToken.
     */
    public ResumptionToken() {
    }

    /**
     * Default constructor. Builds a ResumptionToken.
     */
    public ResumptionToken(Element rt) {
        token = rt.getText();

        String expDt = rt.getAttributeValue("expirationDate");
        String listSz = rt.getAttributeValue("completeListSize");
        String curs = rt.getAttributeValue("cursor");

        expirDate = (expDt == null) ? null : new ISODate(expDt);
        listSize = (listSz == null) ? null : Integer.valueOf(listSz);
        cursor = (curs == null) ? null : Integer.valueOf(curs);
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ISODate getExpirDate() {
        return expirDate;
    }

    public void setExpirDate(ISODate date) {
        this.expirDate = date;
    }

    public Integer getCompleteListSize() {
        return listSize;
    }

    public Integer getCursor() {
        return cursor;
    }

    //---------------------------------------------------------------------------

    public boolean isTokenEmpty() {
        return token.length() == 0;
    }

    //---------------------------------------------------------------------------

    public Element toXml() {
        Element root = new Element("resumptionToken", OaiPmh.Namespaces.OAI_PMH);

        root.setText(token);

        if (expirDate != null)
            root.setAttribute("expirationDate", expirDate.toString());

        if (listSize != null)
            root.setAttribute("completeListSize", listSize.toString());

        if (cursor != null)
            root.setAttribute("cursor", cursor.toString());

        return root;
    }

}

//=============================================================================

