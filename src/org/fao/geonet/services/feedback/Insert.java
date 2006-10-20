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

package org.fao.geonet.services.feedback;

import org.fao.geonet.constants.*;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.util.MailSender;
import org.jdom.Element;

//=============================================================================

/** Stores the feedback from a user into the database and sends an e-mail
  */

public class Insert implements Service
{
	private String subject;
	private String user;
	private String server;
	private String port;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		subject = params.getMandatoryValue(Geonet.Config.SUBJECT);
		user    = params.getMandatoryValue(Geonet.Config.USER);
		server  = params.getMandatoryValue(Geonet.Config.MAIL_SERVER);
		port    = params.getMandatoryValue(Geonet.Config.PORT);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, final ServiceContext context) throws Exception
	{
		final String name     = Util.getParam(params, Params.NAME);
		final String org      = Util.getParam(params, Params.ORG);
		final String email    = Util.getParam(params, Params.EMAIL);
		final String comments = Util.getParam(params, Params.COMMENTS);

		MailSender sender = new MailSender(context);
		sender.send(server, Integer.parseInt(port), email, name +" ("+org+")", user, null, subject, comments);
		
		return params;
	}
}

//=============================================================================


