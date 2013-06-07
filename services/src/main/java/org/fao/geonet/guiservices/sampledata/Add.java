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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple service that adds sample data mef files from each schemas
 * sample-data directory.
 * 
 */
public class Add implements Service {

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	/**
	 * 
	 * 
	 * @return A report on the sample import with information about the status
	 *         of the insertion operation (failed|loaded).
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {

		String schemaList = Util.getParam(params, Params.SCHEMA);
		String serviceStatus = "true";
		String serviceError = "";

		Element result = new Element(Jeeves.Elem.RESPONSE);

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager schemaMan = gc.getBean(SchemaManager.class);
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		String schemas[] = schemaList.split(",");

		for (String schemaName : schemas) {
			Log.info(Geonet.DATA_MANAGER, "Loading sample data for schema "
					+ schemaName);
			String schemaDir = schemaMan.getSchemaSampleDataDir(schemaName);
			if (schemaDir == null) {
				Log.error(Geonet.DATA_MANAGER, "Skipping - No sample data?");
				continue;
			}

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Searching for mefs in: " + schemaDir);
			File sampleDataFiles[] = new File(schemaDir).listFiles();
			List<File> sampleDataFilesList = new ArrayList<File>();

			if (sampleDataFiles != null) {
				for (File file : sampleDataFiles)
					if (file.getName().endsWith(".mef"))
						sampleDataFilesList.add(file);
			}

			for (File file : sampleDataFilesList) {
				try {
                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                        Log.debug(Geonet.DATA_MANAGER, "Loading sample data: " + file);
					MEFLib.doImport(params, context, file, "");
					dbms.commit();
				}
                catch (Exception e) {
                    e.printStackTrace();
					serviceStatus = "false";
					serviceError = e.getMessage() + " whilst loading " + file;
					Log.error(Geonet.DATA_MANAGER,
							"Error loading sample data: " + e.getMessage());
				}
			}
		}

		result.setAttribute("status", serviceStatus);
		result.setAttribute("error", serviceError);
		return result;
	}
}
