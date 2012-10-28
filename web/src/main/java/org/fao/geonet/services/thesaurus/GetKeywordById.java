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
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns a list of keywords given a list of thesaurus
 * 
 * @author mcoudert
 */
public class GetKeywordById implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	private enum Formats {
	    iso,raw
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		String sThesaurusName = Util.getParam(params, "thesaurus");
		String uri = Util.getParam(params, "id");
		String lang = Util.getParam(params, "lang", context.getLanguage());
        Formats format = Formats.valueOf(Util.getParam(params, "format", Formats.iso.toString()));
        String langForThesaurus = IsoLanguagesMapper.getInstance().iso639_2_to_iso639_1(lang);
        
		boolean multiple = Util.getParam(params, "multiple", false);
		
		KeywordsSearcher searcher = null;

		// perform the search and save search result into session
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		ThesaurusManager thesaurusMan = gc.getThesaurusManager();
		
		searcher = new KeywordsSearcher(thesaurusMan);
		KeywordBean kb = null;
		if (!multiple) {
			kb = searcher.searchById(uri, sThesaurusName, langForThesaurus);
			if (kb == null) {
                switch (format) {
        		    case iso: new Element ("null");
        		    case raw: new Element("descKeys");
    		    }
			} else {
                switch (format) {
        		    case iso: return kb.getIso19139();
        		    case raw: return KeywordsSearcher.toRawElement(new Element("descKeys"), kb);
    		    }
	        }
		} else {
			String[] url = uri.split(",");
			List<KeywordBean> kbList = new ArrayList<KeywordBean>();
			for (int i = 0; i < url.length; i++) {
				String currentUri = url[i];
				kb = searcher.searchById(currentUri, sThesaurusName, langForThesaurus);
				if (kb == null) {
					return new Element ("null");
				} else {
					kbList.add(kb);
					kb = null;
				}
			}
			switch (format) {
			case iso:
			    Element complexeKeywordElt = KeywordBean.getComplexIso19139Elt(kbList);
			    return (Element) complexeKeywordElt.detach();
			case raw:
			    Element root = new Element("descKeys");
			    for (KeywordBean keywordBean : kbList) {
                    KeywordsSearcher.toRawElement(root, keywordBean);
                }
			    return root;
			}

		}

        return new Element ("null");
	}
}

// =============================================================================

