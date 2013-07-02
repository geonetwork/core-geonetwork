package org.fao.geonet.kernel.security;

import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.PasswordUtil;
import junit.framework.TestCase;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test GeonetworkAuthenticationProvider.
 *
 * @author heikki doeleman
 */
public class GeonetworkAuthenticationProviderTest {
    /**
     * The class under test.
     */
    private GeonetworkAuthenticationProvider geonetworkAuthenticationProvider;

    //
    // Test doubles
    //

    private UsernamePasswordAuthenticationToken authentication;
    private Dbms dbms;
    private ApplicationContext applicationContext;

    /**
     * Creates a GeonetworkAuthenticationProvider using a mock ApplicationContext that uses a mock ResourceManager to
     * provide a mock Dbms. This method is invoked before each @Test method.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        dbms = mock(Dbms.class);
        ResourceManager resourceManager = mock(ResourceManager.class);
        when(resourceManager.openDirect(Geonet.Res.MAIN_DB)).thenReturn(dbms);

        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ResourceManager.class)).thenReturn(resourceManager);

        geonetworkAuthenticationProvider = new GeonetworkAuthenticationProvider();
        geonetworkAuthenticationProvider.setApplicationContext(applicationContext);
    }

    /**
     * Makes dbms not find a user.
     *
     * @throws Exception
     */
    private void userNotFoundSetup() throws Exception{
        Element response = new Element("response");
        when(dbms.select(anyString(), anyString())).thenReturn(response);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() throws Exception{
        userNotFoundSetup();

        geonetworkAuthenticationProvider.retrieveUser("username", authentication);
    }

    /**
     * Minimalist representation of a user retrieved from the DB.
     *
     * @return user in XML
     */
    private Element userXML() {
        Element record = new Element("record");
        Element id = new Element("id");
        id.setText(Integer.toString(new Random().nextInt()));
        record.addContent(id);
        return record;
    }

    /**
     * Makes dbms find a user.
     *
     * @throws Exception
     */
    private void userFoundSetup() throws Exception{
        Element response = new Element("response");
        Element record = userXML();
        Element security = new Element(PasswordUtil.SECURITY_FIELD);
        record.addContent(security);
        response.addContent(record);
        when(dbms.select(anyString(), anyString())).thenReturn(response);
    }

    @Test
    public void testUserFound() throws Exception{
        userFoundSetup();

        geonetworkAuthenticationProvider.retrieveUser("username", authentication);
        TestCase.assertNotNull("User should be found",
                geonetworkAuthenticationProvider.retrieveUser("username", authentication));
    }

    @Test
    public void testFindUserWithAuthenticationToken() throws Exception{
        userFoundSetup();
        authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.getCredentials()).thenReturn("password");

        TestCase.assertNotNull("User with authentication token should be found",
                geonetworkAuthenticationProvider.retrieveUser("username", authentication));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateWithoutToken() throws Exception {
        userFoundSetup();

        geonetworkAuthenticationProvider.authenticate(authentication);
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithoutCredentials() throws Exception {
        userFoundSetup();
        authentication = mock(UsernamePasswordAuthenticationToken.class);

        geonetworkAuthenticationProvider.authenticate(authentication);
    }

    /**
     * Makes applicationContext use a mock encoder. The mock encoder either approves or rejects any password.
     *
     * @param match whether a password matches
     */
    private void mockEncoderSetup(boolean match) {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches(anyString(), anyString())).thenReturn(match);
        when(applicationContext.getBean(PasswordUtil.ENCODER_ID)).thenReturn(encoder);
    }

    /**
     * Sets up a mock authentication that can return a password.
     */
    private void mockAuthenticationSetup() {
        authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.getCredentials()).thenReturn("password");
    }

    /**
     * Creates mock GeonetworkUser. The mock user can return a password and has all Spring's UserDetails flags set to be
     * a valid user (not locked, not disabled, not expired, credentials not expired).
     *
     * @return mock user
     */
    private GeonetworkUser mockUserSetup() {
        GeonetworkUser user = mock(GeonetworkUser.class);
        when(user.isAccountNonLocked()).thenReturn(true);
        when(user.isEnabled()).thenReturn(true);
        when(user.isAccountNonExpired()).thenReturn(true);
        when(user.isCredentialsNonExpired()).thenReturn(true);
        when(user.getPassword()).thenReturn("password");
        return user;
    }

    /**
     * Makes GAP use a mock user on authenticate. Uses a spy because we need to stub only one method (retrieveUser) of
     * the class under test while using the real thing for its other methods.
     *
     * @param userFound whether the user is found
     * @return GeonetworkAuthenticationProvider spy
     */
    private GeonetworkAuthenticationProvider spyOnAuthenticateSetup(boolean userFound) {
        GeonetworkAuthenticationProvider spy = spy(geonetworkAuthenticationProvider);
        GeonetworkUser user = userFound ? mockUserSetup() : null ;

        doReturn(user).when(spy).retrieveUser(anyString(), any(UsernamePasswordAuthenticationToken.class));
        spy.setApplicationContext(applicationContext);

        return spy;
    }

    /**
     * Sets up a GeonetworkAuthenticationProvider for authentication tests.
     *
     * @param userFound whether the user is found
     * @param OK whether the authentication succeeds
     * @return Spy on GeonetworkAuthenticationProvider
     * @throws Exception
     */
    private GeonetworkAuthenticationProvider authenticationSetup(boolean userFound, boolean OK) throws Exception{
        mockEncoderSetup(OK);
        mockAuthenticationSetup();
        return spyOnAuthenticateSetup(userFound);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateWithUserNotFound() throws Exception {
        geonetworkAuthenticationProvider = authenticationSetup(false, true);
        geonetworkAuthenticationProvider.authenticate(authentication);
    }

    @Test
    public void testAuthenticateWithTokenWithCorrectCredentials() throws Exception {
        geonetworkAuthenticationProvider = authenticationSetup(true, true);
        TestCase.assertNotNull("Authentication with correct credentials should succeed",
                geonetworkAuthenticationProvider.authenticate(authentication));
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithWrongCredentials() throws Exception {
        geonetworkAuthenticationProvider = authenticationSetup(true, false);
        geonetworkAuthenticationProvider.authenticate(authentication);
    }

}