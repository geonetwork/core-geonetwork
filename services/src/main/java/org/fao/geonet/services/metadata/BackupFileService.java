/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.metadata;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author heikki doeleman
 */
@Deprecated
public abstract class BackupFileService extends NotInReadOnlyModeService {

    /**
     *
     * @param context
     * @param id
     * @param uuid
     * @param file
     */
    protected void backupFile(ServiceContext context, String id, String uuid, Path file) {
        Path outDir = Lib.resource.getRemovedDir(context, id);
        Path outFile;
        try {
            // When metadata records contains character not supported by filesystem
            // it may be an issue. eg. acri-st.fr/96443
            outFile = outDir.resolve(URLEncoder.encode(uuid, Constants.ENCODING) + ".mef");
        } catch (UnsupportedEncodingException e1) {
            outFile = outDir.resolve(uuid + ".mef");
        }


        try {
            Files.createDirectories(outDir);
            try (InputStream is = IO.newInputStream(file);
                 OutputStream os = Files.newOutputStream(outFile)) {

                BinaryFile.copy(is, os);
            }
        } catch (Exception e) {
            Log.warning(Geonet.GEONETWORK, "Cannot backup mef file : "+e.getMessage(), e);
        }

        IO.deleteFile(file, false, Geonet.MEF);
    }
}
