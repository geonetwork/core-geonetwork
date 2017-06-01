/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.userfeedback;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.swagger.annotations.*;
import jeeves.server.UserSession;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.userfeedback.service.IUserFeedbackService;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.utils.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = {
        "/api",
        "/api/" + API.VERSION_0_1
})
@Api(value = "userfeedback", tags = "userfeedback")
@Controller("userfeedback")
public class UserFeedbackAPI {

    public static final String API_PARAM_CSW_SERVICE_IDENTIFIER = "Service identifier";
    public static final String API_PARAM_CSW_SERVICE_DETAILS = "Service details";

    // GET
    @ApiOperation(
            value = "Finds a list of usercomment records",
            notes = "Finds a list of usercomment records, filtered by: target={uuid}"
                    + " any={searchstring} "
                    + " From To user={userid} Orderby Sortorder Published Ownergroup "
                    + " (filter feedbacks on metadata owned by group x) ",
                    nickname = "getUserComments")
    @RequestMapping(
            value = "/userfeedback",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<UserFeedback> getUserComments(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getUserComments");

        IUserFeedbackService userFeedbackService = getUserFeedbackService();

        String uuid = "";

        return userFeedbackService.retrieveUserFeedbackForMetadata(uuid);
    }

    @ApiOperation(
            value = "Finds a specific usercomment",
            notes = "Finds a specific usercomment",
            nickname = "getUserComment")
    @RequestMapping(
            value = "/userfeedback/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public UserFeedback getUserComment(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getUserComment");

        IUserFeedbackService userFeedbackService =
            (IUserFeedbackService) ApplicationContextHolder.get().getBean("userFeedbackService");

        return userFeedbackService.retrieveUserFeedback(uuid);
    }

    @ApiOperation(
            value = "Provides an average rating for a metadata record",
            notes = "Provides an average rating for a metadata record",
            nickname = "getMetadataUserComments")
    @RequestMapping(
            value = "/metadata/{uuid}/userfeedback",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Rating getMetadataRating(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getMetadataUserComments");

        IUserFeedbackService userFeedbackService = getUserFeedbackService();

        return userFeedbackService.retrieveMetadataRating(uuid);
    }

    @ApiOperation(
            value = "Publishes a record, send notification ",
            notes = "Publishes a record, send notification ",
            nickname = "publish")
    @RequestMapping(
            value = "/userfeedback/{uuid}/publish",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "User feedback puvlished."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @ResponseBody
    public ResponseEntity publish(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final HttpSession httpSession
            ) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "publish");

        UserSession session = ApiUtils.getUserSession(httpSession);

        IUserFeedbackService userFeedbackService = getUserFeedbackService();

        userFeedbackService.publishUserFeedback(uuid, session.getPrincipal());

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    // PUT

    @ApiOperation(
            value = "Create a userfeedback (draft), send notification to owner ",
            notes = "Create a userfeedback (draft), send notification to owner ",
            nickname = "newUserFeedback")
    @RequestMapping(
            value = "/userfeedback",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity newUserFeedback(
        @ApiParam(
            name = "userFeedback"
        )
        @RequestBody
            UserFeedback userFeedback,
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "newUserFeedback");

        IUserFeedbackService userFeedbackService = getUserFeedbackService();

        userFeedbackService.saveUserFeedback(userFeedback);

        return new ResponseEntity(HttpStatus.CREATED);
    }


    // DELETE
    @ApiOperation(
            value = "Removes a user feedback",
            notes = "Removes a user feedback",
            nickname = "deleteUserFeedback")
    @RequestMapping(
            value = "/userfeedback/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "User feedback removed."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @ResponseBody
    public ResponseEntity deleteUserFeedback(
            @PathVariable(value = "uuid") final String uuid,
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "deleteUserFeedback");

        IUserFeedbackService userFeedbackService = getUserFeedbackService();

        userFeedbackService.removeUserFeedback(uuid);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private IUserFeedbackService getUserFeedbackService() {
        return (IUserFeedbackService) ApplicationContextHolder.get().getBean("userFeedbackService");
    }
}
