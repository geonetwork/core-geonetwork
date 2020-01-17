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

package org.fao.geonet.services.log;

import jeeves.server.ServiceConfig;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.site.model.ListLogFilesResponse;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.api.site.model.ListLogFilesResponse.LogFileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieves all log4j file in folder
 */
@Controller("admin.log.list")
@Deprecated
public class List {
    private final String regexp = "log4j(-(.*?))?\\.xml";

    @Autowired
    private ApplicationContext appContext;
    //--------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    @RequestMapping(value = "/{portal}/{lang}/admin.logfile.list", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    ListLogFilesResponse exec() throws Exception {
        java.util.List<LogFileResponse> logFileList = new ArrayList<LogFileResponse>();
        final GeonetworkDataDirectory dataDirectory =
            ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        String classesFolder = dataDirectory.getWebappDir() + "/WEB-INF/classes";
        File folder = new File(classesFolder);

        if (folder != null && folder.isDirectory()) {
            Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            Matcher matcher;
            String fileName;
            for (File file : folder.listFiles()) {
                fileName = file.getName();
                matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    String key = matcher.group(2);

                    if (StringUtils.isEmpty(key))
                        key = "prod";
                    logFileList.add(new LogFileResponse(key.toUpperCase(), fileName));
                }
            }
        }
        return new ListLogFilesResponse(logFileList);
    }
}
