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

import jeeves.constants.Jeeves;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.exceptions.OperationNotAllowedEx;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;

import static org.springframework.data.jpa.domain.Specifications.*;

//=============================================================================

/**
 * Retrieves the groups for a particular user
 */
@Deprecated
public class UserGroups implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        String id = params.getChildText(Params.ID);

        if (id == null) return new Element(Jeeves.Elem.RESPONSE);

        UserSession usrSess = context.getUserSession();
        Profile myProfile = usrSess.getProfile();
        String myUserId = usrSess.getUserId();

        final UserRepository userRepository = context.getBean(UserRepository.class);
        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);

        if (myProfile == Profile.Administrator || myProfile == Profile.UserAdmin || myUserId.equals(id)) {

            // -- get the profile of the user id supplied
            User user = userRepository.findOne(Integer.valueOf(id));
            if (user == null) {
                throw new IllegalArgumentException("user " + id + " doesn't exist");
            }

            String theProfile = user.getProfile().name();

            //--- retrieve user groups of the user id supplied
            Element elGroups = new Element(Geonet.Elem.GROUPS);
            List<Group> theGroups;
            List<UserGroup> userGroups;

            if (myProfile == Profile.Administrator && theProfile.equals(Profile.Administrator.name())) {
                theGroups = groupRepository.findAll();

                for (Group group : theGroups) {
                    final Element element = group.asXml();
                    element.addContent(new Element("profile").setText(Profile.Administrator.name()));
                    elGroups.addContent(element);
                }
            } else {
                userGroups = userGroupRepository.findAll(UserGroupSpecs.hasUserId(Integer.valueOf(id)));

                for (UserGroup userGroup : userGroups) {
                    final Element element = userGroup.getGroup().asXml();
                    element.addContent(new Element("profile").setText(userGroup.getProfile().name()));
                    elGroups.addContent(element);
                }
            }

            if (!(myUserId.equals(id)) && myProfile == Profile.UserAdmin) {

                //--- retrieve session user groups and check to see whether this user is
                //--- allowed to get this info
                List<Integer> adminList = userGroupRepository.findGroupIds(where(UserGroupSpecs.hasUserId(Integer.valueOf(myUserId)))
                    .or(UserGroupSpecs.hasUserId(Integer.valueOf(id))));
                if (adminList.isEmpty()) {
                    throw new OperationNotAllowedEx("You don't have rights to do this because the user you want is not part of your group");
                }
            }

            //--- return data

            return elGroups;
        } else {
            throw new OperationNotAllowedEx("You don't have rights to do get the groups for this user");
        }

    }
}

//=============================================================================

