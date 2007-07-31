package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;

//=============================================================================

/**
  */

public class Reset implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		MapMerger mm = new MapMerger();
		MapUtil.setDefaultContext(mm);
		//MapUtil.setDefBoundingBox(mm);
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		return null;
	}
	
}

//=============================================================================

