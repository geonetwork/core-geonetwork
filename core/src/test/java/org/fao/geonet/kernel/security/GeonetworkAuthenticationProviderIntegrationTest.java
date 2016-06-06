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

package org.fao.geonet.kernel.security;

import junit.framework.TestCase;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSecurityNotification;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserRepositoryTest;
import org.fao.geonet.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test GeonetworkAuthenticationProvider.
 *
 * @author heikki doeleman
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:encoder-bean.xml")
public class GeonetworkAuthenticationProviderIntegrationTest extends AbstractCoreIntegrationTest {

    public static final String PASSWORD = "password";
    /**
     * The class under test.
     */
    @Autowired
    private GeonetworkAuthenticationProvider _geonetworkAuthenticationProvider;
    @Autowired
    private PasswordEncoder _encoder;

    //
    // Test doubles
    //

    private UsernamePasswordAuthenticationToken authentication;
    @Autowired
    private ApplicationContext _appContext;
    @Autowired
    private UserRepository _userRepo;
    private AtomicInteger _inc = new AtomicInteger();

    @Before
    public void setUp() throws Exception {
        System.out.println(_appContext.getId());

    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() throws Exception {
        final User user = userFoundSetup(PasswordEncoding.CURRENT);

        _geonetworkAuthenticationProvider.retrieveUser(user.getUsername() + "not", authentication);
    }

    /**
     * Makes dbms find a user.
     */
    private User userFoundSetup(PasswordEncoding passwordEncoding) throws Exception {
        final User entity = UserRepositoryTest.newUser(_inc);
        switch (passwordEncoding) {
            case CURRENT:
                entity.getSecurity().setPassword(_encoder.encode(PASSWORD));
                break;
            case OLD:
                entity.getSecurity().getSecurityNotifications().add(UserSecurityNotification.UPDATE_HASH_REQUIRED);
                final Method method = PasswordUtil.class.getDeclaredMethod("oldScramble", String.class);
                method.setAccessible(true);
                final Object unsaltedScramble = method.invoke(null, PASSWORD);
                entity.getSecurity().setPassword(unsaltedScramble.toString());
                break;
            case UNSALTED:
                entity.getSecurity().getSecurityNotifications().add(UserSecurityNotification.UPDATE_HASH_REQUIRED);
                final Method method2 = PasswordUtil.class.getDeclaredMethod("unsaltedScramble", String.class);
                method2.setAccessible(true);
                final Object oldScramble = method2.invoke(null, PASSWORD);
                entity.getSecurity().setPassword(oldScramble.toString());
                break;
        }
        return _userRepo.save(entity);
    }

    @Test
    public void testUserFound() throws Exception {
        final User user = userFoundSetup(PasswordEncoding.CURRENT);

        _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User should be found",
            _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication));
    }

    @Test
    public void testFindUserWithAuthenticationTokenUnsalted() throws Exception {
        final User user = userFoundSetup(PasswordEncoding.UNSALTED);

        mockAuthenticationSetup(user);

        final UserDetails userDetails = _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }

    @Test
    public void testFindUserWithAuthenticationTokenOldPasswordHash() throws Exception {
        final User user = userFoundSetup(PasswordEncoding.OLD);

        mockAuthenticationSetup(user);

        final UserDetails userDetails = _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }

    @Test
    public void testFindUserWithAuthenticationToken() throws Exception {
        final User user = userFoundSetup(PasswordEncoding.CURRENT);
        mockAuthenticationSetup(user);

        final UserDetails userDetails = _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateWithoutToken() throws Exception {
        userFoundSetup(PasswordEncoding.CURRENT);

        _geonetworkAuthenticationProvider.authenticate(authentication);
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithoutCredentials() throws Exception {
        userFoundSetup(PasswordEncoding.CURRENT);
        authentication = mock(UsernamePasswordAuthenticationToken.class);

        _geonetworkAuthenticationProvider.authenticate(authentication);
    }

    /**
     * Sets up a mock authentication that can return a password.
     */
    private void mockAuthenticationSetup(User user) {
        authentication = mock(UsernamePasswordAuthenticationToken.class);
        if (user != null) {
            when(authentication.getName()).thenReturn(user.getUsername());
            when(authentication.getPrincipal()).thenReturn(user);
        }
        when(authentication.getCredentials()).thenReturn(PASSWORD);
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithUserNotFound() throws Exception {
        userFoundSetup(PasswordEncoding.CURRENT);
        mockAuthenticationSetup(null);
        _geonetworkAuthenticationProvider.authenticate(authentication);
    }

    @Test
    public void testAuthenticateWithTokenWithCorrectCredentials() throws Exception {
        final User user = userFoundSetup(PasswordEncoding.CURRENT);
        mockAuthenticationSetup(user);
        TestCase.assertNotNull("Authentication with correct credentials should succeed",
            _geonetworkAuthenticationProvider.authenticate(authentication));
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithWrongCredentials() throws Exception {
        mockAuthenticationSetup(null);
        _geonetworkAuthenticationProvider.authenticate(authentication);
    }

    enum PasswordEncoding {
        UNSALTED, OLD, CURRENT
    }

}
