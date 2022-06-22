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

package org.fao.geonet.kernel.security.listener;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.Log;
import org.keycloak.KeycloakPrincipal;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;

/**
 * This class logs the last succesful login events from the app.
 *
 * Can be de/activated adding config-security-core(-overrides).xml
 *
 * <bean class="org.fao.geonet.kernel.security.listener.UpdateTimestampListener"
 * id="updateTimestampListener"> <property name="activate" value="true"/> </bean>
 *
 * @author delawen
 * @author Jose García
 */
public class UpdateTimestampListener implements
    ApplicationListener<AbstractAuthenticationEvent> {

    @Override
    /**
     * Depending on which type of app event we will log one or other thing.
     */
    public void onApplicationEvent(AbstractAuthenticationEvent e) {
        UserRepository userRepo = ApplicationContextHolder.get().getBean(UserRepository.class);

        if (e instanceof InteractiveAuthenticationSuccessEvent
            || e instanceof AuthenticationSuccessEvent
            || e instanceof AuthenticationSwitchUserEvent) {

            try {
                Object principal = e.getAuthentication().getPrincipal();
                String username;
                if (principal instanceof UserDetails) {
                    username = ((UserDetails)principal).getUsername();
                } else {
                    if (principal instanceof KeycloakPrincipal && ((KeycloakPrincipal) e.getAuthentication().getPrincipal()).getKeycloakSecurityContext().getIdToken() != null) {
                        username = ((KeycloakPrincipal) e.getAuthentication().getPrincipal()).getKeycloakSecurityContext().getIdToken().getPreferredUsername();
                    }
                    else if (principal instanceof OidcUser) {
                        username =  ((OidcUser)principal).getPreferredUsername();
                    }
                    else if (principal instanceof OAuth2User) {
                        username =  ((OAuth2User)principal).getAttribute(StandardClaimNames.PREFERRED_USERNAME);
                    }
                    else {
                        username = principal.toString();
                    }
                }

                User user = userRepo.findOneByUsername(username);
                if (user != null) {
                    user.setLastLoginDate(new ISODate().toString());
                    userRepo.save(user);
                }

            } catch (Exception ex) {
                Log.error(Geonet.GEONETWORK, "UpdateTimestampListener error: " + ex.getMessage(), ex);
            }

        }

    }
}
