package org.wfp.vam.intermap.services.map.layers;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;
import org.wfp.vam.intermap.services.map.MapUtil;
import jeeves.utils.Xml;
import java.util.*;

public class SetOrder implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		List order = params.getChildren("im_layerList"); // FIXME: we dont want such coupling with JS objects' ids
		int a[] = new int[order.size()];
		int n = 0;
		for (Iterator i = order.iterator(); i.hasNext(); a[n++] = Integer.parseInt(((Element)i.next()).getText()));

		mm.setServicesOrder(a);

		return mm.toElementSimple();
	}

}

//=============================================================================

