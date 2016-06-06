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

package org.fao.geonet.services.main;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.jdom.Element;

import java.nio.file.Path;

//=============================================================================

/**
 * remote.result service. Returns just one metadata formatted like the local search
 */

public class RemoteShow implements Service {
    private ServiceConfig _config;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig config) throws Exception {
        _config = config;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        Element elCurrTab = params.getChild(Params.CURRTAB);
        if (elCurrTab != null) {
            UserSession session = context.getUserSession();
            session.setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());
        }
        // build result data
        MetaSearcher searcher = (MetaSearcher) context.getUserSession().getProperty(Geonet.Session.SEARCH_RESULT);
        return searcher.get(context, params, _config);
    }
}

//=============================================================================

