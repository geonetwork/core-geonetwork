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

package org.fao.geonet.api.tools.mail;
//==============================================================================
//===	Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_TAG;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.api.API;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.FeedbackLanguages;
import org.fao.geonet.util.LocalizedEmail;
import org.fao.geonet.util.LocalizedEmailParameter;
import org.fao.geonet.util.LocalizedEmailComponent;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletRequest;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 */

@RequestMapping(value = {
    "/{portal}/api/tools/mail"
})
@Tag(name = API_CLASS_TOOLS_TAG,
    description = API_CLASS_TOOLS_OPS)
@Controller("mail")
public class MailApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    SettingManager settingManager;

    @Autowired
    FeedbackLanguages feedbackLanguages;

    @io.swagger.v3.oas.annotations.Operation(summary = "Test mail configuration",
        description = "Send an email to the catalog feedback email.")
    @RequestMapping(value = "/test",
        produces = MediaType.TEXT_PLAIN_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Administrator')")
    public ResponseEntity<String> testMailConfiguration(ServletRequest request) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);
        Locale[] feedbackLocales = feedbackLanguages.getLocales(locale);

        String to = settingManager.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);

        LocalizedEmailComponent emailSubjectComponent = new LocalizedEmailComponent(LocalizedEmailComponent.ComponentType.SUBJECT, "mail_config_test_subject", LocalizedEmailComponent.KeyType.MESSAGE_KEY, LocalizedEmailComponent.ReplacementType.POSITIONAL_FORMAT);
        LocalizedEmailComponent emailMessageComponent = new LocalizedEmailComponent(LocalizedEmailComponent.ComponentType.MESSAGE, "mail_config_test_message", LocalizedEmailComponent.KeyType.MESSAGE_KEY, LocalizedEmailComponent.ReplacementType.POSITIONAL_FORMAT);

        for (Locale feedbackLocale : feedbackLocales) {
            emailSubjectComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(LocalizedEmailParameter.ParameterType.RAW_VALUE, 1, settingManager.getSiteName()),
                new LocalizedEmailParameter(LocalizedEmailParameter.ParameterType.RAW_VALUE, 2, to)
            );

            emailMessageComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(LocalizedEmailParameter.ParameterType.RAW_VALUE, 1, settingManager.getNodeURL()),
                new LocalizedEmailParameter(LocalizedEmailParameter.ParameterType.RAW_VALUE, 2, settingManager.getNodeURL()),
                new LocalizedEmailParameter(LocalizedEmailParameter.ParameterType.RAW_VALUE, 3, settingManager.getNodeURL())
            );
        }

        LocalizedEmail localizedEmail = new LocalizedEmail(true);
        localizedEmail.addComponents(emailSubjectComponent, emailMessageComponent);

        String subject = localizedEmail.getParsedSubject(feedbackLocales);
        String message = localizedEmail.getParsedMessage(feedbackLocales);

        try {
            MailUtil.testSendMail(to, subject, null, message, settingManager, to, "");
            return new ResponseEntity<>(String.format(
                messages.getString("mail_config_test_success"), to), HttpStatus.CREATED);
        } catch (Exception ex) {
            Log.error(API.LOG_MODULE_NAME, "Error sending test email", ex);
            String error = ex.getMessage();
            if (ex.getCause() != null) error = error + ". " + ex.getCause().getMessage();
            return new ResponseEntity<>(String.format(
                error), HttpStatus.PRECONDITION_FAILED);
        }

    }
}
