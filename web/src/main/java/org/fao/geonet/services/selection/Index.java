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

package org.fao.geonet.services.selection;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Set;

//=============================================================================

/**
 * Force rebuild Lucene index
 */

public class Index implements Service {
    private Integer maxToIndex;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig config) throws Exception {
        this.maxToIndex = Integer.valueOf(config.getValue("maxToIndex"));
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        final SelectionManager manager = SelectionManager.getManager(context.getUserSession());
        final Set<String> selection = manager.getSelection(SelectionManager.SELECTION_METADATA);

        int index = 0;
        if (selection != null && !selection.isEmpty()) {
            if (selection.size() > maxToIndex) {
                return new Element("error").setText("Attempted to index " + selection.size() + ".  The maximum allowed elements: " + maxToIndex);
            }
            final GeonetContext geonet = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            geonet.getDataManager().indexInThreadPool(context, new ArrayList<String>(selection), dbms, true, false);
        }

        return new Element("results").setAttribute("numberIndexed", "" + index);
    }
}

//=============================================================================


