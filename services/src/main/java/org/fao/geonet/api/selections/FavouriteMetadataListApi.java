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
import org.fao.geonet.domain.FavouriteMetadataList;
import org.fao.geonet.domain.FavouriteMetadataListItem;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.FavouriteMetadataListRepository;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The API for FavouriteMaetadataList.
 *
 * session:
 *    a. usually null
 *    b. when you create a list, you are given a cookie with your session ID
 *    c. if you are logged in, then your created lists are owned by a user
 *    d. if you are not logged in, then your created lists are owned by the session cookie.
 *
 *
 *    1. get all viewable by user (getFavouritesLists)
 *        This returns a list of FavouriteMetadataListVM that are visible.
 *        1. If logged in, then all lists "owned" by that user
 *        2. If not logged in, then all list "owned" by that session
 *        3. Any lists (user or session) that are "public"
 *
 *    2. Get FavouriteMetadataListVM by id (getFavouritesList)
 *        Gets a single FavouriteMetadataListVM by ID.  This must be visible;
 *           1. If logged in, "owned" by that user
 *           2. If not logged in, then "owned" by that session
 *           3. list must be "public"
 *
 *    3. Modify a list (name or metadataUuids) - updateFavourtiesList
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
 *    6. Create a list - createNewFavourtiesList
 *       Creates a new list with the given name and set of metadatauuids.
 *       List is owned by the user/session.
 *       Name must be unique for the user/session.
 *
 *   Note - the administrator can see all items.
 *   Note - all uuids in a list must exist in the database.
 */
@RequestMapping(value = {
    "/{portal}/api/favouriteslist"
})
@Tag(name = "favouriteslist",
    description = "Favourites List related operations")
@Controller("favouriteslist")
public class FavouriteMetadataListApi {

    @Autowired
    FavouriteMetadataListRepository favouriteMetadataListRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MetadataRepository metadataRepository;

    public static String SESSION_COOKIE_NAME="not-logged-in-FavouritesList-sessionid";

    enum ActionType {add, replace, remove}

