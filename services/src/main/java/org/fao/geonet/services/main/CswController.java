/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.component.csw.CatalogDispatcher;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.SOAPUtil;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * Catalogue Services for the Web (CSW) endpoints of the catalogue and its portals.
 *
 * <p>Requests can be sent as KVP (GET or form POST), XML (POST) or SOAP 1.2 (POST).
 * The response is written as XML, or as SOAP when the request is SOAP or the client
 * accepts application/soap+xml.
 */
@Controller
public class CswController {
    private static final Set<String> DISCOVERY_OPERATIONS = Set.of(
        "GetCapabilities", "GetRecords", "GetRecordById", "DescribeRecord", "GetDomain");

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private CatalogDispatcher catalogDispatcher;

    @RequestMapping(value = "/{portal}/{lang:[a-z]{3}}/csw", method = {RequestMethod.GET, RequestMethod.POST})
    public void discovery(@PathVariable String lang, HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        dispatch("csw", lang, request, response);
    }

    /**
     * Transaction and Harvest operations. Only editors can use it (see the security configuration).
     */
    @RequestMapping(value = "/{portal}/{lang:[a-z]{3}}/csw-publication", method = {RequestMethod.GET, RequestMethod.POST})
    public void publication(@PathVariable String lang, HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (NodeInfo.isReadOnly()) {
            writeException(new NoApplicableCodeEx("The catalogue is in read-only mode."),
                HttpServletResponse.SC_SERVICE_UNAVAILABLE, isSoapRequest(request), response);
            return;
        }
        dispatch("csw-publication", lang, request, response);
    }

    private void dispatch(String service, String lang, HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }

        InputMethod input = InputMethod.GET;
        OutputMethod output = OutputMethod.DEFAULT;
        String accept = request.getHeader("Accept");
        if (accept != null) {
            if (accept.contains("application/soap+xml")) {
                output = OutputMethod.SOAP;
            } else if (accept.contains("application/xml") && !accept.contains("html")) {
                output = OutputMethod.XML;
            }
        }
        if ("POST".equals(request.getMethod())) {
            input = InputMethod.POST;
            String contentType = StringUtils.defaultString(request.getContentType());
            if (contentType.contains("application/soap+xml")) {
                input = InputMethod.SOAP;
                output = OutputMethod.SOAP;
            } else if (contentType.contains("application/xml") || contentType.contains("text/xml")) {
                input = InputMethod.XML;
            }
        }
        boolean soap = input == InputMethod.SOAP || output == OutputMethod.SOAP;

        Element params;
        if (input == InputMethod.XML || input == InputMethod.SOAP) {
            try {
                params = Xml.loadStream(request.getInputStream());
            } catch (Exception e) {
                writeException(new NoApplicableCodeEx("Invalid XML request: " + e.getMessage()),
                    HttpServletResponse.SC_BAD_REQUEST, soap, response);
                return;
            }
        } else {
            params = new Element(Jeeves.Elem.REQUEST);
            for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements(); ) {
                String name = names.nextElement();
                if (!name.isEmpty()) {
                    for (String value : request.getParameterValues(name)) {
                        params.addContent(new Element(name).setText(value));
                    }
                }
            }
        }

        ServiceContext context = serviceManager.createServiceContext(service, lang, request);
        context.setInputMethod(input);
        context.setOutputMethod(output);
        context.setAsThreadLocal();

        String operation = getOperation(params);
        Element result;
        if (operation == null) {
            result = info("No 'request' parameter found");
        } else if (service.equals("csw") && !DISCOVERY_OPERATIONS.contains(operation)) {
            result = info("Not a CSW Discovery operation: " + operation
                + ". Did you mean to use the CSW Publication service? Use service name /csw-publication");
        } else if (!settingManager.getValueAsBool(Settings.SYSTEM_CSW_ENABLE, false)) {
            Log.info(Geonet.CSW, "CSW is disabled");
            result = info("CSW is disabled");
        } else {
            result = catalogDispatcher.dispatch(params, context);
        }

        if (soap) {
            write(SOAPUtil.isEnvelope(result) ? result : SOAPUtil.embed(result),
                HttpServletResponse.SC_OK, "application/soap+xml", response);
        } else if (StringUtils.contains(request.getQueryString(), "_content_type=json")) {
            setNoCache(response);
            response.setContentType("application/json; charset=UTF-8");
            response.getOutputStream().write(Xml.getJSON(result).getBytes(StandardCharsets.UTF_8));
        } else {
            write(result, HttpServletResponse.SC_OK, "application/xml", response);
        }
    }

    /**
     * The operation is the request parameter in KVP, the first element of the body in SOAP
     * and the root element in XML.
     */
    private static String getOperation(Element params) {
        if (params.getName().equals(Jeeves.Elem.REQUEST)) {
            return CatalogDispatcher.extractParams(params).get("request");
        }
        if (params.getName().equals("Envelope")) {
            Element body = params.getChild("Body", SOAPUtil.NAMESPACE_ENV);
            @SuppressWarnings("unchecked")
            List<Element> payload = body == null ? List.of() : body.getChildren();
            return payload.isEmpty() ? null : payload.get(0).getName();
        }
        return params.getName();
    }

    private static Element info(String message) {
        return new Element(Jeeves.Elem.RESPONSE).addContent(new Element("info").setText(message));
    }

    private static boolean isSoapRequest(HttpServletRequest request) {
        return StringUtils.contains(request.getContentType(), "application/soap+xml")
            || StringUtils.contains(request.getHeader("Accept"), "application/soap+xml");
    }

    private static void writeException(CatalogException e, int status, boolean soap, HttpServletResponse response)
        throws IOException {
        Element report = CatalogException.marshal(e);
        if (soap) {
            write(SOAPUtil.embedExc(report, status < 500, e.getCode(), e.toString()),
                status, "application/soap+xml", response);
        } else {
            write(report, status, "application/xml", response);
        }
    }

    private static void write(Element xml, int status, String contentType, HttpServletResponse response)
        throws IOException {
        response.setStatus(status);
        setNoCache(response);
        response.setContentType(contentType + "; charset=UTF-8");
        Xml.writeResponse(new Document(xml), response.getOutputStream());
    }

    private static void setNoCache(HttpServletResponse response) {
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "-1");
    }
}
