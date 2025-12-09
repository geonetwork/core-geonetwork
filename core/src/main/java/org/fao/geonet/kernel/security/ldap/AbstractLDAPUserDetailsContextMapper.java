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
package org.fao.geonet.kernel.security.ldap;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.WritableUserDetailsContextMapper;
import org.fao.geonet.utils.Log;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.ObjectUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

/**
 * Map LDAP user information to GeoNetworkUser information.
 *
 * Create the GeoNetworkUser in local database on first login and update all user information on
 * subsequent login.
 *
 * @author francois
 */
public abstract class AbstractLDAPUserDetailsContextMapper implements
    WritableUserDetailsContextMapper {

    protected boolean importPrivilegesFromLdap;
    protected DefaultSpringSecurityContextSource contextSource;
    Map<String, String[]> mapping;
    Map<String, Profile> profileMapping;
    private boolean createNonExistingLdapGroup = true;
    private boolean createNonExistingLdapUser = false;
    private UserDetailsManager ldapManager;

    private String ldapBaseDnPattern = "uid={0},ou=users";

    private String ldapBaseDn;
    private boolean ldapUsernameCaseInsensitive = true;


    private LDAPUtils ldapUtils;

    public void setLdapUtils(LDAPUtils utils){
        this.ldapUtils = utils;
    }


    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        ctx.setAttributeValues("objectclass", new String[]{"top", "person",
            "organizationalPerson", "inetOrgPerson"});
        ctx.setAttributeValue("sn", user.getUsername());
        ctx.setAttributeValue("cn", user.getUsername());
        ctx.setAttributeValue("uid", user.getUsername());

        if (user instanceof InetOrgPerson) {
            InetOrgPerson p = (InetOrgPerson) user;
            ctx.setAttributeValue("mail", p.getMail());
            ctx.setAttributeValue("displayName", p.getDisplayName());
            ctx.setAttributeValue("sn", p.getSn());
        }
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations userCtx,
                                          String username, Collection<? extends GrantedAuthority> authorities) {

        Profile defaultProfile;
        if ((mapping.get("profile") != null) && (mapping.get("profile")[1] != null)) {
            defaultProfile = Profile.valueOf(mapping.get("profile")[1]);
        } else {
            defaultProfile = Profile.RegisteredUser;
        }
        String defaultGroup = mapping.get("privilege")[1];
        //allow proper injection
        LDAPUtils ldapUtils = ApplicationContextHolder.get().getBean(LDAPUtils.class);

        Map<String, ArrayList<String>> userInfo = ldapUtils
            .convertAttributes(userCtx.getAttributes().getAll());

        if (this.isLdapUsernameCaseInsensitive()) {
            username = username.toLowerCase();
        }

        //pass DN along.
        // NOTE: LDAPUser doesn't allow you to set DN!!!
        if (!userInfo.containsKey("dn")) {
            ArrayList dns = new ArrayList(Arrays.asList(
                    userCtx.getDn().toString(),   //will not include base
                    userCtx.getNameInNamespace() // includes base
                ));
            userInfo.put("dn", dns);
        }

        LDAPUser userDetails = new LDAPUser(username);
        User user = userDetails.getUser();
        user.setName(getUserInfo(userInfo, "name"));

        user.setSurname(getUserInfo(userInfo, "surname"));
        user.setOrganisation(getUserInfo(userInfo, "organisation"));
        user.getEmailAddresses().clear();
        user.getEmailAddresses().add(getUserInfo(userInfo, "mail"));
        user.getPrimaryAddress().setAddress(getUserInfo(userInfo, "address"))
            .setState(getUserInfo(userInfo, "state"))
            .setZip(getUserInfo(userInfo, "zip"))
            .setCity(getUserInfo(userInfo, "city"))
            .setCountry(getUserInfo(userInfo, "country"));

        // Set privileges for the user. If not, privileges are handled
        // in local database
        if (importPrivilegesFromLdap) {
            setProfilesAndPrivileges(defaultProfile, defaultGroup, userInfo,
                userDetails);
        }

        // Assign default profile and default group
        // if not set by LDAP info or local database
        if (user.getProfile() == null) {
            user.setProfile(defaultProfile);
            if (userDetails.getPrivileges().size() == 0 && defaultGroup != null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(
                        Geonet.LDAP,
                        "  No privilege defined, setting privilege for group "
                            + defaultGroup + " as "
                            + userDetails.getUser().getProfile());
                }
                userDetails
                    .addPrivilege(defaultGroup, userDetails.getUser().getProfile());
            }
        }

        saveUser(userDetails);

        return userDetails;
    }

    abstract protected void setProfilesAndPrivileges(Profile defaultProfile,
                                                     String defaultGroup, Map<String, ArrayList<String>> userInfo,
                                                     LDAPUser userDetails);

    protected void addProfile(@Nonnull LDAPUser userDetails,
                              @Nonnull String profileName, @Nullable Set<Profile> profileList) {
        // Check if profile exist in profile mapping table
        if (profileMapping != null) {
            Profile mapped = profileMapping.get(profileName);
            if (mapped != null) {
                profileName = mapped.name();
            }
        }

        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "  Assigning profile " + profileName);
        }
        try {
            Profile profile = Profile.valueOf(profileName);
            if (profile != null) {
                userDetails.getUser().setProfile(profile);
                if (profileList != null) {
                    profileList.add(profile);
                }
            } else {
                Log.error(Geonet.LDAP, "  Profile " + profileName
                    + " does not exist.");
            }
        } catch (Exception e) {
            Log.warning(Geonet.LDAP, "  Profile " + profileName
                + " is not a valid profile. " + e.getMessage());
        }
    }

    @Override
    public synchronized void saveUser(LDAPUser userDetails) {
        try {
            //allow proper injection
            LDAPUtils ldapUtils = this.ldapUtils != null? this.ldapUtils :
                ApplicationContextHolder.get().getBean(LDAPUtils.class);

            if (createNonExistingLdapUser
                && !ldapManager.userExists(userDetails.getUsername())) {
                InetOrgPerson.Essence p = new InetOrgPerson.Essence(userDetails);
                p.setDn(ldapBaseDnPattern.replace("{0}",
                    userDetails.getUsername()));
                String surname = userDetails.getUser().getSurname();
                if (ObjectUtils.isEmpty(surname)) {
                    //sn is usually mandatory on LDAP
                    surname = userDetails.getUsername();
                }
                p.setSn(surname);
                p.setUid(userDetails.getUsername());
                p.setMail(userDetails.getUser().getEmail());
                String name = userDetails.getUser().getName();
                if (ObjectUtils.isEmpty(name)) {
                    //displayname is usually mandatory too
                    name = userDetails.getUsername();
                }
                p.setDisplayName(name);
                String[] cn = ldapBaseDn.split(",");
                for (int i = 0; i < cn.length; i++) {
                    cn[i] = cn[i].substring(cn[i].indexOf("=") + 1);
                }
                p.setCn(cn);
                ldapManager.createUser(p.createUserDetails());
            }
            ldapUtils.saveUser(userDetails, importPrivilegesFromLdap, createNonExistingLdapGroup);
        } catch (Exception e) {
            throw new AuthenticationServiceException(
                "Unexpected error while saving/updating LDAP user in database",
                e);
        }
    }

    private String getUserInfo(Map<String, ArrayList<String>> userInfo,
                               String attributeName) {
        return getUserInfo(userInfo, attributeName, "");
    }

    //returns null if not available
    private String getValue(Map<String, ArrayList<String>> userInfo,String ldapAttributeName) {
        if ((ldapAttributeName == null) || (userInfo == null)) //bad args
            return null;
        ArrayList<String> info = userInfo.get(ldapAttributeName);
        if ((info == null) || (info.size() ==0)) //no value supplied
            return null;
        if (info.size() == 1) // only one value -- that's it
            return info.get(0);
        // we sometime get > 1 value here, especially for CN containing the full DN
        if (info.get(1) == null) //no value there
            return info.get(0);
        if (info.get(0).length() < info.get(1).length()) //return shortest
            return info.get(0);
        return info.get(1);
    }

    /**
     * Return the first element of userInfo corresponding to the attribute name. If attributeName
     * mapping is not defined, return empty string. If no value found in LDAP user info, return
     * default value.
     */
    private String getUserInfo(Map<String, ArrayList<String>> userInfo,
                               String attributeName, String defaultValue) {
        String[] attributeMapping = mapping.get(attributeName);
        String value = "";

        if (attributeMapping != null) {
            String ldapAttributeName = attributeMapping[0];
            String configDefaultValue = attributeMapping[1];

            String v = getValue(userInfo, ldapAttributeName);

            if (v != null) {
                value = v;
            } else if (configDefaultValue != null) {
                value = configDefaultValue;
            } else {
                value = defaultValue;
            }
        } else {
            value = defaultValue;
        }

        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "LDAP attribute '" + attributeName + "' = "
                + value);
        }
        return value;
    }

    public boolean isCreateNonExistingLdapGroup() {
        return createNonExistingLdapGroup;
    }

    public void setCreateNonExistingLdapGroup(boolean createNonExistingLdapGroup) {
        this.createNonExistingLdapGroup = createNonExistingLdapGroup;
    }

    public String[] getMappingValue(String key) {
        return mapping.get(key);
    }

    public Map<String, String[]> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String[]> mapping) {
        this.mapping = mapping;
    }

    public Profile getProfileMappingValue(String key) {
        return profileMapping.get(key);
    }

    public Map<String, Profile> getProfileMapping() {
        return profileMapping;
    }

    public void setProfileMapping(Map<String, Profile> profileMapping) {
        this.profileMapping = profileMapping;
    }

    public boolean isImportPrivilegesFromLdap() {
        return importPrivilegesFromLdap;
    }

    public void setImportPrivilegesFromLdap(boolean importPrivilegesFromLdap) {
        this.importPrivilegesFromLdap = importPrivilegesFromLdap;
    }

    public DefaultSpringSecurityContextSource getContextSource() {
        return contextSource;
    }

    public void setContextSource(
        DefaultSpringSecurityContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public UserDetailsManager getLdapManager() {
        return ldapManager;
    }

    public void setLdapManager(UserDetailsManager ldapManager) {
        this.ldapManager = ldapManager;
    }

    public boolean isCreateNonExistingLdapUser() {
        return createNonExistingLdapUser;
    }

    public void setCreateNonExistingLdapUser(boolean createNonExistingLdapUser) {
        this.createNonExistingLdapUser = createNonExistingLdapUser;
    }

    public String getLdapBaseDnPattern() {
        return ldapBaseDnPattern;
    }

    public void setLdapBaseDnPattern(String ldapBaseDnPattern) {
        this.ldapBaseDnPattern = ldapBaseDnPattern;
    }

    public String getLdapBaseDn() {
        return ldapBaseDn;
    }

    public void setLdapBaseDn(String ldapBaseDn) {
        this.ldapBaseDn = ldapBaseDn;
    }

    public void setLdapUsernameCaseInsensitive(boolean ldapUsernameCaseInsensitive) {
        this.ldapUsernameCaseInsensitive = ldapUsernameCaseInsensitive;
    }

    public boolean isLdapUsernameCaseInsensitive() {
        return ldapUsernameCaseInsensitive;
    }
}
