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
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.bridge.AppenderWrapper;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.FileUtil;
import org.fao.geonet.utils.Log;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Service to get last lines of log.
 *
 * This service is deprecated, please transition to use of {@code LoggingApi} service.
 *
 * @author bmaire
 */
@Deprecated
@Controller("/log")
public class LogConfig {
    /**
     * This is the name of the RollingFileAppender in your log4j2.xml configuration file.
     *
     * LogConofig uses this name to lookup RollingFileAppender to check configuration in
     * case a custom log file location has been used.
     */
    private static final String FILE_APPENDER_NAME = "File";

    private static final int MAX_LINES = 20000;

    /**
     * Download the log file in a ZIP.
     */
    @RequestMapping(value = "/{portal}/{lang}/log/file", produces = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public void getLog(HttpServletResponse response) throws IOException {

        File logfile = logfile();
        if (logfile != null) {
            // create ZIP FILE

            String fname = String.valueOf(Calendar.getInstance().getTimeInMillis());

            // set headers for the response
            response.setContentType("application/zip");
            response.setContentLength((int) logfile.length());
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

        File logfile = logfile();
        if (logfile != null) {
            lastActivity = FileUtil.readLastLines( logfile, Math.min(lines, MAX_LINES));
        } else {
            throw new RuntimeException("No log file found. Check logger configuration.");
        }
        return lastActivity;
    }

    /**
     * Logfile location as determined from appender, or system property, or default.
     *
     * Note this code is duplicated with the {@code LoggingApi} replacement
     *
     * @return logfile location, or {@code null} if unable to determine
     */
    private File logfile(){
        // Appender is supplied by LogUtils based on parsing log4j2.xml file indicated
        // by database settings

        // First, try the fileappender from the logger named "geonetwork"
        Appender appender = Logger.getLogger(Geonet.GEONETWORK).getAppender(FILE_APPENDER_NAME);
        // If still not found, try the one from the logger named "jeeves"
        if (appender == null) {
            appender = Logger.getLogger(Log.JEEVES).getAppender(FILE_APPENDER_NAME);
        }
        if (appender != null) {
            if (appender instanceof AppenderWrapper) {
                AppenderWrapper wrapper = (AppenderWrapper) appender;
                org.apache.logging.log4j.core.Appender appender2 = wrapper.getAppender();

                if (appender2 instanceof FileAppender) {
                    FileAppender fileAppender = (FileAppender) appender2;
                    String logFileName = fileAppender.getFileName();
                    if (logFileName != null) {
                        File logFile = new File(logFileName);
                        if (logFile.exists()) {
                            return logFile;
                        }
                    }
                }
                if (appender2 instanceof RollingFileAppender) {
                    RollingFileAppender fileAppender = (RollingFileAppender) appender2;
                    String logFileName = fileAppender.getFileName();
                    if (logFileName != null) {
                        File logFile = new File(logFileName);
                        if (logFile.exists()) {
                            return logFile;
                        }
                    }
                }
            }
        }
        Log.warning(Geonet.GEONETWORK, "Error when getting logger file for the " + "appender named '" + FILE_APPENDER_NAME + "'. "
            + "Check your log configuration file. "
            + "A FileAppender or RollingFileAppender is required to return last activity to the user interface."
            + "Appender file not found.");

        if (System.getProperties().containsKey("log_dir")){
            File logDir = new File( System.getProperty("log_dir"));
            if( logDir.exists() && logDir.isDirectory()){
                File logFile = new File( logDir, "logs/geonetwork.log");
                if(logFile.exists()){
                    return logFile;
                }
            }
        }
        else  {
            File logFile = new File("logs/geonetwork.log");
            if(logFile.exists()){
                return logFile;
            }
        }
        return null; // unavailable
    }

}
