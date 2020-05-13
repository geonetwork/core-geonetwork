//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.thesaurus;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Returns a list of keywords given a list of thesaurus. Keywords are returned in raw format and XSL
 * output may process the output according to the transformation parameter which must match a
 * template name provided in a schema plugin.
 *
 * @author mcoudert
 */
@Deprecated
public class GetKeywordById implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {
        String sThesaurusName = Util.getParam(params, "thesaurus");
        String uri = Util.getParam(params, "id", null);
        String[] lang = Util.getParam(params, "lang", context.getLanguage()).split(",");

        final IsoLanguagesMapper mapper = context.getBean(IsoLanguagesMapper.class);
        for (int i = 0; i < lang.length; i++) {
            lang[i] = mapper.iso639_2_to_iso639_1(lang[i], lang[i].substring(2));
        }

        boolean multiple = Util.getParam(params, "multiple", false);

        // perform the search and save search result into session
        ThesaurusManager thesaurusMan = context.getBean(ThesaurusManager.class);

        Element root;

        if (uri == null) {
            root = new Element("descKeys");
        } else {
            KeywordsSearcher searcher = new KeywordsSearcher(context, thesaurusMan);

            KeywordBean kb;
            if (!multiple) {
                kb = searcher.searchById(uri, sThesaurusName, lang);
                if (kb == null) {
                    root = new Element("descKeys");
                } else {
                    root = KeywordsSearcher.toRawElement(new Element("descKeys"), kb);
                }
            } else {
                String[] url = uri.split(",");
                List<KeywordBean> kbList = new ArrayList<>();
                for (String currentUri : url) {
                    kb = searcher.searchById(currentUri, sThesaurusName, lang);
                    if (kb != null) {
                        kbList.add(kb);
                    }
                }
                root = new Element("descKeys");
                for (KeywordBean keywordBean : kbList) {
                    KeywordsSearcher.toRawElement(root, keywordBean);
                }
            }
        }

        return root;
    }
}

// =============================================================================

