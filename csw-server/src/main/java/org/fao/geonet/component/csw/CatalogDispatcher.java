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

package org.fao.geonet.component.csw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jeeves.server.context.ServiceContext;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;

import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.SOAPUtil;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.OperationNotSupportedEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

//=============================================================================
@Component
public class CatalogDispatcher {
    @Autowired
    ApplicationContext appContext;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static Map<String, String> extractParams(Element request) {
        Map<String, String> hm = new HashMap<String, String>();

        @SuppressWarnings("unchecked")
        List<Element> params = request.getChildren();

        for (Element param : params) {
            String name = param.getName().toLowerCase();
            String value = param.getTextTrim();

            hm.put(name, value);
        }

        return hm;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private method
    //---
    //---------------------------------------------------------------------------

    public Element dispatch(Element request, ServiceContext context) {
        context.info("Received:\n" + Xml.getString(request));

        InputMethod im = context.getInputMethod();
        OutputMethod om = context.getOutputMethod();

        boolean inSOAP = (im == InputMethod.SOAP);
        boolean outSOAP = (inSOAP || om == OutputMethod.SOAP);

        CatalogException exc;

        try {
            if (inSOAP)
                request = SOAPUtil.unembed(request);

            Element response = dispatchI(request, context);

            if (outSOAP)
                response = SOAPUtil.embed(response);

            return response;
        } catch (CatalogException e) {
            exc = e;
        } catch (Exception e) {
            context.info("Exception stack trace : \n" + Util.getStackTrace(e));
            // TODO what's this ?
            exc = new NoApplicableCodeEx(e.toString());
        }

        Element response = CatalogException.marshal(exc);
        boolean sender = (exc instanceof NoApplicableCodeEx);

        if (outSOAP)
            return SOAPUtil.embedExc(response, sender, exc.getCode(), exc.toString());

        //TODO: need to set the status code

        return response;
    }

    private Element dispatchI(Element request, ServiceContext context) throws CatalogException {
        InputMethod im = context.getInputMethod();

        if (im == InputMethod.XML || im == InputMethod.SOAP) {
            String operation = request.getName();

            CatalogService cs = lookUpService(operation);

            if (cs == null)
                throw new OperationNotSupportedEx(operation);

            Log.info(Geonet.CSW, "Dispatching operation : " + operation);

            return cs.execute(request, context);
        } else //--- GET or POST/www-encoded request
        {
            Map<String, String> params = extractParams(request);

            String operation = params.get("request");

            if (operation == null)
                throw new MissingParameterValueEx("request");

            CatalogService cs = lookUpService(operation);

            if (cs == null)
                throw new OperationNotSupportedEx(operation);

            request = cs.adaptGetRequest(params);

            if (context.isDebugEnabled())
                context.debug("Adapted GET request is:\n" + Xml.getString(request));
            context.info("Dispatching operation : " + operation);

            return cs.execute(request, context);
        }
    }

    //---------------------------------------------------------------------------

    private CatalogService lookUpService(String operation) {
        return appContext.getBean(CatalogService.BEAN_PREFIX + operation, CatalogService.class);
    }
}
