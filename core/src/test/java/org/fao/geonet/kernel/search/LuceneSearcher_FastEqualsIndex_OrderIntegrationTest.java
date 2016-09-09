/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.List;

/**
 * Test Xml Search Service.
 *
 * Created by Jesse on 1/27/14.
 */
public class LuceneSearcher_FastEqualsIndex_OrderIntegrationTest extends AbstractLanguageSearchOrderIntegrationTest {
    @Override
    protected String[] doSearch(String lang) throws Exception {
        _serviceContext.setLanguage(lang);
        Element request = new Element("request")
            .addContent(new Element("fast").setText(Geonet.SearchResult.INDEX))
            .addContent(new Element("from").setText("1"))
            .addContent(new Element("to").setText("50"))
            .addContent(new Element("abstract").setText("" + _abstractSearchTerm))
            .addContent(new Element("sortOrder").setText("reverse"))
            .addContent(new Element("sortBy").setText("_title"));
        final ServiceConfig config = new ServiceConfig();
        _luceneSearcher.search(_serviceContext, request, config);
        final Element result = _luceneSearcher.present(_serviceContext, request, config);
        final List<Element> nodes = (List<Element>) Xml.selectNodes(result, "metadata/title");
        String[] titles = new String[nodes.size()];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = nodes.get(i).getText();
        }
        return titles;
    }
}
