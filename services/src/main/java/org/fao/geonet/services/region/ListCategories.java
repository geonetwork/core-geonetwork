//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.region;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.api.regions.ThesaurusBasedRegionsDAO;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Collection;

//=============================================================================

/**
 * Returns a specific region and coordinates given its id
 */
@Deprecated
public class ListCategories implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {

        Collection<RegionsDAO> daos = context.getApplicationContext().getBeansOfType(RegionsDAO.class).values();
        Element result = new Element("categories");
        for (RegionsDAO dao : daos) {
            if (dao instanceof ThesaurusBasedRegionsDAO) {
                java.util.List<KeywordBean> keywords = ((ThesaurusBasedRegionsDAO) dao).getRegionTopConcepts(context);
                if (keywords != null) {
                    for (KeywordBean k : keywords) {
                        Element catEl = new Element("category");
                        catEl.setAttribute("id", k.getUriCode() + "");
                        catEl.setAttribute("label", k.getPreferredLabel(context.getLanguage()));
                        result.addContent(catEl);
                    }
                }
            } else {
                Collection<String> ids = dao.getRegionCategoryIds(context);
                if (ids != null) {
                    for (String id : ids) {
                        Element catEl = new Element("category");
                        catEl.setAttribute("id", id);
                        result.addContent(catEl);
                    }
                }
            }
        }
        return result;
    }

}

// =============================================================================

