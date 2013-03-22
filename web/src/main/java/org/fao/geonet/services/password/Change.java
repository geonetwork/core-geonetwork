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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import jeeves.constants.Jeeves;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.interfaces.Service;
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
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Change users password given the correct change key generated for the user.
 */
public class Change extends NotInReadOnlyModeService {

	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
		this.appPath = appPath;
		this.stylePath = appPath + FS + Geonet.Path.STYLESHEETS + FS;
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context)
			throws Exception {

		String username = Util.getParam(params, Params.USERNAME);
		String password = Util.getParam(params, Params.PASSWORD);
		String changeKey = Util.getParam(params, CHANGE_KEY);
		String template = Util.getParam(params, Params.TEMPLATE, PWD_CHANGED_XSLT);
		
		// check valid user 
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);
		Element elUser = dbms.select(	"SELECT * FROM Users WHERE username=?", username);
		if (elUser.getChildren().size() == 0)
			throw new UserNotFoundEx(username);

		// only let registered users change their password this way  
		if (!elUser.getChild("record").getChild("profile").getText().equals(Geonet.Profile.REGISTERED_USER)) 
			throw new OperationNotAllowedEx("Only users with profile RegisteredUser can change their password using this option");
		
		// construct expected change key - only valid today 
		String scrambledPassword = elUser.getChild("record").getChildText(Params.PASSWORD);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String todaysDate = sdf.format(cal.getTime());
		boolean passwordMatches = PasswordUtil.encoder(context.getServlet().getServletContext()).matches(changeKey, scrambledPassword+todaysDate);

		//check change key
		if (!passwordMatches)
			throw new BadParameterEx("Change key invalid or expired", changeKey);
		
		// get mail details
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		String host = sm.getValue("system/feedback/mailServer/host");
		String port = sm.getValue("system/feedback/mailServer/port");
		String adminEmail = sm.getValue("system/feedback/email");
		String thisSite = sm.getValue("system/site/name");

		// get site URL
		SettingInfo si = new SettingInfo(context);
		String siteURL = si.getSiteUrl() + context.getBaseUrl();

		// Do not allow an unconfigured site to send out change password emails
		if (thisSite == null || host == null || port == null || adminEmail == null || thisSite.equals("dummy") || host.equals("") || port.equals("") || adminEmail.equals("")) {
			throw new IllegalArgumentException("Missing settings in System Configuration (see Administration menu) - cannot change passwords");
		}
		
		// All ok so update password
		dbms.execute ( "UPDATE Users SET password=? WHERE username=?", PasswordUtil.encode(context, password), username);

		// generate email details using customisable stylesheet
		//TODO: allow internationalised emails
		Element root = new Element("root");
		root.addContent((Element)elUser.getChild("record").clone());
		root.addContent(new Element("site").setText(thisSite));
		root.addContent(new Element("siteURL").setText(siteURL));
		root.addContent(new Element("adminEmail").setText(adminEmail));
		root.addContent(new Element("password").setText(password));
		
		String emailXslt = stylePath + template;
		Element elEmail = Xml.transform(root, emailXslt);

		String subject = elEmail.getChildText("subject");
		String to      = elEmail.getChildText("to");
		String content = elEmail.getChildText("content");
		
		// send password changed email
		if (!sendMail(host, Integer.parseInt(port), subject, adminEmail, to, content)) {
			throw new OperationAbortedEx("Could not send email");
		}

		return new Element(Jeeves.Elem.RESPONSE);
	}

	// --------------------------------------------------------------------------
		
	/**
	 * Send the mail to the user.
	 * 
	 * @param from
	 * @param to
	 * @param protocol
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 */
	boolean sendMail(String host, int port, String subject, String from, String to, String content) {
		boolean isSendout = false;

		Properties props = new Properties();
		
		props.put("mail.transport.protocol", PROTOCOL);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "false");

		Session mailSession = Session.getDefaultInstance(props);

		try {
			Message msg = new MimeMessage(mailSession);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			msg.setSentDate(new Date());
			msg.setSubject(subject);
			// Add content message
			msg.setText(content);
			Transport.send(msg);
			isSendout = true;
		} catch (AddressException e) {
			isSendout = false;
			e.printStackTrace();
		} catch (MessagingException e) {
			isSendout = false;
			e.printStackTrace();
		}
		return isSendout;
	}

	private static String FS = File.separator;
	private String appPath;
	private String stylePath;
	private static final String PROTOCOL = "smtp";
	private static final String CONFIRM = "confirm";
	private static final String CHANGE_KEY = "changeKey";
	private static final String PWD_CHANGED_XSLT = "password-changed-email.xsl";
	public static final String DATE_FORMAT = "yyyy-MM-dd";

}