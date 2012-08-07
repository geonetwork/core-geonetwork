package org.fao.geonet.kernel.security;

import jeeves.guiservices.session.JeevesUser;
import jeeves.server.ProfileManager;
import jeeves.utils.PasswordUtil;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

public class GeonetworkUser extends JeevesUser {
	public static final String KIND_COLUMN = "kind";
	public static final String ORGANISATION_COLUMN = "organisation";
	public static final String COUNTRY_COLUMN = "country";
	public static final String ZIP_COLUMN = "zip";
	public static final String STATE_COLUMN = "state";
	public static final String CITY_COLUMN = "city";
	public static final String ADDRESS_COLUMN = "address";
	public static final long serialVersionUID = 1279946739116277388L;
	public static final String USERNAME_COLUMN = "username";

	public GeonetworkUser(ProfileManager profileManager, String username, Element userXml) {
		super(profileManager);
		setUsername(username);
		setId(userXml.getChildTextTrim(Geonet.Elem.ID));
		setPassword(userXml.getChildTextTrim(PasswordUtil.PASSWORD_COLUMN));
		setEmail(userXml.getChildTextTrim(Geonet.Elem.EMAIL));
		setName(userXml.getChildTextTrim(Geonet.Elem.NAME));
		setSurname(userXml.getChildTextTrim(Geonet.Elem.SURNAME));
		setProfile(userXml.getChildTextTrim(Geonet.Elem.PROFILE));
		setAddress(userXml.getChildTextTrim(ADDRESS_COLUMN));
		setCity(userXml.getChildTextTrim(CITY_COLUMN));
		setState(userXml.getChildTextTrim(STATE_COLUMN));
		setZip(userXml.getChildTextTrim(ZIP_COLUMN));
		setCity(userXml.getChildTextTrim(COUNTRY_COLUMN));
		setOrganisation(userXml.getChildTextTrim(ORGANISATION_COLUMN));
		setKind(userXml.getChildTextTrim(KIND_COLUMN));
		
		
	}

}
