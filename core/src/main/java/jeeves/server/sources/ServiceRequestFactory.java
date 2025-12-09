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
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves.server.sources;

import jeeves.constants.Jeeves;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;
import jeeves.server.sources.http.HttpServiceRequest;

import org.fao.geonet.Constants;
import org.fao.geonet.exceptions.FileUploadTooBigEx;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//=============================================================================

public final class ServiceRequestFactory {

    private static final String JSON_URL_FLAG = "_content_type=json";
    private static String DEBUG_URL_FLAG = "!";

    /**
     * Default constructor. Builds a ServiceRequestFactory.
     */
    private ServiceRequestFactory() {
    }

    /**
     * Builds the request with data supplied by tomcat. A request is in the form:
     * srv/<language>/<service>[!]<parameters>
     */

    public static ServiceRequest create(HttpServletRequest req,
                                        HttpServletResponse res,
                                        String portal,
                                        String lang,
                                        String service,
                                        Path uploadDir, long maxUploadSize) throws Exception {
        String url = req.getPathInfo();

        // FIXME: if request character encoding is undefined set it to UTF-8

        String encoding = req.getCharacterEncoding();
        try {
            // verify that encoding is valid
            Charset.forName(encoding);
        } catch (Exception e) {
            encoding = null;
        }

        if (encoding == null) {
            try {
                req.setCharacterEncoding(Constants.ENCODING);
            } catch (UnsupportedEncodingException ex) {
                Log.error(Log.JEEVES, "Create request error: " + ex.getMessage(), ex);
            }
        }

        //--- extract basic info

        HttpServiceRequest srvReq = new HttpServiceRequest(res);

        srvReq.setDebug(extractDebug(url));
        srvReq.setLanguage(lang);
        srvReq.setService(service);
        srvReq.setJSONOutput(extractJSONFlag(req.getQueryString()));
        String ip = ServiceManager.getRequestIpAddress(req);
        srvReq.setAddress(ip);
        srvReq.setOutputStream(res.getOutputStream());

        //--- discover the input/output methods

        String accept = req.getHeader("Accept");

        if (accept != null) {
            int soapNDX = accept.indexOf("application/soap+xml");
            int xmlNDX = accept.indexOf("application/xml");
            int htmlNDX = accept.indexOf("html");

            if (soapNDX != -1)
                srvReq.setOutputMethod(OutputMethod.SOAP);

            else if (xmlNDX != -1 && htmlNDX == -1)
                srvReq.setOutputMethod(OutputMethod.XML);
        }

        if ("POST".equals(req.getMethod())) {
            srvReq.setInputMethod(InputMethod.POST);

            String contType = req.getContentType();

            if (contType != null) {
                if (contType.indexOf("application/soap+xml") != -1) {
                    srvReq.setInputMethod(InputMethod.SOAP);
                    srvReq.setOutputMethod(OutputMethod.SOAP);
                } else if (contType.indexOf("application/xml") != -1 || contType.indexOf("text/xml") != -1)
                    srvReq.setInputMethod(InputMethod.XML);
            }
        }

        //--- retrieve input parameters

        InputMethod input = srvReq.getInputMethod();

        if ((input == InputMethod.XML) || (input == InputMethod.SOAP)) {
            if (req.getMethod().equals("GET"))
                srvReq.setParams(extractParameters(req, uploadDir, maxUploadSize));
            else
                srvReq.setParams(extractXmlParameters(req));
        } else {
            //--- GET or POST
            srvReq.setParams(extractParameters(req, uploadDir, maxUploadSize));
        }

        srvReq.setHeaders(extractHeaders(req));

        return srvReq;
    }

    /**
     * Build up a map of the HTTP headers.
     *
     * @param req The web request
     * @return Map of header keys and values.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> extractHeaders(HttpServletRequest req) {
        Map<String, String> headerMap = new HashMap<String, String>();
        for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            headerMap.put(key, req.getHeader(key));
        }
        // The remote user needs to be saved as a header also
        if (req.getRemoteUser() != null) {
            headerMap.put("REMOTE_USER", req.getRemoteUser());
        }
        return headerMap;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Input retrieving methods
    //---
    //---------------------------------------------------------------------------

    /**
     * Extracts the debug option from the url
     */

    private static boolean extractDebug(String url) {
        if (url == null)
            return false;

        return url.indexOf(DEBUG_URL_FLAG) != -1;
    }

    /**
     * Extracts the JSON output flag from the url
     */
    private static boolean extractJSONFlag(String url) {
        if (url == null)
            return false;

        return url.indexOf(JSON_URL_FLAG) != -1;
    }


