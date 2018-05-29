//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.guiservices.sampledata;

import com.google.common.collect.Lists;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A simple service that adds sample data mef files from each schemas sample-data directory.
 */
@Deprecated
public class Add implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     * @return A report on the sample import with information about the status of the insertion
     * operation (failed|loaded).
     */
    public Element exec(final Element params, final ServiceContext context)
        throws Exception {

        String schemaList = Util.getParam(params, Params.SCHEMA);
        final String[] serviceStatus = {"true"};
        final String[] serviceError = {""};

        Element result = new Element(Jeeves.Elem.RESPONSE);

        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager schemaMan = gc.getBean(SchemaManager.class);

        String schemas[] = schemaList.split(",");

        int count = 0;
        for (String schemaName : schemas) {
            Log.info(Geonet.DATA_MANAGER, "Loading sample data for schema "
                + schemaName);
            Path schemaDir = schemaMan.getSchemaSampleDataDir(schemaName);
            if (schemaDir == null) {
                Log.warning(Geonet.DATA_MANAGER,
                    String.format("Skipping - No samples found for schema '%s'.", schemaName));
                result.addContent(new Element(schemaName).setText("0"));
                continue;
            }

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "Searching for mefs in: " + schemaDir);
            }

            List<Path> sampleDataFilesList;
            try (DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(schemaDir, "*.mef")) {
                sampleDataFilesList = Lists.newArrayList(newDirectoryStream);
            }

            int schemaCount = 0;
            for (final Path file : sampleDataFilesList) {
                try {
                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER,
                            String.format("Loading %s sample file %s ...", schemaName, file));
                    }
                    schemaCount += MEFLib.doImport(params, context, file, null).size();
                } catch (Exception e) {
                    serviceStatus[0] = "false";
                    serviceError[0] = e.getMessage() + " whilst loading " + file;
                    Log.error(Geonet.DATA_MANAGER,
                        String.format("Error loading %s sample file %s. Error is %s.",
                            schemaName, file, e.getMessage()),
                        e);
                }
                context.getBean(DataManager.class).flush();
            }
            count += schemaCount;
            result.addContent(new Element(schemaName).setText("" + schemaCount));
        }

        result.setAttribute("status", serviceStatus[0]);
        result.setAttribute("error", serviceError[0]);
        result.setAttribute("total", "" + count);

        return result;
    }
}
