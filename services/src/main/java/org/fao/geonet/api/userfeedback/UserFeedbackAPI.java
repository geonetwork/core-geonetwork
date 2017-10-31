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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.userfeedback.UserFeedbackUtils.RatingAverage;
import org.fao.geonet.api.userfeedback.service.IUserFeedbackService;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.userfeedback.RatingsSetting;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;

/**
 * User Feedback REST API.
 */
@RequestMapping(value = { "/api", "/api/" + API.VERSION_0_1 })
@Api(value = "userfeedback", tags = "userfeedback")
@Controller("userfeedback")
public class UserFeedbackAPI {

    /** The Constant API_PARAM_CSW_SERVICE_IDENTIFIER. */
    public static final String API_PARAM_CSW_SERVICE_IDENTIFIER = "Service identifier";

    /** The Constant API_PARAM_CSW_SERVICE_DETAILS. */
    public static final String API_PARAM_CSW_SERVICE_DETAILS = "Service details";

    /**
     * Delete user feedback.
     *
     * @param uuid the uuid
     * @param request the request
     * @param response the response
     * @param httpSession the http session
     * @return the response entity
     * @throws Exception the exception
     */
    // DELETE
    @ApiOperation(value = "Removes a user feedback", notes = "Removes a user feedback", nickname = "deleteUserFeedback")
    @RequestMapping(value = "/userfeedback/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "User feedback removed."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER) })
    @ResponseBody
    public ResponseEntity deleteUserFeedback(@PathVariable(value = "uuid")
    final String uuid, final HttpServletRequest request, final HttpServletResponse response, final HttpSession httpSession)
            throws Exception {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "deleteUserFeedback");

        final IUserFeedbackService userFeedbackService = getUserFeedbackService();

        userFeedbackService.removeUserFeedback(uuid);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Gets the metadata rating.
     *
     * @param metadataUuid the metadata uuid
     * @param request the request
     * @param response the response
     * @param httpSession the http session
     * @return the metadata rating
     * @throws Exception the exception
     */
    @ApiOperation(value = "Provides an average rating for a metadata record", nickname = "getMetadataUserComments")
    @RequestMapping(value = "/metadata/{uuid}/userfeedbackrating", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public RatingAverage getMetadataRating(@PathVariable(value = "uuid")
    final String metadataUuid, final HttpServletRequest request, final HttpServletResponse response, final HttpSession httpSession)
            throws Exception {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        try {
            Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getMetadataUserComments");

            // Check permission for metadata
            final Metadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
            if (metadata == null) {
                printOutputMessage(response, HttpStatus.FORBIDDEN, ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
                return null;
            }

            final UserSession session = ApiUtils.getUserSession(httpSession);

            boolean published = true; // Takes only published comments

            // showing not published comments only to logged users (maybe better
            // restrict to Reviewers)
            if (session != null && session.isAuthenticated()) {
                published = false;
            }

            final IUserFeedbackService userFeedbackService = getUserFeedbackService();

            final UserFeedbackUtils utils = new UserFeedbackUtils();

            return utils.getAverage(userFeedbackService.retrieveUserFeedbackForMetadata(metadataUuid, -1, published));
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the user comment.
     *
     * @param uuid the uuid
     * @param request the request
     * @param response the response
     * @param httpSession the http session
     * @return the user comment
     * @throws Exception the exception
     */
    @ApiOperation(value = "Finds a specific usercomment", nickname = "getUserComment")
    @RequestMapping(value = "/userfeedback/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public UserFeedbackDTO getUserComment(@PathVariable(value = "uuid")
    final String uuid, final HttpServletRequest request, final HttpServletResponse response, final HttpSession httpSession)
            throws Exception {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getUserComment");

        final IUserFeedbackService userFeedbackService = (IUserFeedbackService) ApplicationContextHolder.get()
                .getBean("userFeedbackService");

        final UserSession session = ApiUtils.getUserSession(httpSession);

        boolean published = true; // Takes only published comments

        // showing not published comments only to logged users (maybe better
        // restrict to Reviewers)
        if (session != null && session.isAuthenticated()) {
            published = false;
        }

        final UserFeedback userfeedback = userFeedbackService.retrieveUserFeedback(uuid, published);

        UserFeedbackDTO dto = null;

        if (userfeedback != null) {
            dto = UserFeedbackUtils.convertToDto(userfeedback);
        }

        // Check permission for metadata
        final Metadata metadata = ApiUtils.canViewRecord(userfeedback.getMetadata().getUuid(), request);
        if (metadata == null) {
            printOutputMessage(response, HttpStatus.FORBIDDEN, ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
            return null;
        }

        return dto;
    }

    /**
     * Gets the user comments.
     *
     * @param request the request
     * @param response the response
     * @param httpSession the http session
     * @return the user comments
     * @throws Exception the exception
     */
    // GET
    @ApiOperation(value = "Finds a list of user feedback records. ", 
            notes = " This list will include also the draft uf if the client is logged as reviewer. "
            + " target={metadata uuid} maxnumber={max result size} "
            , nickname = "getUserComments")
    @RequestMapping(value = "/userfeedback", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<UserFeedbackDTO> getUserComments(final HttpServletRequest request, final HttpServletResponse response,
            final HttpSession httpSession) throws Exception {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        try {
            Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getUserComments");

            final IUserFeedbackService userFeedbackService = getUserFeedbackService();

            final String uuid = request.getParameter("target");

            final String maxnumber = request.getParameter("maxnumber");

            int maxsize = -1;

            if (maxnumber != null) {
                maxsize = Integer.parseInt(maxnumber);
            }

            final UserSession session = ApiUtils.getUserSession(httpSession);

            boolean published = true; // Takes only published comments

            // showing not published comments only to logged users (maybe better
            // restrict to Reviewers)
            if (session != null && session.isAuthenticated()) {
                published = false;
            }

            List<UserFeedback> listUserfeedback = null;

            if (uuid == null || uuid.equals("")) {
                listUserfeedback = userFeedbackService.retrieveUserFeedback(maxsize, published);
            } else {
                listUserfeedback = userFeedbackService.retrieveUserFeedbackForMetadata(uuid, maxsize, published);
            }

            return listUserfeedback.stream().map(feedback -> UserFeedbackUtils.convertToDto(feedback)).collect(Collectors.toList());
        } catch (final Exception e) {
            e.printStackTrace();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return null;
        }
    }

    /**
     * Gets the user feedback service.
     *
     * @return the user feedback service
     */
    // POST
    private IUserFeedbackService getUserFeedbackService() {
        return (IUserFeedbackService) ApplicationContextHolder.get().getBean("userFeedbackService");
    }

    /**
     * New user feedback.
     *
     * @param userFeedbackDto the user feedback dto
     * @param request the request
     * @param response the response
     * @param httpSession the http session
     * @return the response entity
     * @throws Exception the exception
     */
    @ApiOperation(value = "Creates a userfeedback", notes = "Creates a userfeedback in draft status if the user is not logged in.", nickname = "newUserFeedback")
    @RequestMapping(value = "/userfeedback", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity newUserFeedback(@ApiParam(name = "uf") @RequestBody UserFeedbackDTO userFeedbackDto,
            final HttpServletRequest request, final HttpServletResponse response, final HttpSession httpSession) throws Exception {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        try {

            final UserSession session = ApiUtils.getUserSession(httpSession);

            Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "newUserFeedback");

            final IUserFeedbackService userFeedbackService = getUserFeedbackService();

            userFeedbackService
                    .saveUserFeedback(UserFeedbackUtils.convertFromDto(userFeedbackDto, session != null ? session.getPrincipal() : null));

            return new ResponseEntity(HttpStatus.CREATED);
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Prints the output message.
     *
     * @param response the response
     * @param code the code
     * @param message the message
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void printOutputMessage(final HttpServletResponse response, final HttpStatus code, final String message) throws IOException {
        response.setStatus(code.value());
        final PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println(message);
        response.flushBuffer();
    }

    /**
     * Publish.
     *
     * @param uuid the uuid
     * @param request the request
     * @param response the response
     * @param httpSession the http session
     * @return the response entity
     * @throws Exception the exception
     */
    @ApiOperation(value = "Publishes a record", notes = "For reviewers", nickname = "publish")
    @RequestMapping(value = "/userfeedback/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Reviewer')")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "User feedback published."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER) })
    @ResponseBody
    public ResponseEntity publish(@PathVariable(value = "uuid")
    final String uuid, final HttpServletRequest request, final HttpServletResponse response, final HttpSession httpSession)
            throws Exception {

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final SettingManager settingManager = appContext.getBean(SettingManager.class);
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        try {
            final UserSession session = ApiUtils.getUserSession(httpSession);

            final IUserFeedbackService userFeedbackService = getUserFeedbackService();

            userFeedbackService.publishUserFeedback(uuid, session.getPrincipal());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);

    }

}
