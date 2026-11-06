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

import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * MEF version 3 visitor. Reads a MEF {@link MEFLib.Version#V3} archive: the same one-folder-per-
 * record container as {@link MEF2Visitor} (inherited {@link #handleXml} traversal is identical),
 * but each record's attachments - public and private alike - live in a single flat {@code store}
 * directory, dispatched to the public/private handler based on its own registration in info.xml
 * (see {@link MEFLib#isRegisteredFile}), not from where it physically is.
 */
public class MEF3Visitor extends MEF2Visitor {

    /**
     * Check binary files to import. Every resource - public and private alike - lives flat under
     * store/, so which handler to call is decided per file from its own registration in
     * info.xml, not from where it physically is.
     */
    @Override
    public void handleBin(Path file, IMEFVisitor v, Element info, int index)
        throws Exception {

        List<Element> pubFiles = MEFLib.getFilesElement(info, "public");
        List<Element> prvFiles = MEFLib.getFilesElement(info, "private");

        Path storeFile = file.resolve(MEFConstants.DIR_STORE);
        if (Files.exists(storeFile) && (!pubFiles.isEmpty() || !prvFiles.isEmpty())) {
            try (Stream<Path> paths = Files.walk(storeFile)) {
                for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                    String fileName = IO.toUnixStylePath(storeFile.relativize(path));
                    try (InputStream in = IO.newInputStream(path)) {
                        if (MEFLib.isRegisteredFile(pubFiles, fileName)) {
                            v.handlePublicFile(fileName, MEFLib.getChangeDate(pubFiles, fileName), in, index);
                        } else {
                            v.handlePrivateFile(fileName, MEFLib.getChangeDate(prvFiles, fileName), in, index);
                        }
                    }
                }
            }
        }
    }
}
