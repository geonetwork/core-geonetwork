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
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Test Xml Search Service.
 *
 * Created by Jesse on 1/27/14.
 */
public class LuceneSearcher_FastEqualsFullLoad_OrderIntegrationTest extends AbstractLanguageSearchOrderIntegrationTest {
    static String[] getTitlesFromMetadataElements(ServiceContext _serviceContext, Element request, Element result) throws JDOMException {
        final String xpath = "*//gmd:identificationInfo/*/gmd:citation/*/gmd:title";
        final List<Namespace> theNSs = Arrays.asList(Geonet.Namespaces.GMD);
        final List<Element> nodes = (List<Element>) Xml.selectNodes(result, xpath, theNSs);

        final SettingInfo settingInfo = _serviceContext.getBean(SearchManager.class).getSettingInfo();
        final LuceneSearcher.LanguageSelection language = LuceneSearcher.determineLanguage(_serviceContext, request, settingInfo);

        String[] titles = new String[nodes.size()];
        final String langCode;
        if (language.presentationLanguage.equals("fre")) {
            langCode = "#FR";
        } else if (language.presentationLanguage.equals("eng")) {
            langCode = "#EN";
        } else {
            throw new AssertionError("Unexpected language code.  Add a new if clause for " + language);
        }
        for (int i = 0; i < titles.length; i++) {
            final String titleSelectXpath = "gmd:PT_FreeText//gmd:LocalisedCharacterString[@locale = '%s']";
            titles[i] = Xml.selectString(nodes.get(i), String.format(titleSelectXpath, langCode), theNSs);
            if (titles[i] == null || titles[i].isEmpty()) {
                final List<Text> translatedTitles = (List<Text>) Xml.selectNodes(nodes.get(i), "*//gmd:LocalisedCharacterString/text()", theNSs);
                titles[i] = translatedTitles.get(0).getText();
            }
        }
        return titles;
    }

    @Override
    protected String[] doSearch(String lang) throws Exception {
        _serviceContext.setLanguage(lang);
        Element request = new Element("request")
            .addContent(new Element("from").setText("1"))
            .addContent(new Element("to").setText("50"))
            .addContent(new Element("abstract").setText("" + _abstractSearchTerm))
            .addContent(new Element("sortOrder").setText("reverse"))
            .addContent(new Element("sortBy").setText("_title"));

        final ServiceConfig config = new ServiceConfig();
        _luceneSearcher.search(_serviceContext, request, config);
        final Element result = _luceneSearcher.present(_serviceContext, request, config);
        return getTitlesFromMetadataElements(_serviceContext, request, result);
    }
}
