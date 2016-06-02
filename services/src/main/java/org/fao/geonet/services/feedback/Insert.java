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

package org.fao.geonet.services.feedback;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.MailSender;
import org.jdom.Element;

import java.nio.file.Path;

//=============================================================================

/** Stores the feedback from a user into the database and sends an e-mail
  */

public class Insert implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(Path appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, final ServiceContext context) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		String name     = Util.getParam(params, Params.NAME);
		String org      = Util.getParam(params, Params.ORG);
		String email    = Util.getParam(params, Params.EMAIL);
		String comments = Util.getParam(params, Params.COMMENTS);
		String subject  = Util.getParam(params, Params.SUBJECT, "New feedback");
        // TODO : i18n
        // TODO : Would be good to add captcha to avoid spamming catalog admin

		String host = sm.getValue("system/feedback/mailServer/host");
		String to   = sm.getValue("system/feedback/email");

		MailSender sender = new MailSender(context);
		sender.send(host, 
		        sm.getValueAsInt("system/feedback/mailServer/port"), 
		        sm.getValue("system/feedback/mailServer/username"), 
		        sm.getValue("system/feedback/mailServer/password"), 
		        sm.getValueAsBool("system/feedback/mailServer/ssl"), 
				sm.getValueAsBool("system/feedback/mailServer/tls"),
		        email, name +" ("+org+")", to, null, subject, comments);

		return new Element("response").addContent(params.cloneContent());
	}
}

//=============================================================================


