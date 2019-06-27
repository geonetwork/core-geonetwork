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

package jeeves.server.sources.http;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.config.springutil.JeevesContextLoaderListener;
import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.constants.Jeeves;
import jeeves.server.JeevesEngine;
import jeeves.server.UserSession;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.ServiceRequestFactory;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Util;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.FileUploadTooBigEx;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

//=============================================================================

/**
 * This is the main class. It handles http connections and inits the system
 */

public class JeevesServlet extends HttpServlet {
    public static final String USER_SESSION_ATTRIBUTE_KEY = Jeeves.Elem.SESSION;
    private static final long serialVersionUID = 1L;
    private boolean initialized = false;

    //---------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //---------------------------------------------------------------------------

    public void init() throws ServletException {
        try {
            final ServletContext servletContext = getServletContext();
            final ServletPathFinder pathFinder = new ServletPathFinder(servletContext);

            JeevesApplicationContext jeevesAppContext = (JeevesApplicationContext) servletContext.getAttribute(User.NODE_APPLICATION_CONTEXT_KEY);
            ApplicationContextHolder.set(jeevesAppContext);
            jeevesAppContext.setServletConfig(getServletConfig());
            jeevesAppContext.getBean(JeevesEngine.class).init(pathFinder.getAppPath(), pathFinder.getConfigPath(),
                pathFinder.getBaseUrl(), this);

            initialized = true;
        } catch (Throwable e) {
            JeevesEngine.handleStartupError(e);
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- HTTP Request / Response
    //---
    //---------------------------------------------------------------------------

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        execute(req, res);
    }

    //---------------------------------------------------------------------------

    /**
     * This is the core of the servlet. It receives http requests and invokes the proper service
     */

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        execute(req, res);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void execute(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String ip = req.getRemoteAddr();
        // if we do have the optional x-forwarded-for request header then
        // use whatever is in it to record ip address of client
        String forwardedFor = req.getHeader("x-forwarded-for");
        if (forwardedFor != null) {
            ip = forwardedFor;
        }

        Log.info(Log.REQUEST, "==========================================================");
        Log.info(Log.REQUEST, "HTML Request (from " + ip + ") : " + req.getRequestURI());
        if (Log.isDebugEnabled(Log.REQUEST)) {
            Log.debug(Log.REQUEST, "Method       : " + req.getMethod());
            Log.debug(Log.REQUEST, "Content type : " + req.getContentType());
            //		Log.debug(Log.REQUEST, "Context path : " + req.getContextPath());
            //		Log.debug(Log.REQUEST, "Char encoding: " + req.getCharacterEncoding());
            Log.debug(Log.REQUEST, "Accept       : " + req.getHeader("Accept"));
            //		Log.debug(Log.REQUEST, "Server name  : " + req.getServerName());
            //		Log.debug(Log.REQUEST, "Server port  : " + req.getServerPort());
        }
//		for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
//			String theHeader = (String)e.nextElement();
//        if(Log.isDebugEnabled(Log.REQUEST)) {
//			Log.debug(Log.REQUEST, "Got header: "+theHeader);
//			Log.debug(Log.REQUEST, "With value: "+req.getHeader(theHeader));
//        }
//		}
        HttpSession httpSession = req.getSession();
        if (Log.isDebugEnabled(Log.REQUEST)) {
            Log.debug(Log.REQUEST, "Session id is " + httpSession.getId());
        }
        UserSession session = (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE_KEY);

        //------------------------------------------------------------------------
        //--- create a new session if doesn't exist

        if (session == null) {
            //--- create session

            session = new UserSession();

            httpSession.setAttribute(USER_SESSION_ATTRIBUTE_KEY, session);
            session.setsHttpSession(httpSession);

            if (Log.isDebugEnabled(Log.REQUEST)) {
                Log.debug(Log.REQUEST, "Session created for client : " + ip);
            }
        }

        //------------------------------------------------------------------------
        //--- build service request

        ServiceRequest srvReq;

        //--- create request

        ConfigurableApplicationContext applicationContext = JeevesDelegatingFilterProxy.getApplicationContextFromServletContext
            (getServletContext());
        JeevesEngine jeeves = applicationContext.getBean(JeevesEngine.class);

        try {
            srvReq = ServiceRequestFactory.create(req, res,
                null, null, null, jeeves.getUploadDir(), jeeves.getMaxUploadSize());
        } catch (FileUploadTooBigEx e) {
            StringBuilder sb = new StringBuilder();
            sb.append("File upload too big - exceeds ").append(jeeves.getMaxUploadSize()).append(" Mb\n")
                .append("Error : ").append(e.getClass().getName()).append("\n");

            res.sendError(SC_BAD_REQUEST, sb.toString());

            // now stick the stack trace on the end and log the whole lot
            sb.append("Stack :\n").append(Util.getStackTrace(e));

            Log.error(Log.REQUEST, sb.toString());
            return;
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();

            sb.append("Cannot build ServiceRequest\n").append("Cause : ").append(e.getMessage()).append("\n")
                .append("Error : ").append(e.getClass().getName()).append("\n");

            res.sendError(SC_BAD_REQUEST, sb.toString());

            // now stick the stack trace on the end and log the whole lot
            sb.append("Stack :\n").append(Util.getStackTrace(e));
            Log.error(Log.REQUEST, sb.toString());
            return;
        }

        // Set the language of the request as the preferred language in a cookie
        final Cookie langCookie = new Cookie(Jeeves.LANG_COOKIE, srvReq.getLanguage());
        langCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(7));
        langCookie.setComment("Keeps the last language chosen to be the preferred language");
        langCookie.setVersion(1);
        langCookie.setPath("/");
        res.addCookie(langCookie);

        //--- execute request

        jeeves.dispatch(srvReq, session);
    }

    public boolean isInitialized() {
        return initialized;
    }
}

//=============================================================================


