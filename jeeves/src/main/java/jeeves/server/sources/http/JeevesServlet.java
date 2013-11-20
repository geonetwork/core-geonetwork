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

import jeeves.config.springutil.DelegatingFilterProxy;
import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.constants.Jeeves;
import jeeves.server.JeevesEngine;
import jeeves.server.UserSession;
import jeeves.server.overrides.ConfigurationOverrides;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.ServiceRequestFactory;
import org.fao.geonet.Util;
import org.fao.geonet.exceptions.FileUploadTooBigEx;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class JeevesServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
	public static final String USER_SESSION_ATTRIBUTE_KEY = Jeeves.Elem.SESSION;
    public static final String NODES_INIT_PARAM = "nodes";
    public static final String NODE_APPLICATION_CONTEXT_KEY = "jeevesNodeApplicationContext_";
    private boolean initialized = false;

    //---------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//---------------------------------------------------------------------------

    public void init() throws ServletException {
        final Pattern nodeNamePattern = Pattern.compile("[a-zA-Z0-9_\\-]+");
        final ServletContext servletContext = getServletContext();
        final ServletPathFinder pathFinder = new ServletPathFinder(servletContext);

        JeevesApplicationContext defaultContext = null;
        String[] nodes = servletContext.getInitParameter(NODES_INIT_PARAM).split("\\s+");

        if (nodes.length == 0 || (nodes.length == 1 && nodes[0].trim().isEmpty())) {
            throw new IllegalArgumentException("Need at least one node defined");
        }

        for (String node : nodes) {
            if (node.trim().isEmpty()) {
                continue;
            }
            if (!nodeNamePattern.matcher(node.trim()).matches()) {
                throw new IllegalArgumentException(node.trim() + " has an illegal name.  Node names must be of the form: [a-zA-Z_\\-]+ ");

            }
            ConfigurationOverrides overrides = ConfigurationOverrides.DEFAULT;
            String commonConfigFile = "/WEB-INF/config-spring-geonetwork.xml";
            String configFile = "/WEB-INF/config-spring-geonetwork-" + node + ".xml";
            JeevesApplicationContext jeevesAppContext = new JeevesApplicationContext(overrides, node, commonConfigFile, configFile);
            jeevesAppContext.setServletConfig(getServletConfig());
            jeevesAppContext.setServletContext(getServletContext());
            jeevesAppContext.refresh();

            // initialize all JPA Repositories.  This should be done outside of the init
            // because spring-data-jpa first looks up named queries (based on method names) and
            // if the query is not found an exception is thrown.  This exception will set rollback
            // on the transaction if a transaction is active.
            //
            // We want to initialize all repositories here so they are not lazily initialized
            // at random places through out the code where it may be in a transaction.
            jeevesAppContext.getBeansOfType(JpaRepository.class, false, true);

            servletContext.setAttribute(NODE_APPLICATION_CONTEXT_KEY + node.trim(), jeevesAppContext);

            // check if the context is the default context
            try {
                Boolean isDefault = jeevesAppContext.getBean(JeevesApplicationContext.IS_DEFAULT_CONTEXT_BEAN_ID, Boolean.class);
                if (isDefault != null && isDefault) {
                    if (defaultContext != null) {
                        throw new IllegalArgumentException("Two nodes where defined as the default.  This is not acceptable.");
                    }
                    defaultContext = jeevesAppContext;

                    servletContext.setAttribute(NODE_APPLICATION_CONTEXT_KEY, jeevesAppContext);
                }
            } catch (NoSuchBeanDefinitionException e) {
                // no default bean defined so not default
            }
        }

        // make sure there is a default context
        for (int i = 0; i < nodes.length && defaultContext == null; i++) {
            defaultContext = (JeevesApplicationContext) servletContext.getAttribute(NODE_APPLICATION_CONTEXT_KEY + nodes[i].trim());

            if (defaultContext != null) {
                try {
                    defaultContext.getBeanFactory().registerSingleton(JeevesApplicationContext.IS_DEFAULT_CONTEXT_BEAN_ID, true);
                    servletContext.setAttribute(NODE_APPLICATION_CONTEXT_KEY, defaultContext);
                } catch (Exception e) {
                    defaultContext = null;
                }
            }
        }

        // ensure that all application contexts have the is default context bean defined.
        for (String node : nodes) {
            if (!node.trim().isEmpty()) {

                JeevesApplicationContext jeevesAppContext = (JeevesApplicationContext) servletContext.getAttribute(
                        NODE_APPLICATION_CONTEXT_KEY + node.trim());
                try {
                    Boolean isDefault = jeevesAppContext.getBean(JeevesApplicationContext.IS_DEFAULT_CONTEXT_BEAN_ID, Boolean.class);
                    if (isDefault == null) {
                        jeevesAppContext.getBeanFactory().registerSingleton(JeevesApplicationContext.IS_DEFAULT_CONTEXT_BEAN_ID,
                                defaultContext == jeevesAppContext);
                    }
                } catch (NoSuchBeanDefinitionException e) {
                    jeevesAppContext.getBeanFactory().registerSingleton(JeevesApplicationContext.IS_DEFAULT_CONTEXT_BEAN_ID, defaultContext == jeevesAppContext);
                }
            }
        }

        // initialize each JeevesEngine now
        for (String node : nodes) {
            if (!node.trim().isEmpty()) {

                JeevesApplicationContext jeevesAppContext = (JeevesApplicationContext) servletContext.getAttribute(
                        NODE_APPLICATION_CONTEXT_KEY + node.trim());
                jeevesAppContext.getBean(JeevesEngine.class).init(pathFinder.getAppPath(), pathFinder.getConfigPath(),
                        pathFinder.getBaseUrl(), this);

            }
        }

        initialized = true;
    }

	//---------------------------------------------------------------------------
	//---
	//--- Destroy
	//---
	//---------------------------------------------------------------------------

	public void destroy()
	{
        final ServletContext servletContext = getServletContext();
        final Enumeration names = servletContext.getAttributeNames();

        while (names.hasMoreElements()) {
            String attributeName = (String) names.nextElement();
            if (attributeName.startsWith(NODE_APPLICATION_CONTEXT_KEY)) {
                final JeevesApplicationContext applicationContext =
                        (JeevesApplicationContext) servletContext.getAttribute(attributeName);
                applicationContext.destroy();
            }
        }
        super.destroy();
	}

	//---------------------------------------------------------------------------
	//---
	//--- HTTP Request / Response
	//---
	//---------------------------------------------------------------------------

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		execute(req, res);
	}

	//---------------------------------------------------------------------------
	/** This is the core of the servlet. It receives http requests and invokes
	  * the proper service
	  */

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		execute(req, res);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void execute(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
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
		UserSession session     = (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE_KEY);

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

        ConfigurableApplicationContext applicationContext = DelegatingFilterProxy.getApplicationContextAttributeKey(getServletContext());
        JeevesEngine jeeves = applicationContext.getBean(JeevesEngine.class);

		try {
			srvReq = ServiceRequestFactory.create(req, res, jeeves.getUploadDir(), jeeves.getMaxUploadSize());
		} catch (FileUploadTooBigEx e) {
            StringBuilder sb = new StringBuilder();
            sb.append("File upload too big - exceeds ").append(jeeves.getMaxUploadSize()).append(" Mb\n")
                .append("Error : ").append(e.getClass().getName()).append("\n");

            res.sendError(SC_BAD_REQUEST, sb.toString());

            // now stick the stack trace on the end and log the whole lot
			sb.append("Stack :\n").append(Util.getStackTrace(e));

			Log.error(Log.REQUEST,sb.toString());
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

		//--- execute request

		jeeves.dispatch(srvReq, session);
	}

	public boolean isInitialized() { return initialized; }
}

//=============================================================================


