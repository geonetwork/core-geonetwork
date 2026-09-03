//=============================================================================
//===   Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.fao.geonet.kernel.security.ldap.LDAPConstants;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.FeedbackLanguages;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.util.PasswordUtil;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.util.LocalizedEmail;
import org.fao.geonet.util.LocalizedEmailComponent;
import org.fao.geonet.util.LocalizedEmailParameter;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.fao.geonet.util.LocalizedEmailComponent.ComponentType.*;
import static org.fao.geonet.util.LocalizedEmailComponent.KeyType;
import static org.fao.geonet.util.LocalizedEmailComponent.ReplacementType.*;
import static org.fao.geonet.util.LocalizedEmailParameter.ParameterType;

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
    public static final String USER_PASSWORD_SENT = "user_password_sent";

    // A value the change key can't match, verified when the request can't succeed (unknown or
    // LDAP user) so the response time doesn't reveal whether the account exists. Its length is
    // similar to a real change key value (a scrambled password plus the date) to keep the cost
    // of the check comparable.
    private static final String DUMMY_CHANGE_KEY_VALUE =
        "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SettingManager sm;
    @Autowired
    FeedbackLanguages feedbackLanguages;

    @Autowired(required = false)
    SecurityProviderConfiguration securityProviderConfiguration;

    @io.swagger.v3.oas.annotations.Operation(summary = "Update user password",
        description = "Get a valid changekey by email first and then update your password.")
    @PatchMapping(
        value = "/{username}",
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
        HttpServletRequest request) {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);
        Locale[] feedbackLocales = feedbackLanguages.getLocales(locale);

        if (securityProviderConfiguration != null && !securityProviderConfiguration.isUserProfileUpdateEnabled()) {
            return new ResponseEntity<>(messages.getString("security_provider_unsupported_functionality"), HttpStatus.PRECONDITION_FAILED);
        }

        ServiceContext context = ApiUtils.createServiceContext(request);

        // All the failure cases below return the exact same response.
        // Details about the actual reason are available in the server log.
        ResponseEntity<String> passwordNotChanged = new ResponseEntity<>(
            messages.getString("user_password_notchanged"), HttpStatus.PRECONDITION_FAILED);

        PasswordEncoder encoder = PasswordUtil.encoder(ApplicationContextHolder.get());

        List<User> existingUsers = userRepository.findByUsernameIgnoreCase(username);

        if (existingUsers.isEmpty()) {
            Log.warning(LOGGER, String.format("User update password. Can't find user '%s'",
                username));

            // Verify the change key against a dummy value too, so the response time doesn't
            // reveal whether the user exists.
            verifyChangeKey(encoder, DUMMY_CHANGE_KEY_VALUE, passwordAndChangeKey.getChangeKey());
            return passwordNotChanged;
        }

        User user = existingUsers.get(0);

        if (LDAPConstants.LDAP_FLAG.equals(user.getSecurity().getAuthType())) {
            Log.warning(LOGGER, String.format("User '%s' is authenticated using LDAP. Password can't be sent by email.",
                username));

            verifyChangeKey(encoder, DUMMY_CHANGE_KEY_VALUE, passwordAndChangeKey.getChangeKey());
            return passwordNotChanged;
        }

        // construct expected change key - only valid today
        String scrambledPassword = user.getPassword();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String todaysDate = sdf.format(cal.getTime());

        //check change key
        if (!verifyChangeKey(encoder, scrambledPassword + todaysDate,
            passwordAndChangeKey.getChangeKey())) {
            Log.warning(LOGGER, String.format("User update password. Invalid change key for user '%s'",
                username));

            return passwordNotChanged;
        }

        user.getSecurity().setPassword(PasswordUtil.encode(context, passwordAndChangeKey.getPassword()));
        userRepository.save(user);

        String adminEmail = sm.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);

        LocalizedEmailComponent emailSubjectComponent = new LocalizedEmailComponent(SUBJECT, "password_change_subject", KeyType.MESSAGE_KEY, POSITIONAL_FORMAT);
        LocalizedEmailComponent emailMessageComponent = new LocalizedEmailComponent(MESSAGE, "password_change_message", KeyType.MESSAGE_KEY, POSITIONAL_FORMAT);

        for (Locale feedbackLocale : feedbackLocales) {
            emailSubjectComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, sm.getSiteName())
            );

            emailMessageComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, sm.getSiteName()),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 2, adminEmail),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 3, sm.getSiteName())
            );
        }

        LocalizedEmail localizedEmail = new LocalizedEmail(false);
        localizedEmail.addComponents(emailSubjectComponent, emailMessageComponent);

        String subject = localizedEmail.getParsedSubject(feedbackLocales);
        String content = localizedEmail.getParsedMessage(feedbackLocales);

        // send change link via email with admin in CC
        Boolean mailSent = MailUtil.sendMail(user.getEmail(),
            subject,
            content,
            null, sm,
            adminEmail, "");
        if (Boolean.FALSE.equals(mailSent)) {
            return new ResponseEntity<>(String.format(
                messages.getString("mail_error")), HttpStatus.PRECONDITION_FAILED);
        }

        return new ResponseEntity<>(String.format(
            messages.getString("user_password_changed"),
            XslUtil.encodeForJavaScript(username)
        ), HttpStatus.CREATED);
    }

    /**
     * Check a change key against the value it is expected to encode.
     *
     * Always runs the same encoder work whether or not the account exists, so the caller can
     * verify a real change key or spend the same time against a dummy value and keep the
     * response time from revealing whether the user exists.
     *
     * @param encoder            the password encoder
     * @param expectedChangeKeyValue the value the change key should encode
     * @param changeKey          the change key provided in the request
     * @return true if the change key matches the expected value
     */
    private boolean verifyChangeKey(PasswordEncoder encoder, String expectedChangeKeyValue,
                                    String changeKey) {
        try {
            return encoder.matches(expectedChangeKeyValue, changeKey);
        } catch (RuntimeException e) {
            // A change key which is not a hash the encoder can read makes it fail instead of
            // returning false (eg. StandardPasswordEncoder rejects a key which is not hex
            // encoded). Handle it like any other invalid change key.
            Log.warning(LOGGER, String.format(
                "User update password. The change key can't be read by the password encoder: %s",
                e.getMessage()));

            return false;
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Send user password reminder by email",
        description = "An email is sent to the requested user with a link to " +
            "reset his password. User MUST have an email to get the link. " +
            "LDAP users will not be able to retrieve their password " +
            "using this service.")
    @PutMapping(
        value = "/actions/forgot-password",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<String> sendPasswordByEmail(
        @Parameter(description = "The user name",
            required = true)
        @RequestParam
        String username,
        HttpServletRequest request) {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);
        Locale[] feedbackLocales = feedbackLanguages.getLocales(locale);

        if (securityProviderConfiguration != null && !securityProviderConfiguration.isUserProfileUpdateEnabled()) {
            return new ResponseEntity<>(messages.getString("security_provider_unsupported_functionality"), HttpStatus.PRECONDITION_FAILED);
        }

        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        List<User> existingUsers = userRepository.findByUsernameIgnoreCase(username);

        if (existingUsers.isEmpty()) {
            Log.warning(LOGGER, String.format("User reset password. Can't find user '%s'",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(
                messages.getString(USER_PASSWORD_SENT), HttpStatus.CREATED);
        }
        User user = existingUsers.get(0);

        if (LDAPConstants.LDAP_FLAG.equals(user.getSecurity().getAuthType())) {
            Log.warning(LOGGER, String.format("User '%s' is authenticated using LDAP. Password can't be sent by email.",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(
                messages.getString(USER_PASSWORD_SENT), HttpStatus.CREATED);
        }

        String email = user.getEmail();
        if (!StringUtils.hasLength(email)) {
            Log.warning(LOGGER, String.format("User reset password. User '%s' has no email",
                username));

            // Return response not providing details about the issue, that should be logged.
            return new ResponseEntity<>(
                messages.getString(USER_PASSWORD_SENT), HttpStatus.CREATED);
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

        LocalizedEmailComponent emailSubjectComponent = new LocalizedEmailComponent(SUBJECT, "password_forgotten_subject", KeyType.MESSAGE_KEY, POSITIONAL_FORMAT);
        LocalizedEmailComponent emailMessageComponent = new LocalizedEmailComponent(MESSAGE, "password_forgotten_message", KeyType.MESSAGE_KEY, POSITIONAL_FORMAT);

        for (Locale feedbackLocale : feedbackLocales) {
            emailSubjectComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, sm.getSiteName()),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 2, username)
            );

            emailMessageComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, sm.getSiteName()),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 2, sm.getSiteURL(feedbackLocale.getISO3Language())),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 3, username),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 4, changeKey),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 5, sm.getSiteName())
            );
        }

        LocalizedEmail localizedEmail = new LocalizedEmail(false);
        localizedEmail.addComponents(emailSubjectComponent, emailMessageComponent);

        String subject = localizedEmail.getParsedSubject(feedbackLocales);
        String content = localizedEmail.getParsedMessage(feedbackLocales);

        // send change link via email with admin in CC
        Boolean mailSent = MailUtil.sendMail(email,
            subject,
            content,
            null, sm,
            adminEmail, "");
        if (Boolean.FALSE.equals(mailSent)) {
            return new ResponseEntity<>(String.format(
                messages.getString("mail_error")), HttpStatus.PRECONDITION_FAILED);
        }

        return new ResponseEntity<>(
            messages.getString(USER_PASSWORD_SENT), HttpStatus.CREATED);
    }
}
