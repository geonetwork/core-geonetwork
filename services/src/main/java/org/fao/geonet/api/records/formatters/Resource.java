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

package org.fao.geonet.api.records.formatters;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.FilePathChecker;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.io.Files.getFileExtension;

/**
 * Allows a user load a file from the identified formatter bundle. Typically used for reading images
 * or the bundle editor UI.
 *
 * @author jeichar
 */
@Controller("md.formatter.resource")
public class Resource extends AbstractFormatService {

    @RequestMapping(value = "/{portal}/{lang}/md.formatter.resource")
    @io.swagger.v3.oas.annotations.Operation(hidden = true)
    public void exec(
        @RequestParam(Params.ID) String xslid,
        @RequestParam(Params.FNAME) String fileName,
        @RequestParam(value = Params.SCHEMA, required = false) String schema,
        HttpServletResponse response
    ) throws Exception {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = applicationContext.getBean(SchemaManager.class).getSchemaDir(schema);
        }

        try {
            FilePathChecker.verify(fileName);
        } catch (BadParameterEx ex) {
            response.sendError(403, fileName + " does not identify a file in the " + xslid + " format bundle");
            return;
        }

        Path formatDir = getAndVerifyFormatDir(applicationContext.getBean(GeonetworkDataDirectory.class), Params.ID, xslid, schemaDir);
        Path desiredFile = formatDir.resolve(fileName);

        if (!Files.isRegularFile(desiredFile)) {
            response.sendError(404, fileName + " does not identify a file in formatter bundle: " + xslid);
            return;
        }

        if (!containsFile(formatDir, desiredFile)) {
            response.sendError(403, fileName + " does not identify a file in the " + xslid + " format bundle");
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
            default:
                response.setContentType("application/octet-stream");
        }
    }
}
