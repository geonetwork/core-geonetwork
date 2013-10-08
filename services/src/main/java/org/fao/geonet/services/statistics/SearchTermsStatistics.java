//=============================================================================
//===   Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.services.statistics;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.repository.specification.SearchRequestParamSpecs;
import org.fao.geonet.repository.statistic.SearchRequestParamRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.util.List;

/**
 * Service to get the search terms for a field and optionally a specific service
 */
public class SearchTermsStatistics extends NotInReadOnlyModeService {

    private static final String FIELD_PARAM = "field";
    private static final String SERVICE_PARAM = "service";
    private static final String LIMIT_PARAM = "limit";
    public static final int DEFAULT_LIMIT = 25;

    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String field = Util.getParam(params, FIELD_PARAM);
        String service = Util.getParam(params, SERVICE_PARAM, "");
        int limit = Util.getParam(params, LIMIT_PARAM, DEFAULT_LIMIT);

        final SearchRequestParamRepository paramRepository = context.getBean(SearchRequestParamRepository.class);
        Specification<SearchRequestParam> specification = SearchRequestParamSpecs.hasTermField(field);
        if (!service.equals("")) {
            specification = Specifications.where(specification).and(SearchRequestParamSpecs.hasService(service));
        }

        final List<Pair<String, Integer>> termTextToRequestCount = paramRepository.getTermTextToRequestCount(limit,
                specification);
        return toElement(termTextToRequestCount);
    }

    private Element toElement(List<Pair<String, Integer>> termTextToRequestCount) {
        final Element param = new Element("params");
        for (Pair<String, Integer> summary : termTextToRequestCount) {
            param.addContent(
                    new Element("record")
                            .addContent(new Element("termtext").setText(summary.one()))
                            .addContent(new Element("total").setText("" + summary.two()))
            );
        }
        return param;
    }
}