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

import java.util.ArrayList;
import java.util.List;

/**
 * Current user info
 */
public class MeResponse {

    private String id;
    private String profile;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String hash;
    private String organisation;
    private boolean admin;

    // Each list is associated with a profile and contains the groups for which the user is enabled with that profile
    private List<Integer> groupsWithRegisteredUser = new ArrayList<>();
    private List<Integer> groupsWithEditor = new ArrayList<>();
    private List<Integer> groupsWithReviewer = new ArrayList<>();
    private List<Integer> groupsWithUserAdmin = new ArrayList<>();

    public MeResponse() {
    }

    public String getId() {
        return id;
    }

    public MeResponse setId(final String id) {
        this.id = id;
        return this;
    }

    public String getProfile() {
        return profile;
    }

    public MeResponse setProfile(final String profile) {
        this.profile = profile;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MeResponse setUsername(final String username) {
        this.username = username;
        return this;
    }

    public String getName() {
        return name;
    }

    public MeResponse setName(final String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public MeResponse setSurname(final String surname) {
        this.surname = surname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public MeResponse setEmail(final String email) {
        this.email = email;
        this.hash = email != null ? org.apache.commons.codec.digest.DigestUtils.md5Hex(email) : "";
        return this;
    }

    public String getHash() {
        return hash;
    }

    public MeResponse setHash(final String hash) {
        this.hash = hash;
        return this;
    }

    public String getOrganisation() {
        return organisation;
    }

    public MeResponse setOrganisation(final String organisation) {
        this.organisation = organisation;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public MeResponse setAdmin(final boolean admin) {
        this.admin = admin;
        return this;
    }

    public List<Integer> getGroupsWithRegisteredUser() {
        return groupsWithRegisteredUser;
    }

    public MeResponse setGroupsWithRegisteredUser(List<Integer> groupsWithRegisteredUser) {
        this.groupsWithRegisteredUser = groupsWithRegisteredUser;
        return this;
    }

    public List<Integer> getGroupsWithEditor() {
        return groupsWithEditor;
    }

    public MeResponse setGroupsWithEditor(List<Integer> groupsWithEditor) {
        this.groupsWithEditor = groupsWithEditor;
        return this;
    }

    public List<Integer> getGroupsWithReviewer() {
        return groupsWithReviewer;
    }

    public MeResponse setGroupsWithReviewer(List<Integer> groupsWithReviewer) {
        this.groupsWithReviewer = groupsWithReviewer;
        return this;
    }

    public List<Integer> getGroupsWithUserAdmin() {
        return groupsWithUserAdmin;
    }

    public MeResponse setGroupsWithUserAdmin(List<Integer> groupsWithUserAdmin) {
        this.groupsWithUserAdmin = groupsWithUserAdmin;
        return this;
    }

}
