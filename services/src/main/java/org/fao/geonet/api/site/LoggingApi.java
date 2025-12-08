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

package org.fao.geonet.api.site;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.api.site.model.ListLogFilesResponse;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.fao.geonet.api.ApiParams.API_CLASS_CATALOG_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_CATALOG_TAG;


@RequestMapping(value = {
    "/{portal}/api/site/logging"
})
@Tag(name = API_CLASS_CATALOG_TAG,
    description = API_CLASS_CATALOG_OPS)
@Controller("logging")
@PreAuthorize("hasAuthority('Administrator')")
public class LoggingApi {
    private static final int MAX_LINES = 20000;

    /**
     * List log4j2 configuration files.
     */
    private final String regexp = "log4j2(-(.*?))?\\.xml";
    @Autowired
    GeonetworkDataDirectory dataDirectory;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get log files",
        description = "")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<ListLogFilesResponse.LogFileResponse> getLogFiles(
        HttpServletRequest request
    ) throws Exception {
        java.util.List<ListLogFilesResponse.LogFileResponse> logFileList =
            new ArrayList<>();
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

                    if (ObjectUtils.isEmpty(key))
                        key = "prod";
                    logFileList.add(
                        new ListLogFilesResponse.LogFileResponse(
                            key.toUpperCase(),
                            fileName));
                }
            }
        }
        return logFileList;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get last activity",
        description = "")
    @RequestMapping(
        value = "/activity",
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        })
    @ResponseBody
    public String getLastActivity(
        @Parameter(
            description = "Number of lines to return",
            required = false
        )
        @RequestParam(
            value = "lines",
            required = false,
            defaultValue = "2000")
            int lines) {
        String lastActivity = null;

        File logfile = GeonetworkDataDirectory.getLogfile();
        if (logfile != null) {
            lastActivity = FileUtil.readLastLines(logfile, Math.min(lines, MAX_LINES));
        } else {
            throw new RuntimeException("No log file found. Check logger configuration.");
        }
        return lastActivity;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get last activity in a ZIP",
        description = "")
    @RequestMapping(
        value = "/activity/zip",
        method = RequestMethod.GET,
        produces = {
            "application/zip"
        })
    @ResponseBody
    public void getLastActivityInAZip(HttpServletResponse response) throws IOException {
        File logfile = GeonetworkDataDirectory.getLogfile();
        if (logfile != null) {
            // create ZIP FILE

            String fname = String.valueOf(Calendar.getInstance().getTimeInMillis());

            // set headers for the response
            response.setContentType("application/zip");
            String headerKey = "Content-Disposition";
            String headerValue = String.format(
                "attachment; filename=\"catalog-log-%s-%s.zip\"",
                fname,
                new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date())
            );
            response.setHeader(headerKey, headerValue);

            int read = 0;
            byte[] bytes = new byte[1024];
            ZipOutputStream zos = null;
            ZipEntry ze;
            InputStream in = null;
            try {
                zos = new ZipOutputStream(response.getOutputStream());
                ze = new ZipEntry(logfile.getName());
                zos.putNextEntry(ze);
                in = new FileInputStream(logfile);
                while ((read = in.read(bytes)) != -1) {
                    zos.write(bytes, 0, read);
                }
            } finally {
                IOUtils.closeQuietly(in);
                if (zos != null) zos.flush();
                IOUtils.closeQuietly(zos);
            }
        } else {
            throw new RuntimeException(
                "No log file found for download. Check logger configuration."
            );
        }
    }
}
