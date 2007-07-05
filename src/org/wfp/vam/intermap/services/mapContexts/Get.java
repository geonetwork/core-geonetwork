package org.wfp.vam.intermap.services.mapContexts;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

//=============================================================================

/** main.result service. shows search results
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
		return new Element("response")
			.addContent(DefaultMapServers.getContexts());
	}
	
}

//=============================================================================

