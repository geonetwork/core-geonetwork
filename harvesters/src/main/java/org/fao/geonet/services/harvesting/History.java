package org.fao.geonet.services.harvesting;


import jeeves.constants.Jeeves;
import jeeves.exceptions.ObjectNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.HarvesterHistoryDao;
import org.jdom.Element;


public class History  implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception
	{

	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception {
		String uuid = params.getChildText("uuid");
		String sort = params.getChildText("sort");
		String sortCriteria = "date";
		if ((sort != null) && (sort.equals("type"))) sortCriteria = "type";

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Element result = null;
		if ((uuid == null) || (uuid.equals(""))) {
			result = HarvesterHistoryDao.retrieveSort(dbms, sortCriteria);
		} else {
      result = HarvesterHistoryDao.retrieve(dbms, uuid);
    }



		if (result != null) {
			String id = params.getChildText("id");

			Element harvesterInfo = gc.getBean(HarvestManager.class).get(id, context, null);
			Element response = new Element(Jeeves.Elem.RESPONSE);

			response.addContent(result.detach());
			response.addContent(harvesterInfo.detach());

			Element sortEl = new Element("sort");
			sortEl.addContent(sortCriteria);
			response.addContent(sortEl);

			return response;
		}

		//--- we get here only if the 'uuid' is present and the node was not found

		throw new ObjectNotFoundEx(uuid);
	}

}
