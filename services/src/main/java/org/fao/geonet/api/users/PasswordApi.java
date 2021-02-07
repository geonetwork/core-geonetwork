//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.users;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.ldap.LDAPConstants;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.util.PasswordUtil;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/{portal}/api/user"
})
@Tag(name = "users",
    description = "User operations")
public class PasswordApi {
    public static final String LOGGER = Geonet.GEONETWORK + ".api.user";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SettingManager sm;

    @io.swagger.v3.oas.annotations.Operation(summary = "Update user password",
        description = "Get a valid changekey by email first and then update your password.")
    @RequestMapping(
        value = "/{username}",
        method = RequestMethod.PATCH,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<String> updatePassword(
        @Parameter(description = "The user name",
            required = true)
        @PathVariable
            String username,
        @Parameter(description = "The new password and a valid change key",
            required = true)
        @RequestBody
            PasswordUpdateParameter passwordAndChangeKey,
        HttpServletRequest request)
        throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);

        ServiceContext context = ApiUtils.createServiceContext(request);

        User user = userRepository.findOneByUsername(username);
        if (user == null) {
            Log.warning(LOGGER, String.format("User update password. Can't find user '%s'",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(String.format(
                messages.getString("user_password_notchanged"),
                XslUtil.encodeForJavaScript(username)
            ), HttpStatus.PRECONDITION_FAILED);
        }
        if (LDAPConstants.LDAP_FLAG.equals(user.getSecurity().getAuthType())) {
            Log.warning(LOGGER, String.format("User '%s' is authenticated using LDAP. Password can't be sent by email.",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(String.format(
                messages.getString("user_password_notchanged"),
                XslUtil.encodeForJavaScript(username)
            ), HttpStatus.PRECONDITION_FAILED);
        }

        // construct expected change key - only valid today
        String scrambledPassword = user.getPassword();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String todaysDate = sdf.format(cal.getTime());
        boolean passwordMatches = PasswordUtil.encoder(ApplicationContextHolder.get()).matches(scrambledPassword + todaysDate, passwordAndChangeKey.getChangeKey());

        //check change key
        if (!passwordMatches) {
            return new ResponseEntity<>(String.format(
                messages.getString("user_password_invalid_changekey"),
                passwordAndChangeKey.getChangeKey(), XslUtil.encodeForJavaScript(username)
            ), HttpStatus.PRECONDITION_FAILED);
        }

        user.getSecurity().setPassword(PasswordUtil.encode(context, passwordAndChangeKey.getPassword()));
        userRepository.save(user);

        String adminEmail = sm.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);
        String subject = String.format(
            messages.getString("password_change_subject"),
            sm.getSiteName());
        String content = String.format(
            messages.getString("password_change_message"),
            sm.getSiteName(),
            adminEmail,
            sm.getSiteName());

        // send change link via email with admin in CC
        if (!MailUtil.sendMail(user.getEmail(),
            subject,
            content,
            null, sm,
            adminEmail, "")) {
            return new ResponseEntity<>(String.format(
                messages.getString("mail_error")), HttpStatus.PRECONDITION_FAILED);
        }
        return new ResponseEntity<>(String.format(
            messages.getString("user_password_changed"),
            XslUtil.encodeForJavaScript(username)
        ), HttpStatus.CREATED);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Send user password reminder by email",
        description = "An email is sent to the requested user with a link to " +
            "reset his password. User MUST have an email to get the link. " +
            "LDAP users will not be able to retrieve their password " +
            "using this service.")
    @RequestMapping(
        value = "/{username}/actions/forgot-password",
        method = RequestMethod.GET,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<String> sendPasswordByEmail(
        @Parameter(description = "The user name",
            required = true)
        @PathVariable
            String username,
        HttpServletRequest request)
        throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = locale.getISO3Language();
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);

        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        final User user = userRepository.findOneByUsername(username);
        if (user == null) {
            Log.warning(LOGGER, String.format("User reset password. Can't find user '%s'",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(String.format(
                messages.getString("user_password_sent"),
                XslUtil.encodeForJavaScript(username)
            ), HttpStatus.CREATED);
        }

        if (LDAPConstants.LDAP_FLAG.equals(user.getSecurity().getAuthType())) {
            Log.warning(LOGGER, String.format("User '%s' is authenticated using LDAP. Password can't be sent by email.",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(String.format(
                messages.getString("user_password_sent"),
                XslUtil.encodeForJavaScript(username)
            ), HttpStatus.CREATED);
        }

        String email = user.getEmail();
        if (StringUtils.isEmpty(email)) {
            Log.warning(LOGGER, String.format("User reset password. User '%s' has no email",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(String.format(
                messages.getString("user_password_sent"),
                XslUtil.encodeForJavaScript(username)
            ), HttpStatus.CREATED);
        }

        // get mail settings
        String adminEmail = sm.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);

        // construct change key - only valid today
        String scrambledPassword = user.getPassword();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String todaysDate = sdf.format(cal.getTime());
        String changeKey = PasswordUtil.encode(serviceContext,
            scrambledPassword + todaysDate);

        String subject = String.format(
            messages.getString("password_forgotten_subject"),
            sm.getSiteName(),
            username);
        String content = String.format(
            messages.getString("password_forgotten_message"),
            sm.getSiteName(),
            sm.getSiteURL(language),
            username,
            changeKey,
            sm.getSiteName());

        // send change link via email with admin in CC
        if (!MailUtil.sendMail(email,
            subject,
            content,
            null, sm,
            adminEmail, "")) {
            return new ResponseEntity<>(String.format(
                messages.getString("mail_error")), HttpStatus.PRECONDITION_FAILED);
        }
        return new ResponseEntity<>(String.format(
            messages.getString("user_password_sent"),
            XslUtil.encodeForJavaScript(username)
        ), HttpStatus.CREATED);
    }
}
