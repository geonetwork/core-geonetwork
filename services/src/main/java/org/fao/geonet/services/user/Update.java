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

import com.vividsolutions.jts.util.Assert;
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
		String id;
        if (operation.equalsIgnoreCase(Params.Operation.NEWUSER)) {
            id = Util.getParam(params, Params.ID, "");
        } else {
            id = Util.getParam(params, Params.ID);
        }
        String username = Util.getParam(params, Params.USERNAME, null);
        String password = Util.getParam(params, Params.PASSWORD, null);

        String surname  = Util.getParam(params, Params.SURNAME, null);
		String name     = Util.getParam(params, Params.NAME,    null);
		Profile profile = Profile.findProfileIgnoreCase(Util.getParam(params, Params.PROFILE, null));
		String address  = Util.getParam(params, Params.ADDRESS, null);
		String city     = Util.getParam(params, Params.CITY,    null);
		String state    = Util.getParam(params, Params.STATE,   null);
		String zip      = Util.getParam(params, Params.ZIP,     null);
		String country  = Util.getParam(params, Params.COUNTRY, null);
		String email    = Util.getParam(params, Params.EMAIL,   null);
		String organ    = Util.getParam(params, Params.ORG,     null);
        String kind     = Util.getParam(params, Params.KIND, null);

        UserSession usrSess = context.getUserSession();
        Profile myProfile = usrSess.getProfile();
        String      myUserId  = usrSess.getUserId();

        final UserGroupRepository groupRepository = context.getBean(UserGroupRepository.class);
        final UserRepository userRepository = context.getBean(UserRepository.class);
        @SuppressWarnings("unchecked")
        java.util.List<Element> userGroups = params.getChildren(Params.GROUPS);

        if (profile == Profile.Administrator) {
            userGroups = new ArrayList<Element>();
        }

        if (myProfile == Profile.Administrator ||
				myProfile == Profile.UserAdmin ||
				myUserId.equals(id)) {
            checkAccessRights(operation, id, username, myProfile, myUserId, userGroups, groupRepository);


            User user = getUser(userRepository, operation, id, username);
            if (username != null) {
                user.setUsername(username);
            }

            if (name != null) {
                user.setName(name);
            }
            if (surname != null) {
                user.setSurname(surname);
            }

            if (profile != null) {
                if (!myProfile.getAll().contains(profile)) {
                    throw new IllegalArgumentException("Trying to set profile to "+profile+" max profile permitted is: "+myProfile);
                }
                user.setProfile(profile);
            }
            if (kind != null) {
                user.setKind(kind);
            }
            if (organ != null) {
                user.setOrganisation(organ);
            }

            Address addressEntity;
            if (user.getAddresses().isEmpty()) {
                addressEntity = new Address();
            } else {
                addressEntity = user.getAddresses().iterator().next();

            }
            if (address != null) {
                addressEntity.setAddress(address);
            }
            if (city != null) {
                addressEntity.setCity(city);
            }
            if (state != null) {
                addressEntity.setState(state);
            }
            if (zip != null) {
                addressEntity.setZip(zip);
            }
            if (country != null) {
                addressEntity.setCountry(country);
            }

            user.getAddresses().add(addressEntity);
            if (email != null) {
                user.getEmailAddresses().add(email);
            }


            if (password != null) {
                user.getSecurity().setPassword(PasswordUtil.encode(context, password));
            } else if (operation.equals(Params.Operation.RESETPW)) {
                throw new IllegalArgumentException("password is a required parameter for operation: " + Params.Operation.RESETPW);
            }

            // -- For adding new user
            if (operation.equals(Params.Operation.NEWUSER)) {
                user = userRepository.save(user);

				setUserGroups(user, params, context);
			} else if (operation.equals(Params.Operation.FULLUPDATE) || operation.equals(Params.Operation.EDITINFO)) {
                user = userRepository.save(user);

                //--- add groups
                groupRepository.deleteAllByIdAttribute(UserGroupId_.userId, Arrays.asList(user.getId()));

                setUserGroups(user, params, context);
			} else if (operation.equals(Params.Operation.RESETPW)) {
             user = userRepository.save(user);
			} else {
                throw new IllegalArgumentException("unknown user update operation " + operation);
            }
		} else {
			throw new IllegalArgumentException("You don't have rights to do this");
		}

		return new Element(Jeeves.Elem.RESPONSE);
	}

    private User getUser(final UserRepository repo, final String operation, final String id, final String username) {
        if (Params.Operation.NEWUSER.equalsIgnoreCase(operation)) {
            if (username == null) {
                throw new IllegalArgumentException(Params.USERNAME + " is a required parameter for " + Params.Operation.NEWUSER + " " +
                                                   "operation");
            }
            User user = repo.findOneByUsername(username);

            if (user != null) {
                throw new IllegalArgumentException("User with username " + username + " already exists");
            }
            return new User();
        } else {
            User user = repo.findOne(id);
            if (user == null) {
                throw new IllegalArgumentException("No user found with id: " + id);
            }
            return user;
        }
    }

    private void checkAccessRights(final String operation, final String id, final String username, final Profile myProfile,
                                   final String myUserId, final List<Element> userGroups, final UserGroupRepository groupRepository) {
        // Before we do anything check (for UserAdmin) that they are not trying
        // to add a user to any group outside of their own - if they are then
        // raise an exception - this shouldn't happen unless someone has
        // constructed their own malicious URL!
        //
        if (operation.equals(Params.Operation.NEWUSER) || operation.equals(Params.Operation.EDITINFO) ||
            operation.equals(Params.Operation.FULLUPDATE)) {
            if (!(myUserId.equals(id)) && myProfile == Profile.UserAdmin) {
                final List<Integer> groupIds = groupRepository.findGroupIds(UserGroupSpecs.hasUserId(Integer.valueOf(myUserId)));
                for (Element userGroup : userGroups) {
                    String group = userGroup.getText();
                    boolean found = false;
                    for (int myGroup : groupIds) {
                        if (Integer.valueOf(group) == myGroup) {
                            found = true;
                        }
                    }
                    if (!found) {
                        throw new IllegalArgumentException("Tried to add group id " + group + " to user " + username + " - not allowed " +
                                                           "because you are not a member of that group!");
                    }
                }
            }
        }
    }

    private void setUserGroups(final User user, final Element params, final ServiceContext context) throws Exception {
		String[] profiles = {Profile.UserAdmin.name(), Profile.Reviewer.name(), Profile.Editor.name(), Profile.RegisteredUser.name()};
        Collection<UserGroup> toAdd = new ArrayList<UserGroup>();

        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);

        for (String profile : profiles) {
		    
			@SuppressWarnings("unchecked")
            java.util.List<Element> userGroups = params.getChildren(Params.GROUPS + '_' + profile);
			for (Element element : userGroups) {
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