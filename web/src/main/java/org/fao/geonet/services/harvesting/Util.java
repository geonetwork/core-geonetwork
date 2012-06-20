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

package org.fao.geonet.services.harvesting;

import java.util.Iterator;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.Email;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.ErrorTracker;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

//=============================================================================

public class Util
{
	//--------------------------------------------------------------------------
	//---
	//--- Job interface
	//---
	//--------------------------------------------------------------------------

	public interface Job
	{
		public OperResult execute(Dbms dbms, HarvestManager hm, String id) throws Exception;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Exec service: executes the job on all input ids returning the status
	//---               for each one
	//---
	//--------------------------------------------------------------------------

	public static Element exec(Element params, ServiceContext context, Job job) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		HarvestManager hm = gc.getHarvestManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Iterator i = params.getChildren().iterator();

		Element response = new Element(Jeeves.Elem.RESPONSE);

		while (i.hasNext())
		{
			Element el  = (Element) i.next();
			String  id  = el.getText();
			String  res = job.execute(dbms, hm, id).toString();

			el = new Element("id")
							.setText(id)
							.setAttribute(new Attribute("status", res));

			response.addContent(el);
		}

		return response;
	}
	
	public static void warnAdminByMail(ServiceContext context, String mailBody, String server, String UUID, String filePath) {
		GeonetContext ctx = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager settings = ctx.getSettingManager();
		
		String fServer = (server == null ? "" : String.format("\n\t- Server : %s", server));
		String fUUID = (UUID == null ? "" : String.format("\n\t- UUID : %s", UUID));
		String fFile = (filePath == null ? "" : String.format("\n\t- File : %s", filePath));
		
		String emailBody = String.format(mailBody, fServer, fUUID, fFile);
			
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);		
		gc.getEmail().sendToAdmin("[geocat.ch] Harvesting error", emailBody, false);

	}
	private static String SKEL_MAIL_UUID_CLASH = "Hello,\n\n" +
	"This e-mail was sent automatically in order to warn you that the current harvested metadata :\n"+
	"%s" +
	"%s" +
	"%s" +
	"\n\nis entering in conflict with an already in-base Metadata. Therefore, it has not been " +
	"imported.\n" +
	"Sincerely yours,\n" +
	"-- \nGeoCatalogue automatic mail system";
	
	public static String SKEL_MAIL_WEBDAV_ERROR = "Hello,\n\n" +
	"This e-mail was sent automatically in order to warn you that the current harvested metadata :\n"+
	"%s" +
	"%s" +
	"%s" +
	"\n\ncould not be imported.\n\n" +
	"Sincerely yours,\n" +
	"-- \nGeoCatalogue automatic mail system";

    public static String uuid(ServiceContext context, Dbms dbms, String url, Element md, Logger log, 
            String resource, ErrorTracker tracker, String schema) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getDataManager();
        
        String tempUuid = dataMan.extractUUID(schema, md);

        if (tempUuid != null) {
            // check if the UUID is not already in database
            if (dataMan.getMetadataId(dbms, tempUuid) != null) {
                tracker.incrementError();
                Util.warnAdminByMail(context, Util.SKEL_MAIL_UUID_CLASH, url, tempUuid, resource);
                log.debug("  - UUID clash : record already in database ; mailing the GC admins");
                return null;
            }
        } else {
            log.debug("  - No UUID found in the remote MD ; generating a new one");
            tempUuid = UUID.randomUUID().toString();
        }
        return tempUuid;
    }
}

//=============================================================================

