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

import org.fao.geonet.ZipUtil;
import org.fao.geonet.exceptions.BadFormatEx;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PRIVATE;
import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PUBLIC;
import static org.fao.geonet.kernel.mef.MEFConstants.DIR_STORE;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_INFO;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_METADATA;

/**
 * MEF version 1 visitor to process and load MEF files.
 */
public class MEFVisitor implements IVisitor {
    public void visit(Path mefFile, IMEFVisitor v) throws Exception {
        Element info = handleXml(mefFile, v);
        handleBin(mefFile, v, info, 0);

        // Index the record so that the resources are included
        v.indexMetadata(0);
    }

    // --------------------------------------------------------------------------

    /**
     * Read the input MEF file and check structure for metadata.xml and info.xml files.
     */
    public Element handleXml(Path mefFile, IMEFVisitor v) throws Exception {
        Element md = null;
        Element info = null;

        try (FileSystem zipFs = ZipUtil.openZipFs(mefFile)) {
            Path fileMdFile = zipFs.getPath(FILE_METADATA);
            Path infoMdFile = zipFs.getPath(FILE_INFO);

            if (Files.exists(fileMdFile)) {
                md = Xml.loadFile(fileMdFile);
            }
            if (Files.exists(infoMdFile)) {
                info = Xml.loadFile(infoMdFile);
            }
        }

        if (md == null)
            throw new BadFormatEx("Missing metadata file : " + FILE_METADATA);

        if (info == null)
            throw new BadFormatEx("Missing info file : " + FILE_INFO);

        v.handleMetadata(md, 0);
        v.handleInfo(info, 0);

        return info;
    }

    /**
     * Check binary files structure. Binary files are stored flat in the store folder (MEF 3.0+)
     * or split across the legacy public/private folders (pre-3.0). All binary files MUST be
     * registered in the information document (ie. info.xml).
     */
    public void handleBin(Path mefFile, IMEFVisitor v, Element info, int index)
        throws Exception {

        // Reads either the MEF 3.0 unified <store> element (filtered by access) or the pre-3.0
        // <public>/<private> elements; either way, this is only a changeDate lookup table.
        List<Element> pubFiles = MEFLib.getFilesElement(info, "public");
        List<Element> prvFiles = MEFLib.getFilesElement(info, "private");

        try (FileSystem zipFs = ZipUtil.openZipFs(mefFile)) {
            // MEF 3.0+ layout: every resource - public and private alike - lives flat under
            // store/, so which handler to call is decided per file from its own registration in
            // pubFiles/prvFiles, not from where it physically is.
            Path storePath = zipFs.getPath(DIR_STORE);
            if (Files.isDirectory(storePath)) {
                try (Stream<Path> paths = Files.walk(storePath)) {
                    for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                        String relativeName = IO.toUnixStylePath(storePath.relativize(path));
                        try (InputStream isb = IO.newInputStream(path)) {
                            if (MEFLib.isRegisteredFile(pubFiles, relativeName)) {
                                v.handlePublicFile(relativeName, MEFLib.getChangeDate(pubFiles, relativeName), isb, 0);
                            } else {
                                v.handlePrivateFile(relativeName, MEFLib.getChangeDate(prvFiles, relativeName), isb, 0);
                            }
                        }
                    }
                }
            }

            // Legacy (pre-3.0) layout: separate public/ and private/ folders, kept for archives
            // written before the flat store/ folder existed.
            Path pubPath = zipFs.getPath(DIR_PUBLIC);
            if (Files.isDirectory(pubPath)) {
                // Resources may live in subfolders (nested paths): walk the whole tree, using
                // the path relative to pubPath - not just the file's own name - as the resource
                // name, so it matches the "/"-separated names registered in info.xml.
                try (Stream<Path> paths = Files.walk(pubPath)) {
                    for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                        String relativeName = IO.toUnixStylePath(pubPath.relativize(path));
                        try (InputStream isb = IO.newInputStream(path)) {
                            v.handlePublicFile(relativeName, MEFLib.getChangeDate(pubFiles, relativeName), isb, 0);
                        }
                    }
                }
            }
            Path priPath = zipFs.getPath(DIR_PRIVATE);
            if (Files.isDirectory(priPath)) {
                try (Stream<Path> paths = Files.walk(priPath)) {
                    for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                        String relativeName = IO.toUnixStylePath(priPath.relativize(path));
                        try (InputStream isb = IO.newInputStream(path)) {
                            v.handlePrivateFile(relativeName, MEFLib.getChangeDate(prvFiles, relativeName), isb, 0);
                        }
                    }
                }
            }
        }
    }

}

// =============================================================================
