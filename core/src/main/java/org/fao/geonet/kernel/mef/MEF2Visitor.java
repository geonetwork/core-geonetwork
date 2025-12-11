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

package org.fao.geonet.kernel.mef;

import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadFormatEx;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static org.fao.geonet.kernel.mef.MEFConstants.FILE_INFO;

/**
 * MEF version 2 visitor
 */
public class MEF2Visitor implements IVisitor {

    public void visit(Path mefFile, IMEFVisitor v) throws Exception {
        handleXml(mefFile, v);
    }

    /**
     * Read the input MEF file and for each metadata found, check structure for metadata.xml,
     * info.xml and optional feature catalogue files.
     */
    public Element handleXml(Path mefFile, IMEFVisitor v) throws Exception {

        Logger log = Log.createLogger(Geonet.MEF);

        int nbMetadata = 0;
        Element fc;

        Element info = new Element("info");


        try (FileSystem zipFs = ZipUtil.openZipFs(mefFile)) {
            Path root = zipFs.getRootDirectories().iterator().next();
            // Get the metadata depth
            if (IO.isEmptyDir(root)) {
                log.debug("Metadata folder is directly under the unzip temporary folder.");
            } else {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                    for (Path file : paths) {
                        if (Files.isDirectory(file)) {
                            // Handle metadata file
                            Path metadataDir = file.resolve("metadata");

                            if (IO.isEmptyDir(metadataDir)) {
                                throw new BadFormatEx(
                                    "Missing XML document in metadata folder " + metadataDir + " in MEF file "
                                        + mefFile + ".");
                            }

                            // Handle feature catalog
                            Path fcFile = getFeatureCalalogFile(file);
                            if (fcFile != null) {
                                fc = Xml.loadFile(fcFile);
                            } else {
                                fc = null;
                            }

                            // Handle info file
                            Path fileInfo = file.resolve(FILE_INFO);
                            if (Files.exists(fileInfo)) {
                                info = Xml.loadFile(fileInfo);
                            }

                            try (DirectoryStream<Path> xmlFiles = Files.newDirectoryStream(metadataDir)) {
                                v.handleMetadataFiles(xmlFiles, info, nbMetadata);
                            }
                            v.handleFeatureCat(fc, nbMetadata);
                            v.handleInfo(info, nbMetadata);

                            // Handle binaries
                            handleBin(file, v, info, nbMetadata);

                            // Index the record so that the resources are included
                            v.indexMetadata(nbMetadata);

                            nbMetadata++;
                        }
                    }
                }
            }
        }

        return info;
    }

    /**
     * Check binary files to import.
     */
    public void handleBin(Path file, IMEFVisitor v, Element info, int index)
        throws Exception {

        List<Element> pubFiles = null;
        List<Element> prvFiles = null;

        if (info.getChildren().size() != 0) {
            @SuppressWarnings("unchecked")
            List<Element> tmpPub = info.getChild("public").getChildren();
            pubFiles = tmpPub;
            @SuppressWarnings("unchecked")
            List<Element> tmpPrv = info.getChild("private").getChildren();
            prvFiles = tmpPrv;
        }

        Path publicFile = file.resolve(MEFConstants.DIR_PUBLIC);
        Path privateFile = file.resolve(MEFConstants.DIR_PRIVATE);

        // Handle public binaries files
        if (Files.exists(publicFile) && pubFiles != null && pubFiles.size() != 0) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(publicFile)) {
                for (Path path : paths) {
                    String fileName = path.getFileName().toString();
                    try (InputStream in = IO.newInputStream(path)) {
                        v.handlePublicFile(fileName,
                            MEFLib.getChangeDate(pubFiles, fileName),
                            in, index);
                    }
                }
            }
        }

        // Handle private binaries files
        if (Files.exists(privateFile) && prvFiles != null && prvFiles.size() != 0) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(privateFile)) {
                for (Path path : paths) {
                    String fileName = path.getFileName().toString();
                    try (InputStream in = IO.newInputStream(path)) {
                        v.handlePrivateFile(fileName,
                            MEFLib.getChangeDate(prvFiles, fileName),
                            in, index);
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------

    /**
     * getFeatureCalalogFile method return feature catalog xml file if exists
     *
     * @return File
     */
    private Path getFeatureCalalogFile(Path file) throws IOException {
        Path tmp = null;
        Path fcRepo = file.resolve(MEFConstants.SCHEMA);

        if (Files.exists(fcRepo)) {
            Path fc = fcRepo.resolve(MEFConstants.FILE_METADATA);
            if (Files.exists(fc))
                tmp = fc;
            else {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(fcRepo)) {
                    final Iterator<Path> iterator = paths.iterator();
                    // Retrieve first files into applschema directory, without any
                    // tests.
                    if (iterator.hasNext()) {
                        tmp = iterator.next();
                    }
                }
            }
        }
        return tmp;
    }
}
