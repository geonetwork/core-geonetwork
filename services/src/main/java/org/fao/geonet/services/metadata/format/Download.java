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
import org.fao.geonet.kernel.GeonetworkDataDirectory;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.Util;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Params;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Allows a user to download as a zip file a format and all of the associated data
 * 
 * @author jeichar
 */
public class Download extends AbstractFormatService implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        String xslid = Util.getParam(params, Params.ID, null);
        String schema = Util.getParam(params, Params.SCHEMA, null);
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = context.getBean(SchemaManager.class).getSchemaDir(schema);
        }
        Path formatDir = getAndVerifyFormatDir(context.getBean(GeonetworkDataDirectory.class), Params.ID, xslid, schemaDir);
        
        try {
            
            File tmpDir = (File) context.getServlet().getServletContext().getAttribute("javax.servlet.context.tempdir");
            Path zippedFile = Files.createTempFile(tmpDir.toPath(), xslid, ".zip");

            try (FileSystem zipFs = ZipUtil.createZipFs(zippedFile);
                 DirectoryStream<Path> paths = Files.newDirectoryStream(formatDir)) {
                Path root = zipFs.getRootDirectories().iterator().next();
                for (Path path : paths) {
                    IO.copyDirectoryOrFile(path, root, true);
                }
            }
            
            return BinaryFile.encode(200, zippedFile, xslid + ".zip", true).getElement();
        } catch (IOException e) {
            throw new RuntimeException("Error occured while trying to download file");
        }
    }
}
