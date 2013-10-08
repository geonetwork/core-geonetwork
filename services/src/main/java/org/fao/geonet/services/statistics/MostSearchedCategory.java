package org.fao.geonet.services.statistics;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.repository.specification.SearchRequestParamSpecs;
import org.fao.geonet.repository.statistic.SearchRequestParamRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service to get the db-stored requests most searched category.
 * made a java service because number of results can be filtered in based on input parameter
 * @author nicolas Ribot
 * @author Simon Pigot
 *
 */
public class MostSearchedCategory extends NotInReadOnlyModeService {
	private List<String> luceneTermFields;
	private int maxHits;
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception	{
        super.init(appPath, params);

        final String luceneTermFields1 = params.getValue("luceneTermFields");
        if (luceneTermFields1!= null && luceneTermFields1.length() > 0) {
            luceneTermFields = Arrays.asList(luceneTermFields1.split(","));
        } else {
            luceneTermFields = new ArrayList<String>();
        }
		maxHits = Integer.parseInt(params.getValue("maxHits"));
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------
    @Override
	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {


        final List<Pair<String,Integer>> termTextToRequestCount;
        if (luceneTermFields.isEmpty()) {
            termTextToRequestCount = context.getBean(SearchRequestParamRepository.class)
                    .getTermTextToRequestCount(maxHits);
        } else {
            Specification<SearchRequestParam> spec = SearchRequestParamSpecs.hasTermFieldIn(luceneTermFields);
            termTextToRequestCount = context.getBean(SearchRequestParamRepository.class)
                .getTermTextToRequestCount(maxHits, spec);
        }

        Element results = new Element(Jeeves.Elem.RESPONSE);
        for (Pair<String, Integer> record : termTextToRequestCount) {
            results.addContent(
                    new Element("record")
                            .addContent(new Element("termtext").setText(record.one()))
                            .addContent(new Element("cnt").setText(""+record.two()))
            );
        }
        return results;
	}
}
