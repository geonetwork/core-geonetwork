package org.fao.geonet.services.harvesting;


import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.HarvestHistory_;
import org.fao.geonet.exceptions.ObjectNotFoundEx;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.SortUtils;
import org.jdom.Element;
import org.springframework.data.domain.Sort;

import static org.fao.geonet.repository.SortUtils.createPath;
import static org.fao.geonet.repository.specification.HarvestHistorySpecs.hasHarvesterUuid;


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

        final HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);

        final Sort.Order harvestDateOrder = new Sort.Order(Sort.Direction.DESC, createPath(HarvestHistory_.harvestDate));
        final Sort.Order harvesterUuidOrder = new Sort.Order(createPath(HarvestHistory_.harvesterUuid));
        final Sort.Order harvesterTypeSort = new Sort.Order(createPath(HarvestHistory_.harvesterType));

        Element result = null;
        if ((uuid == null) || (uuid.equals(""))) {
            Sort springSort;
            if (sortCriteria.equals("date")) {
                springSort = new Sort(harvestDateOrder, harvesterUuidOrder);
            } else {
                springSort = new Sort(harvesterTypeSort, harvestDateOrder);
            }
            result = historyRepository.findAllAsXml(springSort);
		} else {
            result = historyRepository.findAllAsXml(hasHarvesterUuid(uuid), new Sort(harvestDateOrder, harvesterUuidOrder));
        }



		if (!result.getChildren().isEmpty()) {
			String id = params.getChildText("id");

			Element harvesterInfo = context.getBean(HarvestManager.class).get(id, context, null);
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
