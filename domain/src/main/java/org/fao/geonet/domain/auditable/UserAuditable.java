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
package org.fao.geonet.domain.auditable;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.hibernate.envers.Audited;

/**
 * An entity to audit the changes for user entities.
 *
 * @see org.fao.geonet.domain.User
 */
@Entity
@Access(AccessType.PROPERTY)
@Audited(withModifiedFlag = true)
public class UserAuditable extends AuditableEntity {

    private int id;
    private String profile;
    private String username;
    private String name;
    private String surname;
    private String emailAddress;
    private String organisation;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String kind;
    private String groupsRegisteredUser;
    private String groupsEditor;
    private String groupsReviewer;
    private String groupsUserAdmin;
    private boolean enabled;

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
        if (!user.getEmailAddresses().isEmpty()) {
            // A user can have only 1 address defined in the UI.
            userAuditable.setEmailAddress((String) user.getEmailAddresses().toArray()[0]);
        }
        if (!user.getAddresses().isEmpty()) {
            // A user can have only 1 address defined in the UI.
            Address userAddress = (Address) user.getAddresses().toArray()[0];
            userAuditable.setAddress(userAddress.getAddress());
            userAuditable.setZip(userAddress.getZip());
            userAuditable.setState(userAddress.getState());
            userAuditable.setCity(userAddress.getCity());
            userAuditable.setCountry(userAddress.getCountry());
        }
        userAuditable.setEnabled(user.isEnabled());

        Set<String> groupsRegisteredUserList = new TreeSet<>();
        Set<String> groupsEditorList = new TreeSet<>();
        Set<String> groupsReviewerList = new TreeSet<>();
        Set<String> groupsUserAdminList = new TreeSet<>();

        // Groups
        if (userGroups != null) {
            userGroups.forEach(userGroup -> {
                switch (userGroup.getProfile()) {
                    case RegisteredUser:
                        groupsRegisteredUserList.add(userGroup.getGroup().getName());
                        break;
                    case Editor:
                        groupsEditorList.add(userGroup.getGroup().getName());
                        break;
                    case Reviewer:
                        groupsReviewerList.add(userGroup.getGroup().getName());
                        break;
                    case UserAdmin:
                        groupsUserAdminList.add(userGroup.getGroup().getName());
                        break;
                    default:
                        break;
                }
            });
        }


        userAuditable.setGroupsRegisteredUser(StringUtils.join(groupsRegisteredUserList, ","));
        userAuditable.setGroupsEditor(StringUtils.join(groupsEditorList, ","));
        userAuditable.setGroupsReviewer(StringUtils.join(groupsReviewerList, ","));
        userAuditable.setGroupsUserAdmin(StringUtils.join(groupsUserAdminList, ","));

        return userAuditable;
    }

    @Id
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

    @Nonnull
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nonnull String username) {
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

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddresses) {
        this.emailAddress = emailAddresses;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getGroupsRegisteredUser() {
        return groupsRegisteredUser;
    }

    public void setGroupsRegisteredUser(String groupsRegisteredUser) {
        this.groupsRegisteredUser = groupsRegisteredUser;
    }

    public String getGroupsEditor() {
        return groupsEditor;
    }

    public void setGroupsEditor(String groupsEditor) {
        this.groupsEditor = groupsEditor;
    }

    public String getGroupsReviewer() {
        return groupsReviewer;
    }

    public void setGroupsReviewer(String groupsReviewer) {
        this.groupsReviewer = groupsReviewer;
    }

    public String getGroupsUserAdmin() {
        return groupsUserAdmin;
    }

    public void setGroupsUserAdmin(String groupsUserAdmin) {
        this.groupsUserAdmin = groupsUserAdmin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
