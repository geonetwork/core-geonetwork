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
    public void testUserNotFound() throws Exception{
        final User user = userFoundSetup(PasswordEncoding.CURRENT);

        _geonetworkAuthenticationProvider.retrieveUser(user.getUsername() + "not", authentication);
    }

    enum PasswordEncoding {
        UNSALTED, OLD, CURRENT
    }
    /**
     * Makes dbms find a user.
     *
     * @throws Exception
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
    public void testUserFound() throws Exception{
        final User user = userFoundSetup(PasswordEncoding.CURRENT);

        _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User should be found",
                _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication));
    }

    @Test
    public void testFindUserWithAuthenticationTokenUnsalted() throws Exception{
        final User user = userFoundSetup(PasswordEncoding.UNSALTED);

        mockAuthenticationSetup(user);

        final UserDetails userDetails = _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }

    @Test
    public void testFindUserWithAuthenticationTokenOldPasswordHash() throws Exception{
        final User user = userFoundSetup(PasswordEncoding.OLD);

        mockAuthenticationSetup(user);

        final UserDetails userDetails = _geonetworkAuthenticationProvider.retrieveUser(user.getUsername(), authentication);
        TestCase.assertNotNull("User with authentication token should be found", userDetails);
    }
    @Test
    public void testFindUserWithAuthenticationToken() throws Exception{
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

}