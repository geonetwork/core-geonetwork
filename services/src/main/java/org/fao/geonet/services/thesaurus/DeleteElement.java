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
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * For editing : removes a concept from a thesaurus. Use parameter "namespace" and "code" to remove
 * a specific concept, if not set, the current selection is removed ({@link
 * org.fao.geonet.services.thesaurus.SelectKeywords}).
 *
 * Access is restricted
 */
public class DeleteElement implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        UserSession session = context.getUserSession();

        // Retrieve thesaurus
        String sThesaurusName = Util.getParam(params, "pThesaurus");
        ThesaurusManager thesaurusMan = gc.getBean(ThesaurusManager.class);
        Thesaurus thesaurus = thesaurusMan.getThesaurusByName(sThesaurusName);

        // Optional keyword info - if none, selection is used
        String code = Util.getParam(params, "id", "");

        if ("".equals(code)) {
            KeywordsSearcher searcher = (KeywordsSearcher) session
                .getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);
            List<?> keywords = searcher.getSelectedKeywordsInList();

            Iterator<?> iter = keywords.iterator();
            while (iter.hasNext()) {
                KeywordBean keyword = (KeywordBean) iter.next();
                thesaurus.removeElement(keyword);
            }

        } else {
            thesaurus.removeElement(code);
        }

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        return elResp;
    }
}
