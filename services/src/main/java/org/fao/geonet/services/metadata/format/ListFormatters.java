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

import jeeves.interfaces.Service;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * List all formatters
 * 
 * @author jeichar
 */
public class ListFormatters extends AbstractFormatService implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        String schema = Util.getParam(params, Params.SCHEMA, null);
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

        File userXslDir = context.getBean(GeonetworkDataDirectory.class).getFormatterDir();
        addFormatters(schema, response, userXslDir,  userXslDir, false);

        final SchemaManager schemaManager = context.getBean(SchemaManager.class);
        final Set<String> schemas = schemaManager.getSchemas();
        for (String schemaName : schemas) {
            if (schema.equals("all") || schema.equals(schemaName)) {
                final String schemaDir = schemaManager.getSchemaDir(schemaName);
                final File formatterDir = new File(schemaDir, FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR);
                addFormatters(schema, response, formatterDir, formatterDir, true);
            }
        }
        return response;
    }

    private void addFormatters(String schema, Element response, File root, File file, boolean isSchemaPluginFormatter) throws IOException {
        File[] xslFormatters = file.listFiles();
        final FormatterFilter formatterFilter = new FormatterFilter();

        if (xslFormatters != null) {
            for (File xsl : xslFormatters) {
                boolean add = true;
                if (formatterFilter.accept(xsl)) {
                    if (!schema.equalsIgnoreCase("all") && !isSchemaPluginFormatter) {
                        ConfigFile config = new ConfigFile(xsl, true, null);
                        if (!config.listOfApplicableSchemas().contains(schema)) {
                            add = false;
                        }
                    }

                    if (add) {
                        String path = xsl.getPath().substring(root.getPath().length()).replace("\\", "/");
                        if (path.startsWith("/")) {
                            path = path.substring(1);
                        }
                        response.addContent(new Element(FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR).setText(path));
                    }
                } else {
                    addFormatters(schema, response, root, xsl, isSchemaPluginFormatter);
                }
            }
        }
    }

}
