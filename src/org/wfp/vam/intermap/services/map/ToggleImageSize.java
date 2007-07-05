package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.Constants;

//=============================================================================

/** main.result service. shows search results
  */

public class ToggleImageSize implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size.equals("small"))
			context.getUserSession().setProperty(Constants.SESSION_SIZE, "big");
		else
			context.getUserSession().setProperty(Constants.SESSION_SIZE, "small");

				// Get the current image size from the user session
		int width = MapUtil.getImageWidth(context);
		int height = MapUtil.getImageHeight(context);

		return new Element("response")
			.addContent(new Element("width").setText("" + width))
			.addContent(new Element("height").setText("" + height));
	}
	
}

//=============================================================================

