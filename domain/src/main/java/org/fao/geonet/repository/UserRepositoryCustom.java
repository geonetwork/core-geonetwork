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

package org.fao.geonet.repository;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Custom (Non spring-data) Query methods for {@link User} entities.
 *
 * @author Jesse
 */
public interface UserRepositoryCustom {
    /**
     * Find the use with the given userid (where userid is a string).  The string will be converted
     * to an integer for making the query.
     *
     * @param userId the userid.
     * @return the use with the given userid
     */
    @Nullable
    User findOne(@Nonnull String userId);

    /**
     * Find all the users that are part of the ownerGroup of a particular set of metadata.
     *
     * @param metadataIds the metadataIds of the metadata to inspect.
     * @param profile     if non-null then filter the users by the given profile.
     * @param sort        if non-null then Sort the results by the <em>User</em> property.  The sort
     *                    object must contain user properties only.
     * @return all the users that are part of the ownerGroup of a particular set of metadata.
     */
    @Nonnull
    List<Pair<Integer, User>> findAllByGroupOwnerNameAndProfile(@Nonnull Collection<Integer> metadataIds,
                                                                @Nullable Profile profile, @Nullable Sort sort);

    /**
     * Find all the users that own at least one metadata element.
     *
     * @return all the users that own at least one metadata element.
     */
    @Nonnull
    List<User> findAllUsersThatOwnMetadata();

    /**
     * Find all the users are part of one of the {@link UserGroup}s selected by the specificatio.
     *
     * @param userGroupSpec a specification for selecting which {@link UserGroup}s the user has to
     *                      be part of.
     * @return all the users are part of one of the {@link UserGroup}s selected by the
     * specification.
     */
    @Nonnull
    List<User> findAllUsersInUserGroups(@Nonnull Specification<UserGroup> userGroupSpec);

    /**
     * Find a user by looking up their email address.
     *
     * @param email the email address to search for
     * @return a user if found or null if not.
     */
    @Nullable
    User findOneByEmail(@Nonnull String email);

    /**
     * Find the user identified by the email but also has a null authtype (if user exists but has a
     * nonnull authtype null is returned)
     *
     * @param email the email to use in the query.
     * @return the user identified by the username but also has a null authtype (if user exists but
     * has a nonnull authtype null is returned)
     */
    @Nullable
    public User findOneByEmailAndSecurityAuthTypeIsNullOrEmpty(@Nonnull String email);

    /**
     * Find the user identified by the username but also has a null authtype (if user exists but has
     * a nonnull authtype null is returned)
     *
     * @param username the username to use in the query.
     * @return the user identified by the username but also has a null authtype (if user exists but
     * has a nonnull authtype null is returned)
     */
    @Nullable
    public User findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(@Nonnull String username);

    /**
     * Find the all the usernames that using a case insensitive compare appear more than one time. For example,
     * if we have a list of users with usernames "usernNAME1", USerName1", "uSernaME2", uSERname2", "username3"
     * this method return a list with "username1" and "username2".
     * @return the list of usernames shared by more than one user (case insensitive.
     */
    @Nonnull
    public List<String> findDuplicatedUsernamesCaseInsensitive();
}
