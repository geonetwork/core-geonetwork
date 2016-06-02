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

    public Set<Profile> getParents() {
        HashSet<Profile> parents = new HashSet<Profile>();
        for (Profile profile : values()) {
            if (profile.parents.contains(this)) {
                parents.add(profile);
            }
        }

        return parents;
    }

    public Set<Profile> getAll() {
        HashSet<Profile> all = new HashSet<Profile>();
        all.add(this);
        for (Profile parent : getParents()) {
            all.addAll(parent.getAll());
        }

        return all;
    }

    public Element asElement() {
        Element elResult = new Element(PROFILES_ELEM_NAME);

        for (Profile profile : getAll()) {
            if (profile == Guest)
                continue;

            elResult.addContent(new Element(profile.name()));
        }

        return elResult;
    }

    public Set<String> getAllNames() {
        HashSet<String> names = new HashSet<String>();
        for (Profile p : getAll()) {
            names.add(p.name());
        }
        return names;
    }
}
