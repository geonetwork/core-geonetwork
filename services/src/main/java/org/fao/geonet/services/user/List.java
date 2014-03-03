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

import static org.fao.geonet.repository.specification.UserGroupSpecs.*;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.repository.*;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

//=============================================================================

/** Retrieves all users in the system
  */

public class List implements Service {
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		UserSession session = context.getUserSession();

		//--- retrieve groups for myself


		Set<Integer> hsMyGroups = getGroups(context, session.getUserIdAsInt(), session.getProfile());

        Set<String> profileSet = session.getProfile().getAllNames();

		//--- retrieve all users
        final java.util.List<User> all = context.getBean(UserRepository.class).findAll(SortUtils.createSort(User_.username));

		//--- now filter them

		java.util.Set<Integer> usersToRemove = new HashSet<Integer>();

		if (session.getProfile() != Profile.Administrator) {

			for (User user : all) {
				int userId = user.getId();
				Profile profile= user.getProfile();

                if (user.getId() == session.getUserIdAsInt()) {
                    // user is permitted to access his/her own user information
                    continue;
                }
				Set<Integer> userGroups = getGroups(context, userId, profile);
				// Is user belong to one of the current user admin group?
				boolean isInCurrentUserAdminGroups = false;
				for (Integer userGroup : userGroups) {
					if (hsMyGroups.contains(userGroup)) {
						isInCurrentUserAdminGroups = true;
						break;
					}
				}
				//if (!hsMyGroups.containsAll(userGroups))
				if (!isInCurrentUserAdminGroups) {
					usersToRemove.add(user.getId());
                }

				if (!profileSet.contains(profile.name())) {
					usersToRemove.add(user.getId());
                }
			}
		}

        Element rootEl = new Element(Jeeves.Elem.RESPONSE);

        for (User user : all) {
            if (!usersToRemove.contains(user.getId())) {
                rootEl.addContent(user.asXml());
            }
        }

        return rootEl;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private Set<Integer> getGroups(ServiceContext context, final int id, final Profile profile) throws Exception {
        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepo = context.getBean(UserGroupRepository.class);
        Set<Integer> hs = new HashSet<Integer>();

        if (profile == Profile.Administrator) {
            hs.addAll(groupRepository.findIds());
        } else if (profile == Profile.UserAdmin) {
            hs.addAll(userGroupRepo.findGroupIds(Specifications.where(hasProfile(profile)).and(hasUserId(id))));
        } else {
            hs.addAll(userGroupRepo.findGroupIds(hasUserId(id)));
        }

		return hs;
	}
}

//=============================================================================

