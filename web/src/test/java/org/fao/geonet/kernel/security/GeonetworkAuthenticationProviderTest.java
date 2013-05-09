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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author heikki doeleman
 */
public class GeonetworkAuthenticationProviderTest extends TestCase {

    private GeonetworkAuthenticationProvider geonetworkAuthenticationProvider;

    private UsernamePasswordAuthenticationToken authentication;
    private Dbms dbms;
    private ResourceManager resourceManager;
    private ApplicationContext applicationContext;
    private PasswordEncoder encoder;

    @Before
    public void setUp() throws Exception{
        dbms = mock(Dbms.class);
        resourceManager = mock(ResourceManager.class);
        when(resourceManager.openDirect(Geonet.Res.MAIN_DB)).thenReturn(dbms);

        applicationContext = mock(ApplicationContext.class);

        when(applicationContext.getBean(ResourceManager.class)).thenReturn(resourceManager);
        geonetworkAuthenticationProvider = new GeonetworkAuthenticationProvider();
        geonetworkAuthenticationProvider.setApplicationContext(applicationContext);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() throws Exception{
        Element response = new Element("response");
        when(dbms.select(anyString(), anyString())).thenReturn(response);
        try {
            geonetworkAuthenticationProvider.retrieveUser("xxx", authentication);
            TestCase.fail("Expected UsernameNotFoundException");
        }
        catch (UsernameNotFoundException x) {}
    }

    @Test
    public void testUserFound() throws Exception{
        userFoundSetup();

        geonetworkAuthenticationProvider.retrieveUser("xxx", authentication);
        TestCase.assertNotNull(geonetworkAuthenticationProvider.retrieveUser("xxx", authentication));
    }

    @Test
    public void testFindUserWithAuthenticationToken() throws Exception{
        userFoundSetup();
        authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.getCredentials()).thenReturn("old password");

        TestCase.assertNotNull(geonetworkAuthenticationProvider.retrieveUser("xxx", authentication));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateWithoutToken() throws Exception {
        userFoundSetup();
        try {
            geonetworkAuthenticationProvider.authenticate(authentication);
            fail("Expected IllegalArgumentException");
        }
        catch(IllegalArgumentException x){}
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithoutCredentials() throws Exception {
        userFoundSetup();
        authentication = mock(UsernamePasswordAuthenticationToken.class);
        try {
            geonetworkAuthenticationProvider.authenticate(authentication);
            fail("Expected BadCredentialsException");
        }
        catch(BadCredentialsException x) {}
    }

    @Test
    public void testAuthenticateWithTokenWithCredentials() throws Exception {
        GeonetworkAuthenticationProvider spy = authenticationSetup(true);
        spy.authenticate(authentication);
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateWithTokenWithWrongCredentials() throws Exception {
        GeonetworkAuthenticationProvider spy = authenticationSetup(false);
        try {
            spy.authenticate(authentication);
            fail("Expected BadCredentialsException");
        }
        catch(BadCredentialsException x) {}
    }

    /**
     * Sets up things for mock authentication tests.
     *
     * @param OK whether the authentication succeeds
     * @return Spy on GeonetworkAuthenticationProvider
     * @throws Exception
     */
    private GeonetworkAuthenticationProvider authenticationSetup(boolean OK) throws Exception{
        userFoundSetup();
        mockEncoderSetup(OK);
        mockAuthenticationSetup();
        return spyOnAuthenticateSetup();
    }

    /**
     * Sets up a mock authentication.
     */
    private void mockAuthenticationSetup() {
        authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.getCredentials()).thenReturn("password");
    }

    /**
     * Makes applicationContext use a mock encoder.
     *
     * @param match whether a password matches
     */
    private void mockEncoderSetup(boolean match) {
        encoder = mock(PasswordEncoder.class);
        when(encoder.matches(anyString(), anyString())).thenReturn(match);
        when(applicationContext.getBean(PasswordUtil.ENCODER_ID)).thenReturn(encoder);
    }
    /**
     * Creates mock GeonetworkUser.
     *
     * @return
     */
    private GeonetworkUser mockUserSetup() {
        GeonetworkUser user = mock(GeonetworkUser.class);
        when(user.isAccountNonLocked()).thenReturn(true);
        when(user.isEnabled()).thenReturn(true);
        when(user.isAccountNonExpired()).thenReturn(true);
        when(user.getPassword()).thenReturn("password");
        when(user.isCredentialsNonExpired()).thenReturn(true);
        return user;
    }

    /**
     * Makes GAP use a mock user on authenticate.
     *
     * @return
     */
    private GeonetworkAuthenticationProvider spyOnAuthenticateSetup() {
        GeonetworkAuthenticationProvider spy = spy(geonetworkAuthenticationProvider);
        GeonetworkUser user = mockUserSetup();

        doReturn(user).when(spy).retrieveUser(anyString(), any(UsernamePasswordAuthenticationToken.class));
        spy.setApplicationContext(applicationContext);

        return spy;
    }

    /**
     * Mocks a user found.
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

    /**
     *
     * @return
     */
    private Element userXML() {
        Element record = new Element("record");
        Element id = new Element("id");
        id.setText("666");
        record.addContent(id);
        return record;
    }

}