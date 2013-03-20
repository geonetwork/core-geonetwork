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

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.BadServerResponseEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet.GeonetHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet.GeonetParams;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.net.URL;
import java.util.List;

/**
 * User rating of metadata. If the metadata was harvested using the 'GeoNetwork' protocol and
 * the system setting "localrating/enable" is false (the default), the user's rating is shared
 * between GN nodes partaking in this harvesting network. If the metadata was not harvested or
 * if "localrating/enable" is true then 'local rating' is applied, counting only rating from
 * users of this node itself.
 *
 * When a remote rating is applied, the local rating is not updated. It will be updated 
 * on the next harvest run (FIXME ?).
 */
public class Rate extends NotInReadOnlyModeService {

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

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager    dm = gc.getDataManager();
		HarvestManager hm = gc.getHarvestManager();

		String id = Utils.getIdentifierFromParameters(params, context);

		String rat = Util.getParam(params, Params.RATING);
		String ip  = context.getIpAddress();

		int iLocalId = Integer.parseInt(id);
		if (!dm.existsMetadata(dbms, iLocalId))
			throw new IllegalArgumentException("Metadata not found --> " + id);

		if (ip == null)
			ip = "???.???.???.???";

		if (!Lib.type.isInteger(rat))
			throw new BadParameterEx(Params.RATING, rat);

		int rating = Integer.parseInt(rat);

		if (rating < 1 || rating > 5)
			throw new BadParameterEx(Params.RATING, rat);

		String harvUuid = getHarvestingUuid(dbms, id);

		// look up value of localrating/enable
		SettingManager settingManager = gc.getSettingManager();
		boolean localRating = settingManager.getValueAsBool("system/localrating/enable", false);
		
		if (localRating || harvUuid == null)
			//--- metadata is local, just rate it
			rating = dm.rateMetadata(dbms, new Integer(id), ip, rating);
		else
		{
			//--- the metadata is harvested, is type=geonetwork?

			AbstractHarvester ah = hm.getHarvester(harvUuid);

			if (ah.getType().equals(GeonetHarvester.TYPE)) {
				String uuid = dm.getMetadataUuid(dbms, id);
				rating = setRemoteRating(context, (GeonetParams) ah.getParams(), uuid, rating);
			} else
				rating = -1;
		}

		return new Element(Params.RATING).setText(Integer.toString(rating));
	}

	//--------------------------------------------------------------------------

	private String getHarvestingUuid(Dbms dbms, String id) throws Exception
	{
		String query = "SELECT harvestUuid FROM Metadata WHERE id=?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		//--- if we don't have any metadata, just return

		if (list.size() == 0)
			throw new MetadataNotFoundEx("id:"+ id);

		Element rec = (Element) list.get(0);

		String harvUuid = rec.getChildText("harvestuuid");

		//--- metadata not harvested

		return (harvUuid.length() == 0) ? null : harvUuid;
	}

	//--------------------------------------------------------------------------

	private int setRemoteRating(ServiceContext context, GeonetParams params, String uuid, int rating) throws Exception
	{
        if(context.isDebug()) context.debug("Rating remote metadata with uuid:"+ uuid);

		XmlRequest req = new XmlRequest(new URL(params.host));

		Lib.net.setupProxy(context, req);

		req.setAddress(params.getServletPath() +"/srv/en/"+ Geonet.Service.XML_METADATA_RATE);
		req.clearParams();
		req.addParam("uuid",   uuid);
		req.addParam("rating", rating);

		Element response = req.execute();

		if (!response.getName().equals(Params.RATING))
			throw new BadServerResponseEx(response);

		return Integer.parseInt(response.getText());
	}
}