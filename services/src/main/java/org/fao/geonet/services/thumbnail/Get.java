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

package org.fao.geonet.services.thumbnail;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.metadata.Update;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;

//=============================================================================
@Deprecated
public class Get implements Service {
    private Update update = new Update();

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        update.init(appPath, params);
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        //--- store changed fields if we arrive here from the editing form
        //--- the update service uses the following parameters:
        //--- id, data, validate, currTab, version

        if (saveEditData(params))
            //--- data is not saved if someone else has changed the metadata
            update.exec(params, context);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        String id = Util.getParam(params, Params.ID);

        //-----------------------------------------------------------------------
        //--- get metadata

        Element result = dataMan.getThumbnails(context, id);

        if (result == null)
            throw new IllegalArgumentException("Metadata not found --> " + id);

        result.addContent(new Element("version").setText(dataMan.getNewVersion(id)));

        return result;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    private boolean saveEditData(Element params) {
        @SuppressWarnings("unchecked")
        List<Element> list = params.getChildren();

        for (Element el : list) {
            if (el.getName().startsWith("_"))
                return true;
        }

        return false;
    }
}

//=============================================================================


