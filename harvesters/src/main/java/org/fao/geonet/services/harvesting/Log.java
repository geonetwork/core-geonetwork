/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.services.harvesting;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import opendap.servlet.BadURLException;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Download a logfile from harvesting
 *
 * @author delawen
 */
@Deprecated
public class Log implements Service {
    public void init(Path appPath, ServiceConfig config) throws Exception {

    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {
        String logfile = params.getChildText("file").trim();

        // Security checks, this is no free proxy!!
        if (logfile.startsWith("http") || logfile.startsWith("ftp")
            || logfile.startsWith("sftp")) {
            throw new BadURLException(
                "This is no proxy. Stopping possible hacking attempt to url: "
                    + logfile);
        }

        if (!logfile.endsWith(".log")) {
            throw new BadURLException(
                "Strange suffix for this log file. Stopping possible hacking attempt to uri: "
                    + logfile);
        }

        if (!logfile.contains("harvester_")) {
            throw new BadURLException(
                "This doesn't seem like a harvester log file. Stopping possible hacking attempt to uri: "
                    + logfile);
        }


        File mainLogFile = GeonetworkDataDirectory.getLogfile();
        Path pathLogFile;
        if (mainLogFile != null) {
            pathLogFile = mainLogFile.toPath().getParent().resolve(logfile);
        }
        else {
            pathLogFile = Paths.get(logfile);
        }

        if (!Files.exists(pathLogFile) || !Files.isReadable(pathLogFile)) {
            throw new NullPointerException(String.format(
                "Couldn't find or read the logfile %s in catalogue log directory. Check log file configuration.",
                logfile));
        }

        return BinaryFile.encode(200, pathLogFile.toAbsolutePath().normalize(), false).getElement();
    }

}
