//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
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

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.thumbnail.ThumbnailMaker;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;

public class Generate extends NotInReadOnlyModeService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element serviceSpecificExec(Element params, ServiceContext context)
            throws Exception {
        String id = Util.getParam(params, Params.ID);
        String version = Util.getParam(params, Params.VERSION);
        String file = Util.getParam(params, Params.NAME, "thumbnail.png");
        String jsonConfig = Util.getParam(params, "config");
        String rotation = Util.getParam(params, "rotation", null);
        Integer rotationAngle = null;
        try {
            rotationAngle = Integer.valueOf(rotation);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        Lib.resource.checkEditPrivilege(context, id);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        ThumbnailMaker thumbnailMaker = gc.getBean(ThumbnailMaker.class);

        //--- check if the metadata has been modified from last time
        if (version != null && !dataMan.getVersion(id).equals(version)) {
            throw new ConcurrentUpdateEx(id);
        }


        Path thumbnailFile = thumbnailMaker.generateThumbnail(
                jsonConfig,
                rotationAngle);

        //--- create destination directory
        Path dataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, id);
        Files.createDirectories(dataDir);

        Path outFile = dataDir.resolve(file);

        Files.deleteIfExists(outFile);

        try {
            IO.moveDirectoryOrFile(thumbnailFile, outFile, false);
        } catch (Exception e) {
            IO.deleteFile(thumbnailFile, false, context);
            throw new Exception(
                    "Unable to move uploaded thumbnail to destination: "
                            + outFile + ". Error: " + e.getMessage());
        }

        dataMan.setThumbnail(context, id, false, file, false);

        dataMan.indexMetadata(id, true);

        Element response = new Element("a");
        response.addContent(new Element("id").setText(id));
        response.addContent(new Element("version").setText(dataMan.getNewVersion(id)));
        return response;
    }
}