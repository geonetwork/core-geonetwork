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
