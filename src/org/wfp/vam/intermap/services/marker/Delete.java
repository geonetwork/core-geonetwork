package org.wfp.vam.intermap.services.marker;



import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.exceptions.ObjectNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;


/**
 * Remove a marker from the MarkerSet
 *
 * @author ETj
 */

public class Delete implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//--- Service
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		//-- Fetch params
		String sid = params.getChildText("id");

		//-- Parameter check
		if(sid == null)
			throw new MissingParameterEx("Missing id");

		long id;
		try
		{
			id = Long.parseLong(sid);
		}
		catch (NumberFormatException e)
		{
			throw new BadParameterEx("id", sid);
		}

		//-- remove the marker

		MarkerSet ms = (MarkerSet)context.getUserSession().getProperty(Constants.SESSION_MARKERSET);

		if(ms == null)
		{
			ms = new MarkerSet();
			context.getUserSession().setProperty(Constants.SESSION_MARKERSET, ms);
		}

		if( ! ms.remove(id))
			throw new ObjectNotFoundEx(""+id);

		return ms.toElement();
	}

}

//=============================================================================

