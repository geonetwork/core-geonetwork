/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Changes were taken from org.keycloak/keycloak-adapter-core/11.0.1 as there were bugs in current version
// that we prevending the Admin URL client backchannel logout from working.
// If upgrading maven dependency then it may be possible that the most recent KeycloakHttpSessionManager can be used
// and this file may be removed.

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
