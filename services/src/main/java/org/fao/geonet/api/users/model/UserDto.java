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
package org.fao.geonet.api.users.model;

import org.fao.geonet.domain.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO class for user information.
 *
 * @author Jose García
 */
public class UserDto {
    private String id;
    private String profile;
    private String username;
    private String name;
    private String surname;
    private List<String> emailAddresses;
    private String organisation;
    private List<Address> addresses;
    private String kind;
    private String password;
    private List<String> groupsRegisteredUser;
    private List<String> groupsEditor;
    private List<String> groupsReviewer;
    private List<String> groupsUserAdmin;
    private boolean enabled;

    public UserDto() {
        this.emailAddresses = new ArrayList<>();
        this.addresses = new ArrayList<>();
        this.groupsRegisteredUser = new ArrayList<>();
        this.groupsEditor = new ArrayList<>();
        this.groupsReviewer = new ArrayList<>();
        this.groupsUserAdmin = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public UserDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getProfile() {
        return profile;
    }

    public UserDto setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public UserDto setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public UserDto setEmail(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
        return this;
    }

    public String getOrganisation() {
        return organisation;
    }

    public UserDto setOrganisation(String organisation) {
        this.organisation = organisation;
        return this;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public UserDto setAddresses(List<Address> addresses) {
        this.addresses = addresses;
        return this;
    }


    public String getKind() {
        return kind;
    }

    public UserDto setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserDto setPassword(String password) {
        this.password = password;
        return this;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public UserDto setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public List<String> getGroupsRegisteredUser() {
        return groupsRegisteredUser;
    }

    public UserDto setGroupsRegisteredUser(List<String> groupsRegisteredUser) {
        this.groupsRegisteredUser = groupsRegisteredUser;
        return this;
    }

    public List<String>  getGroupsEditor() {
        return groupsEditor;
    }

    public UserDto setGroupsEditor(List<String> groupsEditor) {
        this.groupsEditor = groupsEditor;
        return this;
    }

    public List<String>  getGroupsReviewer() {
        return groupsReviewer;
    }

    public UserDto setGroupsReviewer(List<String> groupsReviewer) {
        this.groupsReviewer = groupsReviewer;
        return this;
    }

    public List<String>  getGroupsUserAdmin() {
        return groupsUserAdmin;
    }

    public UserDto setGroupsUserAdmin(List<String> groupsUserAdmin) {
        this.groupsUserAdmin = groupsUserAdmin;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDto userDto = (UserDto) o;

        if (enabled != userDto.enabled) return false;
        if (id != null ? !id.equals(userDto.id) : userDto.id != null) return false;
        if (profile != null ? !profile.equals(userDto.profile) : userDto.profile != null) return false;
        if (username != null ? !username.equals(userDto.username) : userDto.username != null) return false;
        if (name != null ? !name.equals(userDto.name) : userDto.name != null) return false;
        if (surname != null ? !surname.equals(userDto.surname) : userDto.surname != null) return false;
        if (emailAddresses != null ? !emailAddresses.equals(userDto.emailAddresses) : userDto.emailAddresses != null)
            return false;
        if (organisation != null ? !organisation.equals(userDto.organisation) : userDto.organisation != null)
            return false;
        if (addresses != null ? !addresses.equals(userDto.addresses) : userDto.addresses != null) return false;
        if (kind != null ? !kind.equals(userDto.kind) : userDto.kind != null) return false;
        if (password != null ? !password.equals(userDto.password) : userDto.password != null) return false;
        if (groupsRegisteredUser != null ? !groupsRegisteredUser.equals(userDto.groupsRegisteredUser) : userDto.groupsRegisteredUser != null)
            return false;
        if (groupsEditor != null ? !groupsEditor.equals(userDto.groupsEditor) : userDto.groupsEditor != null)
            return false;
        if (groupsReviewer != null ? !groupsReviewer.equals(userDto.groupsReviewer) : userDto.groupsReviewer != null)
            return false;
        if (groupsUserAdmin != null ? !groupsUserAdmin.equals(userDto.groupsUserAdmin) : userDto.groupsUserAdmin != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (emailAddresses != null ? emailAddresses.hashCode() : 0);
        result = 31 * result + (organisation != null ? organisation.hashCode() : 0);
        result = 31 * result + (addresses != null ? addresses.hashCode() : 0);
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (groupsRegisteredUser != null ? groupsRegisteredUser.hashCode() : 0);
        result = 31 * result + (groupsEditor != null ? groupsEditor.hashCode() : 0);
        result = 31 * result + (groupsReviewer != null ? groupsReviewer.hashCode() : 0);
        result = 31 * result + (groupsUserAdmin != null ? groupsUserAdmin.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
