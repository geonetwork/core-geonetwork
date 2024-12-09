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

package org.fao.geonet.domain;

import org.jdom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The enumeration of profiles available in geonetwork.
 *
 * @author Jesse
 */
public enum Profile {
    Administrator, UserAdmin(Administrator), Reviewer(UserAdmin), Editor(Reviewer), RegisteredUser(Editor), Guest(RegisteredUser),
    Monitor(Administrator);

    public static final String PROFILES_ELEM_NAME = "profiles";
    private final Set<Profile> parents;

    private Profile(Profile... parents) {
        this.parents = new HashSet<Profile>(Arrays.asList(parents));
    }

    /**
     * A case-sensitive search for profile
     *
     * @param profile the name of the profile to check.
     */
    public static boolean exists(String profile) {
        try {
            Profile.valueOf(profile);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Find the profile by name but ignore case errors.
     *
     * @param profileName The profile name.
     */
    public static Profile findProfileIgnoreCase(String profileName) {
        for (Profile actualProfile : Profile.values()) {
            if (actualProfile.name().equalsIgnoreCase(profileName)) {
                return actualProfile;
            }
        }
        return null;
    }

    /**
     * Retrieves all direct child profiles of the current profile.
     * Child profiles have fewer permissions than parents.
     *
     * @return A set containing profiles that have this profile as a parent.
     */
    public Set<Profile> getChildren() {
        HashSet<Profile> children = new HashSet<Profile>();
        for (Profile profile : values()) {
            if (profile.parents.contains(this)) {
                children.add(profile);
            }
        }

        return children;
    }

    /**
     * Retrieves the direct parent profiles of the current profile.
     * Parent profiles have more permissions than children.
     *
     * @return A set of profiles that are direct parents of this profile.
     */
    public Set<Profile> getParents() {
        return parents;
    }

    /**
     * Retrieves the profile and all of its children recursively.
     * The returned set will include the profile itself.
     * Child profiles have fewer permissions than parents.
     *
     * @return A {@link Set<Profile>} containing the profile and all of its children.
     */
    public Set<Profile> getProfileAndAllChildren() {
        HashSet<Profile> profiles = new HashSet<Profile>();
        profiles.add(this);
        for (Profile child : getChildren()) {
            profiles.addAll(child.getProfileAndAllChildren());
        }

        return profiles;
    }

    /**
     * Retrieves the profile and all of its parents recursively.
     * The returned set will include the profile itself.
     * Parent profiles have more permissions than children.
     *
     * @return A {@link Set<Profile>} containing the profile and all of its parents.
     */
    public Set<Profile> getProfileAndAllParents() {
        Set<Profile> profiles = new HashSet<>();
        profiles.add(this);
        for (Profile parent : getParents()) {
            profiles.addAll(parent.getProfileAndAllParents());
        }
        return profiles;
    }

    public Element asElement() {
        Element elResult = new Element(PROFILES_ELEM_NAME);

        for (Profile profile : getProfileAndAllChildren()) {
            if (profile == Guest)
                continue;

            elResult.addContent(new Element(profile.name()));
        }

        return elResult;
    }

    public Set<String> getAllNames() {
        HashSet<String> names = new HashSet<String>();
        for (Profile p : getProfileAndAllChildren()) {
            names.add(p.name());
        }
        return names;
    }
}
