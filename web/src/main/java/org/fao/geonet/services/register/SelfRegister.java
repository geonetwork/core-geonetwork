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

import java.io.File;
import java.sql.SQLException;
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
import jeeves.utils.PasswordUtil;
import jeeves.utils.Util;
import jeeves.utils.Xml;

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

	private static final String PROFILE_TEMPLATE = "profileTemplate";
	private static final String PROFILE = "RegisteredUser";
	private static final String PROTOCOL = "smtp";
	private static String FS = File.separator;
	private String stylePath;
	private static final String PASSWORD_EMAIL_XSLT = "registration-pwd-email.xsl";
	private static final String PROFILE_EMAIL_XSLT = "registration-prof-email.xsl";

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
		
		String username = email;
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

		if (userExists(dbms, email)) {
			return element.addContent(new Element("result").setText("errorEmailAddressAlreadyRegistered"));
		}

		// Add new user to database

		String id = context.getSerialFactory().getSerial(dbms, "Users")
					+ "";
		String group = getGroupID(dbms);
		String query = "INSERT INTO Users (id, username, password, surname, name, profile, "
				+ "address, city, state, zip, country, email, organisation, kind) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String passwordHash = PasswordUtil.encode(context, password);
		dbms.execute(query, new Integer(id), username, passwordHash , surname, name, PROFILE, address,
				city, state, zip, country, email, organ, kind);

		dbms.execute("INSERT INTO UserGroups(userId, groupId) VALUES (?, ?)", new Integer(id), new Integer(group));

		// Send email to user confirming registration

    SettingInfo si = new SettingInfo(context);
    String siteURL = si.getSiteUrl() + context.getBaseUrl();

    if (!sendRegistrationEmail(params, password, host, port, from, thisSite, siteURL)) {
      dbms.abort();
      return element.addContent(new Element("result").setText("errorEmailToAddressFailed"));
    }

    // Send email to admin requesting non-standard profile if required

    if (!profile.equalsIgnoreCase(Geonet.Profile.REGISTERED_USER) && !sendProfileRequest(params, host, port, from, thisSite, siteURL)) {
        return element.addContent(new Element("result").setText("errorProfileRequestFailed"));
      }

    return element.setAttribute(Params.USERNAME, username);
	}

	/**
	 * Send the mail to the registering user.
	 * 
	 * @param params
	 * @param password
	 * @param host
	 * @param port
	 * @param from
	 * @param thisSite
	 * @param siteURL
	 * @return
	 */
	private boolean sendRegistrationEmail(Element params, String password,
            String host, String port, String from, String thisSite,
            String siteURL) throws Exception, SQLException {
		
	    //TODO: allow internationalised emails
		
		Element root = new Element("root");
	    
	    root.addContent(new Element("site").setText(thisSite));
	    root.addContent(new Element("siteURL").setText(siteURL));
	    root.addContent((Element)params.clone());
	    root.addContent(new Element("password").setText(password));
	    
		String template = Util.getParam(params, Params.TEMPLATE, PASSWORD_EMAIL_XSLT);
	    String emailXslt = stylePath + template;
	    Element elEmail = Xml.transform(root, emailXslt);
	    
		String email = Util.getParam(params, Params.EMAIL);
	    String subject = elEmail.getChildText("subject");
	    String message = elEmail.getChildText("content");

	    return sendMail(host, Integer.parseInt(port), subject, from, email, message);
    }

	/**
	 * Send the profile request.
	 * 
	 * @param params
	 * @param host
	 * @param port
	 * @param from
	 * @param thisSite
	 * @param siteURL
	 * @return
	 */
	private boolean sendProfileRequest(Element params, String host, String port, String from,
			String thisSite, String siteURL) throws Exception {
		
	    //TODO: allow internationalised emails
		
	    Element root = new Element("root");
	    
	    root.addContent(new Element("site").setText(thisSite));
	    root.addContent(new Element("siteURL").setText(siteURL));
	    root.addContent((Element)params.clone());
	    
		String profileTemplate = Util.getParam(params, PROFILE_TEMPLATE, PROFILE_EMAIL_XSLT);
	    String emailXslt = stylePath + profileTemplate;
	    Element elEmail = Xml.transform(root, emailXslt);
	    
	    String subject = elEmail.getChildText("subject");
	    String message = elEmail.getChildText("content");
	    
	    return sendMail(host, Integer.parseInt(port), subject, from, from, message);
    }

	// --------------------------------------------------------------------------
		
	/**
	 * Send an email.
	 * 
	 * @param host
	 * @param port
	 * @param subject
	 * @param from
	 * @param to
	 * @param content
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

	// --------------------------------------------------------------------------
		
	/**
	 * Check if the user exists. 
   *
	 * @param dbms
	 * @param mail
	 * @return
	 * @throws SQLException
	 */
	boolean userExists(Dbms dbms, String mail) throws SQLException {
		Element e = dbms.select("SELECT email " +
                "FROM Users " +
                "WHERE lower(username)=lower(?)",
                 mail);
		return (e.getChildren().size() > 0);
	}

	// --------------------------------------------------------------------------
		
	/**
	 * Get group id.
   *
	 * @param dbms
	 * @return
	 * @throws SQLException
	 */
	String getGroupID(Dbms dbms) throws SQLException {
		String sql = "select id from Groups where name=?";
		Element e = dbms.select(sql, "GUEST");
		return e.getChild("record").getChild("id").getText();
	}

	// --------------------------------------------------------------------------
		
	/**
	 * Get initial password - a randomly generated string.
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

}

// =============================================================================

