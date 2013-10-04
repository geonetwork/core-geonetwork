package org.fao.geonet.services.harvesting;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.harvest.harvester.HarvesterHistoryDao;
import org.jdom.Element;
import java.util.List;

public class HistoryDelete implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
        List<Element> ids = params.getChildren("id");
        List<Element> files = params.getChildren("file");

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		int nrRecs = HarvesterHistoryDao.deleteHistory(dbms, ids, files);

		return new Element(Jeeves.Elem.RESPONSE).setText(nrRecs+"");
	}
}
