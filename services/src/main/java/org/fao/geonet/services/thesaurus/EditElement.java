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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.kernel.search.keyword.KeywordSort;
import org.fao.geonet.kernel.search.keyword.SortDirection;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;

//=============================================================================

/**
 * For editing : adds a tag to a thesaurus. Access is restricted
 */

public class EditElement implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
        throws Exception {
        String ref = Util.getParam(params, Params.REF);
        String id = Util.getParam(params, Params.ID, "");
        String uri = Util.getParam(params, Params.URI, "");
        String mode = Util.getParam(params, Params.MODE, "");
        String lang = context.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(context.getLanguage());

        String modeType = "add";

        Element elResp = new Element(Jeeves.Elem.RESPONSE);

        if (!id.equals("") || !uri.equals("")) {
            KeywordBean kb = null;

            if (!id.equals("")) {
                UserSession session = context.getUserSession();
                KeywordsSearcher searcher = (KeywordsSearcher) session
                    .getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);
                kb = searcher.getKeywordFromResultsById(id);
            } else {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                ThesaurusManager thesaurusMan = gc.getBean(ThesaurusManager.class);
                KeywordsSearcher searcher = new KeywordsSearcher(context, thesaurusMan);

                kb = searcher.searchById(uri, ref, lang);

            }
            // Add info needed by thesaurus.edit
            elResp.addContent(new Element("prefLab").setText(kb.getDefaultValue()));
            elResp.addContent(new Element("definition").setText(kb.getDefaultDefinition()));

            elResp.addContent(new Element("relCode").setText(kb.getRelativeCode()));
            elResp.addContent(new Element("nsCode").setText(kb.getNameSpaceCode()));
            if (kb.getCoordEast() != null) {
                elResp.addContent(new Element("east").setText(kb.getCoordEast()));
            }
            if (kb.getCoordWest() != null) {
                elResp.addContent(new Element("west").setText(kb.getCoordWest()));
            }
            if (kb.getCoordSouth() != null) {
                elResp.addContent(new Element("south").setText(kb.getCoordSouth()));
            }
            if (kb.getCoordNorth() != null) {
                elResp.addContent(new Element("north").setText(kb.getCoordNorth()));
            }


            modeType = "edit";
            uri = kb.getRelativeCode();
        } else {
            elResp.addContent(new Element("nsCode").setText("#"));
        }


        // Only if consult (ie. external thesaurus) search for related concept
        if (mode.equals("consult")) {
            ArrayList<KeywordRelation> reqType = new ArrayList<KeywordRelation>();
            reqType.add(KeywordRelation.BROADER);
            reqType.add(KeywordRelation.NARROWER);
            reqType.add(KeywordRelation.RELATED);

            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            ThesaurusManager thesaurusMan = gc.getBean(ThesaurusManager.class);
            KeywordsSearcher searcherBNR = new KeywordsSearcher(context, thesaurusMan);

            for (int i = 0; i <= reqType.size() - 1; i++) {
                searcherBNR.searchForRelated(uri, ref, reqType.get(i), KeywordSort.defaultLabelSorter(SortDirection.DESC), lang);

                String type;

                if (reqType.get(i) == KeywordRelation.BROADER) {
                    // If looking for broader search concept in a narrower element
                    type = "narrower";
                } else if (reqType.get(i) == KeywordRelation.NARROWER) {
                    type = "broader";
                } else {
                    type = "related";
                }
                Element keywordType = new Element(type);
                keywordType.addContent(searcherBNR.getResults());

                elResp.addContent(keywordType);
            }
        }

        String thesaType = ref;
        thesaType = thesaType.substring(thesaType.indexOf('.') + 1, thesaType.length());
        thesaType = thesaType.substring(0, thesaType.indexOf('.'));

        elResp.addContent(new Element("thesaType").setText(thesaType));
        elResp.addContent(new Element("thesaurus").setText(ref));
        elResp.addContent(new Element("mode").setText(modeType));

        return elResp;
    }


}


// =============================================================================

