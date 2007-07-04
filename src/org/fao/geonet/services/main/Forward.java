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

package org.fao.geonet.services.main;

import java.net.URL;
import java.util.List;
import jeeves.exceptions.MissingParameterEx;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.XmlRequest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

public class Forward implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		Element site = Util.getChild(params, "site");
		Element par  = Util.getChild(params, "params");
		Element acc  = site.getChild("account");

		String url  = Util.getParam(site, "url");
		String type = Util.getParam(site, "type", "generic");

		String username = (acc == null) ? null : Util.getParam(acc, "username");
		String password = (acc == null) ? null : Util.getParam(acc, "password");

		List list = par.getChildren();

		if (list.size() == 0)
			throw new MissingParameterEx("<request>", par);

		params = (Element) list.get(0);

		XmlRequest req = new XmlRequest(new URL(url));

		//--- does we need to authenticate?

		if (username != null)
			authenticate(req, username, password, type);

		Lib.net.setupProxy(context, req);
		req.setRequest(params);

		return req.execute();
	}

	//--------------------------------------------------------------------------

	private void authenticate(XmlRequest req, String username, String password,
									  String type) throws Exception
	{
		if (!type.equals("geonetwork"))
			//--- set basic/digest credentials for non geonetwork sites
			req.setCredentials(username, password);

		else
		{
			String addr = req.getAddress();
			int    pos  = addr.lastIndexOf("/");

			req.setAddress(addr.substring(0, pos +1) + Geonet.Service.XML_LOGIN);
			req.addParam("username", username);
			req.addParam("password", password);

			Element response = req.execute();

			if (!response.getName().equals("ok"))
				throw new UserNotFoundEx(username);

			req.setAddress(addr);
		}
	}
}

//=============================================================================

