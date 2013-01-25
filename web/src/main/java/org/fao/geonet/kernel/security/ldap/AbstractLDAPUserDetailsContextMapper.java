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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Profile;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 * Map LDAP user information to GeoNetworkUser information.
 * 
 * Create the GeoNetworkUser in local database on first login and update all
 * user information on subsequent login.
 * 
 * @author francois
 */
public abstract class AbstractLDAPUserDetailsContextMapper implements
        UserDetailsContextMapper, ApplicationContextAware {

    Map<String, String[]> mapping;

    Map<String, String> profileMapping;

    protected boolean importPrivilegesFromLdap;

    private boolean createNonExistingLdapGroup = true;

    private ApplicationContext applicationContext;

    protected DefaultSpringSecurityContextSource contextSource;

    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations userCtx,
            String username, Collection<? extends GrantedAuthority> authorities) {
        ResourceManager resourceManager = applicationContext
                .getBean(ResourceManager.class);
        ProfileManager profileManager = applicationContext
                .getBean(ProfileManager.class);
        SerialFactory serialFactory = applicationContext
                .getBean(SerialFactory.class);

        String defaultProfile = (mapping.get("profile")[1] != null ? mapping
                .get("profile")[1] : Profile.REGISTERED_USER);
        String defaultGroup = mapping.get("privilege")[1];

        Map<String, ArrayList<String>> userInfo = LDAPUtils
                .convertAttributes(userCtx.getAttributes().getAll());

        LDAPUser userDetails = new LDAPUser(profileManager, username);
        userDetails.setName(getUserInfo(userInfo, "name"))
                .setSurname(getUserInfo(userInfo, "surname"))
                .setEmail(getUserInfo(userInfo, "mail"))
                .setOrganisation(getUserInfo(userInfo, "organisation"))
                .setAddress(getUserInfo(userInfo, "address"))
                .setState(getUserInfo(userInfo, "state"))
                .setZip(getUserInfo(userInfo, "zip"))
                .setCity(getUserInfo(userInfo, "city"))
                .setCountry(getUserInfo(userInfo, "country"));

        // Set privileges for the user. If not, privileges are handled
        // in local database
        if (importPrivilegesFromLdap) {
            setProfilesAndPrivileges(resourceManager, profileManager,
                    defaultProfile, defaultGroup, userInfo, userDetails);
        } else {
            setDefaultProfilesAndPrivileges(resourceManager, profileManager,
                    defaultProfile, userDetails);
        }

        // Assign default profile if not set by LDAP info or local database
        if (userDetails.getProfile() == null) {
            userDetails.setProfile(defaultProfile);
        }

        // Check that user profile is defined and fallback to registered user
        // in order to avoid inconsistent JeevesUser creation
        Set<String> checkProfile = profileManager.getProfilesSet(userDetails
                .getProfile());
        if (checkProfile == null) {
            Log.error(Geonet.LDAP, "  User profile " + userDetails.getProfile()
                    + " is not set in Jeeves registered profiles."
                    + " Assigning registered user profile.");
            userDetails.setProfile(Profile.REGISTERED_USER);
        }

        saveUser(resourceManager, serialFactory, userDetails);

        return userDetails;
    }

    abstract protected void setProfilesAndPrivileges(
            ResourceManager resourceManager, ProfileManager profileManager,
            String defaultProfile, String defaultGroup,
            Map<String, ArrayList<String>> userInfo, LDAPUser userDetails);

    /**
     * Check if user exist in database and set his profile or use default one.
     * 
     * @param resourceManager
     * @param profileManager
     * @param defaultProfile
     * @param userDetails
     */
    protected void setDefaultProfilesAndPrivileges(
            ResourceManager resourceManager, ProfileManager profileManager,
            String defaultProfile, LDAPUser userDetails) {

        Dbms dbms = null;
        // If user already exist in database, retrieve his profile information
        try {
            dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
            Element dbUserProfilRequest = dbms.select(
                    "SELECT profile FROM Users WHERE username=?",
                    userDetails.getUsername());
            if (dbUserProfilRequest.getChild("record") != null) {
                String dbUserProfil = dbUserProfilRequest.getChild("record")
                        .getChildText("profile");
                userDetails.setProfile(dbUserProfil);
            }
        } catch (Exception e) {
            try {
                resourceManager.abort(Geonet.Res.MAIN_DB, dbms);
                dbms = null;
            } catch (Exception e2) {
                e.printStackTrace();
                Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e2);
            }
            Log.error(
                    Geonet.LDAP,
                    "Unexpected error while retrieving LDAP user profil in user database",
                    e);
            throw new AuthenticationServiceException(
                    "Unexpected error while retrieving LDAP user profil in user database",
                    e);
        } finally {
            if (dbms != null) {
                try {
                    resourceManager.close(Geonet.Res.MAIN_DB, dbms);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e);
                }
            }
        }
    }

    protected void addProfile(ProfileManager profileManager,
            LDAPUser userDetails, String profile) {
        // Check if profile exist in profile mapping table
        String mappedProfile = profileMapping.get(profile);
        if (mappedProfile != null) {
            profile = mappedProfile;
        }

        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "  Assigning profile " + profile);
        }
        if (profileManager.exists(profile)) {
            userDetails.setProfile(profile);
        } else {
            Log.error(Geonet.LDAP, "  Profile " + profile + " does not exist.");
        }
    }

    private void saveUser(ResourceManager resourceManager,
            SerialFactory serialFactory, LDAPUser userDetails) {
        Dbms dbms = null;
        try {
            dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
            LDAPUtils.saveUser(userDetails, dbms, serialFactory,
                    importPrivilegesFromLdap, createNonExistingLdapGroup);
        } catch (Exception e) {
            try {
                resourceManager.abort(Geonet.Res.MAIN_DB, dbms);
                dbms = null;
            } catch (Exception e2) {
                e.printStackTrace();
                Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e2);
            }
            Log.error(
                    Geonet.LDAP,
                    "Unexpected error while saving/updating LDAP user in database",
                    e);
            throw new AuthenticationServiceException(
                    "Unexpected error while saving/updating LDAP user in database",
                    e);
        } finally {
            if (dbms != null) {
                try {
                    resourceManager.close(Geonet.Res.MAIN_DB, dbms);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e);
                }
            }
        }
    }

    private String getUserInfo(Map<String, ArrayList<String>> userInfo,
            String attributeName) {
        return getUserInfo(userInfo, attributeName, "");
    }

    /**
     * Return the first element of userInfo corresponding to the attribute name.
     * If attributeName mapping is not defined, return empty string. If no value
     * found in LDAP user info, return default value.
     * 
     * @param userInfo
     * @param attributeName
     * @param defaultValue
     * @return
     */
    private String getUserInfo(Map<String, ArrayList<String>> userInfo,
            String attributeName, String defaultValue) {
        String[] attributeMapping = mapping.get(attributeName);
        String value = "";

        if (attributeMapping != null) {
            String ldapAttributeName = attributeMapping[0];
            String configDefaultValue = attributeMapping[1];

            if (ldapAttributeName != null
                    && userInfo.get(ldapAttributeName) != null
                    && userInfo.get(ldapAttributeName).get(0) != null) {
                value = userInfo.get(ldapAttributeName).get(0);
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

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
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

    public void setMapping(Map<String, String[]> mapping) {
        this.mapping = mapping;
    }

    public Map<String, String[]> getMapping() {
        return mapping;
    }

    public String getProfileMappingValue(String key) {
        return profileMapping.get(key);
    }

    public void setProfileMapping(Map<String, String> profileMapping) {
        this.profileMapping = profileMapping;
    }

    public Map<String, String> getProfileMapping() {
        return profileMapping;
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

}