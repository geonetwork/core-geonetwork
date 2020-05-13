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
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class LDAPUtils {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save or update an LDAP user to the local GeoNetwork database.
     */

    @Transactional
    public synchronized void saveUser(LDAPUser user,
                                      boolean importPrivilegesFromLdap, boolean createNonExistingLdapGroup)
        throws Exception {
        String userName = user.getUsername();
        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "LDAP user sync for " + userName + " ...");
        }
        User toSave = getUser(user, importPrivilegesFromLdap, userName);

        // Add user groups if the user has no group assigned.
        // This means that if some privileges has been assigned
        // to the user after the creation, those are preserved.
        // The default one from the LDAP config are only set
        // it user has no privileges.
        UserGroupRepository userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);
        List<UserGroup> existingGroups = userGroupRepository.findAll(UserGroupSpecs.hasUserId(user.getUser().getId()));

        if (existingGroups.size() == 0 && user.getPrivileges().size() > 0) {
            entityManager.flush();
            entityManager.clear();
            List<UserGroup> ug = getPrivilegesAndCreateGroups(user,
                createNonExistingLdapGroup, toSave);
            entityManager.flush();
            setUserGroups(toSave, ug);
        }
    }

    @Transactional
    protected User getUser(LDAPUser user, boolean importPrivilegesFromLdap,
                         String userName) {
        UserRepository userRepo = ApplicationContextHolder.get().getBean(UserRepository.class);

        User loadedUser = userRepo.findOneByUsername(userName);
        User toSave;
        if (loadedUser != null) {
            // If we don't import privileges from LDAP
            // Set the LDAP user profile to be the one set
            // in the local database. If not, the db profile
            // would be always reset by merge.
            if (!importPrivilegesFromLdap) {
                user.getUser().setProfile(loadedUser.getProfile());
            }
            loadedUser.mergeUser(user.getUser(), false);
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP,
                    "  - Update LDAP user " + user.getUsername() + " ("
                        + loadedUser.getId() + ") in local database.");
            }
            toSave = loadedUser;

        } else {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP,
                    "  - Saving new LDAP user " + user.getUsername()
                        + " to database.");
            }
            toSave = user.getUser();
        }
        toSave.getSecurity().setAuthType(LDAPConstants.LDAP_FLAG);
        toSave = userRepo.save(toSave);
        user.setUser(toSave);
        return toSave;
    }

    @Transactional
    protected List<UserGroup> getPrivilegesAndCreateGroups(LDAPUser user,
                                                         boolean createNonExistingLdapGroup, User toSave) {
        GroupRepository groupRepo = ApplicationContextHolder.get().getBean(GroupRepository.class);

        List<UserGroup> ug = new LinkedList<UserGroup>();
        for (Map.Entry<String, Profile> privilege : user.getPrivileges()
            .entries()) {
            // Add group privileges for each groups

            // Retrieve group id
            String groupName = privilege.getKey();
            Profile profile = privilege.getValue();

            Group group = groupRepo.findByName(groupName);

            if (group == null && createNonExistingLdapGroup) {
                group = new Group().setName(groupName);
                group = groupRepo.save(group);

                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  - Add LDAP group " + groupName
                        + " for user.");
                }
            }
            if (group != null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  - Add LDAP group " + groupName
                        + " for user.");
                }
                UserGroup usergroup = new UserGroup();
                usergroup.setGroup(group);
                usergroup.setUser(toSave);
                usergroup.setProfile(profile);
                ug.add(usergroup);
            } else {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(
                        Geonet.LDAP,
                        "  - Can't create LDAP group "
                            + groupName
                            + " for user. "
                            + "Group does not exist in local database or createNonExistingLdapGroup is set to false.");
                }
            }
        }
        return ug;
    }

    @Transactional
    protected void setUserGroups(final User user, List<UserGroup> userGroups)
        throws Exception {
        UserGroupRepository userGroupRepo = ApplicationContextHolder.get().getBean(UserGroupRepository.class);

        Collection<UserGroup> all = userGroupRepo.findAll(UserGroupSpecs
            .hasUserId(user.getId()));

        if (Log.isTraceEnabled(Log.JEEVES)) {
            Log.trace(
                Log.JEEVES,
                "Current usergroups:"
                    + UserGroupSpecs.hasUserId(user.getId()));
            Log.trace(Log.JEEVES, all.size());

            for (UserGroup g : all) {
                Log.trace(Log.JEEVES, g);
            }
        }
        // Have a quick reference of existing groups and profiles for this user
        Set<String> listOfAddedProfiles = new HashSet<String>();
        for (UserGroup ug : all) {
            String key = ug.getProfile().name() + ug.getGroup().getId();
            if (!listOfAddedProfiles.contains(key)) {
                listOfAddedProfiles.add(key);
            }
        }

        // We start removing all old usergroup objects. We will remove the
        // explicitly defined for this call
        Collection<UserGroup> toRemove = new ArrayList<UserGroup>();
        toRemove.addAll(all);

        // New pairs of group-profile we need to add
        Collection<UserGroup> toAdd = new ArrayList<UserGroup>();

        // For each of the parameters on the request, make sure the group is
        // updated.
        for (UserGroup element : userGroups) {
            Group group = element.getGroup();
            String profile = element.getProfile().name();
            // The user has a new group and profile

            // Combine all groups editor and reviewer groups
            if (profile.equals(Profile.Reviewer.name())) {
                final UserGroup userGroup = new UserGroup().setGroup(group)
                    .setProfile(Profile.Editor).setUser(user);
                String key = Profile.Editor.toString() + group.getId();
                if (!listOfAddedProfiles.contains(key)) {
                    toAdd.add(userGroup);
                    listOfAddedProfiles.add(key);
                }

                // If the user is already part of this group with this profile,
                // leave it alone:
                for (UserGroup g : all) {
                    if (g.getGroup().getId() == group.getId()
                        && g.getProfile().equals(Profile.Editor)) {
                        toRemove.remove(g);
                    }
                }
            }

            final UserGroup userGroup = new UserGroup().setGroup(group)
                .setProfile(Profile.findProfileIgnoreCase(profile))
                .setUser(user);
            String key = profile + group.getId();
            if (!listOfAddedProfiles.contains(key)) {
                toAdd.add(userGroup);
                listOfAddedProfiles.add(key);

            }

            // If the user is already part of this group with this profile,
            // leave it alone:
            for (UserGroup g : all) {
                if (g.getGroup().getId() == group.getId()
                    && g.getProfile().name().equalsIgnoreCase(profile)) {
                    toRemove.remove(g);
                }
            }
        }

        // Remove deprecated usergroups (if any)
        userGroupRepo.delete(toRemove);
        entityManager.flush();
        entityManager.clear();

        // Add only new usergroups (if any)
        userGroupRepo.save(toAdd);
        entityManager.flush();

    }

    protected Map<String, ArrayList<String>> convertAttributes(
        NamingEnumeration<? extends Attribute> attributesEnumeration) {
        Map<String, ArrayList<String>> userInfo = new HashMap<String, ArrayList<String>>();
        try {
            while (attributesEnumeration.hasMore()) {
                Attribute attr = attributesEnumeration.next();
                String id = attr.getID();

                ArrayList<String> values = userInfo.get(id);
                if (values == null) {
                    values = new ArrayList<String>();
                    userInfo.put(id, values);
                }

                // --- loop on all attribute's values
                NamingEnumeration<?> valueEnum = attr.getAll();

                while (valueEnum.hasMore()) {
                    Object value = valueEnum.next();
                    // Only retrieve String attribute
                    if (value instanceof String) {
                        values.add((String) value);
                    }
                }
            }
        } catch (NamingException e) {
            Log.error(Geonet.LDAP, e.getMessage(), e);
        }
        return userInfo;
    }
}
