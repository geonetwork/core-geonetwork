//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.config;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import jeeves.constants.Jeeves;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.BadInputEx;
import org.fao.geonet.lib.Lib;

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
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "server/port",              "system/server/port"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "intranet/network",         "system/intranet/network"),
		new ConfigEntry(ConfigEntry.Type.STRING, true,  "intranet/netmask",         "system/intranet/netmask"),
		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "z3950/enable",             "system/z3950/enable"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "z3950/port",               "system/z3950/port"),
		new ConfigEntry(ConfigEntry.Type.BOOL,   true,  "proxy/use",                "system/proxy/use"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "proxy/host",               "system/proxy/host"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "proxy/port",               "system/proxy/port"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "feedback/email",           "system/feedback/email"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "feedback/mailServer/host", "system/feedback/mailServer/host"),
		new ConfigEntry(ConfigEntry.Type.STRING, false, "feedback/mailServer/port", "system/feedback/mailServer/port")
	};
}

//=============================================================================

class ConfigEntry
{
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

		if (mandatory && value.length() == 0)
			throw new BadParameterEx("srcPath", value);

		checkValue(value);
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

