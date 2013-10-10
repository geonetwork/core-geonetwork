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
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.PasswordUtil;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.List;

/**
 * Update the information of a user.
 */
public class Update extends NotInReadOnlyModeService {
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

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		String operation = Util.getParam(params, Params.OPERATION);
		String id       = params.getChildText(Params.ID);
		String username = Util.getParam(params, Params.USERNAME);
		String password = Util.getParam(params, Params.PASSWORD);
		String surname  = Util.getParam(params, Params.SURNAME, "");
		String name     = Util.getParam(params, Params.NAME,    "");
		String profile  = Util.getParam(params, Params.PROFILE);
		String address  = Util.getParam(params, Params.ADDRESS, "");
		String city     = Util.getParam(params, Params.CITY,    "");
		String state    = Util.getParam(params, Params.STATE,   "");
		String zip      = Util.getParam(params, Params.ZIP,     "");
		String country  = Util.getParam(params, Params.COUNTRY, "");
		String email    = Util.getParam(params, Params.EMAIL,   "");
		String organ    = Util.getParam(params, Params.ORG,     "");
        String kind     = Util.getParam(params, Params.KIND, "");

        UserSession usrSess = context.getUserSession();
        Profile myProfile = usrSess.getProfile();
        String      myUserId  = usrSess.getUserId();

        @SuppressWarnings("unchecked")
        java.util.List<Element> userGroups = params.getChildren(Params.GROUPS);

        final UserGroupRepository groupRepository = context.getBean(UserGroupRepository.class);
        final UserRepository userRepository = context.getBean(UserRepository.class);

        if (!operation.equals(Params.Operation.RESETPW)) {
            if (!context.getProfileManager().exists(profile)) {
                throw new Exception("Unknown profile : "+ profile);
            }

            if (profile.equals(Profile.Administrator)) {
                userGroups = new ArrayList<Element>();
            }
        }

        if (myProfile == Profile.Administrator ||
				myProfile == Profile.UserAdmin ||
				myUserId.equals(id)) {

            // Before we do anything check (for UserAdmin) that they are not trying
            // to add a user to any group outside of their own - if they are then
            // raise an exception - this shouldn't happen unless someone has
            // constructed their own malicious URL!
            //
            if (operation.equals(Params.Operation.NEWUSER) || operation.equals(Params.Operation.EDITINFO)) {
                if (!(myUserId.equals(id)) && myProfile.equals("UserAdmin")) {
                    final List<Integer> groupIds = groupRepository.findGroupIds(UserGroupSpecs.hasUserId(Integer.valueOf(myUserId)));
					for(Element userGroup : userGroups) {
						String group = userGroup.getText();
						boolean found = false;
						for (int myGroup : groupIds) {
							if (Integer.valueOf(group) == myGroup) {
								found = true;
							}
						}
						if (!found) {
							throw new IllegalArgumentException("Tried to add group id "+group+" to user "+username+" - not allowed because you are not a member of that group!");	
						}
					}
				}
			}

            User user = new User()
                    .setUsername(username)
                    .setName(name)
                    .setSurname(surname)
                    .setProfile(Profile.findProfileIgnoreCase(profile))
                    .setKind(kind)
                    .setOrganisation(organ);
            user.getAddresses().add(new Address()
                    .setAddress(address)
                    .setCity(city)
                    .setState(state)
                    .setZip(zip)
                    .setCountry(country));
            user.getEmailAddresses().add(email);


            // -- For adding new user
            if (operation.equals(Params.Operation.NEWUSER)) {
                user.getSecurity().setPassword(password);
                final User userByUsername = userRepository.findOneByUsername(username);
                // check if the new username already exists - if so then don't do this
                if (userByUsername != null) {
                    throw new IllegalArgumentException("User with username "+username+" already exists");
                }


                user = userRepository.save(user);

				setUserGroups(user, profile, params, context);
			} else {

			// -- full update
				if (operation.equals(Params.Operation.FULLUPDATE)) {
                    user.getSecurity().setPassword(password);
                    user.setId(Integer.valueOf(id));
                    user = userRepository.save(user);

					//--- add groups
                    groupRepository.deleteAllByIdAttribute(UserGroupId_.userId, Arrays.asList(user.getId()));

					setUserGroups(user, profile, params, context);
				} else if (operation.equals(Params.Operation.EDITINFO)) {

                    user.setId(Integer.valueOf(id));
                    user = userRepository.save(user);
					//--- add groups

                    groupRepository.deleteAllByIdAttribute(UserGroupId_.userId, Arrays.asList(user.getId()));
					setUserGroups(user, profile, params, context);

			// -- reset password
				} else if (operation.equals(Params.Operation.RESETPW)) {
					ApplicationContext appContext = context.getApplicationContext();
					PasswordUtil.updatePasswordWithNew(false, null, password, Integer.valueOf(id), appContext);
				} else {
					throw new IllegalArgumentException("unknown user update operation " + operation);
				}
			} 
		} else {
			throw new IllegalArgumentException("You don't have rights to do this");
		}

		return new Element(Jeeves.Elem.RESPONSE);
	}

	private void setUserGroups(User user, String userProfile, Element params, ServiceContext context) throws Exception {
		String[] profiles = {Profile.UserAdmin.name(), Profile.Reviewer.name(), Profile.Editor.name(), Profile.RegisteredUser.name()};
        Collection<UserGroup> toAdd = new ArrayList<UserGroup>();

        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);

        for (String profile : profiles) {
		    
			@SuppressWarnings("unchecked")
            java.util.List<Element> userGroups = params.getChildren(Params.GROUPS + '_' + profile);
			for(Element element : userGroups) {
				String groupEl = element.getText();
				if (!groupEl.equals("")) {
					int groupId = Integer.valueOf(groupEl);
                    Group group = groupRepository.findOne(groupId);

                    // Combine all groups editor and reviewer groups
                    if (profile.equals(Profile.Reviewer.name())) {
                        final UserGroup userGroup = new UserGroup()
                                .setGroup(group)
                                .setProfile(Profile.Editor)
                                .setUser(user);
                        toAdd.add(userGroup);
					}

                    final UserGroup userGroup = new UserGroup()
                            .setGroup(group)
                            .setProfile(Profile.findProfileIgnoreCase(profile))
                            .setUser(user);
                    toAdd.add(userGroup);
				}
			}
		}

        userGroupRepository.save(toAdd);

	}
}