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

package org.fao.geonet.services.metadata;

import jeeves.exceptions.MissingParameterEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.TimerTask;
import java.util.Timer;

//=============================================================================

/** Retrieves a particular metadata. Access is restricted
  */

public class Show implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		String skip = params.getValue("skipPopularity", "n");

		skipPopularity = skip.equals("y");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		UserSession session = context.getUserSession();

		//-----------------------------------------------------------------------
		//--- handle current tab

		Element elCurrTab = params.getChild(Params.CURRTAB);

		if (elCurrTab != null)
			session.setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());

		//-----------------------------------------------------------------------
		//--- check access

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm = gc.getDataManager();

		// the metadata ID
		String id;
		
		// does the request contain a UUID ?
		try {
			String uuid = Util.getParam(params, Params.UUID);
			// lookup ID by UUID
			id = dm.getMetadataId(context, uuid);	
		}
		catch(MissingParameterEx x) {
			// request does not contain UUID; use ID from request
			try {
				id = Util.getParam(params, Params.ID);
			}
			// request does not contain ID
			catch(MissingParameterEx xx) {
				// give up
				throw new Exception("Request must contain a UUID or an ID");
			}			
		}
		
		Lib.resource.checkPrivilege(context, id, AccessManager.OPER_VIEW);

		//-----------------------------------------------------------------------
		//--- get metadata
		
		Element elMd = dm.getMetadata(context, id, false);
		elMd.addNamespaceDeclaration(Csw.NAMESPACE_CSW);	

		//
		// setting schemaLocation
		// TODO currently it's only set for ISO metadata
		
		// document has ISO root element and ISO namespace
		if(elMd.getName().equals("MD_Metadata") && elMd.getNamespaceURI().equals("http://www.isotc211.org/2005/gmd")) {
			// whether the metadata describes a service
			boolean isServiceMetadata = false;
			Element identificationInfo = elMd.getChild("identificationInfo", Csw.NAMESPACE_GMD);
			if(identificationInfo != null) {
				Element srvIdentification = identificationInfo.getChild("SV_ServiceIdentification", Csw.NAMESPACE_SRV);
				if(srvIdentification != null) {
					isServiceMetadata = true;
				}
			}
			// document describes a dataset (not a service)
			if(!isServiceMetadata){
				Attribute schemaLocation = new Attribute("schemaLocation","http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd", Csw.NAMESPACE_XSI);
				elMd.setAttribute(schemaLocation);
			}
			// document describes a service
			else if(isServiceMetadata) {
				Attribute schemaLocation = new Attribute("schemaLocation","http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd", Csw.NAMESPACE_XSI);
				elMd.setAttribute(schemaLocation);			
			}
		}

		if (elMd == null)
			throw new MetadataNotFoundEx(id);

		//--- increase metadata popularity

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		if (!skipPopularity) {
			Timer t = new Timer();
             		t.schedule(new IncreasePopularityTask(context, id), 10);
		}
		
		return elMd;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private boolean skipPopularity;
}

class IncreasePopularityTask extends TimerTask {
     ServiceContext context;
     Dbms dbms;
     String id;

     IncreasePopularityTask(ServiceContext context, String id) {
         this.context = context;
         this.id = id;
     }
 
     public void run() {
         try {
             Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
 
             GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
             DataManager dm = gc.getDataManager();
 
             dm.increasePopularity(dbms, id);

             //-- explicitly close Dbms resource to avoid exhausting Dbms pool
             context.getResourceManager().close();

         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
//=============================================================================

