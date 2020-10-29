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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.apache.jcs.access.exception.ObjectNotFoundException;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.api.userfeedback.UserFeedbackUtils.RatingAverage;
import org.fao.geonet.api.userfeedback.service.IUserFeedbackService;
import org.fao.geonet.api.users.recaptcha.RecaptchaChecker;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.userfeedback.RatingCriteria;
import org.fao.geonet.domain.userfeedback.RatingsSetting;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.userfeedback.RatingCriteriaRepository;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_FEEDBACK_EMAIL;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_SITE_NAME_PATH;


/**
 * User Feedback REST API.
 */
@RequestMapping(value = {"/{portal}/api"})
@Tag(name = "userfeedback",
    description = "User feedback")
@Controller("userfeedback")
public class UserFeedbackAPI {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    SettingManager settingManager;

    @Autowired
    RatingCriteriaRepository criteriaRepository;

    @Autowired
    MetadataRepository metadataRepository;

    /**
     * Gets rating criteria
     *
     * @param response the response
     * @return the list of rating criteria
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get list of rating criteria")
    @RequestMapping(
        value = "/userfeedback/ratingcriteria",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<RatingCriteria> getRatingCriteria(
        @Parameter(hidden = true) final HttpServletResponse response
    ) {
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        } else {
            return criteriaRepository.findAll();
        }
    }

    /**
     * Delete user feedback.
     *
     * @param uuid the uuid
     * @return the response entity
     * @throws Exception the exception
     */
    // DELETE
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Removes a user feedback",
        description = "Removes a user feedback")
    @RequestMapping(value = "/userfeedback/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('Reviewer')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "User feedback removed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)})
    @ResponseBody
    public ResponseEntity deleteUserFeedback(
        @Parameter(
            description = "User feedback UUID.",
            required = true
        )
        @PathVariable(value = "uuid") final String uuid,
        final HttpServletRequest request)
        throws Exception {

        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "deleteUserFeedback");

        final IUserFeedbackService userFeedbackService = getUserFeedbackService();

        userFeedbackService.removeUserFeedback(uuid, request.getRemoteAddr());

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Gets the metadata rating.
     *
     * @param metadataUuid the metadata uuid
     * @param request      the request
     * @param response     the response
     * @param httpSession  the http session
     * @return the metadata rating
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Provides an average rating for a metadata record")
    @RequestMapping(value = "/records/{metadataUuid}/userfeedbackrating", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public RatingAverage getMetadataRating(
        @Parameter(
            description = "Metadata record UUID.",
            required = true
        )
        @PathVariable(value = "metadataUuid") final String metadataUuid,
        @Parameter(hidden = true) final HttpServletRequest request,
        @Parameter(hidden = true) final HttpServletResponse response,
        @Parameter(hidden = true) final HttpSession httpSession) {

        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        try {
            Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getMetadataUserComments");

            // Check permission for metadata
            final AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
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
            Log.error(API.LOG_MODULE_NAME, "UserFeedbackAPI - getMetadataRating: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets the user comment.
     *
     * @param uuid        the uuid
     * @param request     the request
     * @param response    the response
     * @param httpSession the http session
     * @return the user comment
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Finds a specific user feedback")
    @RequestMapping(value = "/userfeedback/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public UserFeedbackDTO getUserComment(
        @Parameter(
            description = "User feedback UUID.",
            required = true
        )
        @PathVariable(value = "uuid") final String uuid,
        @Parameter(hidden = true) final HttpServletRequest request,
        @Parameter(hidden = true) final HttpServletResponse response,
        @Parameter(hidden = true) final HttpSession httpSession)
        throws Exception {

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
        final AbstractMetadata metadata = ApiUtils.canViewRecord(userfeedback.getMetadata().getUuid(), request);
        if (metadata == null) {
            printOutputMessage(response, HttpStatus.FORBIDDEN, ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
            return null;
        }

        return dto;
    }

    /**
     * Gets the user comments for one record.
     *
     * @param response    the response
     * @param httpSession the http session
     * @return the user comments
     * @throws Exception the exception
     */
    // GET
    @io.swagger.v3.oas.annotations.Operation(summary = "Finds a list of user feedback for a specific records. ",
        description = " This list will include also the draft user feedback if the client is logged as reviewer."
    )
    @RequestMapping(
        value = "/records/{metadataUuid}/userfeedback",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<UserFeedbackDTO> getUserCommentsOnARecord(
        @Parameter(
            description = "Metadata record UUID.",
            required = true
        )
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Maximum number of feedback to return.",
            required = false
        )
        @RequestParam(
            defaultValue = "-1",
            required = false
        )
            int size,
        @Parameter(hidden = true) final HttpServletResponse response,
        @Parameter(hidden = true) final HttpSession httpSession) throws Exception {

        return getUserFeedback(metadataUuid, size, response, httpSession);
    }

    /**
     * Gets the user comments.
     *
     * @param response    the response
     * @param httpSession the http session
     * @return the user comments
     * @throws Exception the exception
     */
    // GET
    @io.swagger.v3.oas.annotations.Operation(summary = "Finds a list of user feedback records. ",
        description = " This list will include also the draft user feedback if the client is logged as reviewer."
    )
    @RequestMapping(
        value = "/userfeedback",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<UserFeedbackDTO> getUserComments(
        @Parameter(
            description = "Metadata record UUID.",
            required = false
        )
        @RequestParam(
            defaultValue = "",
            required = false
        )
            String metadataUuid,
        @Parameter(
            description = "Maximum number of feedback to return.",
            required = false
        )
        @RequestParam(
            defaultValue = "-1",
            required = false
        )
            int size,
        @Parameter(hidden = true) final HttpServletResponse response,
        @Parameter(hidden = true) final HttpSession httpSession) throws Exception {

        return getUserFeedback(metadataUuid, size, response, httpSession);
    }

    private List<UserFeedbackDTO> getUserFeedback(
        String metadataUuid,
        int size,
        HttpServletResponse response,
        HttpSession httpSession) {
        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        try {
            Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "getUserComments");

            final IUserFeedbackService userFeedbackService = getUserFeedbackService();

            final UserSession session = ApiUtils.getUserSession(httpSession);

            boolean published = true; // Takes only published comments

            // showing not published comments only to logged users (maybe better
            // restrict to Reviewers)
            if (session != null && session.isAuthenticated()) {
                published = false;
            }

            List<UserFeedback> listUserfeedback = null;

            if (metadataUuid == null || metadataUuid.equals("")) {
                listUserfeedback = userFeedbackService.retrieveUserFeedback(size, published);
            } else {
                listUserfeedback = userFeedbackService.retrieveUserFeedbackForMetadata(metadataUuid, size, published);
            }

            return listUserfeedback.stream().map(feedback -> UserFeedbackUtils.convertToDto(feedback)).collect(Collectors.toList());
        } catch (final Exception e) {
            Log.error(API.LOG_MODULE_NAME, "UserFeedbackAPI - getUserFeedback: " + e.getMessage(), e);
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
     * @param httpSession     the http session
     * @return the response entity
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Creates a user feedback",
        description = "Creates a user feedback in draft status if the user is not logged in.")
    @RequestMapping(value = "/userfeedback", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity newUserFeedback(
        @Parameter(name = "uf") @RequestBody UserFeedbackDTO userFeedbackDto,
        @Parameter(hidden = true) final HttpSession httpSession,
        @Parameter(hidden = true) final HttpServletRequest request) throws Exception {

        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        try {

            final UserSession session = ApiUtils.getUserSession(httpSession);

            Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
            ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);

            Log.debug("org.fao.geonet.api.userfeedback.UserFeedback", "newUserFeedback");

            final IUserFeedbackService userFeedbackService = getUserFeedbackService();

            boolean recaptchaEnabled = settingManager.getValueAsBool(Settings.SYSTEM_USERSELFREGISTRATION_RECAPTCHA_ENABLE);

            if (recaptchaEnabled) {
                boolean validRecaptcha = RecaptchaChecker.verify(userFeedbackDto.getCaptcha(),
                    settingManager.getValue(Settings.SYSTEM_USERSELFREGISTRATION_RECAPTCHA_SECRETKEY));
                if (!validRecaptcha) {
                    return new ResponseEntity<>(
                        messages.getString("recaptcha_not_valid"), HttpStatus.PRECONDITION_FAILED);
                }
            }

            userFeedbackService
                .saveUserFeedback(UserFeedbackUtils.convertFromDto(userFeedbackDto, session != null ? session.getPrincipal() : null),
                    request.getRemoteAddr());

            return new ResponseEntity(HttpStatus.CREATED);
        } catch (final Exception e) {
            Log.error(API.LOG_MODULE_NAME, "UserFeedbackAPI - newUserFeedback: " + e.getMessage(), e);
            throw e;
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Send an email to catalogue administrator or record's contact",
        description = "")
    @RequestMapping(
        value = "/records/{metadataUuid}/alert",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity sendEmailToContact(
        @Parameter(
            description = "Metadata record UUID.",
            required = true
        )
        @PathVariable(value = "metadataUuid") final String metadataUuid,
        @Parameter(
            description = "Recaptcha validation key.",
            required = false
        )
        @RequestParam(required = false, defaultValue = "") final String recaptcha,
        @Parameter(
            description = "User name.",
            required = true
        )
        @RequestParam final String name,
        @Parameter(
            description = "User organisation.",
            required = true
        )
        @RequestParam final String org,
        @Parameter(
            description = "User email address.",
            required = true
        )
        @RequestParam final String email,
        @Parameter(
            description = "A comment or question.",
            required = true
        )
        @RequestParam final String comments,
        @Parameter(
            description = "User phone number.",
            required = false
        )
        @RequestParam(required = false, defaultValue = "") final String phone,
        @Parameter(
            description = "Email subject.",
            required = false
        )
        @RequestParam(required = false, defaultValue = "User feedback") final String subject,
        @Parameter(
            description = "User function.",
            required = false
        )
        @RequestParam(required = false, defaultValue = "-") final String function,
        @Parameter(
            description = "Comment type.",
            required = false
        )
        @RequestParam(required = false, defaultValue = "-") final String type,
        @Parameter(
            description = "Comment category.",
            required = false
        )
        @RequestParam(required = false, defaultValue = "-") final String category,
        @Parameter(
            description = "List of record's contact to send this email.",
            required = false
        )
        @RequestParam(required = false, defaultValue = "") final String metadataEmail,
        @Parameter(hidden = true) final HttpServletRequest request
    ) throws IOException {

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);

        boolean recaptchaEnabled = settingManager.getValueAsBool(Settings.SYSTEM_USERSELFREGISTRATION_RECAPTCHA_ENABLE);

        if (recaptchaEnabled) {
            boolean validRecaptcha = RecaptchaChecker.verify(recaptcha,
                settingManager.getValue(Settings.SYSTEM_USERSELFREGISTRATION_RECAPTCHA_SECRETKEY));
            if (!validRecaptcha) {
                return new ResponseEntity<>(
                    messages.getString("recaptcha_not_valid"), HttpStatus.PRECONDITION_FAILED);
            }
        }


        String to = settingManager.getValue(SYSTEM_FEEDBACK_EMAIL);
        String catalogueName = settingManager.getValue(SYSTEM_SITE_NAME_PATH);

        List<String> toAddress = new LinkedList<String>();
        toAddress.add(to);
        if (isNotBlank(metadataEmail)) {
            //Check metadata email belongs to metadata security!!
            AbstractMetadata md = metadataRepository.findOneByUuid(metadataUuid);
            if (md.getData().indexOf(metadataEmail) > 0) {
                toAddress.add(metadataEmail);
            }
        }

        String title = XslUtil.getIndexField(null, metadataUuid, "resourceTitle", "");

        MailUtil.sendMail(toAddress,
            String.format(
                messages.getString("user_feedback_title"),
                catalogueName, title, subject),
            String.format(
                messages.getString("user_feedback_text"),
                name, org, function, email, phone, title, type, category, comments,
                settingManager.getNodeURL(), metadataUuid),
            settingManager);

        return new ResponseEntity(HttpStatus.CREATED);
    }

    /**
     * Prints the output message.
     *
     * @param response the response
     * @param code     the code
     * @param message  the message
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
     * @param uuid        the uuid
     * @param httpSession the http session
     * @return the response entity
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Publishes a feedback",
        description = "For reviewers")
    @RequestMapping(value = "/userfeedback/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('Reviewer')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "User feedback published."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)})
    @ResponseBody
    public ResponseEntity publishFeedback(
        @Parameter(
            description = "User feedback UUID.",
            required = true
        )
        @PathVariable(value = "uuid") final String uuid,
        @Parameter(hidden = true) final HttpSession httpSession)
        throws Exception {

        final String functionEnabled = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

        if (!functionEnabled.equals(RatingsSetting.ADVANCED)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        try {
            final UserSession session = ApiUtils.getUserSession(httpSession);
            final IUserFeedbackService userFeedbackService = getUserFeedbackService();

            userFeedbackService.publishUserFeedback(uuid, session.getPrincipal());

        } catch (final ObjectNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            Log.error(API.LOG_MODULE_NAME, "UserFeedbackAPI - publish: " + e.getMessage(), e);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
