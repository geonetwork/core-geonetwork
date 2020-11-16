package org.fao.geonet;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.ldap.LDAPConstants;
import org.fao.geonet.kernel.security.ldap.LDAPProfileToDbUserSynchronizer;
import org.fao.geonet.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        locations = { "classpath:/synchronize-sxt-user-profile-config-test.xml" }
    )
public class LDAPProfileToDbUserSynchronizerIT {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private LDAPProfileToDbUserSynchronizer toTest;

    @Autowired
    private UserRepository userRepository;

    private static Boolean usersCreated = false;

    private void createUser(String username, String emailAddress,
                            Profile profile, Boolean ldapUser) {
        User usr = new User()
            .setUsername(username)
            .setEmailAddresses(new HashSet<>(Arrays.asList(emailAddress)))
            .setProfile(profile);

        if (ldapUser) {
            usr.getSecurity().setAuthType(LDAPConstants.LDAP_FLAG);
        }
        userRepository.save(usr);
    }

    @Before
    public void setUp() {
        // The AbstractLDAPUserDetailsContextMapper is making use
        // of the holder class to get a hand on the ApplicationContext.
        ApplicationContextHolder.set((ConfigurableApplicationContext) context);

        // We cannot use the @BeforeClass annotation, as we need
        // the injection of @Autowired dependent beans to have already taken
        // place. the annotation also requires a static method, which is
        // incompatible with using non-static injected beans.
        //
        // Also the boolean is static, because when the tests execute
        // each method runs in its own instance of the object.
        //
        // I cannot figure out a proper way to do so, though, and I am
        // pretty sure this is not idempotent.
        if (! usersCreated) {
            createUser("pmauduit", "pierre.mauduit@camptocamp.com",
                Profile.RegisteredUser, true);
            // admin is a LdapUser, and should be Administrator
            // this account should not be affected by the process
            createUser("admin", "admin@camptocamp.com",
                Profile.Administrator, true);
            // bbinet is a LdapUser but should not be recognized as administrator
            // he should be updated by the process
            createUser("bbinet", "bbinet@camptocamp.com",
                Profile.Administrator, true);
            // The following is not a LdapUser, and should not be affected by the process
            createUser("fvanderbiest", "fvanderbiest@camptocamp.com",
                Profile.Reviewer, false);
            // jeichar exists in the LDAP (see sextant.ldif) but not in db
            // he should not be affected (not created in the GN database)
            // createUser("jeichar", "jeichar@mail.ru", Profile.Editor, true);
            // The following user exists in the database but not in the LDAP
            createUser("notInTheLDAPUser", "notanldapuser@example.org",
                Profile.RegisteredUser, true);
            usersCreated = true;
        }
    }

    @Test
    public void testFindSingleUserInLdap() {
        // This user exists in the LDAP
        User usr = toTest.findSingleUserInLdap("pmauduit");
        assertEquals("pmauduit", usr.getUsername());

        // This one does not
        usr = toTest.findSingleUserInLdap("notexisting");
        assertNull(usr);
    }

    @Test
    public void testfindAllUsersWithLdapAuth() {
        List<User> list = toTest.findAllUsersWithLdapAuth();
        // fvanderbiest is not a LDAP user
        User notFound = list.stream().filter(usr -> usr.getUsername().equals("fvanderbiest"))
            .findFirst().orElse(null);
        // on the contrary, pmauduit does exist in db
        User pmauduit = list.stream().filter(usr -> usr.getUsername().equals("pmauduit"))
            .findFirst().orElse(null);

        assertNull(notFound);
        assertNotNull(pmauduit);

    }

    @Test
    public void testDoExecute() throws JobExecutionException {

        toTest.doExecute();

        List<User> list = userRepository.findAll();
        User pmauduit = list.stream().filter(user -> user.getUsername().equals("pmauduit"))
            .findFirst().orElse(null);
        User admin = list.stream().filter(user -> user.getUsername().equals("admin"))
            .findFirst().orElse(null);
        User bbinet = list.stream().filter(user -> user.getUsername().equals("bbinet"))
            .findFirst().orElse(null);
        User fvanderbiest = list.stream().filter(user -> user.getUsername().equals("fvanderbiest"))
            .findFirst().orElse(null);
        User notInTheLDAPUser = list.stream().filter(user -> user.getUsername().equals("notInTheLDAPUser"))
            .findFirst().orElse(null);

        // pmauduit should have been promoted to Administrator after job execution
        assertEquals(pmauduit.getProfile(), Profile.Administrator);
        // admin should stay Administrator
        assertEquals(admin.getProfile(), Profile.Administrator);
        // bbinet should have been downgraded to Editor (as listesiteweb: SXT5_MIMEL_Editor)
        assertEquals(bbinet.getProfile(), Profile.Editor);
        // fvanderbiest should stay Reviewer (as not a LdapUser, he should not be affected)
        assertEquals(fvanderbiest.getProfile(), Profile.Reviewer);
        // notInTheLdapUser should also stay RegisteredUser (because not in the LDAP)
        assertEquals(notInTheLDAPUser.getProfile(), Profile.RegisteredUser);


    }
}
