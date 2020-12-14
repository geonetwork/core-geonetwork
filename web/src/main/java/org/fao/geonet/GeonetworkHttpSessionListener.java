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
package org.fao.geonet;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import org.fao.geonet.utils.Log;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to keep track of the number of active sessions.\
 */
public class GeonetworkHttpSessionListener implements HttpSessionListener {

    private final AtomicInteger activeSessions;
    private final Logger LOGGER = Log.createLogger(Log.JEEVES);

    public GeonetworkHttpSessionListener() {
        activeSessions = new AtomicInteger();
    }


    public int getTotalActiveSession() {
        return activeSessions.get();
    }

    public void sessionCreated(final HttpSessionEvent event) {
        activeSessions.incrementAndGet();
        if( LOGGER.isDebugEnabled()) {
            HttpSession session = event.getSession();

            ServletContext context = session.getServletContext();
            review(context);

            LOGGER.debug("sessions " + getTotalActiveSession() +": session created");
        }
    }

    public void sessionDestroyed(final HttpSessionEvent event) {
        activeSessions.decrementAndGet();
        if( LOGGER.isDebugEnabled()) {
            HttpSession session = event.getSession();

            ServletContext context = session.getServletContext();
            review(context);

            LOGGER.debug("sessions " + getTotalActiveSession() +": session destroyed");
        }
    }

    /**
     * Debug messages reviewing current servlet health and happiness.
     * <p>
     * This is primarily used to check on resource use and identify resources leaks over time.
     * </p>
     */
    protected void review( ServletContext context){
        Enumeration<String> e = context.getAttributeNames();
        while (e.hasMoreElements()) {
            String attributeName = e.nextElement();

            StringBuilder build = new StringBuilder();

            build.append("sessionContent: ");
            build.append(attributeName);

            Object attribute = context.getAttribute(attributeName);
            if( attribute == null ){
                build.append( " --> null");
            }
            else if( "jeevesNodeApplicationContext_".equals(attributeName)){
                build.append(" -- jeeves application context");
            }
            else if (Jeeves.Elem.SESSION.equals(attributeName)){
                build.append(" -- jeeves session");
            }
            else {
                build.append(" --> ");
                build.append(attribute);
            }
            LOGGER.debug(build.toString());
        }
    }
}
