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

package org.fao.geonet.services.metadata;

import java.util.List;
import java.util.Vector;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

/** Given a metadata id returns all operation allowed on it. Called by the
  * metadata.admin service
  */

public class GetAdminOper implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String id = Util.getParam(params, Params.ID);

		//-----------------------------------------------------------------------
		//--- check access

		if (!dataMan.existsMetadata(dbms, id))
			throw new IllegalArgumentException("Metadata not found --> " + id);

		//--- get all operations

		Element elOper = Lib.local.retrieve(dbms, "Operations").setName(Geonet.Elem.OPERATIONS);

		//-----------------------------------------------------------------------
		//--- retrieve groups operations

		Element elGroup = Lib.local.retrieve(dbms, "Groups");

		List list = elGroup.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);

			el.setName(Geonet.Elem.GROUP);

			//--- get all operations that this group can do on given metadata

			String grpId = el.getChildText("id");

			String query = "SELECT operationId FROM OperationAllowed WHERE metadataId=? AND groupId=?";

			List listAllow = dbms.select(query, id, grpId).getChildren();

			//--- now extend the group list adding proper operations

			List listOper = elOper.getChildren();

			for(int j=0; j<listOper.size(); j++)
			{
				String operId = ((Element) listOper.get(j)).getChildText("id");

				Element elGrpOper = new Element(Geonet.Elem.OPER)
													.addContent(new Element(Geonet.Elem.ID).setText(operId));

				boolean bFound = false;

				for(int k=0; k<listAllow.size(); k++)
				{
					Element elAllow = (Element) listAllow.get(k);

					if (operId.equals(elAllow.getChildText("operationid")))
					{
						bFound = true;
						break;
					}
				}

				if (bFound)
					elGrpOper.addContent(new Element(Geonet.Elem.ON));

				el.addContent(elGrpOper);
			}
		}

		//-----------------------------------------------------------------------
		//--- put all together

		Element elRes = new Element(Jeeves.Elem.RESPONSE)
										.addContent(new Element(Geonet.Elem.ID).setText(id))
										.addContent(elOper)
										.addContent(elGroup);

		return elRes;
	}
}

//=============================================================================


