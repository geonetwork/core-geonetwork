//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.user;

import com.google.common.collect.Sets;
import jeeves.server.ServiceConfig;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.domain.responses.UserList;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;

//=============================================================================

/**
 * Retrieves all users in the system
 */
@Controller("admin.user.list")
public class List {
    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	@RequestMapping(value = "/{lang}/admin.user.list", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	UserList exec() throws Exception {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            throw new AuthenticationCredentialsNotFoundException("User needs to log in");
        }
        User me = userRepository.findOneByUsername(context.getAuthentication().getName());

        if (me == null) {
            throw new AccessDeniedException(SecurityContextHolder.class.getSimpleName() + " has a user that is not in the database: " +
                                            context.getAuthentication());
        }

		Set<Integer> hsMyGroups = getGroups(me.getId(), me.getProfile());

		Collection<? extends GrantedAuthority> roles = me.getAuthorities();

		Set<String> profileSet = Sets.newHashSet();

		for (GrantedAuthority rol : roles) {
			profileSet.add(rol.getAuthority());
		}

		// --- retrieve all users
		final java.util.List<User> all = userRepository.findAll(SortUtils
				.createSort(User_.username));

		// --- now filter them

		java.util.Set<Integer> usersToRemove = new HashSet<Integer>();

		if (!profileSet.contains(Profile.Administrator.name())) {

			for (User user : all) {
				int userId = user.getId();
				Profile profile = user.getProfile();

				// TODO is this already equivalent to ID?
				if (user.getUsername().equals(context.getAuthentication().getName())) {
					// user is permitted to access his/her own user information
					continue;
				}
				Set<Integer> userGroups = getGroups(userId, profile);
				// Is user belong to one of the current user admin group?
				boolean isInCurrentUserAdminGroups = false;
				for (Integer userGroup : userGroups) {
					if (hsMyGroups.contains(userGroup)) {
						isInCurrentUserAdminGroups = true;
						break;
					}
				}
				// if (!hsMyGroups.containsAll(userGroups))
				if (!isInCurrentUserAdminGroups) {
					usersToRemove.add(user.getId());
				}

				if (!profileSet.contains(profile.name())) {
					usersToRemove.add(user.getId());
				}
			}
		}
		UserList res = new UserList();

		for (User u : Collections.unmodifiableList(all)) {
			if (!usersToRemove.contains(u.getId())) {
				res.addUser(u);
			}
		}

		
		return res;
	}

    // --------------------------------------------------------------------------
    // ---
    // --- Private methods
    // ---
    // --------------------------------------------------------------------------

    private Set<Integer> getGroups(final int id, final Profile profile)
            throws Exception {
        Set<Integer> hs = new HashSet<Integer>();

        if (profile == Profile.Administrator) {
            hs.addAll(groupRepository.findIds());
        } else if (profile == Profile.UserAdmin) {
            hs.addAll(userGroupRepo.findGroupIds(Specifications.where(
                    hasProfile(profile)).and(hasUserId(id))));
        } else {
            hs.addAll(userGroupRepo.findGroupIds(hasUserId(id)));
        }

        return hs;
    }

    @Autowired
    private ConfigurableApplicationContext jeevesApplicationContext;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserGroupRepository userGroupRepo;
    @Autowired
    private UserRepository userRepository;

}

// =============================================================================

