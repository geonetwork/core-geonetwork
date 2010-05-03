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
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

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
		String skip;
		
		skip = params.getValue("skipPopularity", "n");
		skipPopularity = skip.equals("y");

		skip = params.getValue("skipInfo", "n");
		skipInfo = skip.equals("y");
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

		String id = Utils.getIdentifierFromParameters(params, context);
		
		if (id == null)
			throw new MetadataNotFoundEx("Metadata not found.");
		
		Lib.resource.checkPrivilege(context, id, AccessManager.OPER_VIEW);

		//-----------------------------------------------------------------------
		//--- get metadata
		
		boolean addEditing = false;
		Element elMd = dm.getMetadata(context, id, addEditing);
		if (skipInfo) elMd.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);

		//
		// setting schemaLocation
		// TODO currently it's only set for ISO metadata - this should all move to
		// the updatefixedinfo.xsl for each schema

		// document has ISO root element and ISO namespace
		Namespace gmdNs = elMd.getNamespace("gmd");
		if (gmdNs != null && gmdNs.getURI().equals("http://www.isotc211.org/2005/gmd")) {
			// document gets default gmd namespace schemalocation
			String locations = "http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd";
			// if document has srv namespace then add srv namespace location
			if (elMd.getNamespace("srv") != null) {
				locations += " http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd";
			}
			Attribute schemaLocation = new Attribute("schemaLocation", locations, Csw.NAMESPACE_XSI);
			elMd.setAttribute(schemaLocation);			
		}

		if (elMd == null)
			throw new MetadataNotFoundEx(id);

		//--- increase metadata popularity

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
	private boolean skipInfo;
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

