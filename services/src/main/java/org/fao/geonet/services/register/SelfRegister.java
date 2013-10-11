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
import java.util.Random;

import jeeves.constants.Jeeves;
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
import org.fao.geonet.util.MailUtil;
import org.jdom.Element;

/**
 * Register user.
 */
public class SelfRegister extends NotInReadOnlyModeService {

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

	public Element serviceSpecificExec(Element params, ServiceContext context)
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
		SettingManager sm = gc.getBean(SettingManager.class);
		
		String catalogAdminEmail = sm.getValue("system/feedback/email");
		String thisSite = sm.getValue("system/site/name");

		
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
		dbms.execute(query, Integer.valueOf(id), username, passwordHash , surname, name, PROFILE, address,
				city, state, zip, country, email, organ, kind);

		dbms.execute("INSERT INTO UserGroups(userId, profile, groupId) VALUES (?, ?, ?)", Integer.valueOf(id), PROFILE, Integer.valueOf(group));

		// Send email to user confirming registration

	SettingInfo si = new SettingInfo(context);
	String siteURL = si.getSiteUrl() + context.getBaseUrl();

	if (!sendRegistrationEmail(params, password, catalogAdminEmail, thisSite, siteURL, sm)) {
	  dbms.abort();
	  return element.addContent(new Element("result").setText("errorEmailToAddressFailed"));
	}

	// Send email to admin requesting non-standard profile if required

	if (!profile.equalsIgnoreCase(Geonet.Profile.REGISTERED_USER) && !sendProfileRequest(params, catalogAdminEmail, thisSite, siteURL, sm)) {
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
			String from, String thisSite,
			String siteURL, SettingManager sm) throws Exception, SQLException {
		
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

		return MailUtil.sendMail(email, subject, message, sm);
	}

	/**
	 * Send the profile request to the catalog administrator.
	 * 
	 * @param params
	 * @param from
	 * @param thisSite
	 * @param siteURL
	 * @return
	 */
	private boolean sendProfileRequest(Element params, String from, String thisSite, String siteURL,
			SettingManager sm) throws Exception {
		
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

		return MailUtil.sendMail(from, subject, message, sm);
	}

	/**
	 * Check if the user exists. 
   *
	 * @param dbms
	 * @param mail
	 * @return
	 * @throws java.sql.SQLException
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
	 * @throws java.sql.SQLException
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
		StringBuilder password = new StringBuilder();
		char c = 'a';
		for (int i = 0; i < 6; i++) {
			int j = random.nextInt(10);
			String rand;
			if (j < 5) {
				if (j < 3) {
					rand = String.valueOf(
							(char) (c + (int) (random.nextInt() * 26)))
							.toUpperCase();
				} else {
					rand = String.valueOf(
							(char) (c + (int) (random.nextInt() * 26)))
							.toLowerCase();
				}
			} else {
				rand = String.valueOf(random.nextInt(10));
			}
			password.append(rand);
		}
		return password.toString();
	}

}