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

import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.kernel.search.LuceneSearcher_FastEqualsFullLoad_OrderIntegrationTest.getTitlesFromMetadataElements;

/**
 * Test Xml Search Service.
 *
 * Created by Jesse on 1/27/14.
 */
public class LuceneSearcher_FastEqualsFast_OrderIntegrationTest extends AbstractLanguageSearchOrderIntegrationTest {

    @Autowired
    private MetadataRepository _metadataRepository;

    @Override
    protected String[] doSearch(String lang) throws Exception {
        _serviceContext.setLanguage(lang);
        Element request = new Element("request")
            .addContent(new Element(Geonet.SearchResult.FAST).setText("true"))
            .addContent(new Element("from").setText("1"))
            .addContent(new Element("to").setText("50"))
            .addContent(new Element("abstract").setText("" + _abstractSearchTerm))
            .addContent(new Element("sortOrder").setText("reverse"))
            .addContent(new Element("sortBy").setText("_title"));
        final ServiceConfig config = new ServiceConfig();
        _luceneSearcher.search(_serviceContext, request, config);
        final Element result = _luceneSearcher.present(_serviceContext, request, config);
        final List<Element> nodes = (List<Element>) Xml.selectNodes(result, "metadata/geonet:info/id", Arrays.asList(Edit.NAMESPACE));
        String[] titles = new String[nodes.size()];
        for (int i = 0; i < titles.length; i++) {
            final String mdId = nodes.get(i).getText();
            final AbstractMetadata md = _metadataRepository.findOne(mdId);
            final Element xmlData = md.getXmlData(false);
            titles[i] = getTitlesFromMetadataElements(_serviceContext, request, new Element("record").addContent(xmlData))[0];
        }
        return titles;
    }
}
