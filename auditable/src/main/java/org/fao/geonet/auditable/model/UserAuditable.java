/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.auditable.model;

import org.fao.geonet.auditable.AuditableEntity;
import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.TypeName;

import java.util.ArrayList;
import java.util.List;

@TypeName("User")
public class UserAuditable extends AuditableEntity {
    @Id
    private int id;
    private String profile;
    private String username;
    private String name;
    private String surname;
    private List<String> emailAddresses;
    private String organisation;
    private List<Address> addresses;
    private String kind;
    private List<String> groupsRegisteredUser;
    private List<String> groupsEditor;
    private List<String> groupsReviewer;
    private List<String> groupsUserAdmin;
    private boolean enabled;

    public UserAuditable() {
        this.groupsRegisteredUser = new ArrayList<>();
        this.groupsEditor = new ArrayList<>();
        this.groupsReviewer = new ArrayList<>();
        this.groupsUserAdmin = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<String> getGroupsRegisteredUser() {
        return groupsRegisteredUser;
    }

    public void setGroupsRegisteredUser(List<String> groupsRegisteredUser) {
        this.groupsRegisteredUser = groupsRegisteredUser;
    }

    public List<String> getGroupsEditor() {
        return groupsEditor;
    }

    public void setGroupsEditor(List<String> groupsEditor) {
        this.groupsEditor = groupsEditor;
    }

    public List<String> getGroupsReviewer() {
        return groupsReviewer;
    }

    public void setGroupsReviewer(List<String> groupsReviewer) {
        this.groupsReviewer = groupsReviewer;
    }

    public List<String> getGroupsUserAdmin() {
        return groupsUserAdmin;
    }

    public void setGroupsUserAdmin(List<String> groupsUserAdmin) {
        this.groupsUserAdmin = groupsUserAdmin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static UserAuditable build(User user, List<UserGroup> userGroups) {
        UserAuditable userAuditable = new UserAuditable();

        userAuditable.setId(user.getId());
        userAuditable.setUsername(user.getUsername());
        userAuditable.setName(user.getName());
        userAuditable.setSurname(user.getSurname());
        userAuditable.setEnabled(user.isEnabled());
        userAuditable.setKind(user.getKind());
        userAuditable.setOrganisation(user.getOrganisation());
        userAuditable.setProfile(user.getProfile().name());
        userAuditable.setEnabled(user.isEnabled());

        // Groups
        userGroups.stream().forEach(userGroup -> {
            switch (userGroup.getProfile()) {
                case UserAdmin:
                    userAuditable.getGroupsUserAdmin().add(userGroup.getGroup().getName());
                    break;
                case Reviewer:
                    userAuditable.getGroupsReviewer().add(userGroup.getGroup().getName());
                    break;
                case Editor:
                    userAuditable.getGroupsEditor().add(userGroup.getGroup().getName());
                    break;
                case RegisteredUser:
                    userAuditable.getGroupsRegisteredUser().add(userGroup.getGroup().getName());
                    break;
                default:
                    break;
            }
        });

        return userAuditable;
    }

    @Override
    public String getEntityName() {
        return "User";
    }
}
