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

package org.fao.geonet.services.login;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.sql.SQLException;
import java.util.Map;

import org.fao.geonet.exceptions.UserLoginEx;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

//=============================================================================

/**
 * <code>ShibLogin</code> processes the result of a Shibboleth (or other external authentication system) login. The user will have already
 * been challenged for userid and password and will have had their credentials placed in the HTTP headers. These are then used to find or
 * create the user's account.
 * 
 * @author James Dempsey <James.Dempsey@csiro.au>
 * @version $Revision: 1629 $
 */
public class ShibLogin extends NotInReadOnlyModeService {
    private static final String VIA_SHIBBOLETH = "Via Shibboleth";
    private static final String SHIBBOLETH_FLAG = "SHIBBOLETH";

    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see jeeves.interfaces.Service#exec(org.jdom.Element, jeeves.server.context.ServiceContext)
     */
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        // Get the header keys to lookup from the settings
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);
        String prefix = "system/shib";
        String usernameKey = sm.getValue(prefix + "/attrib/username");
        String surnameKey = sm.getValue(prefix + "/attrib/surname");
        String firstnameKey = sm.getValue(prefix + "/attrib/firstname");
        String profileKey = sm.getValue(prefix + "/attrib/profile");
        String groupKey = sm.getValue(prefix + "/attrib/group");
        String defGroup = sm.getValue(prefix + "/defaultGroup");

        // Read in the data from the headers
        Map<String, String> headers = context.getHeaders();
        String username = Util.getHeader(headers, usernameKey, "");
        String surname = Util.getHeader(headers, surnameKey, "");
        String firstname = Util.getHeader(headers, firstnameKey, "");
        String profileName = Util.getHeader(headers, profileKey, "");
        String group = Util.getHeader(headers, groupKey, "");

        // Make sure the profile name is an exact match
        Profile profile = context.getProfileManager().getCorrectCase(profileName);
        if (profile == null) {
            profile = Profile.Guest;
        }

        if (group.equals("")) {
            group = defGroup;
        }

        UserRepository userRepository = context.getBean(UserRepository.class);

        // Create or update the user
        if (username != null && username.length() > 0) {
            User user = updateUser(context, userRepository, username, surname, firstname, profile, group);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                    user.getAuthorities());
            authentication.setDetails(user);

            if (SecurityContextHolder.getContext() == null) {
                SecurityContextHolder.createEmptyContext();
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);

            context.info("User '" + username + "' logged in as '" + user.getProfile() + "'");

            return new Element("ok");
        } else {
            throw new UserLoginEx(username);
        }

    }

    // --------------------------------------------------------------------------

    /**
     * Update the user to match the provided details, or create a new record for them if they don't have one already.
     * 
     * @param context The Jeeves ServiceContext
     * @param userRepository The user repository.
     * @param username The user's username, must not be null.
     * @param surname The surname of the user
     * @param firstname The first name of the user.
     * @param profile The name of the user type.
     * @throws java.sql.SQLException If the record cannot be saved.
     */
    private User updateUser(ServiceContext context, UserRepository userRepository, String username, String surname, String firstname,
            Profile profile, String groupName) throws SQLException {

        boolean groupProvided = ((groupName != null) && (!(groupName.equals(""))));
        int groupId = -1;
        int userId = -1;

        if (groupProvided) {
            GroupRepository groupRepo = context.getBean(GroupRepository.class);
            Group group = groupRepo.findByName(groupName);

            if (group == null) {
                group = groupRepo.save(new Group().setName(groupName));
            }
            groupId = group.getId();
        }
        // --- update user information into the database
        if (username.length() > 256) // only accept the first 256 chars
        {
            username = username.substring(0, 256);
        }
        User user = userRepository.findOneByUsername(username);

        if (user == null) {
            user = new User().setUsername(username);
        }

        user.setName(firstname).setSurname(surname).setProfile(profile).getSecurity().setPassword(VIA_SHIBBOLETH.toCharArray())
                .setAuthType(SHIBBOLETH_FLAG);

        userRepository.save(user);

        if (groupProvided) {
            UserGroupRepository userGroupRepo = context.getBean(UserGroupRepository.class);

            long count = userGroupRepo.count(where(hasGroupId(groupId)).and(hasUserId(userId)));

            if (count == 0) {
                UserGroup userGroup = new UserGroup();
                userGroup.getId().setGroupId(groupId).setUserId(userId);
                userGroupRepo.save(userGroup);
            }
        }

        return user;
    }

}
