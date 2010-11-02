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

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
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
		Element elMd;
		if (!skipInfo) {
			elMd = dm.getMetadata(context, id, addEditing);
		} else {
			elMd = dm.getMetadataNoInfo(context, id);
		}

		if (elMd == null) throw new MetadataNotFoundEx(id);

		//
		// setting schemaLocation
		// TODO currently it's only set for ISO metadata - this should all move to
		// the updatefixedinfo.xsl for each schema

        // do not set schemaLocation if it is already there
        if(elMd.getAttribute("schemaLocation", Csw.NAMESPACE_XSI) == null) {
            Namespace gmdNs = elMd.getNamespace("gmd");
            // document has ISO root element and ISO namespace
            if (gmdNs != null && gmdNs.getURI().equals("http://www.isotc211.org/2005/gmd")) {
                String schemaLocation;
                // if document has srv namespace then add srv schemaLocation
                if (elMd.getNamespace("srv") != null) {
                    schemaLocation = " http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd";
                }
                // otherwise add gmd schemaLocation
                // (but not both! as that is invalid, the schemas describe partially the same schema types)
                else {
                    schemaLocation = "http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd";
                }
                Attribute schemaLocationA = new Attribute("schemaLocation", schemaLocation, Csw.NAMESPACE_XSI);
                elMd.setAttribute(schemaLocationA);
            }
        }

		//--- increase metadata popularity
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		if (!skipPopularity)
			dm.increasePopularity(dbms, id);

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
//=============================================================================