    /**
     * Extracts the service name from the url
     */

    private static String extractService(String url) {
        if (url == null)
            return null;

        if (url.endsWith(DEBUG_URL_FLAG))
            url = url.substring(0, url.length() - 1);

        if (url.endsWith(JSON_URL_FLAG))
            url = url.substring(0, url.length() - JSON_URL_FLAG.length());

        int pos = url.lastIndexOf('/');

        if (pos == -1)
            return null;

        return url.substring(pos + 1);
    }

    //---------------------------------------------------------------------------

    private static Element extractXmlParameters(HttpServletRequest req)
        throws IOException, JDOMException {
        return Xml.loadStream(req.getInputStream());
    }

    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Element extractParameters(HttpServletRequest req, Path uploadDir, long maxUploadSize) throws Exception {
        //--- set parameters from multipart request
        if (req instanceof MultipartRequest) {
            AbstractMultipartHttpServletRequest request = (AbstractMultipartHttpServletRequest) req;
            return getMultipartParams(request, uploadDir, maxUploadSize);
        }
        Element params = new Element(Jeeves.Elem.REQUEST);
        convertParamToElem(req, params);


        return params;
    }

    private static void convertParamToElem(HttpServletRequest req, Element params) {
        //--- add parameters from POST request

        for (Enumeration<String> e = req.getParameterNames(); e.hasMoreElements(); ) {
            String name = e.nextElement();

            //--- we don't overwrite params given in the url

            if (!name.equals("")) {
                if (params.getChild(name) == null) {
                    for (String value : req.getParameterValues(name)) {
                        params.addContent(new Element(name).setText(value));
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------

    private static Element getMultipartParams(AbstractMultipartHttpServletRequest req, Path uploadDir, long maxUploadSize) throws Exception {
        Element params = new Element("params");

        long maxSizeInBytes = maxUploadSize * 1024L * 1024L;
        convertParamToElem(req, params);
        final Map<String, MultipartFile> fileMap = req.getFileMap();
        for (Map.Entry<String, MultipartFile> fileEntry : fileMap.entrySet()) {
            final MultipartFile multipartFile = fileEntry.getValue();
            final long size = multipartFile.getSize();
            if (size > maxSizeInBytes) {
                throw new FileUploadTooBigEx();
            }
            String file = fileEntry.getValue().getOriginalFilename();
            final String type = multipartFile.getContentType();
            if (Log.isDebugEnabled(Log.REQUEST)) {
                Log.debug(Log.REQUEST, "Uploading file " + file + " type: " + type + " size: " + size);
            }

            //--- remove path information from file (some browsers put it, like IE)
            file = simplifyName(file);
            if (Log.isDebugEnabled(Log.REQUEST)) {
                Log.debug(Log.REQUEST, "File is called " + file + " after simplification");
            }
            multipartFile.transferTo(uploadDir.resolve(file).toFile());

            Element elem = new Element(multipartFile.getName())
                .setAttribute("type", "file")
                .setAttribute("size", Long.toString(size))
                .setText(file);

            if (type != null)
                elem.setAttribute("content-type", type);

            if (Log.isDebugEnabled(Log.REQUEST))
                Log.debug(Log.REQUEST, "Adding to parameters: " + Xml.getString(elem));
            params.addContent(elem);
        }

        return params;
    }

    //---------------------------------------------------------------------------

    private static String simplifyName(String file) {
        //--- get the file name without path

        file = new File(file).getName();

        //--- the previous getName method is not enough
        //--- with IE and a server running on Linux we still have a path problem

        int pos1 = file.lastIndexOf("\\");
        int pos2 = file.lastIndexOf('/');

        int pos = Math.max(pos1, pos2);

        if (pos != -1)
            file = file.substring(pos + 1).trim();

        //--- we need to sanitize the filename here - make it UTF8, no ctrl
        //--- characters and only containing [A-Z][a-z][0-9],_.-

        //--- start by converting to UTF-8
        try {
            byte[] utf8Bytes = file.getBytes("UTF8");
            file = new String(utf8Bytes, "UTF8");
        } catch (UnsupportedEncodingException e) {
            Log.error(Log.JEEVES, e.getMessage(), e);
        }

        //--- replace whitespace with underscore
        file = file.replaceAll("\\s", "_");

        //--- remove everything that isn't [0-9][a-z][A-Z],#_.-
        file = file.replaceAll("[^\\w&&[^,_.-]]", "");
        return file;
    }
}

//=============================================================================

