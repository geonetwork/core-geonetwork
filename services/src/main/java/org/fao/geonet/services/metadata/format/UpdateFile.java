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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Allows a user to set the xsl used for displaying metadata
 * 
 * @author jeichar
 */
public class UpdateFile extends AbstractFormatService implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {

        String fileName = URLDecoder.decode(Util.getParam(params, Params.FNAME), Constants.ENCODING);
        String xslid = Util.getParam(params, Params.ID);
        String data =  Util.getParam(params, Params.DATA);
        String schema = Util.getParam(params, Params.SCHEMA, null);
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = context.getBean(SchemaManager.class).getSchemaDir(schema);
        }

        Path formatDir = getAndVerifyFormatDir(context.getBean(GeonetworkDataDirectory.class), Params.ID, xslid, schemaDir);
        
        Path toUpdate = formatDir.resolve(fileName);
        
        Files.write(toUpdate, Collections.singleton(data), Constants.CHARSET);

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        elResp.addContent(new Element(Geonet.Elem.ID).setText(xslid));
        elResp.addContent(new Element(Params.FNAME).setText(fileName));

        return elResp;
    }

}
