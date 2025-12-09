/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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
import jeeves.server.JeevesEngine;
import jeeves.server.UserSession;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.ServiceRequestFactory;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.Util;
import org.fao.geonet.exceptions.FileUploadTooBigEx;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

@Controller
public class GenericController {
    public static final String USER_SESSION_ATTRIBUTE_KEY = Jeeves.Elem.SESSION;

    @Autowired
    NodeInfo node;


    @RequestMapping(value = {
        "/{portal}/{lang:[a-z]{3}}/{service:.+}"
    })
    @ResponseBody
    public void dispatch(@PathVariable String portal,
                         @PathVariable String lang,
                         @PathVariable String service,
                         HttpServletRequest request,
                         HttpServletResponse response)
        throws Exception {
        HttpSession httpSession = request.getSession(false);

        String ip = ServiceManager.getRequestIpAddress(request);

        Log.info(Log.REQUEST, "==========================================================");

        Log.info(Log.REQUEST, "HTML Request (from " + ip + ") : " + request.getRequestURI());
        if (Log.isDebugEnabled(Log.REQUEST)) {
            Log.debug(Log.REQUEST, "Method       : " + request.getMethod());
            Log.debug(Log.REQUEST, "Content type : " + request.getContentType());
            // Log.debug(Log.REQUEST, "Context path : "+ req.getContextPath());
            // Log.debug(Log.REQUEST, "Char encoding: "+
            // req.getCharacterEncoding());
            Log.debug(Log.REQUEST, "Accept       : " + request.getHeader("Accept"));
            // Log.debug(Log.REQUEST, "Server name  : "+ req.getServerName());
            // Log.debug(Log.REQUEST, "Server port  : "+ req.getServerPort());
        }

        if (Log.isDebugEnabled(Log.REQUEST)) {
            if (httpSession != null) {
                Log.debug(Log.REQUEST, "Session id is " + httpSession.getId());
            } else {
                Log.debug(Log.REQUEST, "No session created");
            }
        }

        UserSession session = null;

        if (httpSession != null) {
            session = (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE_KEY);

            if (session == null) {
                // --- create session

                session = new UserSession();

                httpSession.setAttribute(USER_SESSION_ATTRIBUTE_KEY, session);
                session.setsHttpSession(httpSession);

                if (Log.isDebugEnabled(Log.REQUEST))
                    Log.debug(Log.REQUEST, "Session created for client : " + ip);
            }
        }

        ServiceRequest srvReq = null;

        ApplicationContext jeevesApplicationContext = ApplicationContextHolder.get();
        JeevesEngine jeeves = jeevesApplicationContext.getBean(JeevesEngine.class);
        try {
            final Path uploadDir = jeeves.getUploadDir();
            srvReq = ServiceRequestFactory.create(request, response,
                portal, lang, service, uploadDir, jeeves.getMaxUploadSize());
        } catch (FileUploadTooBigEx e) {
            StringBuffer sb = new StringBuffer();
            sb.append("File upload too big - exceeds ").append(jeeves.getMaxUploadSize()).append(" Mb\n");
            sb.append("Error : ").append(e.getClass().getName()).append("\n");
            response.sendError(400, sb.toString());

            // now stick the stack trace on the end and log the whole lot
            sb.append("Stack :\n");
            sb.append(Util.getStackTrace(e));
            Log.error(Log.REQUEST, sb.toString());

        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();

            sb.append("Cannot build ServiceRequest\n");
            sb.append("Cause : ").append(e.getMessage()).append("\n");
            sb.append("Error : ").append(e.getClass().getName()).append("\n");
            response.sendError(400, sb.toString());

            // now stick the stack trace on the end and log the whole lot
            sb.append("Stack :\n");
            sb.append(Util.getStackTrace(e));
            Log.error(Log.REQUEST, sb.toString());
        }

        // --- execute request
        try {
            jeeves.dispatch(srvReq, session);
        } finally {
            // Cleanup uploaded resources
            if (request instanceof MultipartRequest) {
                Map<String, MultipartFile> files = ((MultipartRequest) request).getFileMap();
                Iterator<Map.Entry<String, MultipartFile>> it =
                    files.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, MultipartFile> file = it.next();

                    Path uploadedFile = jeeves.getUploadDir().resolve(file.getValue().getOriginalFilename());
                    FileUtils.deleteQuietly(uploadedFile.toFile());
                }
            }

        }
    }
}
