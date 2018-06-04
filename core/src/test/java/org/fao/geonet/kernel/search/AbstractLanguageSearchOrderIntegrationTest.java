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

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.repository.SettingRepository;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Test the order of search results with regards to language settings.
 * <p/>
 * Created by Jesse on 1/27/14.
 */
public abstract class AbstractLanguageSearchOrderIntegrationTest extends AbstractCoreIntegrationTest {
    public static List<String> METADATA_TO_IMPORT = new ArrayList<String>(10);
    protected MetaSearcher _luceneSearcher;
    protected ServiceContext _serviceContext;
    protected String _abstractSearchTerm;
    @Autowired
    private SettingRepository _settingRepository;
    @Autowired
    private EsSearchManager _searchManager;

    @BeforeClass
    public static synchronized void bareMetadataXml() throws IOException, JDOMException {
        if (METADATA_TO_IMPORT.isEmpty()) {
            final URL url = AbstractLanguageSearchOrderIntegrationTest.class.getResource("templated-name-lang.iso19139.xml");
            final String xmlString = IOUtils.toString(url,
                "UTF-8");

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">zz</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));
            METADATA_TO_IMPORT.add(loadMetadata("ita",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">yy</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("ita",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">xx</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">A ENG EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">A ENG EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">E2 ENG EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">Z2 ENG EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">G eng is " +
                    "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">e eng en and fr is " +
                    "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">e eng en and fr is " +
                    "en</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">é fra is " +
                    "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">A FRA EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">A FRA EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">Z3 FRA EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">E3 FRA EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));
        }
    }


    private static String loadMetadata(String lang, String title, String xmlString) throws IOException, JDOMException {
        final String updatedXmlString = xmlString.replace("{lang}", lang).replace("{title}", title);
        return updatedXmlString;
    }

    public void importMetadata(String searchTerm) throws Exception {
        this._serviceContext = createServiceContext();
        loginAsAdmin(_serviceContext);
        _abstractSearchTerm = searchTerm;
        for (String element : METADATA_TO_IMPORT) {
            byte[] bytes = element.replace("{uuid}", "" + _abstractSearchTerm).getBytes("UTF-8");
            importMetadataXML(_serviceContext, "uuid:" + System.currentTimeMillis(), new ByteArrayInputStream(bytes),
                MetadataType.METADATA, ReservedGroup.intranet.getId(), Params.GENERATE_UUID);
        }
// TODOES
//        this._luceneSearcher = _searchManager.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
    }

    protected abstract String[] doSearch(String lang) throws Exception;

    @After
    public void tearDownResources() throws Exception {
        if (this._luceneSearcher != null) {
            this._luceneSearcher.close();
        }
    }
}