    /**
     * This will retrieve a list of FavouriteMetadataListVM
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get list of FavouritesLists accessible to the user/session")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<FavouriteMetadataListVM> getFavouritesLists(
        @Parameter(hidden = true)
        HttpSession httpSession,

        @Parameter(hidden = true)
            HttpServletRequest  httpServletRequest
    )
        throws Exception {
        String sessionId = getSessionId(httpServletRequest);
        User user =  getUser(httpSession);
        boolean isAdmin = isAdmin(httpSession);
        return favouriteMetadataListRepository.findPublic(user,sessionId)
            .stream()
            .map(x->new FavouriteMetadataListVM(x
                ,permittedWrite(x,user,sessionId,isAdmin),
                ownedByMe(x,user,sessionId)))
            .collect(Collectors.toList());
    }

    /**
     *   Get the `SESSION_COOKIE_NAME` cookie value from the request.
     *   If there isn't one, then returns null.
     */
    String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies ==null) {
            return null;
        }
        Optional<Cookie> sessionCookie = Arrays.stream(cookies)
            .filter(x->x.getName().equals(SESSION_COOKIE_NAME))
            .findFirst();

        return sessionCookie.isPresent() ?  sessionCookie.get().getValue() : null;
    }

    /**
     *   adds a new SESSION_COOKIE_NAME cookie (set-cookie) to the response.
     *   Value will be a random UUID.
     *
     * @return the SESSION_COOKIE_NAME value (uuid)
     */
    String setSessionId(HttpServletResponse response) {
        return setSessionId(response, UUID.randomUUID().toString());
    }

    /**
     *   adds a new SESSION_COOKIE_NAME cookie (set-cookie) to the response.
     *
     * @return the SESSION_COOKIE_NAME value (uuid)
     */
    String setSessionId(HttpServletResponse response, String value) {
        response.addCookie(new Cookie(SESSION_COOKIE_NAME,value));
        return value;
    }

    /**
     *  Get a FavouriteMetadataListVM by id.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Get a specific (by DB id) of FavouriteList list")
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/{favouriteListIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    FavouriteMetadataListVM getFavouritesList(
        @Parameter(description = "FavouritesList DB id (int)",required = true)
        @PathVariable
        Integer favouriteListIdentifier,
        @Parameter(hidden = true)
        HttpSession httpSession,
        @Parameter(hidden = true)
        HttpServletRequest  httpServletRequest
    )
        throws Exception {

        if (favouriteListIdentifier == null) {
            throw new IllegalArgumentException("no favouriteListIdentifier given");
        }
        if (favouriteListIdentifier <=0) {
            throw new IllegalArgumentException("invalid favouriteListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(httpServletRequest);
        User user =  getUser(httpSession);

        Optional<FavouriteMetadataList> list = favouriteMetadataListRepository.findById(favouriteListIdentifier);
        if (!list.isPresent()) {
            throw new ResourceNotFoundException(String.format("cannot find favouriteListIdentifier, based on id %d", favouriteListIdentifier));
        }
        FavouriteMetadataList result = list.get();

        if (permittedRead(result,user,sessionId,isAdmin)) {
            return new FavouriteMetadataListVM(result);
        }

        throw new NotAllowedException("you don't have permission to read that favouriteList list");
    }

    /**
     *  update a FavouriteMetadataList
     *  user or session must own the FavouriteMetadataList.
     *  change name - name must be unique for the user/session.
     *
     *  update the set of metadataUuuids (add/remove/replace)
     *
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Update a specific (by DB id) of FavouriteList list")
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/{favouriteListIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    FavouriteMetadataListVM updateFavouriteList(
        @Parameter(description = "favouriteList DB id (int)",required = true)
        @PathVariable
            Integer favouriteListIdentifier,

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
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest  httpServletRequest
    )
        throws Exception {

        if (favouriteListIdentifier == null) {
            throw new IllegalArgumentException("no favouriteListIdentifier given");
        }
        if (favouriteListIdentifier <= 0) {
            throw new IllegalArgumentException("invalid favouriteListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(httpServletRequest);
        User user = getUser(httpSession);

        Optional<FavouriteMetadataList> list1 = favouriteMetadataListRepository.findById(favouriteListIdentifier);
        if (!list1.isPresent()) {
            throw new ResourceNotFoundException(String.format("cannot find favouriteListIdentifier, based on id %d", favouriteListIdentifier));
        }
        FavouriteMetadataList list = list1.get();

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
            FavouriteMetadataList item = favouriteMetadataListRepository.findName(name,user,sessionId);
            if (item != null) {
                throw new IllegalArgumentException(String.format("name '%s' is already in use!", name));
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
                    throw new IllegalArgumentException(String.format("Metadata with uuid %s is not found", uuid));
                }
                if (md.getDataInfo().getType().codeString.equals("y")) {
                    continue; //don't add templates
                }
                boolean alreadyInList = list.getSelections().stream()
                    .anyMatch(x->x.getMetadataUuid().equals(uuid));
                if (!alreadyInList) {
                    FavouriteMetadataListItem selection = new FavouriteMetadataListItem();
                    selection.setMetadataUuid(uuid);
                    list.getSelections().add(selection);
                }
            }
        }
        else {
            //action = remove
            for(String uuid : metadataUuids) {
                Optional<FavouriteMetadataListItem> existing = list.getSelections().stream()
                    .filter(x->x.getMetadataUuid().equals(uuid))
                    .findFirst();
                if (existing.isPresent()) {
                    list.getSelections().remove(existing.get());
                }

            }
        }
        //save
        updateChangeTimeToNow(list);
        list = favouriteMetadataListRepository.save(list);
        return new FavouriteMetadataListVM(list);
    }


    /**
     *  Change the FavouriteMetadataList's IsPublic Attribute.
     *  User or session must own the FavouriteMetadataList.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Update a specific (by DB id) List's IsPublic attribute")
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/{favouriteListIdentifier}/status",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    FavouriteMetadataListVM updateStatus(
        @Parameter(description = "favouriteListIdentifier DB id (int)",required = true)
        @PathVariable
            Integer favouriteListIdentifier,

        @Parameter(description = "new isPublic status",required = false)
        @RequestParam(name="public",required = true)
            boolean isPublic,

        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest  httpServletRequest
    )
        throws Exception {

        if (favouriteListIdentifier == null) {
            throw new IllegalArgumentException("no favouriteListIdentifier given");
        }
        if (favouriteListIdentifier <= 0) {
            throw new IllegalArgumentException("invalid favouriteListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);

        String sessionId = getSessionId(httpServletRequest);
        User user = getUser(httpSession);

        Optional<FavouriteMetadataList> list1 = favouriteMetadataListRepository.findById(favouriteListIdentifier);
        if (!list1.isPresent()) {
            throw new ResourceNotFoundException("cannot find favouriteMetadataListRepository, based on id" + favouriteListIdentifier);
        }
        FavouriteMetadataList list = list1.get();

        if (!permittedWrite(list, user, sessionId, isAdmin)) {
            throw new NotAllowedException("You do not have permission to modify this list");
        }

        if (list.getUser()==null) {
            throw new Exception("anonymous lists cannot be made public!");
        }

        list.setIsPublic(isPublic);
        updateChangeTimeToNow(list);
        list = favouriteMetadataListRepository.save(list);
        return new FavouriteMetadataListVM(list);
    }

    /**
     * Delete (by id) a favouriteMetadataList.
     * User or Session must own the favouriteMetadataList.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a specific (by DB id) FavouritesList")
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/{favouriteListIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    boolean deleteFavouriteList(
        @Parameter(description = "FavouritesList DB id (int)",required = true)
        @PathVariable
            Integer favouriteListIdentifier,

        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest  httpServletRequest
    )
        throws Exception {

        if (favouriteListIdentifier == null) {
            throw new IllegalArgumentException("no favouriteListIdentifier given");
        }
        if (favouriteListIdentifier <= 0) {
            throw new IllegalArgumentException("invalid favouriteListIdentifier given");
        }

        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(httpServletRequest);
        User user = getUser(httpSession);

        Optional<FavouriteMetadataList> list1 = favouriteMetadataListRepository.findById(favouriteListIdentifier);
        if (!list1.isPresent()) {
            throw new ResourceNotFoundException(String.format("cannot find favouriteListIdentifier, based on id %d", favouriteListIdentifier));
        }
        FavouriteMetadataList list = list1.get();

        if (!permittedWrite(list, user, sessionId, isAdmin)) {
            throw new NotAllowedException("You do not have permission to modify this list");
        }
        favouriteMetadataListRepository.delete(list);
        return true;
    }


    /**
     * A favouriteList is created, for logged users are assigned to the user and reused between sessions.
     *
     * For unlogged users, are assigned to the user session.
     *
     * Create as private.
     *
     * example:
     * curl -X POST "http://localhost:8080/geonetwork/srv/api/favouritelist"
     *     -H "accept: application/json"
     *     -H "X-XSRF-TOKEN: ccc"
     *     -H $'Cookie: XSRF-TOKEN=ccc'
     *     -d "name=dave&metadataUuids=1&metadataUuids=2&listType=WatchList"
     *
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new FavouritesList")
    @RequestMapping(
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    FavouriteMetadataListVM createNewFavouritesList(
        @Parameter(description = "Name of the list to be created",required = true)
        @RequestParam(name="name",required = true)
        String name,

        @Parameter(description = "List of metadata to add to the FavourtiesList",required = false)
        @RequestParam(name="metadataUuids",required = false)
        String[] metadataUuids,

        @Parameter(description = "Type of list to create (Watch or Preferred)",required = true)
        @RequestParam(name="listType",required = true)
        FavouriteMetadataList.ListType listType,

        @Parameter(hidden = true)
        HttpSession httpSession,

        @Parameter(hidden = true)
            HttpServletRequest  httpServletRequest,
        @Parameter(hidden = true)
            HttpServletResponse httpServletResponse
    )
        throws Exception {
            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("no name given");
            }
            name = name.trim();

            User user = getUser(httpSession);
            String sessionId = getSessionId(httpServletRequest);
            if (sessionId == null) {
                //create a new session
                sessionId = setSessionId(httpServletResponse);
            }
            FavouriteMetadataList alreadyExistsList = favouriteMetadataListRepository.findName(name,user,sessionId);

            if (alreadyExistsList != null) {
                throw new IllegalArgumentException("There is already a list of that name");
            }
            FavouriteMetadataList list = new FavouriteMetadataList();
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
                    if (md.getDataInfo().getType().codeString.equals("y")) {
                        continue; //don't add templates
                    }
                    FavouriteMetadataListItem selection = new FavouriteMetadataListItem();
                    selection.setMetadataUuid(uuid);
                    list.getSelections().add(selection);
                }
            }
            //save
            list = favouriteMetadataListRepository.save(list);
            return new FavouriteMetadataListVM(list);
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
    boolean permittedRead(FavouriteMetadataList list, User user, String sessionId, boolean isAdmin) {
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


    boolean ownedByMe(FavouriteMetadataList list, User user, String sessionId) {
        //owned by same user
        if ( (user != null) && (list.getUser() !=null) && (user.equals(list.getUser()))) {
            return true;
        }
        //owned by same session
        if ( (sessionId != null) && (list.getSessionId() !=null) && (sessionId.equals(list.getSessionId()))) {
            return true;
        }
        //otherwise its private and owned by someone else
        return false;
    }

    /**
     * @return true if the user/session owns the list.
     */
    boolean permittedWrite(FavouriteMetadataList list, User user, String sessionId, boolean isAdmin) {
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

    FavouriteMetadataList updateChangeTimeToNow(FavouriteMetadataList list) {
        list.setChangeDate(new ISODate());
        return list;
    }

}
