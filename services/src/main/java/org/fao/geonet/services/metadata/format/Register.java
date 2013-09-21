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
import org.fao.geonet.utils.IO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.util.ZipUtil;
import org.jdom.Element;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Upload a formatter bundle.  Uploaded file can be a single xsl or a zip file containing
 * resources as well as the xsl file.  If a zip the zip must contain view.xsl which is the 
 * root xsl file.
 * 
 * The  zip file can be flat or contain a single directory.
 *  
 * @author jeichar
 */
public class Register extends AbstractFormatService {

    public Element exec(Element params, ServiceContext context) throws Exception {
        ensureInitializedDir(context);
        String fileName = Util.getParam(params, Params.FNAME);
        String xslid = Util.getParam(params, Params.ID, null);
        if (xslid == null) {
            xslid = fileName;
            int extentionIdx = xslid.lastIndexOf('.');
            if (extentionIdx != -1) {
                xslid = xslid.substring(0, extentionIdx);
            }
        }

        checkLegalId(Params.ID, xslid);
        File file = new File(userXslDir + xslid);
        
        File uploadedFile = new File(context.getUploadDir(), fileName);
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }
        IO.mkdirs(file, "Formatter directory");

        try {
            try {
                ZipFile zipFile = new ZipFile(uploadedFile);
                handleZipFile(zipFile, file);
            } catch (ZipException e) {
                handleRawXsl(uploadedFile, file);
            } catch (IllegalStateException e){
                FileUtils.deleteDirectory(file);
                throw e;
            }

            addOptionalFiles(file);
            
            Element response = new Element("result");
            Element idElem = new Element("id");
            idElem.setAttribute("id", xslid);
            response.addContent(idElem);

            return response;
        } finally {
            IO.delete(uploadedFile, false, Geonet.FORMATTER);
        }
    }

    private void addOptionalFiles(File file) throws IOException {
    	ConfigFile.generateDefault(file);

        if (!new File(file, "loc").exists()) {
            IO.mkdirs(new File(file, "loc"), "Localization directory");
            PrintStream out = null;
            try {
                out = new PrintStream(new File(file, "loc"+File.separator+"README"), Constants.ENCODING);
                out.println("If a formatter requires localization that cannot be found in strings or schema ");
                out.println("localization the format bundle can have a loc subfolder containing translations.");
                out.println("");
                out.println("The xml document created will have the xml files from loc/<currentLoc>/ added to");
                out.println("xml documentation under the /root/resources tag.");
                out.println("");
                out.println("If a localization folder is not found then the default language will be used.  ");
                out.println("if the default language also does not exist then the first localization will be used");
                out.println("but it is recommended to always have the default language localization");
                out.println("(unless language is fixed in the config.properties)");
            } finally {
                IOUtils.closeQuietly(out);
            }
        }
    }

    private void handleRawXsl(File uploadedFile, File dir) throws IOException {
        FileUtils.moveFile(uploadedFile, new File(dir, VIEW_XSL_FILENAME));
    }

    private void handleZipFile(ZipFile zipFile, File dir) throws IOException {
        ZipUtil.extract(zipFile, dir);
        removeTopDir(dir);
        Collection<File> xslFiles = FileUtils.listFiles(dir, new String[]{"xsl"}, false);
        File toRename = null;
        for (File file : xslFiles) {
            if (file.getName().equals(VIEW_XSL_FILENAME)) return;
            toRename = file;
        }
        
        if (xslFiles.size() > 1 || toRename == null) {
            throw new IllegalStateException("Uploaded zip file must have a view.xsl file or only a single xsl file");
        }
        
        FileUtils.moveFile(toRename, new File(dir, VIEW_XSL_FILENAME));
    }

    private void removeTopDir(File dir) throws IOException {
        File[] files = dir.listFiles(new FileFilter(){
            @Override
            public boolean accept(File file) {
                if (file.isDirectory() && !file.getName().startsWith(".") 
                        && !FileUtils.listFiles(file, new String[]{"xsl"}, false).isEmpty()) {
                    return true;
                }
                return false;
            }
        });
        
        if (files.length == 1) {
            for ( File f : files[0].listFiles()) {
                FileUtils.moveToDirectory(f, dir, true);
            }
            FileUtils.deleteDirectory(files[0]);
        }
    }
}
