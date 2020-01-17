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

import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.exceptions.OperationAbortedEx;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;
@Deprecated
public class Unset extends NotInReadOnlyModeService {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    private static final String FNAME_PARAM = "fname=";

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID);
        String type = Util.getParam(params, Params.TYPE);
        String version = Util.getParam(params, Params.VERSION);

        Lib.resource.checkEditPrivilege(context, id);

        //-----------------------------------------------------------------------
        //--- extract thumbnail filename

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        //--- check if the metadata has been modified from last time

        if (version != null && !dataMan.getVersion(id).equals(version))
            throw new ConcurrentUpdateEx(id);

        Element result = dataMan.getThumbnails(context, id);

        if (result == null)
            throw new OperationAbortedEx("Metadata not found", id);

        result = result.getChild(type);

        if (result == null)
            throw new OperationAbortedEx("Metadata has no thumbnail", id);

        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String uuid = metadataUtils.getMetadataUuid(id);
        dataMan.unsetThumbnail(context, id, type.equals("small"), true);
        store.delResource(context, uuid, MetadataResourceVisibility.PUBLIC, getFileName(result.getText()), true);

        //-----------------------------------------------------------------------

        Element response = new Element("a");
        response.addContent(new Element("id").setText(id));
        response.addContent(new Element("version").setText(dataMan.getNewVersion(id)));

        return response;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //--------------------------------------------------------------------------

    /**
     * Return file name from full url thumbnail formated as http://wwwmyCatalogue.com:8080/srv/eng/resources.get?uuid=34baff6e-3880-4589-a5e9-4aa376ecd2a5&fname=snapshot3.png
     */
    private String getFileName(String file) {
        if (file.indexOf(FNAME_PARAM) < 0) {
            return file;
        } else {
            return file.substring(file.lastIndexOf(FNAME_PARAM) + FNAME_PARAM.length());
        }
    }

}
