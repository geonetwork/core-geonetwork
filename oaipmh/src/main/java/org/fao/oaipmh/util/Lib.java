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

package org.fao.oaipmh.util;

import java.util.Map;

import org.fao.geonet.domain.ISODate;
import org.fao.oaipmh.OaiPmh;
import org.jdom.Element;

//=============================================================================

public class Lib {
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static Element createOaiRoot() {
        Element root = new Element(OaiPmh.ROOT_NAME, OaiPmh.Namespaces.OAI_PMH);
        root.setAttribute("schemaLocation", OaiPmh.SCHEMA_LOCATION, OaiPmh.Namespaces.XSI);

        Element date = new Element("responseDate", OaiPmh.Namespaces.OAI_PMH)
            .setText(new ISODate().getDateAndTime());

        root.addContent(date);

        return root;
    }

    //---------------------------------------------------------------------------

    public static Element createOaiRoot(String url, Map<String, String> params, Element response) {
        Element root = Lib.createOaiRoot();
        Element req = new Element("request", OaiPmh.Namespaces.OAI_PMH)
            .setText(url);

        //--- if there is no verb, there are no params

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                req.setAttribute(param.getKey(), param.getValue());
            }
        }
        root.addContent(req);
        root.addContent(response);

        return root;
    }

    //---------------------------------------------------------------------------

    public static void add(Element parent, String name, String value) {
        parent.addContent(new Element(name, OaiPmh.Namespaces.OAI_PMH).setText(value));
    }

    //---------------------------------------------------------------------------

    /**
     * Checks if the response has a valid OAI-PMH root node
     */

    public static boolean isRootValid(Element response) {
        if (!response.getName().equals(OaiPmh.ROOT_NAME))
            return false;

        if (!response.getNamespace().equals(OaiPmh.Namespaces.OAI_PMH))
            return false;

        return true;
    }

    //---------------------------------------------------------------------------

    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

//=============================================================================

