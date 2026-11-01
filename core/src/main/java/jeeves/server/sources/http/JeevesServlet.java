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

package jeeves.server.sources.http;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.constants.Jeeves;
import jeeves.server.JeevesEngine;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.User;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

//=============================================================================

/**
 * Initializes the system when the web application starts. It does not handle requests.
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


    public boolean isInitialized() {
        return initialized;
    }
}

//=============================================================================


