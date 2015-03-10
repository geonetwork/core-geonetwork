package org.fao.geonet.services.harvesting;


import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.jdom.Element;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
		int page = org.fao.geonet.Util.getParam(params, "page", 0);
		int pageSize = org.fao.geonet.Util.getParam(params, "size", Integer.MAX_VALUE);
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
            springSort = new Sort(harvesterTypeSort, harvestDateOrder, harvesterUuidOrder);
        }
        PageRequest pageRequest = new PageRequest(page, pageSize, springSort);

        Element result;
        long totalRecords;
        if ((uuid == null) || (uuid.equals(""))) {
            result = historyRepository.findAllAsXml(pageRequest);
            totalRecords = historyRepository.count();
		} else {
            final Specification<HarvestHistory> specification = hasHarvesterUuid(uuid);
            result = historyRepository.findAllAsXml(specification, pageRequest);
            totalRecords = historyRepository.count(specification);
        }


        String id = params.getChildText("id");

        Element harvesterInfo = context.getBean(HarvestManager.class).get(id, context, null);
        Element response = new Element(Jeeves.Elem.RESPONSE);

        if (harvesterInfo != null) {
            Element sortEl = new Element("sort");
            sortEl.addContent(sortCriteria);
            response.addContent(sortEl);

            response.addContent(new Element("page").setText("" + page));
            response.addContent(new Element("size").setText("" + pageSize));
            response.addContent(new Element("total").setText("" + totalRecords));
            response.addContent(new Element("pages").setText("" + (totalRecords / pageSize)));

            response.addContent(result.detach());
            response.addContent(harvesterInfo.detach());
        }
        return response;
	}

}
