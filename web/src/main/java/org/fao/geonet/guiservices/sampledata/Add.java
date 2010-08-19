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
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.mef.MEFLib;
import org.jdom.Element;

import java.io.File;

/**
 * A simple service that add the sample mef files in the samples
 * directory.
 * 
 */
public class Add implements Service {
	String sampleDirectoryPath;

	public void init(String appPath, ServiceConfig params) throws Exception {
		sampleDirectoryPath = appPath + "/WEB-INF/classes/setup/samples";
	}

	/**
	 * 
	 * 
	 * @return A report on the sample import with information about the status
	 *         of the insertion operation (failed|loaded).
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {

		String serviceStatus = "true";
		
		Element result = new Element(Jeeves.Elem.RESPONSE);

        Log.info(Geonet.DATA_MANAGER, "Loading sample data");
		File samplesDirectory = new File(sampleDirectoryPath);
		File sampleFiles[] = samplesDirectory.listFiles();
    
        for (File file : sampleFiles) {
            if ((!file.isDirectory()) && (file.getName().endsWith(".mef"))) {
                try {
                    MEFLib.doImport(params, context, file, "");
                } catch (Exception e) {
                    serviceStatus = "false";
                    Log.error(Geonet.DATA_MANAGER,
                            "Error loading sample data: " + e.getMessage());
                }
            }
        }

        result.setAttribute("status", serviceStatus);
		return result;        
	}
}