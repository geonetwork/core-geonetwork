package org.fao.geonet.kernel.security;

import jeeves.guiservices.session.JeevesUser;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

public class GeonetworkUser extends JeevesUser {
	private static final long serialVersionUID = 1279946739116277388L;

	public GeonetworkUser(String username, Element userXml) {
		setUsername(username);
		setId(userXml.getChildTextTrim(Geonet.Elem.ID));
		setPassword(userXml.getChildTextTrim("password"));
		setEmail(userXml.getChildTextTrim(Geonet.Elem.EMAIL));
		setName(userXml.getChildTextTrim(Geonet.Elem.NAME));
		setSurname(userXml.getChildTextTrim(Geonet.Elem.SURNAME));
		setProfile(userXml.getChildTextTrim(Geonet.Elem.PROFILE));
		setAddress(userXml.getChildTextTrim("address"));
		setCity(userXml.getChildTextTrim("city"));
		setState(userXml.getChildTextTrim("state"));
		setZip(userXml.getChildTextTrim("zip"));
		setCity(userXml.getChildTextTrim("country"));
		setOrganisation(userXml.getChildTextTrim("organisation"));
		setKind(userXml.getChildTextTrim("kind"));
	}

}
