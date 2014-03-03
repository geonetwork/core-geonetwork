package org.fao.geonet.services.statistics;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.repository.statistic.SearchRequestParamRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.util.List;

/**
 * Service to get the db-stored requests most searched keyword.
 * made a java service because number of results can be filtered out based on input parameter
 * @author nicolas Ribot
 *
 */
public class MostSearchedKeyword extends NotInReadOnlyModeService {
	private int maxHits;
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception	{
        super.init(appPath, params);
		maxHits = Integer.parseInt(params.getValue("maxHits"));
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------
    @Override
	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {

        final SearchRequestParamRepository repository = context.getBean(SearchRequestParamRepository.class);

        final List<Pair<String, Integer>> termTextToRequestCount = repository.getTermTextToRequestCount(maxHits);
        if (termTextToRequestCount.isEmpty()) {
            return new  Element("mostSearchedKeyword");
        } else {
            Element results = new Element("mostSearchedKeyword");
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
}
