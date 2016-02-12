package org.fao.geonet.services.api.tools.mail;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.services.api.API;
import org.fao.geonet.services.api.tools.i18n.LanguageUtils;
import org.fao.geonet.util.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.*;

/**
 *
 */

@RequestMapping(value = {
        "/api/tools/mail",
        "/api/" + API.VERSION_0_1 +
                "/tools/mail"
})
@Api(value = "tools",
     tags= "tools",
     description = "Mail related operations",
     position = 100)
@Controller("mail")
public class MailApi {

    @Autowired
    LanguageUtils languageUtils;

    @ApiOperation(value = "Test mail configuration",
                  notes = "Send an email to the catalog feedback email.",
                  nickname = "testMailConfiguration")
    @RequestMapping(value = "/test",
                    produces = MediaType.TEXT_PLAIN_VALUE,
                    method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<String> testMailConfiguration(ServletRequest request) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);


        Profile profile = ServiceContext.get().getUserSession().getProfile();
        if (profile != Profile.Administrator) {
            throw new SecurityException(messages.getString("mail_config_test_only_admin"));
        }


        ApplicationContext appContext = ApplicationContextHolder.get();
        SettingManager sm = appContext.getBean(SettingManager.class);
        String to = sm.getValue("system/feedback/email");
        String subject = String.format(messages.getString(
                "mail_config_test_subject"),
                sm.getSiteName(),
                to);
        try {
            MailUtil.testSendMail(to, subject, "Empty message", sm, to, "");
            return new ResponseEntity<>(String.format(
                    messages.getString("mail_config_test_success"), to), HttpStatus.CREATED);
        } catch (Exception ex) {
            String error = ex.getMessage();
            if (ex.getCause() != null) error = error + ". " + ex.getCause().getMessage();
            return new ResponseEntity<>(String.format(
                    error), HttpStatus.PRECONDITION_FAILED);
        }

    }
}