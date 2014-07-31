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

import java.net.URL;
import java.util.List;

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

		List<Element> list = par.getChildren();

		if (list.size() == 0)
			throw new MissingParameterEx("<request>", par);

		XmlRequest req = new XmlRequest(new URL(url));

		//--- do we need to authenticate?

		if (username != null)
			authenticate(context, req, username, password, type);

		Lib.net.setupProxy(context, req);

		if (list.size() == 1) {
			params = (Element) list.get(0);
			req.setRequest(params); 
		} else {
			for (int i = 0; i < list.size();i++) {
				Element elem = (Element) list.get(i);
				req.addParam(elem.getName(), elem.getText());
			}
		}

		Element result = req.execute();
		return result;
	}

	//--------------------------------------------------------------------------

	private void authenticate(ServiceContext context, XmlRequest req, String username, String password, String type) throws Exception
	{
    if (type.equals("geonetwork")) {
      String addr = req.getAddress();
      int    pos  = addr.lastIndexOf('/');
      String addrBase = addr.substring(0,pos +1);
      try {
        context.info("Login check using service : "+req.getAddress()+ " on host "+req.getHost()+" port "+req.getPort());
        req.setCredentials(username, password);
        req.setAddress(addrBase+Geonet.Service.XML_INFO+"?type=me");
        Element response = req.execute();
        if (!response.getName().equals("info") 
              || response.getChild("me") == null) {
          pre29Login(context, addrBase, req, username, password);
        } else if (!"true".equals(response.getChild("me").getAttributeValue("authenticated"))) {
          throw new UserNotFoundEx(username);
        }
      } catch (Exception e) {
        pre29Login(context, addrBase, req, username, password);
      }
      req.setAddress(addr);
    } else {
		  //--- set basic/digest credentials
		  req.setCredentials(username, password);
    }
	}

	//--------------------------------------------------------------------------

  private void pre29Login(ServiceContext context, String addrBase, XmlRequest req, String username, String password) throws Exception
  {
      context.info("Login using pre-2.9 service "+Geonet.Service.XML_LOGIN);
      req.setAddress(addrBase+Geonet.Service.XML_LOGIN);
      req.addParam("username", username);
      req.addParam("password", password);

      Element response = req.execute();
      if (!response.getName().equals("ok"))
        throw new UserNotFoundEx(username);
  }   
}

//=============================================================================

