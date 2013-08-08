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
import jeeves.exceptions.OperationAbortedEx;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.PasswordUtil;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.MailSendingService;
import org.jdom.Element;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//=============================================================================

/**
 * Send change password link email
 */

public class SendLink extends MailSendingService {

	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
		this.stylePath = appPath + FS + Geonet.Path.STYLESHEETS + FS;
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {

		String username = Util.getParam(params, Params.USERNAME);
		String template = Util.getParam(params, Params.TEMPLATE, CHANGE_EMAIL_XSLT);
		
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);
		
		// check valid user 
		Element elUser = dbms.select(	"SELECT * FROM Users WHERE username=?",username);
		if (elUser.getChildren().size() == 0)
			throw new UserNotFoundEx(username);

		// only let registered users change their password  
		if (!elUser.getChild("record").getChild("profile").getText().equals(Geonet.Profile.REGISTERED_USER)) 
			// Don't throw OperationNotAllowedEx because it is not related to not having enough priviledges
			throw new IllegalArgumentException("Only users with profile RegisteredUser can change their password using this option");
		
		// get mail settings		
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		String host = sm.getValue("system/feedback/mailServer/host");
		String port = sm.getValue("system/feedback/mailServer/port");
		String adminEmail = sm.getValue("system/feedback/email");
		String thisSite = sm.getValue("system/site/name");

		SettingInfo si = new SettingInfo(context);
		String siteURL = si.getSiteUrl() + context.getBaseUrl();

		// Do not allow an unconfigured site to send out change password emails
		if (thisSite == null || host == null || port == null || adminEmail == null || thisSite.equals("dummy") || host.equals("") || port.equals("") || adminEmail.equals("")) {
			throw new IllegalArgumentException("Missing settings in System Configuration (see Administration menu) - cannot change passwords");
		}
		
		// construct change key - only valid today 
		String scrambledPassword = elUser.getChild("record").getChildText(Params.PASSWORD);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String todaysDate = sdf.format(cal.getTime());
		String changeKey = PasswordUtil.encode(context, scrambledPassword+todaysDate);

		// generate email details using customisable stylesheet
		// TODO: allow internationalised emails
		Element root = new Element("root");
		root.addContent(new Element("username").setText(username));
		root.addContent(new Element("email").setText(elUser.getChild("record").getChildText("email")));
		root.addContent(new Element("site").setText(thisSite));
		root.addContent(new Element("siteURL").setText(siteURL));
		root.addContent(new Element("changeKey").setText(changeKey));
		
		String emailXslt = stylePath + template;
		Element elEmail = Xml.transform(root, emailXslt);

		String subject = elEmail.getChildText("subject");
		String to      = elEmail.getChildText("to");
		String content = elEmail.getChildText("content");
		
		// send change link via email
        if (!sendMail(host, Integer.parseInt(port), subject, adminEmail, to, content, PROTOCOL)) {
            throw new OperationAbortedEx("Could not send email");
		}

		return new Element(Jeeves.Elem.RESPONSE);
	}

	private static String FS = File.separator;
	private String stylePath;
	private static final String PROTOCOL = "smtp";
	private static final String CHANGE_EMAIL_XSLT = "password-forgotten-email.xsl";
	public static final String DATE_FORMAT = "yyyy-MM-dd";

}