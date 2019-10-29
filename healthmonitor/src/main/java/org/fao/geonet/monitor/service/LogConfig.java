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

package org.fao.geonet.monitor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.FileUtil;
import org.fao.geonet.utils.Log;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * get last lines of log
 *
 * @author bmaire
 */
@Deprecated
@Controller("/log")
public class LogConfig {
    private static final String fileAppenderName = "fileAppender";
    private static final int maxLines = 20000;
    private FileAppender fileAppender = null;

    @PostConstruct
    public void init() throws Exception {
        isAppenderLogFileLoaded();
    }


    boolean isAppenderLogFileLoaded() {
        if (fileAppender == null || fileAppender.getFile() == null) {
            fileAppender = (FileAppender) Logger.getLogger(Geonet.GEONETWORK).getAppender(fileAppenderName);

            if (fileAppender == null) {
                fileAppender = (FileAppender) Logger.getLogger(Log.JEEVES).getAppender(fileAppenderName);
            }

            if (fileAppender == null) {
                Log.error(Geonet.GEONETWORK,
                    "Error when getting appender named 'fileAppender'. " +
                        "Check your log configuration file. " +
                        "No appender found.");
                return false;
            } else {
                String logFileName = fileAppender.getFile();
                if (logFileName == null) {
                    Log.error(Geonet.GEONETWORK,
                        "Error when getting logger file for the " +
                            "appender named 'fileAppender'. " +
                            "Check your log configuration file. " +
                            "A FileAppender is required to return last activity to the user interface." +
                            "Appender file not found.");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Download the log file in a ZIP.
     */
    @RequestMapping(value = "/{portal}/{lang}/log/file", produces = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public void getLog(HttpServletResponse response) throws IOException {
        if (isAppenderLogFileLoaded()) {
            File file = new File(fileAppender.getFile());

            // create ZIP FILE

            String fname = String.valueOf(Calendar.getInstance().getTimeInMillis());

            // set headers for the response
            response.setContentType("application/zip");
            response.setContentLength((int) file.length());
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", "export-log-" + fname + ".zip");
            response.setHeader(headerKey, headerValue);

            int read = 0;
            byte[] bytes = new byte[1024];
            ZipOutputStream zos = null;
            ZipEntry ze;
            InputStream in = null;
            try {
                zos = new ZipOutputStream(response.getOutputStream());
                ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);
                in = new FileInputStream(file);
                while ((read = in.read(bytes)) != -1) {
                    zos.write(bytes, 0, read);
                }
            } finally {
                IOUtils.closeQuietly(in);
                if (zos != null) zos.flush();
                IOUtils.closeQuietly(zos);
            }
        } else {
            throw new RuntimeException("No log file found for download. Check logger configuration.");
        }
    }

    /**
     * Return the last lines of the log file.
     *
     * @param lines Number of lines to return. Default 2000. Max number of lines returned 20000.
     */
    @RequestMapping(value = "/{portal}/{lang}/log/activity", produces = {
        MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public String activity(@RequestParam(value = "lines",
        required = false, defaultValue = "2000") int lines) {
        String lastActivity = null;

        if (isAppenderLogFileLoaded()) {
            lastActivity = FileUtil.readLastLines(new File(fileAppender.getFile()),
                Math.min(lines, maxLines));
        } else {
            throw new RuntimeException("No log file found. Check logger configuration.");
        }
        return lastActivity;
    }

}
