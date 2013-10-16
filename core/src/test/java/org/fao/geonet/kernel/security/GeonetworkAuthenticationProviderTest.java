package org.fao.geonet.kernel.security;

import junit.framework.TestCase;

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSecurityNotification;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserRepositoryTest;
import org.fao.geonet.util.PasswordUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
public class GeonetworkAuthenticationProviderTest extends AbstractSpringDataTest {
    public static final String PASSWORD = "password";
    /**
     * The class under test.
     */
    @Autowired
    private GeonetworkAuthenticationProvider geonetworkAuthenticationProvider;

    //
    // Test doubles
    //

    private UsernamePasswordAuthenticationToken authentication;
    @Autowired
    private ApplicationContext _appContext;
    @Autowired
    private UserRepository _userRepo;
    private AtomicInteger _inc = new AtomicInteger();
    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() throws Exception{
        final User user = userFoundSetup(false);

        geonetworkAuthenticationProvider.retrieveUser(user.getUsername()+"not", authentication);
    }

    /**
     * Makes dbms find a user.
     *
     * @throws Exception
     */
    private User userFoundSetup(boolean oldPassword) throws Exception {
        final User entity = UserRepositoryTest.newUser(_inc);
        if (oldPassword) {
            entity.getSecurity().getSecurityNotifications().add(UserSecurityNotification.HASH_UPDATE_REQUIRED);
        }
        entity.getSecurity().setPassword(PASSWORD);
        return _userRepo.save(entity);
    }

    @Test
    public void testUserFound() throws Exception{
        final User user = userFoundSetup(false);

        geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User should be found",
                geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication));
    }

    @Test
    public void testFindUserWithAuthenticationTokenUnsalted() throws Exception{
        final User user = userFoundSetup(true);
        final Method method = PasswordUtil.class.getDeclaredMethod("unsaltedScramble", String.class);
        method.setAccessible(true);
        final Object unsaltedScramble = method.invoke(null, user.getPassword());
        char[] hashPassword = unsaltedScramble.toString().toCharArray();
        user.getSecurity().setPassword(hashPassword);
        _userRepo.save(user);

        mockAuthenticationSetup(user);

        final UserDetails userDetails = geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }

    @Test
    public void testFindUserWithAuthenticationTokenOldPasswordHash() throws Exception{
        final User user = userFoundSetup(true);
        final Method method = PasswordUtil.class.getDeclaredMethod("oldScramble", String.class);
        method.setAccessible(true);
        final Object unsaltedScramble = method.invoke(null, user.getPassword());
        char[] hashPassword = unsaltedScramble.toString().toCharArray();
        user.getSecurity().setPassword(hashPassword);
        _userRepo.save(user);

        mockAuthenticationSetup(user);

        final UserDetails userDetails = geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }
    @Test
    public void testFindUserWithAuthenticationToken() throws Exception{
        final User user = userFoundSetup(false);
        mockAuthenticationSetup(user);

        final UserDetails userDetails = geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateWithoutToken() throws Exception {
        userFoundSetup(false);

        geonetworkAuthenticationProvider.authenticate(authentication);
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithoutCredentials() throws Exception {
        userFoundSetup(false);
        authentication = mock(UsernamePasswordAuthenticationToken.class);

        geonetworkAuthenticationProvider.authenticate(authentication);
    }

    /**
     * Sets up a mock authentication that can return a password.
     * @param user
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
        userFoundSetup(false);
        mockAuthenticationSetup(null);
        geonetworkAuthenticationProvider.authenticate(authentication);
    }

    @Test
    public void testAuthenticateWithTokenWithCorrectCredentials() throws Exception {
        final User user = userFoundSetup(false);
        mockAuthenticationSetup(user);
        TestCase.assertNotNull("Authentication with correct credentials should succeed",
                geonetworkAuthenticationProvider.authenticate(authentication));
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithWrongCredentials() throws Exception {
        mockAuthenticationSetup(null);
        geonetworkAuthenticationProvider.authenticate(authentication);
    }

}