/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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
package org.fao.geonet.api.selections;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserMetadataSelectionList;
import org.fao.geonet.repository.UserMetadataSelectionListRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@RequestMapping(value = {
    "/{portal}/api/userselection"
})
@Tag(name = "userselection",
    description = "User selections related operations")
@Controller("userselection")
public class UserSelectionApi {

    @Autowired
    UserMetadataSelectionListRepository userMetadataSelectionListRepository;

    @Autowired
    UserRepository userRepository;


    /**
     * This will retrieve a list of UserMetadataSelectionList/
     * included;
     *      + all public lists (from other users or other sessions)
     *      + all private lists for the user
     *      + all private lists for the session
     *
     * not included;
     *     + private lists for other users
     *     + private lists for other sessions
     *
     * @param httpSession
     * @return
     * @throws Exception
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Get list of user selection sets")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<UserMetadataSelectionList> getSelectionLists(
        @Parameter(hidden = true)
        HttpSession httpSession
    )
        throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        String userId = session.getUserId(); // null = not logged on
        User user = null;
        if (userId != null) {
            user = userRepository.findOne(userId);
        }
        String sessionId = session.getsHttpSession().getId();
        return userMetadataSelectionListRepository.findByUserOrSessionOrPublic(user,sessionId);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get a specific (by DB id) of selection list")
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/{selectionListIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    UserMetadataSelectionList getSelectionLists(
        @Parameter(description = "Selection DB id (int)",required = true)
        @PathVariable
        Integer selectionListIdentifier,
        @Parameter(hidden = true)
        HttpSession httpSession
    )
        throws Exception {

        if (selectionListIdentifier == null) {
            throw new IllegalArgumentException("no selectionListIdentifier given");
        }
        if (selectionListIdentifier <=0) {
            throw new IllegalArgumentException("invalid selectionListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(httpSession);
        User user =  getUser(httpSession);

        Optional<UserMetadataSelectionList> list = userMetadataSelectionListRepository.findById(selectionListIdentifier);
        if (!list.isPresent()) {
            throw new ResourceNotFoundException("cannot find selectionListIdentifier, based on id"+selectionListIdentifier);
        }
        UserMetadataSelectionList result = list.get();

        if (permittedRead(result,user,sessionId,isAdmin)) {
            return result;
        }

        throw new NotAllowedException("you don't have permission to read that user selection list");
    }

    boolean isAdmin(HttpSession httpSession) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        if (session.getProfile() == null) {
            return false;
        }
        return session.getProfile().equals(Profile.Administrator);
    }

    boolean permittedRead(UserMetadataSelectionList list, User user, String sessionId, boolean isAdmin) {
        // admin can always read
        if (isAdmin) {
            return true;
        }
        //owned by same user
        if ( (user != null) && (list.getUser() !=null) && (user.equals(list.getUser()))) {
            return true;
        }
        //owned by same session
        if ( (sessionId != null) && (list.getSessionId() !=null) && (sessionId.equals(list.getSessionId()))) {
            return true;
        }
        //public
        if (list.getIsPublic()) {
            return true;
        }
        //otherwise its private and owned by someone else
        return false;
    }

    String getSessionId(HttpSession httpSession) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        String sessionId = session.getsHttpSession().getId();
        return sessionId;
    }

    User getUser(HttpSession httpSession) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        String userId = session.getUserId(); // null = not logged on
        User user = null;
        if (userId != null) {
            user = userRepository.findOne(userId);
        }
        return user;
    }

}
