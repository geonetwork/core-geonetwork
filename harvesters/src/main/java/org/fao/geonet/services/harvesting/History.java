package org.fao.geonet.services.harvesting;


import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.HarvestHistory_;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.jdom.Element;
import org.springframework.data.domain.Sort;

import java.nio.file.Path;

import static org.fao.geonet.repository.SortUtils.createPath;
import static org.fao.geonet.repository.specification.HarvestHistorySpecs.hasHarvesterUuid;


public class History  implements Service {
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(Path appPath, ServiceConfig config) throws Exception
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

		if ((sort != null) && (sort.equals("type"))) {
            sortCriteria = "type";
        }

        final HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);

        final Sort.Order harvestDateOrder = new Sort.Order(Sort.Direction.DESC, createPath(HarvestHistory_.harvestDate));
        final Sort.Order harvesterUuidOrder = new Sort.Order(createPath(HarvestHistory_.harvesterUuid));
        final Sort.Order harvesterTypeSort = new Sort.Order(createPath(HarvestHistory_.harvesterType));

        Sort springSort;
        if (sortCriteria.equals("date")) {
            springSort = new Sort(harvestDateOrder, harvesterUuidOrder);
        } else {
            springSort = new Sort(harvesterTypeSort, harvestDateOrder);
        }

        Element result;
        if ((uuid == null) || (uuid.equals(""))) {
            result = historyRepository.findAllAsXml(springSort);
		} else {
            result = historyRepository.findAllAsXml(hasHarvesterUuid(uuid), springSort);
        }


        String id = params.getChildText("id");

        Element harvesterInfo = context.getBean(HarvestManager.class).get(id, context, null);
        Element response = new Element(Jeeves.Elem.RESPONSE);

        if (harvesterInfo != null) {
            response.addContent(result.detach());
            response.addContent(harvesterInfo.detach());

            Element sortEl = new Element("sort");
            sortEl.addContent(sortCriteria);
            response.addContent(sortEl);
        }
        return response;
	}

}
