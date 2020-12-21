/*
 * Copyright (C) 2001-2017 Food and Agriculture Organization of the
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

/* Maven dependencies 4.8.3 seems to contains bugs related to the admin url.
 * It was preventing the Admin URL client backchannel logout from working.
 * Issues is explained in the following https://issues.redhat.com/browse/KEYCLOAK-10266 and seems to be fixed in version 8 or greater
 * If upgrading maven dependency to 8 or greater then it should be possible to update spring security configuration
 * to use org.keycloak.adapters.springsecurity.management.HttpSessionManager and remove this version.
 * At the time of writing this code, the goal was to target usage of RedHat SSO version 7.3 which was based on keycloak 4.8.20
 * https://access.redhat.com/articles/2342881.  It is unclear if newer client drivers are supported with this version.
 */

package org.fao.geonet.kernel.security.keycloak;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.keycloak.adapters.spi.UserSessionManagement;
import org.keycloak.adapters.springsecurity.management.LocalSessionManagementStrategy;
import org.keycloak.adapters.springsecurity.management.SessionManagementStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

/**
 * User session manager for handling logout of Spring Secured sessions.
 *
 * @author <a href="mailto:srossillo@smartling.com">Scott Rossillo</a>
 * @version $Revision: 1 $
 */
public class KeycloakHttpSessionManager implements ApplicationListener<ApplicationEvent>, UserSessionManagement {

    private static final Logger log = LoggerFactory.getLogger(KeycloakHttpSessionManager.class);
    private SessionManagementStrategy sessions = new LocalSessionManagementStrategy();


    @Override
    public void logoutAll() {
        log.info("Received request to log out all users.");
        for (HttpSession session : sessions.getAll()) {
            session.invalidate();
        }
        sessions.clear();
    }

    @Override
    public void logoutHttpSessions(List<String> ids) {
        log.info("Received request to log out {} session(s): {}", ids.size(), ids);
        for (String id : ids) {
            HttpSession session = sessions.remove(id);
            if (session != null) {
                session.invalidate();
            }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof HttpSessionCreatedEvent) {
            HttpSessionCreatedEvent e = (HttpSessionCreatedEvent) event;
            HttpSession session = e.getSession();
            log.debug("Session created: {}", session.getId());
            sessions.store(session);
        } else if (event instanceof HttpSessionDestroyedEvent) {
            HttpSessionDestroyedEvent e = (HttpSessionDestroyedEvent) event;
            HttpSession session = e.getSession();
            sessions.remove(session.getId());
            log.debug("Session destroyed: {}", session.getId());
        }
    }
}
