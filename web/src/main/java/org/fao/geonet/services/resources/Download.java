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

package org.fao.geonet.services.resources;

import java.io.File;
import java.util.Iterator;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.ResourceNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.MailSender;
import org.jdom.Element;

//=============================================================================

/** Sends the resource to the client
  */

public class Download implements Service
{
	//-----------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//-----------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//-----------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//-----------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id     = Util.getParam(params, Params.ID);
		String fname  = Util.getParam(params, Params.FNAME);
		String access = Util.getParam(params, Params.ACCESS, Params.Access.PUBLIC);

		boolean doNotify = false;

		if (fname.contains("..")) {
			throw new BadParameterEx("Resource contains invalid characters.", fname);
		}

		if (access.equals(Params.Access.PRIVATE))
		{
			Lib.resource.checkPrivilege(context, id, AccessManager.OPER_DOWNLOAD);
			doNotify = true;
		}

		// Build the response
		File dir = new File(Lib.resource.getDir(context, access, id));
		File file= new File(dir, fname);

		context.info("File is : " +file);

		if (!file.exists())
			throw new ResourceNotFoundEx(fname);

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();
		DataManager    dm = gc.getDataManager();

		//--- increase metadata popularity

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		if (access.equals(Params.Access.PRIVATE))
			dm.increasePopularity(dbms, id);

		//--- send email notification

		if (doNotify)
		{
			String host = sm.getValue("system/feedback/mailServer/host");
			String port = sm.getValue("system/feedback/mailServer/port");
			String from = sm.getValue("system/feedback/email");

			String fromDescr = "GeoNetwork administrator";

			if (host.trim().length() == 0 || from.trim().length() == 0)
				context.debug("Skipping email notification");
			else
			{
				context.debug("Sending email notification for file : "+ file);

				// send emails about downloaded file to groups with notify privilege

				StringBuffer query = new StringBuffer();
				query.append("SELECT g.id, g.name, g.email ");
				query.append("FROM   OperationAllowed oa, Groups g ");
				query.append("WHERE  oa.operationId =" + AccessManager.OPER_NOTIFY + " ");
                query.append("AND    oa.metadataId = ? ");
				query.append("AND    oa.groupId = g.id");

                Element groups = dbms.select(query.toString(), new Integer(id));

				for (Iterator i = groups.getChildren().iterator(); i.hasNext(); )
				{
					Element group = (Element)i.next();
					String  name  = group.getChildText("name");
					String  email = group.getChildText("email");

					if (email.trim().length() != 0)
					{
						String subject = "File " + fname + " has been downloaded";
						String message = "GeoNetwork notifies you, as contact person of group "+ name
							+ " that data file "+ fname
							+ " belonging metadata "+ id
							+ " has beed downloaded from address " + context.getIpAddress() + ".";

						try
						{
							MailSender sender = new MailSender(context);
							sender.send(host, Integer.parseInt(port), from, fromDescr, email, null, subject, message);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		return BinaryFile.encode(200, file.getAbsolutePath());
	}
}

//=============================================================================

