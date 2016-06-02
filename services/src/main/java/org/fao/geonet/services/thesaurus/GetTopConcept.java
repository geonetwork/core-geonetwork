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

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Search the thesaurus for all concepts listed in the concept schema as top concepts (ie
 * skos:hasTopConcept). Return a confected concept uri and preferred label with top concepts as
 * narrower concepts.
 *
 * @author sppigot
 */
public class GetTopConcept implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {
        String sThesaurusName = Util.getParam(params, "thesaurus");
        String lang = Util.getParam(params, "lang", context.getLanguage());

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager thesaurusMan = gc.getBean(ThesaurusManager.class);

        Thesaurus the = thesaurusMan.getThesaurusByName(sThesaurusName);
        String langForThesaurus = the.getIsoLanguageMapper().iso639_2_to_iso639_1(lang);

        KeywordsSearcher searcher = null;

        // perform the search for the top concepts of the concept scheme
        searcher = new KeywordsSearcher(context, thesaurusMan);

        Element response = new Element("descKeys");
        try {
            searcher.searchTopConcepts(sThesaurusName, langForThesaurus);

            KeywordBean topConcept = new KeywordBean(the.getIsoLanguageMapper());
            topConcept.setThesaurusInfo(the);
            topConcept.setValue("topConcepts", langForThesaurus);
            topConcept.setUriCode(sThesaurusName);
            Element root = KeywordsSearcher.toRawElement(response, topConcept);

            Element keywordType = new Element("narrower");
            for (KeywordBean kbr : searcher.getResults()) {
                keywordType.addContent(kbr.toElement("eng", context.getLanguage()));
            }
            root.addContent(keywordType);
        } catch (TermNotFoundException ignored) {
            // No top concept in thesaurus. Return empty element
        }
        return response;
    }
}
