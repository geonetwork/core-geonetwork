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
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserMetadataSelection;
import org.fao.geonet.domain.UserMetadataSelectionList;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserMetadataSelectionListRepository;
import org.fao.geonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The API for userselection.
 *
 *    1. get all viewable by user (getSelectionLists)
 *        This returns a list of UserMetadataSelectionLists that are visible.
 *        1. If logged in, then all lists "owned" by that user
 *        2. If not logged in, then all list "owned" by that session
 *        3. Any lists (user or session) that are "public"
 *
 *    2. Get UserMetadataSelectionList by id (getSelectionList)
 *        Gets a single UserMetadataSelectionList by ID.  This must be visible;
 *           1. If logged in, "owned" by that user
 *           2. If not logged in, then "owned" by that session
 *           3. list must be "public"
 *
 *    3. Modify a list (name or metadataUuids) - updateSelectionList
 *        The list must be owned by the user (either the same sessionId or same User).
 *        If there is a new "name" given, the list's name will be updated (cannot conflict with other named lists that the user/session owns).
 *        The set of MetadataUuids can be (ActionType) added to, removed from, or replaced.
 *
 *    4. Change status (public/private) - updateStatus
 *       The list must be owned by the user (either the same sessionId or same User).
 *       The isPublic property can be set.  Making a list public makes it visible to everyone (Anonymous and all users).
 *
 *    5. Delete a list - deleteItem
 *       The list must be owned by the user (either the same sessionId or same User).
 *       The list is deleted.
 *
 *    6. Create a list - createNewSelectionList
 *       Creates a new list with the given name and set of metadatauuids.
 *       List is owned by the user/session.
 *       Name must be unique for the user/session.
 *
 *   Note - the administrator can see all items.
 *   Note - all uuids in a list must exist in the database.
 */
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

    @Autowired
    MetadataRepository metadataRepository;

    enum ActionType {add, replace, remove}

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


    /**
     *  Get a UserMetadataSelectionList by id.
     */
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
    UserMetadataSelectionList getSelectionList(
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

    /**
     *  update a UserMetadataSelectionList
     *  user or session must own the UserMetadataSelectionList.
     *  change name - name must be unique for the user/session.
     *
     *  update the set of metadataUuuids (add/remove/replace)
     *
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Update a specific (by DB id) of selection list")
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/{selectionListIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    UserMetadataSelectionList updateSelectionList(
        @Parameter(description = "Selection DB id (int)",required = true)
        @PathVariable
            Integer selectionListIdentifier,

        @Parameter(description = "new name of list",required = false)
        @RequestParam(name="name",required = false)
            String name,

        @Parameter(description = "action - add, replace, or remove",required = true)
        @RequestParam(name="action",required = true)
            ActionType action,

        @Parameter(description = "List of metadata uuids to replace in the list",required = false)
        @RequestParam(name="metadataUuids",required = false)
            String[] metadataUuids,

        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {

        if (selectionListIdentifier == null) {
            throw new IllegalArgumentException("no selectionListIdentifier given");
        }
        if (selectionListIdentifier <= 0) {
            throw new IllegalArgumentException("invalid selectionListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(httpSession);
        User user = getUser(httpSession);

        Optional<UserMetadataSelectionList> list1 = userMetadataSelectionListRepository.findById(selectionListIdentifier);
        if (!list1.isPresent()) {
            throw new ResourceNotFoundException("cannot find selectionListIdentifier, based on id" + selectionListIdentifier);
        }
        UserMetadataSelectionList list = list1.get();

        if (!permittedWrite(list,user,sessionId,isAdmin)) {
            throw new NotAllowedException("You do not have permission to modify this list");
        }

        if (metadataUuids !=null) {
            //no duplicates
            metadataUuids =  Arrays.stream(metadataUuids).distinct().toArray(String[]::new);
        }
        else {
            metadataUuids = new String[0];
        }

        //update name
        if (StringUtils.hasText(name) && !name.equals(list.getName())) {
            //is this valid?
            UserMetadataSelectionList item = userMetadataSelectionListRepository.findByNameAndUserOrSessionId(name,user,sessionId);
            if (item != null) {
                throw new IllegalArgumentException("name '"+name+"' is already in use!");
            }
            list.setName(name.trim());
        }

        if (action == ActionType.replace) {
            list.getSelections().clear();
        }

        if (action==ActionType.replace || action==ActionType.add) {
            //add metadataUuids
            for(String uuid : metadataUuids) {
                Metadata md = metadataRepository.findOneByUuid(uuid);
                if (md == null) {
                    throw new IllegalArgumentException("metadataUuids: "+uuid+" is not in DB");
                }
                boolean alreadyInList = list.getSelections().stream()
                    .anyMatch(x->x.getMetadataUuid().equals(uuid));
                if (!alreadyInList) {
                    UserMetadataSelection selection = new UserMetadataSelection();
                    selection.setMetadataUuid(uuid);
                    list.getSelections().add(selection);
                }
            }
        }
        else {
            //action = remove
            for(String uuid : metadataUuids) {
                Optional<UserMetadataSelection> existing = list.getSelections().stream()
                    .filter(x->x.getMetadataUuid().equals(uuid))
                    .findFirst();
                if (existing.isPresent()) {
                    list.getSelections().remove(existing.get());
                }

            }
        }
        //save
        list = userMetadataSelectionListRepository.save(list);
        return list;
    }


    /**
     *  Change the UserMetadataSelectionList's IsPublic Attribute.
     *  User or session must own the UserMetadataSelectionList.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Update a specific (by DB id) List's IsPublic attribute")
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/{selectionListIdentifier}/status",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    UserMetadataSelectionList updateStatus(
        @Parameter(description = "Selection DB id (int)",required = true)
        @PathVariable
            Integer selectionListIdentifier,

        @Parameter(description = "new isPublic status",required = false)
        @RequestParam(name="public",required = true)
            boolean isPublic,

        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {

        if (selectionListIdentifier == null) {
            throw new IllegalArgumentException("no selectionListIdentifier given");
        }
        if (selectionListIdentifier <= 0) {
            throw new IllegalArgumentException("invalid selectionListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(httpSession);
        User user = getUser(httpSession);

        Optional<UserMetadataSelectionList> list1 = userMetadataSelectionListRepository.findById(selectionListIdentifier);
        if (!list1.isPresent()) {
            throw new ResourceNotFoundException("cannot find selectionListIdentifier, based on id" + selectionListIdentifier);
        }
        UserMetadataSelectionList list = list1.get();

        if (!permittedWrite(list, user, sessionId, isAdmin)) {
            throw new NotAllowedException("You do not have permission to modify this list");
        }
        list.setIsPublic(isPublic);
        list = userMetadataSelectionListRepository.save(list);
        return list;
    }

    /**
     * Delete (by id) a UserMetadataSelectionList.
     * User or Session must own the UserMetadataSelectionList.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a specific (by DB id) List")
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/{selectionListIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    boolean deleteItem(
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
        if (selectionListIdentifier <= 0) {
            throw new IllegalArgumentException("invalid selectionListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(httpSession);
        User user = getUser(httpSession);

        Optional<UserMetadataSelectionList> list1 = userMetadataSelectionListRepository.findById(selectionListIdentifier);
        if (!list1.isPresent()) {
            throw new ResourceNotFoundException("cannot find selectionListIdentifier, based on id" + selectionListIdentifier);
        }
        UserMetadataSelectionList list = list1.get();

        if (!permittedWrite(list, user, sessionId, isAdmin)) {
            throw new NotAllowedException("You do not have permission to modify this list");
        }
        userMetadataSelectionListRepository.delete(list);
        return true;
    }


    /**
     * A user selection is created, for logged users are assigned to the user and reused between sessions.
     *
     * For unlogged users, are assigned to the user session.
     *
     * Create as private.
     *
     * example:
     * curl -X POST "http://localhost:8080/geonetwork/srv/api/userselection"
     *     -H "accept: application/json"
     *     -H "X-XSRF-TOKEN: ccc"
     *     -H $'Cookie: XSRF-TOKEN=ccc'
     *     -d "name=dave&metadataUuids=1&metadataUuids=2&listType=WatchList"
     *
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new user selection")
    @RequestMapping(
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    UserMetadataSelectionList createNewSelectionList(
        @Parameter(description = "Name of the list to be created",required = true)
        @RequestParam(name="name",required = true)
        String name,

        @Parameter(description = "List of metadata to add to the selection",required = false)
        @RequestParam(name="metadataUuids",required = false)
        String[] metadataUuids,

        @Parameter(description = "Type of list to create (Watch or Preferred)",required = true)
        @RequestParam(name="listType",required = true)
        UserMetadataSelectionList.ListType listType,

        @Parameter(hidden = true)
        HttpSession httpSession
    )
        throws Exception {
            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("no name given");
            }
            name = name.trim();

            User user = getUser(httpSession);
            String sessionId = getSessionId(httpSession);
            UserMetadataSelectionList alreadyExistsList = userMetadataSelectionListRepository.findByNameAndUserOrSessionId(name,user,sessionId);
            if (alreadyExistsList != null) {
                throw new IllegalArgumentException("There is already a list of that name");
            }
            UserMetadataSelectionList list = new UserMetadataSelectionList();
            list.setName(name);
            list.setIsPublic(false);
            list.setListType(listType);
            list.setCreateDate(new ISODate());
            list.setChangeDate(list.getCreateDate());
            if (user != null) {
                //user-based
                list.setUser(user);
            }
            else {
                //session-based
                list.setSessionId(sessionId);
            }
            if ( (metadataUuids!=null) && (metadataUuids.length >0)) {
                //set metadataUuids
                //don't add duplicates
                metadataUuids =   Arrays.stream(metadataUuids).distinct().toArray(String[]::new);
                list.setSelections(new ArrayList<>());
                for(String uuid : metadataUuids) {
                    Metadata md = metadataRepository.findOneByUuid(uuid);
                    if (md == null) {
                        throw new IllegalArgumentException("metadataUuids: "+uuid+" is not in DB");
                    }
                    UserMetadataSelection selection = new UserMetadataSelection();
                    selection.setMetadataUuid(uuid);
                    list.getSelections().add(selection);
                }
            }
            //save
            list = userMetadataSelectionListRepository.save(list);
            return list;
    }


    //-----------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------

    /**
     * returns true if the session represents an admin
     */
    boolean isAdmin(HttpSession httpSession) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        return Profile.Administrator.equals(session.getProfile());
    }

    /**
     * @return true if the user/session is permitted to read the list
     */
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

    /**
     * @return true if the user/session owns the list.
     */
    boolean permittedWrite(UserMetadataSelectionList list, User user, String sessionId, boolean isAdmin) {
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
        //note - don't allow public ones to be edited by anyone????
        //otherwise its private and owned by someone else
        return false;
    }


    /**
     * gets the sessionId in the httpSession
     */
    String getSessionId(HttpSession httpSession) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        String sessionId = session.getsHttpSession().getId();
        return sessionId;
    }

    /**
     * Gets the user in the httpSession
     */
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
