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

package org.fao.geonet.guiservices.schemas;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

//=============================================================================
@Deprecated
public class Get implements Service {
    private String FS = File.separator;

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

        Element schemas = new Element("schemalist");

        for (String schema : schemaMan.getSchemas()) {
            Element elem = new Element("name").setText(schema);
            elem.setAttribute("plugin", "true"); // all schemas are plugins
            elem.setAttribute("schemaConvertDirectory", schemaMan.getSchemaDir(schema) + "convert" + FS);
            elem.setAttribute("namespaces", schemaMan.getNamespaceString(schema));
            // is it editable?
            if (schemaMan.getSchema(schema).canEdit()) {
                elem.setAttribute("edit", "true");
            } else {
                elem.setAttribute("edit", "false");
            }
            // get the conversion information and add it too
            List<Element> convElems = schemaMan.getConversionElements(schema);
            if (convElems.size() > 0) {
                Element conv = new Element("conversions");
                conv.addContent(convElems);
                elem.addContent(conv);
            }
            schemas.addContent(elem);
        }

        return schemas;
    }
}

//=============================================================================

