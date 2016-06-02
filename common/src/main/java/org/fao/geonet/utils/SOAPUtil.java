//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.utils;

import org.fao.geonet.exceptions.MissingParameterEx;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.List;

//=============================================================================

public final class SOAPUtil {
    public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://www.w3.org/2003/05/soap-envelope");

    /**
     * Default constructor. Builds a SOAPUtil.
     */
    private SOAPUtil() {
    }

    public static Element embed(Element response) {
        Element envl = new Element("Envelope", NAMESPACE_ENV);
        Element body = new Element("Body", NAMESPACE_ENV);

        envl.addContent(body);
        body.addContent(response);

        return envl;
    }

    //---------------------------------------------------------------------------

    public static Element embedExc(Element error, boolean sender, String errorCode, String message) {
        Namespace ns = NAMESPACE_ENV;

        Element fault = new Element("Fault", ns);

        //--- setup code

        Element code = new Element("Code", ns);
        fault.addContent(code);

        String type = sender ? "env:Sender" : "env:Receiver";
        Element value = new Element("Value", ns);
        value.setText(type);

        code.addContent(value);

        //--- setup subcode

        Element subCode = new Element("Subcode", ns);
        code.addContent(subCode);

        value = new Element("Value", ns);
        value.setText(errorCode);

        subCode.addContent(value);

        //--- setup reason

        Element reason = new Element("Reason", ns);
        fault.addContent(reason);

        Element text = new Element("Text", ns);
        reason.addContent(text);

        text.setText(message);
        text.setAttribute("lang", "en", Namespace.XML_NAMESPACE);

        //--- setup detail

        Element detail = new Element("Detail", ns);
        detail.addContent(error);
        fault.addContent(detail);

        return embed(fault);
    }

    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static Element unembed(Element envelope) throws MissingParameterEx {
        Namespace ns = envelope.getNamespace();
        Element body = envelope.getChild("Body", ns);

        if (body == null)
            throw new MissingParameterEx("Body", envelope);

        List<Element> list = body.getChildren();

        if (list.size() == 0)
            throw new MissingParameterEx("*request*", body);

        return list.get(0);
    }

    //---------------------------------------------------------------------------

    public static boolean isEnvelope(Element elem) {
        if (!elem.getName().equals("Envelope"))
            return false;

        Namespace ns = elem.getNamespace();

        return (ns.getURI().equals(NAMESPACE_ENV.getURI()));
    }
}

//=============================================================================

