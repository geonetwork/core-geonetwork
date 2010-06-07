package org.wfp.vam.intermap.services.marker;


import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.marker.Marker;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;


/**
 * Adds a new marker to the MarkerSet
 *
 * @author ETj
 */

public class Add implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//--- Service
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		//-- Fetch params
		String slat = params.getChildText("lat");
		String slon = params.getChildText("lon");
		String title = params.getChildText("title");
		String desc = params.getChildText("desc");

		float lat,lon;

		//-- Parameter check
		if(slat == null || slon == null)
			throw new MissingParameterEx("Missing lat/lon");

		if(title == null)
			throw new MissingParameterEx("Missing title");

		try
		{
			lat = Float.parseFloat(slat);
		}
		catch (NumberFormatException e)
		{
			throw new BadParameterEx("lat", slat);
		}

		try
		{
			lon = Float.parseFloat(slon);
		}
		catch (NumberFormatException e)
		{
			throw new BadParameterEx("lon", slon);
		}

		//-- add the marker

		Marker marker = new Marker(lat, lon, title, desc);

		MarkerSet ms = (MarkerSet)context.getUserSession().getProperty(Constants.SESSION_MARKERSET);

		if(ms == null)
		{
			ms = new MarkerSet();
			context.getUserSession().setProperty(Constants.SESSION_MARKERSET, ms);
		}

		ms.add(marker);


		return new Element("response")
			.addContent(new Element("added").setText(""+marker.getId()))
			.addContent(ms.toElement());
	}

}

//=============================================================================

