//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.config;

import java.util.HashMap;
import java.util.Map;

import jeeves.constants.Jeeves;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

public class Set implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Map<String, Object> values = new HashMap<String, Object>();

		for (ConfigEntry ce : entries)
			ce.eval(values, params);

		if (!sm.setValues(dbms, values))
			throw new OperationAbortedEx("Cannot set all values");

		return new Element(Jeeves.Elem.RESPONSE).setText("ok");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Vars
	//---
	//--------------------------------------------------------------------------

	private ConfigEntry entries[] =
	{
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "site/name",                "system/site/name"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "site/organization",        "system/site/organization"),

		new ConfigEntry(ConfigEntry.Type.STRING, true,  "server/host",              "system/server/host"),
		new ConfigEntry(ConfigEntry.Type.INT,    false, "server/port",              "system/server/port"),

		new ConfigEntry(ConfigEntry.Type.STRING, true,  "intranet/network",         "system/intranet/network"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "intranet/netmask",         "system/intranet/netmask"),

		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "z3950/enable",             "system/z3950/enable"),
		new ConfigEntry(ConfigEntry.Type.INT,    false, "z3950/port",               "system/z3950/port"),
		
		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "csw/enable",               "system/csw/enable"),
		new ConfigEntry(ConfigEntry.Type.INT,    false, "csw/contactId",                               "system/csw/contactId"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/individualName",                          "system/csw/individualName"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/positionName",                            "system/csw/positionName"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/phone/voice",                 "system/csw/contactInfo/phone/voice"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/phone/facsimile",             "system/csw/contactInfo/phone/facsimile"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/address/deliveryPoint",       "system/csw/contactInfo/address/deliveryPoint"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/address/city",                "system/csw/contactInfo/address/city"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/address/administrativeArea",  "system/csw/contactInfo/address/administrativeArea"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/address/postalCode",          "system/csw/contactInfo/address/postalCode"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/address/country",             "system/csw/contactInfo/address/country"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/address/email",               "system/csw/contactInfo/address/email"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/hoursOfService",              "system/csw/contactInfo/hoursOfService"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/contactInfo/contactInstructions",         "system/csw/contactInfo/contactInstructions"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/role",                                    "system/csw/role"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/title",                                   "system/csw/title"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/abstract",                                "system/csw/abstract"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/fees",                                    "system/csw/fees"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "csw/accessConstraints",                       "system/csw/accessConstraints"),

		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "clickablehyperlinks/enable", "system/clickablehyperlinks/enable"),

		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "proxy/use",                "system/proxy/use"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "proxy/host",               "system/proxy/host"),
		new ConfigEntry(ConfigEntry.Type.INT,    false, "proxy/port",               "system/proxy/port"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "proxy/username",           "system/proxy/username"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "proxy/password",           "system/proxy/password"),

		new ConfigEntry(ConfigEntry.Type.STRING, false, "feedback/email",           "system/feedback/email"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "feedback/mailServer/host", "system/feedback/mailServer/host"),
		new ConfigEntry(ConfigEntry.Type.INT,    false, "feedback/mailServer/port", "system/feedback/mailServer/port"),

		new ConfigEntry(ConfigEntry.Type.STRING, true,  "removedMetadata/dir",      "system/removedMetadata/dir"),

		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "ldap/use",                     "system/ldap/use"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "ldap/host",                    "system/ldap/host"),
		new ConfigEntry(ConfigEntry.Type.INT,    false, "ldap/port",                    "system/ldap/port"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "ldap/defaultProfile",          "system/ldap/defaultProfile"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "ldap/distinguishedNames/base", "system/ldap/distinguishedNames/base"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "ldap/distinguishedNames/users","system/ldap/distinguishedNames/users"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "ldap/userAttribs/name",        "system/ldap/userAttribs/name"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "ldap/userAttribs/password",    "system/ldap/userAttribs/password"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "ldap/userAttribs/profile",     "system/ldap/userAttribs/profile"),

		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "userSelfRegistration/enable",     "system/userSelfRegistration/enable"),

		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "shib/use",                     "system/shib/use"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "shib/path",                    "system/shib/path"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "shib/attrib/username",         "system/shib/attrib/username"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "shib/attrib/surname",          "system/shib/attrib/surname"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "shib/attrib/firstname",        "system/shib/attrib/firstname"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "shib/attrib/profile",          "system/shib/attrib/profile")
	};
}

//=============================================================================

class ConfigEntry
{
	/** @param mandatory Means that the value, if the element is present, cannot be empty */
	public ConfigEntry(Type type, boolean mandatory, String srcPath, String desPath)
	{
		this.srcPath   = srcPath;
		this.desPath   = desPath;
		this.type      = type;
		this.mandatory = mandatory;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public void eval(Map<String, Object> values, Element elem) throws BadInputEx
	{
		String value = Lib.element.eval(elem, srcPath);

		if (value == null)
			return;

		//--- ok, the element is present

		if (mandatory)
		{
			if (value.length() == 0)
				throw new BadParameterEx("srcPath", value);

			checkValue(value);
		}
		else
		{
			if (value.length() != 0)
				checkValue(value);
		}

		values.put(desPath, value);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private void checkValue(String value) throws BadInputEx
	{
		if (type == Type.INT && !Lib.type.isInteger(value))
			throw new BadParameterEx("srcPath", value);

		else if (type == Type.BOOL && !Lib.type.isBoolean(value))
			throw new BadParameterEx("srcPath", value);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	enum Type { STRING, INT, BOOL }

	//--------------------------------------------------------------------------

	private String  srcPath;
	private String  desPath;
	private Type    type;
	private boolean mandatory;
}

//=============================================================================

