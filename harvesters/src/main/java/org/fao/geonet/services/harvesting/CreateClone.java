//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.services.harvesting;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.exceptions.ObjectNotFoundEx;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * TODO javadoc.
 */
@Deprecated
public class CreateClone implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    /**
     * TODO javadoc.
     */
    public Element exec(Element params, ServiceContext context) throws Exception {
        //--- if 'id' is null all entries are returned
        String id = params.getChildText("id");

        String newId = context.getBean(HarvestManager.class).createClone(id, context.getUserSession().getUserId(), context);

        if (newId != null) {
            Element elem = new Element(Jeeves.Elem.RESPONSE).addContent(new Element("id").setText(newId));
            return elem;
        }

        //--- we get here only if the 'id' was not present or node was not found
        throw new ObjectNotFoundEx(id);
    }
}
