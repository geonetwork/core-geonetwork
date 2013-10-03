package org.fao.geonet.services.statistics;


import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.repository.statistic.SearchRequestParamRepository;
import org.jdom.Element;

import java.util.List;

/**
 * Jeeves service to select the Metadata popularity from database. made as a java service
 * to allow passing limit parameter to the UI part
 *
 * @author nicolas ribot
 */
public class TagCloud implements Service {
    /**
     * the max number of results to display
     */
    private int limit = 10;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------
    public void init(String appPath, ServiceConfig params) throws Exception {
        this.limit = Integer.parseInt(params.getValue("limit"));
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    /**
     * Physically dumps the given table, writing it to the App tmp folder,
     * returning the URL of the file to get.
     */
    public Element exec(Element params, ServiceContext context) throws Exception {
        final List<Pair<String, Integer>> tagCloudSummary = context.getBean(SearchRequestParamRepository.class).getTermTextToRequestCount(limit);


        Element response = new Element("tagcloud");
        Element elLimit = new Element("limit").setText("" + limit);
        response.addContent(elLimit);

        for (Pair<String, Integer> elem : tagCloudSummary) {
            response.addContent(
                    new Element("record")
                            .addContent(new Element("tagcount").setText(elem.two().toString()))
                            .addContent(new Element("termtext").setText(elem.one()))
            );
        }
        return response;
    }
}
