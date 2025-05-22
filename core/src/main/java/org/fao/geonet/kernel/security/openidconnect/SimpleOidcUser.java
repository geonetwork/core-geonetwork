/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect;

import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains information about the user (from the oidc ID Token).
 * <p>
 * This class knows how to do two things;
 * a) extra info from the OIDC ID Token
 * b) move info to a GN user
 * <p>
 * parts taken from BaselUser (GN keycloak plugin)
 */
public class SimpleOidcUser {


    private String username;
    private String firstname;
    private String surname;
    private String organisation;
    private String profile;
    private String email;
    private Address address;


    /**
     *
     * @param oidcConfiguration OIDC Configuration (mostly controlled by environment vars)
     * @param oidcRoleProcessor Processes roles from the ID Token
     * @param idToken  The User's ID token
     * @param attributes All the user's claims (ID Token claims + USERINFO claims)
     */
    public SimpleOidcUser(OIDCConfiguration oidcConfiguration, OIDCRoleProcessor oidcRoleProcessor, OidcIdToken idToken, Map userAttributes) throws Exception {
        Map attributes = (userAttributes == null) ? new HashMap() : userAttributes;

        username = (String) idToken.getClaims().get(oidcConfiguration.getUserNameAttribute());
        if (username == null) {
            username = (String) attributes.get(oidcConfiguration.getUserNameAttribute());
        }
        if (username == null) {
            username = idToken.getPreferredUsername();
        }
        if (username == null) {
            username = (String) attributes.get(StandardClaimNames.PREFERRED_USERNAME);
        }
        if (username == null) {
            username = idToken.getEmail();
        }
        if (username == null) {
            username = (String) attributes.get(StandardClaimNames.EMAIL);
        }

        if  (username == null) {
            throw new Exception("OIDC: could not extract user ID from ID Token or userinfo.  tried PREFERRED_USERNAME, EMAIL, and "+oidcConfiguration.getUserNameAttribute());
        }

        username = org.apache.commons.lang.StringUtils.left(username, 256); //first max 256 chars

        if (!StringUtils.isBlank(username)) {
            // -- get user surname and given name.  Should be in ID Token, but could be in the USERINFO
            surname = idToken.getFamilyName();
            if ( (surname == null) && (attributes.containsKey(StandardClaimNames.FAMILY_NAME)) ) {
                surname = (String) attributes.get(StandardClaimNames.FAMILY_NAME);
            }
            firstname = idToken.getGivenName();
            if ( (firstname == null) && (attributes.containsKey(StandardClaimNames.GIVEN_NAME)) ) {
                firstname = (String) attributes.get(StandardClaimNames.GIVEN_NAME);
            }
            email = idToken.getEmail();
            if ( (email == null) && (attributes.containsKey(StandardClaimNames.EMAIL)) ) {
                email = (String) attributes.get(StandardClaimNames.EMAIL);
            }


            if (idToken.getClaims() != null && idToken.getClaims().containsKey(oidcConfiguration.organizationProperty)) {
                organisation = (String) idToken.getClaims().get(oidcConfiguration.organizationProperty);
            }
            if ( (organisation == null) && attributes.containsKey(oidcConfiguration.organizationProperty) ) {
                organisation = (String) attributes.get(oidcConfiguration.organizationProperty);
            }


            if (idToken.getAddress() != null) {
                address = new Address();
                address.setAddress(idToken.getAddress().getStreetAddress());
                if ( (address.getAddress() == null) && attributes.containsKey(StandardClaimNames.ADDRESS)) {
                    address.setAddress(attributes.get(StandardClaimNames.ADDRESS).toString());

                }
                address.setCity(idToken.getAddress().getLocality());
                address.setState(idToken.getAddress().getRegion());
                address.setZip(idToken.getAddress().getPostalCode());
                address.setCountry(idToken.getAddress().getCountry());
            }


            Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(idToken);
            if (profileGroups != null && profileGroups.size() > 0) {
                profile = oidcRoleProcessor.getMaxProfile(profileGroups).name();
            }
        }
    }

    public SimpleOidcUser(OIDCConfiguration oidcConfiguration, OIDCRoleProcessor oidcRoleProcessor, Map attributes) throws Exception {
        username = (String) attributes.get(oidcConfiguration.getUserNameAttribute());
        if (username == null) {
            username = (String) attributes.get(StandardClaimNames.PREFERRED_USERNAME);
        }
        if (username == null) {
            username = (String) attributes.get(StandardClaimNames.EMAIL);
        }

        if  (username == null) {
            throw new Exception("OIDC: could not extract user ID from ID Token or userinfo.  tried PREFERRED_USERNAME, EMAIL, and "+oidcConfiguration.getUserNameAttribute());
        }

        if (username == null) {
            username = (String) attributes.get(StandardClaimNames.NAME);
        }
        if (username != null) {
            username = org.apache.commons.lang.StringUtils.left(username, 256); //first max 256 chars
        }

        if (!StringUtils.isEmpty(username)) {
            surname = (String) attributes.get(StandardClaimNames.FAMILY_NAME);
            firstname = (String) attributes.get(StandardClaimNames.GIVEN_NAME);

            email = (String) attributes.get(StandardClaimNames.EMAIL);

            if (attributes.containsKey(oidcConfiguration.organizationProperty)) {
                organisation = (String) attributes.get(oidcConfiguration.organizationProperty);
            }

            Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(attributes);
            if (profileGroups != null && profileGroups.size() > 0) {
                profile = oidcRoleProcessor.getMaxProfile(profileGroups).name();
            }
        }
    }


    public void updateUser(User user) {
        if (!StringUtils.isEmpty(this.getSurname())) {
            user.setSurname(this.getSurname());
        }
        if (!StringUtils.isEmpty(this.getFirstname())) {
            user.setName(this.getFirstname());
        }
        if (!StringUtils.isEmpty(this.getOrganisation())) {
            user.setOrganisation(this.getOrganisation());
        }

        // Only update email if it does not already exist and email is not empty
        if (!StringUtils.isEmpty(this.getEmail()) && !user.getEmailAddresses().contains(this.getEmail())) {
            user.getEmailAddresses().clear(); //todo check if this is ok.  cf keycloak
            user.getEmailAddresses().add(this.getEmail());
        }

        Address address;
        if (this.getAddress() != null) {
            if (user.getAddresses().size() > 0) {
                address = user.getAddresses().iterator().next();
            } else {
                address = new Address();
            }
            address.setAddress(this.getAddress().getAddress());
            address.setCity(this.getAddress().getCity());
            address.setState(this.getAddress().getState());
            address.setZip(this.getAddress().getZip());
            address.setCountry(this.getAddress().getCountry());
            user.getAddresses().clear();
            user.getAddresses().add(address);
        }

        if (!StringUtils.isEmpty(this.getProfile()))
            user.setProfile(Profile.findProfileIgnoreCase(this.getProfile()));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
