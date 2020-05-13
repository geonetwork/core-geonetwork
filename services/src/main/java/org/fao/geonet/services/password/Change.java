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

package org.fao.geonet.services.password;

import jeeves.constants.Jeeves;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.exceptions.UserNotFoundEx;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.PasswordUtil;
import org.fao.geonet.util.MailUtil;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Change users password given the correct change key generated for the user.
 */
@Deprecated
public class Change extends NotInReadOnlyModeService {


    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------
    private static final String CHANGE_KEY = "changeKey";
    private static final String PWD_CHANGED_XSLT = "password-changed-email.xsl";
    private static String FS = File.separator;
    private Path stylePath;

    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.stylePath = appPath.resolve(Geonet.Path.XSLT_FOLDER).resolve("services").resolve("account");
    }

    public Element serviceSpecificExec(Element params, ServiceContext context)
        throws Exception {

        String username = Util.getParam(params, Params.USERNAME);
        String password = Util.getParam(params, Params.PASSWORD);
        String changeKey = Util.getParam(params, CHANGE_KEY);
        String template = Util.getParam(params, Params.TEMPLATE, PWD_CHANGED_XSLT);

        // check valid user
        final UserRepository userRepository = context.getBean(UserRepository.class);
        User elUser = userRepository.findOneByUsername(username);
        if (elUser == null) {
            throw new UserNotFoundEx(username);
        }


        // only let registered users change their password this way
        if (elUser.getProfile() != Profile.RegisteredUser) {
            throw new OperationNotAllowedEx("Only users with profile RegisteredUser can change their password using this option");
        }

        // construct expected change key - only valid today
        String scrambledPassword = elUser.getPassword();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String todaysDate = sdf.format(cal.getTime());
        boolean passwordMatches = PasswordUtil.encoder(context.getServlet().getServletContext()).matches(scrambledPassword + todaysDate, changeKey);

        //check change key
        if (!passwordMatches)
            throw new BadParameterEx("Change key invalid or expired", changeKey);

        // get mail details
        SettingManager sm = context.getBean(SettingManager.class);

        String adminEmail = sm.getValue("system/feedback/email");
        String thisSite = sm.getSiteName();

        // get site URL
        SettingInfo si = context.getBean(SettingInfo.class);
        String siteURL = si.getSiteUrl() + context.getBaseUrl();

        elUser.getSecurity().setPassword(PasswordUtil.encode(context, password));
        userRepository.save(elUser);


        // generate email details using customisable stylesheet
        //TODO: allow internationalised emails
        Element root = new Element("root");
        root.addContent(elUser.asXml());
        root.addContent(new Element("site").setText(thisSite));
        root.addContent(new Element("siteURL").setText(siteURL));
        root.addContent(new Element("adminEmail").setText(adminEmail));
        root.addContent(new Element("password").setText(password));

        FilePathChecker.verify(template);
        Path emailXslt = stylePath.resolve(template);
        Element elEmail = Xml.transform(root, emailXslt);

        String subject = elEmail.getChildText("subject");
        String to = elEmail.getChildText("to");
        String content = elEmail.getChildText("content");

        // send password changed email
        if (!MailUtil.sendMail(to, subject, content, null, sm, adminEmail, "")) {
            throw new OperationAbortedEx("Could not send email");
        }

        return new Element(Jeeves.Elem.RESPONSE);
    }

}
