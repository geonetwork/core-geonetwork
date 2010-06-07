package org.wfp.vam.intermap.services.marker;

import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.marker.Marker;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;
import org.wfp.vam.intermap.util.Util;


/**
 * Return all the session markers contained in the given bbox
 *
 * @author ETj
 */

public class Get implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		//-- Fetch params
		String sid = params.getChildText("id");

		MarkerSet ms = (MarkerSet)context.getUserSession().getProperty(Constants.SESSION_MARKERSET);

		if(ms == null)
		{
			ms = new MarkerSet();
			context.getUserSession().setProperty(Constants.SESSION_MARKERSET, ms);
		}

		// bbox is optional: if present, we'll filter the markerset
		BoundingBox bb = Util.parseBoundingBox(params); // search bb in params
		if( bb != null)
		{
			ms = ms.select(bb.getSouth(), bb.getNorth(), bb.getWest(), bb.getEast());
		}

		if(sid!=null)
		{
			try
			{
				long id = Long.parseLong(sid);
				Marker marker = ms.get(id);
				return marker.toElement();
			}
			catch (NumberFormatException e)
			{
				throw new BadParameterEx("id", sid);
			}
		}

		return ms.toElement();
	}

}

//=============================================================================
