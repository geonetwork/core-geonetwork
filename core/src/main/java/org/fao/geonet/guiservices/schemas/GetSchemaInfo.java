//=============================================================================
//===	Copyright (C) 2010 GeoNetwork
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

package org.fao.geonet.guiservices.schemas;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;

import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Map;

//=============================================================================

public class GetSchemaInfo implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        SchemaManager schemaMan = gc.getBean(SchemaManager.class);

        Element schemas = new Element("schemas");

        for (String schema : schemaMan.getSchemas()) {
            try {
                Map<String, XmlFile> schemaInfo = schemaMan.getSchemaInfo(schema);

                for (Map.Entry<String, XmlFile> entry : schemaInfo.entrySet()) {
                    XmlFile xf = entry.getValue();
                    String fname = entry.getKey();
                    Element response = xf.exec(new Element("junk"), context);
                    response.setName(FilenameUtils.removeExtension(fname));
                    response.removeAttribute("noNamespaceSchemaLocation", Geonet.Namespaces.XSI);
                    Element schemaElem = new Element(schema);
                    schemaElem.addContent(response);
                    schemas.addContent(schemaElem);
                }
            } catch (Exception e) {
                Log.error(Geonet.GEONETWORK, "Failed to load guiservices for schema "+schema+": "+e.getMessage(), e);
            }
        }

        //Log.info(Geonet.SCHEMA_MANAGER, "The response was "+Xml.getString(schemas));
        return schemas;
    }
}

//=============================================================================

