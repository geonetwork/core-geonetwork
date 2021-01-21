package org.fao.geonet.util.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.UserRepository;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MarineIdCasUserDetailAuthenticationProvider<T extends Authentication>
    extends AbstractUserDetailsAuthenticationProvider implements
    AuthenticationUserDetailsService<T> {

    @Autowired
    private UserRepository userRepo;

    private HashMap<String, String> casAttributesToUserMapping = new HashMap<>();
    private static final String MARINEID_CAS_FLAG = "MARINEID";

    private static final Log logger = LogFactory.getLog(MarineIdCasUserDetailAuthenticationProvider.class);

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        User user = userRepo.findOneByUsername(username);
        if (user == null || ! user.getSecurity().getAuthType().equalsIgnoreCase(MARINEID_CAS_FLAG)) {
            throw new AuthenticationServiceException(
                "invalid user or user not found");
        }
        return user;
    }

    @Override
    public UserDetails loadUserDetails(Authentication authentication) throws UsernameNotFoundException {
        if (!(authentication instanceof CasAssertionAuthenticationToken)) {
            throw new UsernameNotFoundException("Unexpected authentication object");
        }
        CasAssertionAuthenticationToken auth = (CasAssertionAuthenticationToken)  authentication;
        Assertion a = auth.getAssertion();
        if (a == null) {
            throw new UsernameNotFoundException("Assertion object is null");
        }
        AttributePrincipal ap = a.getPrincipal();

        Map<String, Object> attrs = ap.getAttributes();
        String username = (String) authentication.getPrincipal();
        try {
            User user = userRepo.findOneByUsername(username);
            if (user == null) // if user does not exist yet, add one as guest
            {
                user = new User();
                user.setUsername(username);
                user.setProfile(Profile.Guest);
            }
            user.getSecurity().setAuthType(MARINEID_CAS_FLAG);
            mapUserAttributes(user, attrs);
            userRepo.saveAndFlush(user);
            return user;
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    private void mapUserAttributes(User user, Map<String, Object> attrs) {
        /* maps the email */
        String emailAttr = this.casAttributesToUserMapping.getOrDefault("email", null);
        if (emailAttr != null) {
            String emailVal = (String) attrs.getOrDefault(emailAttr, null);
            if (emailVal != null) {
                user.setEmailAddresses(new HashSet<String>(Arrays.asList(emailVal)));
            }
        }
        /* maps the surname */
        String surnameAttr = this.casAttributesToUserMapping.getOrDefault("surname", null);
        if (surnameAttr != null) {
            String surnameVal = (String) attrs.getOrDefault(surnameAttr, null);
            if (surnameVal != null) {
                user.setSurname(surnameVal);
            }
        }
        /* maps the organization */
        String orgAttr = this.casAttributesToUserMapping.getOrDefault("organisation", null);
        if (orgAttr != null) {
            String orgVal = (String) attrs.getOrDefault(orgAttr, null);
            if (orgVal != null) {
                user.setOrganisation(orgVal);
            }
        }
        /* maps the name */
        String nameAttr = this.casAttributesToUserMapping.getOrDefault("name", null);
        if (nameAttr != null) {
            String nameVal = (String) attrs.getOrDefault(nameAttr, null);
            if (nameVal != null) {
                user.setName(nameVal);
            }
        }
        /* maps the "kind" */
        String kindAttr =  this.casAttributesToUserMapping.getOrDefault("kind", null);
        if (kindAttr != null) {
            String kindVal = (String) attrs.getOrDefault(kindAttr, null);
            if (kindVal != null) {
                user.setKind(kindVal);
            }
        }
    }

    public void setCasAttributesToUserMapping(HashMap<String, String> casAttributesToUserMapping) {
        this.casAttributesToUserMapping = casAttributesToUserMapping;
    }
}
