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

package org.fao.geonet.services.register;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

//=============================================================================

/**
 * Register user.
 */

public class SelfRegister implements Service {

	private static final String PROFILE = "RegisteredUser";
	private static final String PROTOCOL = "smtp";

	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {

		String surname = Util.getParam(params, Params.SURNAME);
		String name = Util.getParam(params, Params.NAME);
		String email = Util.getParam(params, Params.EMAIL);
		String profile = Util.getParam(params, Params.PROFILE);

		String address = Util.getParam(params, Params.ADDRESS, "");
		String city = Util.getParam(params, Params.CITY, "");
		String state = Util.getParam(params, Params.STATE, "");
		String zip = Util.getParam(params, Params.ZIP, "");
		String country = Util.getParam(params, Params.COUNTRY, "");

		String organ = Util.getParam(params, Params.ORG, "");
		String kind = Util.getParam(params, Params.KIND, "");

		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);
		
		String username = getUsername(dbms, name, surname);
		String password = getInitPassword();

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();
		
		String host = sm.getValue("system/feedback/mailServer/host");
		String port = sm.getValue("system/feedback/mailServer/port");
		String from = sm.getValue("system/feedback/email");
		String thisSite = sm.getValue("system/site/name");

		// Do not allow an unconfigured site to send out self-registration emails
		if (thisSite == null || host == null || port == null || from == null || thisSite.equals("dummy") || host.equals("") || port.equals("") || from.equals("")) {
			throw new IllegalArgumentException("Missing settings in System Configuration (see Administration menu) - cannot do self registration");
		}
		
		Element element = new Element(Jeeves.Elem.RESPONSE);
		element.setAttribute(Params.SURNAME,surname);
		element.setAttribute(Params.NAME,name);
		element.setAttribute(Params.EMAIL,email);

		if (checkUserEmail(dbms, email, name, surname)) {
			String id = context.getSerialFactory().getSerial(dbms, "Users")
					+ "";
			String group = getGroupID(dbms);
			String query = "INSERT INTO Users (id, username, password, surname, name, profile, "
					+ "address, city, state, zip, country, email, organisation, kind) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			dbms.execute(query, new Integer(id), username, Util
					.scramble(password), surname, name, PROFILE, address,
					state, zip, country, email, organ, kind);

			dbms.execute(
					"INSERT INTO UserGroups(userId, groupId) VALUES (?, ?)",
					new Integer(id), new Integer(group));
			
			String subject = "Your registration at "+thisSite;
			
			SettingInfo si = new SettingInfo(context);
			String siteURL = si.getSiteUrl() + context.getBaseUrl();
			String content = getContent(username, password, profile, siteURL, thisSite);

			if (sendMail(host, Integer.parseInt(port), subject, from, email, content)) {
				dbms.commit();
			} else {
				dbms.abort();
				element.addContent(new Element("result").setText("errorEmailToAddressFailed"));
			}
		} else {
			element.addContent(new Element("result").setText("errorEmailAddressAlreadyRegistered"));
		}
		return element;
	}

	// --------------------------------------------------------------------------

	/**
	 * Get content for the email message to user.
	 * 
	 * @param username
	 * @param password
	 * @param profile
	 * @param siteURL
	 * @param thisSite
	 * @return
	 */
	private String getContent(String username, String password, String profile, String siteURL, String thisSite) {
	
		String mailContent = "\n" + "Dear User, \n\n"
				+ "  Your registration at "+thisSite+" was successful. \n\n"
				+ "  Your account is: \n" + "  username :	rusername\n"
				+ "  password :	rpassword\n" + "  usergroup:	GUEST\n"
				+ "  usertype :	REGISTEREDUSER\n\n";
		
		if (!profile.equalsIgnoreCase("RegisteredUser")) {
			mailContent = mailContent
					+ "  You've told us that you want to be \""
					+ profile
					+ "\","
					+ " you will be contacted by our office soon.\n\n";
		}
	
		mailContent = mailContent
				+ "  To log in and access your account, please click on the link below.\n"
				+ "  " + siteURL + " \n\n"
				+ "  Thanks for your registration. \n\n\n"
				+ "Yours sincerely,\n" + "The team at "+thisSite;
		
		mailContent = mailContent.replaceFirst("rusername", username)
				.replaceFirst("rpassword", password);
		
		return mailContent;
	}

	// --------------------------------------------------------------------------
		
	/**
	 * Send the mail to the registering user.
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
			msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(from));
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

	// --------------------------------------------------------------------------
		
	/**
	 * 
	 * @param dbms
	 * @param mail
	 * @return
	 * @throws SQLException
	 */
	boolean checkUserEmail(Dbms dbms, String mail, String name, String surname) throws SQLException {
		String username = surname + String.valueOf(name.charAt(0)).toUpperCase();

		Element e = dbms.select("SELECT email FROM Users WHERE lower(email)=lower(?) and username like ?||'%'", mail, username);
		return (e.getChildren().size() == 0);
	}

	// --------------------------------------------------------------------------
		
	/**
	 * Get group id.
	 */
	String getGroupID(Dbms dbms) throws SQLException {
		String sql = "select id from Groups where name=?";
		Element e = dbms.select(sql, "GUEST");
		return e.getChild("record").getChild("id").getText();
	}

	// --------------------------------------------------------------------------
		
	/**
	 * Get init password.
	 */
	String getInitPassword() {
		Random random = new Random();
		String password = "";
		String rand = "";
		char c = 'a';
		for (int i = 0; i < 6; i++) {
			int j = random.nextInt(10);
			if (j < 5) {
				if (j < 3) {
					rand = String.valueOf(
							(char) (c + (int) (Math.random() * 26)))
							.toUpperCase();
				} else {
					rand = String.valueOf(
							(char) (c + (int) (Math.random() * 26)))
							.toLowerCase();
				}
			} else {
				rand = String.valueOf(random.nextInt(10));
			}
			password += rand;
		}
		return password;
	}

	// --------------------------------------------------------------------------
		
	/**
	 * Get valid username.
	 */
	String getUsername(Dbms dbms, String name, String surname)
			throws SQLException {
		String username = surname
				+ String.valueOf(name.charAt(0)).toUpperCase();

		Element user = dbms.select(
				"SELECT username FROM Users WHERE username=?", username);
		if (user.getChildren().size() == 0) {
			return username;
		} else {
			int i = 0;
			String n = "";
			while (true) {
				i++;
				DecimalFormat format = new java.text.DecimalFormat("000");
				n = username + format.format(i);

				user = dbms.select("SELECT * FROM Users WHERE username=?", n);
				if (user.getChildren().size() == 0) {
					username = n;
					break;
				}
			}
		}
		return username;
	}

}

// =============================================================================

