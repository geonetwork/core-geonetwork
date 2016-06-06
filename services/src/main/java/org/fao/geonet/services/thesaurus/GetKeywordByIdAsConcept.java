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

import java.util.ArrayList;
import java.util.List;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.kernel.search.keyword.KeywordSort;
import org.fao.geonet.kernel.search.keyword.SortDirection;

import org.jdom.Element;

import java.nio.file.Path;

/**
 * Returns a keyword from a thesaurus with narrower, broader and related concepts filled out.
 * Keyword and related concepts are returned in raw format.
 *
 * @author sppigot - taken from GetKeywordById and GetNarrowerBroader
 */
public class GetKeywordByIdAsConcept implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {
        String sThesaurusName = Util.getParam(params, "thesaurus");
        String uri = Util.getParam(params, "id", null);
        String lang = Util.getParam(params, "lang", context.getLanguage());

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager thesaurusMan = gc.getBean(ThesaurusManager.class);

        Thesaurus the = thesaurusMan.getThesaurusByName(sThesaurusName);
        String langForThesaurus = the.getIsoLanguageMapper().iso639_2_to_iso639_1(lang);

        KeywordsSearcher searcher = null;

        Element root = null;

        if (uri == null) {
            root = new Element("descKeys");
        } else {
            // perform the search for the specified concept by uri
            searcher = new KeywordsSearcher(context, thesaurusMan);
            KeywordBean kb = null;

            kb = searcher.searchById(uri, sThesaurusName, langForThesaurus);
            if (kb == null) {
                root = new Element("descKeys");
            } else {
                root = KeywordsSearcher.toRawElement(new Element("descKeys"), kb);

                // now get the narrower, broader and related/equal concepts and
                // place them in the result tree
                String[] relations = {"narrower", "broader", "related"};
                for (String request : relations) {
                    searcher = new KeywordsSearcher(context, thesaurusMan);
                    KeywordRelation reqType;
                    if (request.equals("broader")) {
                        reqType = KeywordRelation.NARROWER;
                    } else if (request.equals("narrower")) {
                        reqType = KeywordRelation.BROADER;
                    } else {
                        reqType = KeywordRelation.RELATED;
                    }

                    searcher.searchForRelated(params, reqType, KeywordSort.defaultLabelSorter(SortDirection.DESC), lang);
                    // build response for each request type
                    Element keywordType = new Element(request);
                    for (KeywordBean kbr : searcher.getResults()) {
                        keywordType.addContent(kbr.toElement(context.getLanguage()));
                    }
                    root.addContent(keywordType);
                }
            }
        }

        return root;
    }
}

// =============================================================================

