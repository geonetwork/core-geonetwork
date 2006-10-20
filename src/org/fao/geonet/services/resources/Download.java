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

package org.fao.geonet.services.resources;

import jeeves.utils.*;
import org.fao.geonet.constants.*;
import org.fao.geonet.kernel.*;
import org.fao.geonet.exceptions.*;

import java.io.File;
import java.util.Iterator;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.util.ResUtil;
import org.jdom.Element;
import org.fao.geonet.util.MailSender;

//=============================================================================

/** Sends the resource to the client
  */

public class Download implements Service
{
	private String _from;
	private String _server;
	private String _port;

	//-----------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//-----------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		_from    = params.getValue(Geonet.Config.USER);
		_server  = params.getValue(Geonet.Config.MAIL_SERVER);
		_port    = params.getValue(Geonet.Config.PORT);
	}

	//-----------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//-----------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id     = Util.getParam(params, Params.ID);
		String fname  = Util.getParam(params, Params.FNAME);
		String access = Util.getParam(params, Params.ACCESS);

		boolean doNotify = false;
		if (access == null || access.equals(Params.Access.PRIVATE))
		{
			ResUtil.checkPrivilege(context, id, AccessManager.OPER_DOWNLOAD);
			doNotify = true;
		}
		// Build the response
		File dir = new File(ResUtil.getResDir(context, access, id));
		File file= new File(dir, fname);

		context.info("File is : " +file);

		if (!file.exists())
			throw new GeoNetException("resource '" + fname + "' not found", GeoNetException.FILE_NOT_FOUND);

		if (doNotify)
		{
			String fromDescr = "GeoNetwork administrator";

			// send emails about downloaded file to groups with notify privilege
			Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

			StringBuffer query = new StringBuffer();
			query.append("SELECT g.id, g.name, g.email ");
			query.append("FROM   OperationAllowed oa, Groups g ");
			query.append("WHERE  oa.operationId =" + AccessManager.OPER_NOTIFY + " ");
			query.append("AND    oa.metadataId = " + id + " ");
			query.append("AND    oa.groupId = g.id");

			Element groups = dbms.select(query.toString());
			for (Iterator i = groups.getChildren().iterator(); i.hasNext(); )
			{
				Element group = (Element)i.next();
				String  name  = group.getChildText("name");
				String  email = group.getChildText("email");

				String subject = "File " + fname + " has been downloaded";
				String message = "GeoNetwork notifies you, as supervisor of group "+ name
					+ " that data file "+ fname
					+ " belonging metadata "+ id
					+ " has beed downloaded from address " + context.getIpAddress() + ".";

				try
				{
					MailSender sender = new MailSender(context);
					sender.send(_server, Integer.parseInt(_port), _from, fromDescr, email, null, subject, message);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return BinaryFile.encode(200, file.getAbsolutePath());
	}
}

//=============================================================================

