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

package org.fao.geonet.services.logo;

import org.fao.geonet.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.Util;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.File;

public class Add implements Service {
	private volatile String logoDirectory;

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {
	    if(logoDirectory == null) {
    	    synchronized (this) {
    	        if(logoDirectory == null) {
    	            logoDirectory = Resources.locateHarvesterLogosDir(context);
    	        }
            }
	    }
		String file = Util.getParam(params, Params.FNAME);
		
		if (file.contains("..")) {
			throw new BadParameterEx("Invalid character found in resource name.", file);
		}
		
		if ("".equals(file)) {
			throw new Exception("Logo name is not defined.");
		}
		
		File inFile = new File(context.getUploadDir(), file);
		File outFile = new File(logoDirectory, file);

		try {
			BinaryFile.moveTo(inFile, outFile, "Copy Logo");
		} catch (Exception e) {
			IO.delete(inFile, false, Geonet.RESOURCES);
			throw new Exception(
					"Unable to move uploaded thumbnail to destination: " + outFile + ". Error: " + e.getMessage());
		}

		Element response = new Element("response");
		response.addContent(new Element("status").setText("Logo added."));
		return response;
	}
}