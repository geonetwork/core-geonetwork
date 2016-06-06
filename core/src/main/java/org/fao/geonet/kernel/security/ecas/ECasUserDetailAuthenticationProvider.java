//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.ecas;

import org.apache.commons.logging.LogFactory;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.PasswordUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ECasUserDetailAuthenticationProvider extends
    AbstractUserDetailsAuthenticationProvider implements
    ApplicationContextAware, UserDetailsService {

    private static final org.apache.commons.logging.Log logger = LogFactory
        .getLog(ECasUserDetailAuthenticationProvider.class);
    private static final String CAS_STATEFUL = "_cas_stateful_";
    private static final String CAS_FLAG = "ECAS";
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepo;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        User gnDetails = userRepo.findOneByUsername(userDetails.getUsername());
        if (authentication.getCredentials() == null) {
            logger.error("Authentication failed: no credentials provided");
            throw new BadCredentialsException(
                "Authentication failed: no credentials provided");
        }
        if (!encoder.matches(authentication.getCredentials().toString(),
            gnDetails.getPassword())) {
            logger.warn("Authentication failed: wrong password provided");
            throw new BadCredentialsException(
                "Authentication failed: wrong password provided");
        }
    }

    @Override
    protected UserDetails retrieveUser(String username,
                                       UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        if (CAS_STATEFUL.equalsIgnoreCase(username)) {
            // cas failed
            logger.error("_cas_stateful_ detected in retrieveUser");
            return null;
        }
        try {
            User user = userRepo.findOneByUsername(username);
            if (user != null && !user.getSecurity().getAuthType().equalsIgnoreCase(CAS_FLAG)) {
                throw new AuthenticationServiceException(
                    "Trying to authenticate through ECAS a user that is not ECAS");
            }

            if (user == null) // if user does not exists, add one as guest
            {
                user = new User();
                user.setUsername(username);
                user.setProfile(Profile.Guest);
                user.setName(username);
                user.getSecurity().setAuthType(CAS_FLAG);
                userRepo.saveAndFlush(user);
            }

            return user;

        } catch (Throwable e) {
            throw new AuthenticationServiceException(
                "Unexpected error while loading user", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.encoder = (PasswordEncoder) applicationContext
            .getBean(PasswordUtil.ENCODER_ID);
    }

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
        return retrieveUser(username, null);
    }

}
