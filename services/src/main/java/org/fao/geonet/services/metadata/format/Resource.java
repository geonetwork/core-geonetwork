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

import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.io.Files.getFileExtension;

/**
 * Allows a user load a file from the identified formatter bundle. Typically used for reading images
 * or the bundle editor UI.
 * 
 * @author jeichar
 */
@Controller("md.formatter.resource")
public class Resource extends AbstractFormatService {

    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @RequestMapping(value = "/{lang}/md.formatter.resource")
    public void exec(
            @RequestParam(Params.ID) String xslid,
            @RequestParam(Params.FNAME) String fileName,
            @RequestParam(value = Params.SCHEMA, required = false) String schema,
            HttpServletResponse response
            ) throws Exception {
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = schemaManager.getSchemaDir(schema);
        }

        Path formatDir = getAndVerifyFormatDir(dataDirectory, Params.ID, xslid, schemaDir);
        Path desiredFile = formatDir.resolve(fileName);

        if(!Files.isRegularFile(desiredFile)) {
            response.sendError(404, fileName+" does not identify a file in formatter bundle: " + xslid);
            return;
        }

        if(!containsFile(formatDir, desiredFile)) {
            response.sendError(403, fileName+" does not identify a file in the "+xslid+" format bundle");
            return;
        }
        
        response.setStatus(200);
        setContentType(response, getFileExtension(desiredFile.getFileName().toString()));

        Files.copy(desiredFile, response.getOutputStream());
    }

    private void setContentType(HttpServletResponse response, String fileExtension) {
        switch (fileExtension) {
            case "css":
                response.setContentType("text/css");
                return;
            case "bmp":
                response.setContentType("image/bmp");
                return;
            case "gif":
                response.setContentType("image/gif");
                return;
            case "html":
                response.setContentType("text/html");
                return;
            case "jpeg":
            case "jpg":
                response.setContentType("image/jpeg");
                return;
            case "js":
                response.setContentType("application/javascript");
                return;
            case "json":
                response.setContentType("application/json");
                return;
            case "png":
                response.setContentType("image/png");
                return;
            case "tif":
                response.setContentType("image/tiff");
                return;
            case "xml":
                response.setContentType("application/xml");
                return;
            case "xsl":
            case "xslt":
                response.setContentType("application/xslt+xml");
                return;
            case "groovy":
                response.setContentType("text/x-groovy-source,groovy");
                return;
           default:
                response.setContentType("application/octet-stream");
        }
    }
}
