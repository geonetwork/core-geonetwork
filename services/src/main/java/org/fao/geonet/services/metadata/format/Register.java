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

import com.vividsolutions.jts.util.Assert;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.Util;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static org.fao.geonet.services.metadata.format.FormatterConstants.VIEW_XSL_FILENAME;

/**
 * Upload a formatter bundle.  Uploaded file can be a single xsl or a zip file containing
 * resources as well as the xsl file.  If a zip the zip must contain view.xsl which is the 
 * root xsl file.
 * 
 * The  zip file can be flat or contain a single directory.
 *  
 * @author jeichar
 */
public class Register extends AbstractFormatService implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
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
        Path userXslDir = context.getBean(GeonetworkDataDirectory.class).getFormatterDir();
        Path newBundle = userXslDir.resolve(xslid);
        
        Path uploadedFile = context.getUploadDir().resolve(fileName);

        Files.createDirectories(newBundle);

        try {

            try (FileSystem zipFs = ZipUtil.openZipFs(uploadedFile)){
                Path viewFile = findViewFile(zipFs);
                if (viewFile == null) {
                    throw new IllegalArgumentException(
                            "A formatter zip file must contain a " + VIEW_XSL_FILENAME + " file as one of its root files");
        }
                ZipUtil.extract(zipFs, newBundle);

            } catch (IOException e) {
                handleRawXsl(uploadedFile, newBundle);
            } catch (Exception e){
                IO.deleteFileOrDirectory(newBundle);
                throw e;
            }

            addOptionalFiles(newBundle);
            
            Element response = new Element("result");
            Element idElem = new Element("id");
            idElem.setAttribute("id", xslid);
            response.addContent(idElem);

            return response;
        } finally {
            IO.deleteFile(uploadedFile, false, Geonet.FORMATTER);
        }
    }

    private Path findViewFile(FileSystem zipFs) throws IOException {
        Path rootView = zipFs.getPath(VIEW_XSL_FILENAME);
        if (Files.exists(rootView)) {
            return rootView;
        }
        final String groovyView = "view.groovy";
        rootView = zipFs.getPath(groovyView);
        if (Files.exists(rootView)) {
            return rootView;
        }
        final Path rootDir = zipFs.getRootDirectories().iterator().next();
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(rootDir, IO.DIRECTORIES_FILTER)) {
            Iterator<Path> dirIter = dirs.iterator();
            if (dirIter.hasNext()) {
                Path next = dirIter.next();
                Assert.isTrue(!dirIter.hasNext(),
                        "The formatter/view zip file must either have a single root directory which contains the view file or " +
                        "it must have all formatter resources at the root of the directory");
                rootView = next.resolve(VIEW_XSL_FILENAME);
                if (Files.exists(rootView)) {
                    return rootView;
                }
                rootView = next.resolve(groovyView);
                if (Files.exists(rootView)) {
                    return rootView;
                }
            }
        }
        return null;
    }

    private void addOptionalFiles(Path file) throws IOException {
    	ConfigFile.generateDefault(file);

        final Path locDir = file.resolve("loc");
        if (!Files.exists(locDir)) {
            Files.createDirectories(locDir);
            try (PrintStream out = new PrintStream(Files.newOutputStream(locDir.resolve("README")), true, Constants.ENCODING)) {
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
            }
        }
    }

    private void handleRawXsl(Path uploadedFile, Path dir) throws IOException {
        Files.createDirectories(dir);
        IO.moveDirectoryOrFile(uploadedFile, dir.resolve(VIEW_XSL_FILENAME), false);
    }
}
