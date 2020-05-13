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
import jeeves.server.UserSession;
import jeeves.server.sources.http.JeevesServlet;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Removes a user from the system. It removes the relationship to a group too.
 */

@Controller("admin.user.remove")
@ReadWriteController
@Deprecated
public class Remove {


    @RequestMapping(value = "/{portal}/{lang}/admin.user.remove", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    String run(HttpSession session,
               @RequestParam(value = Params.ID, required = false) String id)
        throws Exception {

        Profile myProfile = Profile.Guest;
        String myUserId = null;
        Object tmp = session
            .getAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY);
        if (tmp instanceof UserSession) {
            UserSession usrSess = (UserSession) tmp;
            myProfile = usrSess.getProfile();
            myUserId = usrSess.getUserId();
        }

        if (myUserId == null || myUserId.equals(id)) {
            throw new IllegalArgumentException(
                "You cannot delete yourself from the user database");
        }

        int iId = Integer.parseInt(id);

        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        UserGroupRepository userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);
        DataManager dataMan = ApplicationContextHolder.get().getBean(DataManager.class);

        if (myProfile == Profile.Administrator
            || myProfile == Profile.UserAdmin) {

            if (myProfile == Profile.UserAdmin) {
                final Integer iMyUserId = Integer.valueOf(myUserId);
                final List<Integer> groupIds = userGroupRepository
                    .findGroupIds(where(hasUserId(iMyUserId)).or(
                        hasUserId(iId)));
                if (groupIds.isEmpty()) {
                    throw new IllegalArgumentException(
                        "You don't have rights to delete this user because the user is not part of your group");
                }
            }

            // Before processing DELETE check that the user is not referenced
            // elsewhere in the GeoNetwork database - an exception is thrown if
            // this is the case
            if (dataMan.isUserMetadataOwner(iId)) {
                throw new IllegalArgumentException(
                    "Cannot delete a user that is also a metadata owner");
            }

            if (dataMan.isUserMetadataStatus(iId)) {
                throw new IllegalArgumentException(
                    "Cannot delete a user that has set a metadata status");
            }

            userGroupRepository.deleteAllByIdAttribute(UserGroupId_.userId,
                Arrays.asList(iId));
            userRepository.delete(iId);

        } else {
            throw new IllegalArgumentException(
                "You don't have rights to delete this user");
        }

        return "\"" + Jeeves.Elem.RESPONSE + "\"";
    }
}
