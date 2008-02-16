package org.wfp.vam.intermap.services.marker;


import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.exceptions.ObjectNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.marker.Marker;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;


/**
 * Update a marker's info
 *
 * @author ETj
 */

public class Update implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//--- Service
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		//-- Fetch params
		String sid = params.getChildText("id");
		String slat = params.getChildText("lat");
		String slon = params.getChildText("lon");
		String title = params.getChildText("title");
		String desc = params.getChildText("desc");

		Float lat = null;
		Float lon = null;
		long id;

		//-- Parameter check
		if(sid == null)
			throw new MissingParameterEx("Missing id");

		try
		{
			id = Long.parseLong(sid);
		}
		catch (NumberFormatException e)
		{
			throw new BadParameterEx("id", sid);
		}

		if(slat != null)
		{
			try
			{
				lat = new Float(slat);
			}
			catch (NumberFormatException e)
			{
				throw new BadParameterEx("lat", slat);
			}
		}

		if(slon != null)
		{
			try
			{
				lon = new Float(slon);
			}
			catch (NumberFormatException e)
			{
				throw new BadParameterEx("lon", slon);
			}
		}

		//-- get the marker

		MarkerSet ms = (MarkerSet)context.getUserSession().getProperty(Constants.SESSION_MARKERSET);
		if(ms == null)
		{
			ms = new MarkerSet();
			context.getUserSession().setProperty(Constants.SESSION_MARKERSET, ms);
		}

		Marker marker = ms.get(id);

		if(marker == null)
			throw new ObjectNotFoundEx(""+id);

		if(lat != null)
			marker.setLat(lat);

		if(lon != null)
			marker.setLon(lon);

		if(title != null)
			marker.setTitle(title);

		if(desc != null)
			marker.setDesc(desc);

		return ms.toElement();
	}

}

//=============================================================================

