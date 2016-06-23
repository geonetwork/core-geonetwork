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

package org.fao.geonet.services.schema;

import org.fao.geonet.exceptions.OperationAbortedEx;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.SchemaUtils;
import org.jdom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

//=============================================================================
@Deprecated
public class Add implements Service {
    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager scm = gc.getBean(SchemaManager.class);

        String schema = Util.getParam(params, Params.SCHEMA);
        String urlStr, uuid, fname;
        uuid = "";
        URL url = null;

        // -- try the file name argument then the url then the uuid of a metadata
        // -- record to which a schema is attached
        fname = Util.getParam(params, Params.FNAME, "");
        if ("".equals(fname)) {
            urlStr = Util.getParam(params, Params.URL, "");
            if ("".equals(urlStr)) {
                uuid = Util.getParam(params, Params.UUID, "");
                if ("".equals(uuid)) {
                    throw new IllegalArgumentException("One of fname, url or uuid must be supplied");
                }
            } else {
                try {
                    url = new URL(urlStr);
                } catch (MalformedURLException mu) {
                    throw new OperationAbortedEx("URL " + urlStr + " is malformed: " + mu.getMessage());
                }
            }
        }

        // -- test if schema already exists, if so then chuck a fit and exit
        if (scm.existsSchema(schema)) {
            throw new OperationAbortedEx("Schema already exists");
        }

        SchemaUtils su = new SchemaUtils();
        return su.addSchema(context, schema, fname, url, uuid, scm);
    }

}

// =============================================================================

