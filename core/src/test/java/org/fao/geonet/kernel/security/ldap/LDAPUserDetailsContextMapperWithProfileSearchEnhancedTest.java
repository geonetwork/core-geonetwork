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
package org.fao.geonet.kernel.security.ldap;

import net.sf.ehcache.CacheManager;
import org.apache.directory.api.util.FileUtils;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.factory.DSAnnotationProcessor;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.factory.ServerAnnotationProcessor;
import org.apache.directory.server.ldap.LdapServer;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.ldap.core.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.LdapUsernameToDnMapper;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.*;


// ====================================================================================
//  SEE THE DATA.LDIF FILE FOR THE LDAP LAYOUT
//
//   (it has comments at the top showing the tree)
// ====================================================================================

//Normally, you would use an org.apache.directory.server.core.integ.FrameworkRunner to run LDAP tests
// however, that means you cannot use the SPRING runner, which makes life a bit more difficult.
// So, we added setupServer/shutdown() server methods that will start/stop the LDAP server in the same manner.
// This allows SPRING tests (with the spring runner) to run with an LDAP server!
@CreateLdapServer(
    transports = {@CreateTransport(port = 3333, protocol = "LDAP", address = "localhost")},
    allowAnonymousAccess = false
)
@CreateDS(
    name = "myDS",
    partitions = {@CreatePartition(name = "test", suffix = LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest.ldapSearchBase)}
)
@ApplyLdifFiles({"org/fao/geonet/kernel/security/ldap/data.ldif"})
//run with spring
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:org/fao/geonet/kernel/security/ldap/LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest extends AbstractLdapTestUnit {

    @Autowired
    private ConfigurableApplicationContext _appContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    DefaultSpringSecurityContextSource contextSource;

    @Autowired
    LdapUsernameToDnMapper usernameMapper;

    @Autowired
    LdapUserDetailsManager ldapUserDetailsService;

    @Autowired
    LDAPUserDetailsContextMapperWithProfileSearchEnhanced ldapUserContextMapper;

    @Autowired
    LdapAuthenticationProvider ldapAuthProvider;

    @Autowired
    FilterBasedLdapUserSearch ldapUserSearch;

    @Autowired
    LDAPRoleConverterGroupNameParser ldapRoleConverterGroupNameParser;


    //=------------------------------------------------------------------

    @After
    public void after() throws Exception {
        shutdownLDAP();
    }

    @AfterClass
    public static void afterClass() {
        CacheManager.getInstance().shutdown(); //we need to make sure this is gone or we'll conflict with hibernate
    }



    //gets the spring context
    @Before
    public void before() throws Exception {
        setupLDAP();

        ApplicationContextHolder.set(this._appContext); // this is for code using antipattern - ApplicationContextHolder.get().getBean(...)

        userRepository.deleteAll();
        groupRepository.deleteAll();
        userGroupRepository.deleteAll();
    }

    //very simple test
    // this will find the DN by searching by username
    @Test
    public void test_searchingLdapUsernameToDnMapper() {
        DistinguishedName name = usernameMapper.buildDn("dblasby@example.com");
        assertEquals("cn=blasby\\, david,ou=GIS Department,ou=Corporate Users", name.toString());


        name = usernameMapper.buildDn("jgee@example.com");
        assertEquals("cn=gee\\, jody,ou=Project Admin,ou=Corporate Users", name.toString());
    }


    //trivial test - make sure that the we can access the LDAP server and do a query via the contextSource bean
    @Test
    public void test_contextSource() throws NamingException {
        DirContext ctx = contextSource.getReadOnlyContext();
        List<SearchResult> results = queryLDAP("(objectClass=*)", ctx, null, null);
        assertTrue(results.size() > 8);
    }

    //trivial test to make sure ldapUserSearch is working (finds users)
    @Test
    public void test_ldapUserSearch() {
        DirContextOperations result = ldapUserSearch.searchForUser("dblasby@example.com");
        assertNotNull(result);
        assertEquals("cn=blasby\\, david,ou=GIS Department,ou=Corporate Users", result.getDn().toString());
    }

    //test the ldapUserDetailsService bean by looking for a user
    //This bean also imports the LDAP user into geonetwork (i.e. inside UserRepo)
    @Test
    public void test_ldapUserDetailsService() throws NamingException {
        boolean orig_importPrivilegesFromLdap = ldapUserContextMapper.isImportPrivilegesFromLdap();
        try {
            ldapUserContextMapper.setImportPrivilegesFromLdap(false); // we don't want to do this for this test case - this is just about the basics
            UserDetails details = ldapUserDetailsService.loadUserByUsername("dblasby@example.com");
            assertNotNull(details);

            User user = userRepository.findOneByUsername("dblasby@example.com");
            assertNotNull(user);
            assertEquals("blasby", user.getSurname());
        } finally {
            ldapUserContextMapper.setImportPrivilegesFromLdap(orig_importPrivilegesFromLdap); //reset
        }
    }


    //this is the main test - will set group/profiles for the user (dblasby)
    @Test
    @Transactional
    public void test_ldapUserDetailsService_groups_and_profiles_dblasby() throws NamingException {

        //make sure its already in the repository, or nothing will happen
        Group group = new Group().setName("GENERAL");
        group = groupRepository.save(group);


        UserDetails details = ldapUserDetailsService.loadUserByUsername("dblasby@example.com");
        assertNotNull(details);
        assertEquals(3, details.getAuthorities().size());
        assertEquals(Profile.Guest.name(), details.getAuthorities().toArray(new GrantedAuthority[0])[0].getAuthority());
        assertEquals(Profile.Editor.name(), details.getAuthorities().toArray(new GrantedAuthority[0])[1].getAuthority());
        assertEquals(Profile.RegisteredUser.name(), details.getAuthorities().toArray(new GrantedAuthority[0])[2].getAuthority());


        LDAPUser user1 = (LDAPUser) details;
        assertEquals(1, user1.getPrivileges().size());
        assertTrue(user1.getPrivileges().containsKey("GENERAL"));
        assertEquals(1, user1.getPrivileges().get("GENERAL").toArray().length);
        assertEquals(Profile.Editor, user1.getPrivileges().get("GENERAL").toArray()[0]);


        User user = userRepository.findOneByUsername("dblasby@example.com");
        assertNotNull(user);
        assertEquals("blasby", user.getSurname());
        assertEquals(Profile.Editor, user.getProfile());

        List<UserGroup> ug = userGroupRepository.findAll(UserGroupSpecs.hasUserId(user.getId()));
        assertNotNull(ug);
        assertEquals(1, ug.size());
        assertEquals("GENERAL", ug.get(0).getGroup().getName());
    }

    //this is the main test - will set group/profiles for the user (admin)
    @Test
    @Transactional
    public void test_ldapUserDetailsService_groups_and_profiles_admin() throws NamingException {

        //make sure its already in the repository, or nothing will happen
        Group group = new Group().setName("GENERAL");
        group = groupRepository.save(group);


        UserDetails details = ldapUserDetailsService.loadUserByUsername("admin@example.com");
        assertNotNull(details);

        LDAPUser user1 = (LDAPUser) details;
        assertEquals(1, user1.getPrivileges().size());
        assertTrue(user1.getPrivileges().containsKey("GENERAL"));
        assertEquals(1, user1.getPrivileges().get("GENERAL").toArray().length);
        assertEquals(Profile.Administrator, user1.getPrivileges().get("GENERAL").toArray()[0]);


        User user = userRepository.findOneByUsername("admin@example.com");
        assertNotNull(user);
        assertEquals("admin", user.getSurname());
        assertEquals(Profile.Administrator, user.getProfile());

        List<UserGroup> ug = userGroupRepository.findAll(UserGroupSpecs.hasUserId(user.getId()));
        assertNotNull(ug);
        assertEquals(1, ug.size());
        assertEquals("GENERAL", ug.get(0).getGroup().getName());
    }

    //this is the main test - will set group/profiles for the user (jody)
    @Test
    @Transactional
    public void test_ldapUserDetailsService_groups_and_profiles_jody() throws NamingException {

        //make sure its already in the repository, or nothing will happen
        Group group = new Group().setName("GENERAL");
        group = groupRepository.save(group);


        UserDetails details = ldapUserDetailsService.loadUserByUsername("jgee@example.com");
        assertNotNull(details);

        LDAPUser user1 = (LDAPUser) details;
        assertEquals(0, user1.getPrivileges().size());

        User user = userRepository.findOneByUsername("jgee@example.com");
        assertNotNull(user);
        assertEquals("gee", user.getSurname());
        assertEquals(Profile.RegisteredUser, user.getProfile());

        List<UserGroup> ug = userGroupRepository.findAll(UserGroupSpecs.hasUserId(user.getId()));
        assertNotNull(ug);
        assertEquals(0, ug.size());
    }


    @Test
    @Transactional
    public void test_ldapAuthProvider() {
        UsernamePasswordAuthenticationToken userpass = new UsernamePasswordAuthenticationToken("dblasby@example.com", "blasby1", null);

        Authentication result = ldapAuthProvider.authenticate(userpass);
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
    }

    @Test(expected = BadCredentialsException.class)
    public void test_ldapAuthProvider_badPass() {
        UsernamePasswordAuthenticationToken userpass = new UsernamePasswordAuthenticationToken("dblasby@example.com", "BAD_PASSWORD", null);

        //will throw because the password is wrong
        Authentication result = ldapAuthProvider.authenticate(userpass);
    }


    //=========================================================================================

    public final static String ldapSearchBase = "dc=example,dc=com";


    DirectoryService LDAPservice = null;
    LdapServer ldapServer = null;

    public  void setupLDAP() throws Exception {
        CreateLdapServer classLdapServerBuilder = LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest.class.getAnnotation(CreateLdapServer.class);
        CreateDS dsBuilder = LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest.class.getAnnotation(CreateDS.class);
        LDAPservice = DSAnnotationProcessor.createDS(dsBuilder);
        ApplyLdifFiles applyLdifFiles = LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest.class.getAnnotation(ApplyLdifFiles.class);
        DSAnnotationProcessor.injectLdifFiles(applyLdifFiles.clazz(), LDAPservice, applyLdifFiles.value());

        CreateLdapServer createLdapServer = LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest.class.getAnnotation(CreateLdapServer.class);
        ldapServer = ServerAnnotationProcessor.instantiateLdapServer(createLdapServer, LDAPservice);
        ldapServer.start();
    }

    public   void shutdownLDAP() throws Exception {
        ldapServer.stop();
        LDAPservice.shutdown();
        FileUtils.deleteDirectory(LDAPservice.getInstanceLayout().getInstanceDirectory());
    }



    //very basic test - using LDAPTemplate
    // this is testing the LDAP test-case server ... and explores the dataset
    @Test
    public void test_LdapTemplate() {
        LdapTemplate template = new LdapTemplate(this.contextSource);
        String username = "dblasby@example.com";

        DistinguishedName dn = new DistinguishedName("cn=" + username);
        SearchExecutor se = new SearchExecutor() {
            public NamingEnumeration<SearchResult> executeSearch(DirContext ctx)
                throws NamingException {
                DistinguishedName fullDn = LdapUtils.getFullDn(dn, ctx);
                SearchControls ctrls = new SearchControls();
                //ctrls.setReturningAttributes(new String[]{"cn"});
                ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);


                return ctx.search("", "(mail=dblasby@example.com)",
                    new String[]{fullDn.toUrl(), username}, ctrls);

            }
        };

        final boolean[] pass = {false};
        NameClassPairCallbackHandler handler = new NameClassPairCallbackHandler() {
            @Override
            public void handleNameClassPair(NameClassPair nameClassPair) {
                pass[0] = true;
            }
        };

        template.search(se, handler);
        assertEquals(pass[0], true);
    }


    //this is ensuring that the LDAP server is up-and-running, and can do two things;
    // a) find user by name
    // b) find groups a user is a member of
    //These will be used in the main system, but best to check here
    @Test
    public void test_LDAPIsWorking() throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:3333/");

        // anonymous access
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=admin,ou=GIS Department,ou=Corporate Users,dc=example,dc=com");
        env.put(Context.SECURITY_CREDENTIALS, "admin1");

        // Create the initial context
        DirContext ctx = new InitialDirContext(env);
        SearchControls searchControls = new SearchControls();
        searchControls.setCountLimit(1000);
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String ldapSearchBase = "dc=example,dc=com";

        //search for user
        {
            String searchFilter = "(mail=dblasby@example.com)";
            NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
            List<SearchResult> list = Collections.list(results);
            assertEquals(1, list.size());
            assertTrue(list.get(0).getName().contains("blasby"));
        }
        //search for groups user belongs to
        {
            String searchFilter = "(&    (objectClass=*)    (member=cn=blasby\\\\, david,ou=GIS Department,ou=Corporate Users,dc=example,dc=com))";
            NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
            List<SearchResult> list = Collections.list(results);
            assertEquals(3, list.size());
            assertTrue(list.get(0).getName().contains("Geoserver Admin") || list.get(1).getName().contains("Geoserver Admin") || list.get(2).getName().contains("Geoserver Admin"));
            assertTrue(list.get(0).getName().contains("Geonetwork Developer") || list.get(1).getName().contains("Geonetwork Developer") || list.get(2).getName().contains("Geonetwork Developer"));
            assertTrue(list.get(0).getName().contains("GCAT_GENERAL_EDITOR") || list.get(1).getName().contains("GCAT_GENERAL_EDITOR") || list.get(2).getName().contains("GCAT_GENERAL_EDITOR"));

            // assertTrue(list.get(0).getName().contains("blasby"));
        }
    }

    public List<SearchResult> queryLDAP(String searchFilter, DirContext ctx, String ldapSearchBase, SearchControls controls) throws NamingException {
        if (controls == null) {
            controls = new SearchControls();
            controls.setCountLimit(1000);
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        }
        if (ldapSearchBase == null)
            ldapSearchBase = "";
        if (ctx == null)
            ctx = contextSource.getReadOnlyContext();

        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, controls);
        List<SearchResult> list = Collections.list(results);
        return list;
    }
}
