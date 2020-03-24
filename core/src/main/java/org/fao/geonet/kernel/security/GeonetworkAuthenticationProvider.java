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
package org.fao.geonet.kernel.security;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.PasswordUtil;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GeonetworkAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider
    implements UserDetailsService {

    private boolean checkUserNameOrEmail = false;

    public boolean isCheckUserNameOrEmail() {
        return checkUserNameOrEmail;
    }

    public void setCheckUserNameOrEmail(boolean checkUserNameOrEmail) {
        this.checkUserNameOrEmail = checkUserNameOrEmail;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        PasswordEncoder encoder = ApplicationContextHolder.get().getBean(PasswordEncoder.class);


        User gnDetails = (User) userDetails;

        if (authentication.getCredentials() == null) {
            Log.warning(Log.JEEVES, "Authentication failed: no credentials provided");
            throw new BadCredentialsException("Authentication failed: no credentials provided");
        }

        if (authentication.getCredentials().toString().isEmpty() ||
            !encoder.matches(authentication.getCredentials().toString(), gnDetails.getPassword())) {
            Log.warning(Log.JEEVES, "Authentication failed: wrong password provided");
            throw new BadCredentialsException("Authentication failed: wrong password provided");
        }
    }

    @Override
    protected UserDetails retrieveUser(String username,
                                       UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        try {
            final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
            PasswordEncoder encoder = applicationContext.getBean(PasswordEncoder.class);
            UserRepository userRepository = applicationContext.getBean(UserRepository.class);

            // Only check user with local db user (ie. authtype is '')
            User user = userRepository.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(username);
            if (user == null && checkUserNameOrEmail) {
                user = userRepository.findOneByEmailAndSecurityAuthTypeIsNullOrEmpty(username);
            }
            if (user != null) {
                if (authentication != null && authentication.getCredentials() != null) {
                    if (PasswordUtil.hasOldHash(user)) {
                        String oldPassword = user.getPassword();
                        String newPassword = authentication.getCredentials().toString();
                        user = PasswordUtil.updatePasswordWithNew(true, oldPassword, newPassword, user, encoder, userRepository);
                    }
                }

                return user;
            }
        } catch (Exception e) {
            Log.error(Log.JEEVES, "Unexpected error while loading user", e);
            throw new AuthenticationServiceException("Unexpected error while loading user", e);
        }
        throw new UsernameNotFoundException(username + " is not a valid username");
    }

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
        return retrieveUser(username, null);
    }

}
