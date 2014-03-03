//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata.format;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

import java.io.File;

/**
 * List all formatters
 * 
 * @author jeichar
 */
public class ListFormatters extends AbstractFormatService {

    public Element exec(Element params, ServiceContext context) throws Exception {
        ensureInitializedDir(context);
        String schema = Util.getParam(params, "schema", null);
        if (Util.getParam(params, Params.ID, null) != null ||
        		Util.getParam(params, Params.UUID, null) != null) {
	        try {
	        	schema = getMetadataSchema(params, context);
	        } catch (Throwable e) {
	        	// its ok.  just can't use metadata
	        }
        }

        if (schema == null)
        	schema = "all";
        
        schema = schema.trim();
        
        Element response = new Element("formatters");
        File[] xslFormatters = new File(userXslDir).listFiles(new FormatterFilter());
        if (xslFormatters != null) {
            for (File xsl : xslFormatters) {
            	boolean add = true;
            	if(!schema.equalsIgnoreCase("all")) {
            		ConfigFile config = new ConfigFile(xsl);
            		if(!config.listOfApplicableSchemas().contains(schema)){
            			add = false;
            		}
            	}
            	if (add)
            		response.addContent(new Element("formatter").setText(xsl.getName()));
            }
        }
        return response;
    }

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

}
