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

package jeeves.config.springutil;

import jeeves.server.UserSession;
import jeeves.server.sources.http.JeevesServlet;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Clears the UserSession
 *
 * @author jeichar
 */
public class LogoutUserSessionHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response, Authentication authentication) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            Object tmp = httpSession.getAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY);
            if (tmp instanceof UserSession) {
                UserSession userSession = (UserSession) tmp;
                userSession.clear();
            }
            httpSession.invalidate();
        }

    }

}
