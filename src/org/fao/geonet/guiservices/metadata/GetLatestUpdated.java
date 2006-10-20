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

package org.fao.geonet.guiservices.metadata;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.GeonetContext;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.jdom.Element;

import java.util.Iterator;

//=============================================================================

/** Service used to return all categories in the system
  */

public class GetLatestUpdated implements Service
{
	private int     _maxItems;
	private long    _timeBetweenUpdates;

	private Element _response;
	private long    _lastUpdateTime;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		String sMaxItems           = config.getValue("maxItems",           "10");
		String sTimeBetweenUpdates = config.getValue("timeBetweenUpdates", "60");

		_maxItems           = Integer.parseInt(sMaxItems);
		_timeBetweenUpdates = Integer.parseInt(sTimeBetweenUpdates) * 1000;

	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		if (System.currentTimeMillis() > _lastUpdateTime + _timeBetweenUpdates)
		{
			GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
			Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

			// only get public metadata (group 1: internet) viewable (operation O: view)

			String query = "SELECT id FROM Metadata m, OperationAllowed o "+
								"WHERE m.id=o.metadataId AND o.groupId=1 AND o.operationId=0 "+
								"ORDER BY changeDate DESC";

			Element result = dbms.select(query);

			_response = new Element("response");
			int numItems = 0;
			for (Iterator iter = result.getChildren().iterator(); iter.hasNext() && numItems++ < _maxItems; )
			{
				Element rec = (Element)iter.next();
				String  id = rec.getChildText("id");

				Element md = gc.getDataManager().getMetadata(context, id, false);
				_response.addContent(md);
			}
			_lastUpdateTime = System.currentTimeMillis();
		}
		return (Element)_response.clone();
	}
}

//=============================================================================

