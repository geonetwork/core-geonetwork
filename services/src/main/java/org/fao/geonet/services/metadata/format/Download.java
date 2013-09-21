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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.Util;
import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;

/**
 * Allows a user to download as a zip file a format and all of the associated data
 * 
 * @author jeichar
 */
public class Download extends AbstractFormatService {

    public Element exec(Element params, ServiceContext context) throws Exception {
        ensureInitializedDir(context);
        String xslid = Util.getParam(params, Params.ID, null);
        File formatDir = getAndVerifyFormatDir(Params.ID, xslid);
        
        try {
            
            File tmpDir = (File) context.getServlet().getServletContext()
                    .getAttribute("javax.servlet.context.tempdir");
            File zippedFile = File.createTempFile(xslid, ".zip", tmpDir);
            ZipOutputStream zipOut = new ZipOutputStream(zippedFile);
            try {
                zipDir(xslid, formatDir, zipOut);
            } finally {
                zipOut.close();
            }
            
            return BinaryFile.encode(200, zippedFile.getAbsolutePath(), xslid+".zip", true);
        } catch (IOException e) {
            throw new RuntimeException("Error occured while trying to download file");
        }

    }

    private void zipDir(String basePath, File formatDir, ZipOutputStream zipOut) throws IOException {
        for(File f : formatDir.listFiles()) {
            if(f.isFile()) {
                ZipEntry nextExtry = new ZipEntry(basePath + File.separator + f.getName());
                zipOut.putNextEntry(nextExtry );
                FileUtils.copyFile(f, zipOut);
            } else {
                zipDir(basePath + File.separator + f.getName(), f, zipOut);
            }
        }
    }
}