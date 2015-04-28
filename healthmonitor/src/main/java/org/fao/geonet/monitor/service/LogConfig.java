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

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
* get last lines of log
* @author bmaire
*
*/
@Controller("/log")
public class LogConfig {
    private FileAppender fileAppender = null;
    
    private final String fileAppenderName = "fileAppender";
    private final int maxLines = 20000;

    @PostConstruct
    public void init() throws Exception {
        isAppenderLogFileLoaded();
    }


    private boolean isAppenderLogFileLoaded () {
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
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/{lang}/log/file", produces = {
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
            String headerValue = String.format("attachment; filename=\"%s\"","export-log-" + fname + ".zip");
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
                in=new FileInputStream(file);
                while ((read = in.read(bytes)) != -1) {
                    zos.write(bytes, 0, read);
                }
            } finally {
                in.close();
                zos.flush();
                zos.close(); 
            }
        } else {
            throw new RuntimeException("No log file found for download. Check logger configuration.");
        }
    }

    /**
     * Return the last lines of the log file.
     * @param lines Number of lines to return.
     *              Default 2000.
     *              Max number of lines returned 20000.
     * @return
     */
    @RequestMapping(value = "/{lang}/log/activity", produces = {
        MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public String activity(@RequestParam(value = "lines",
            required = false, defaultValue = "2000") int lines) {
        String lastActivity = null;

        if (isAppenderLogFileLoaded()) {
            lastActivity = readLastLines(new File(fileAppender.getFile()),
                    Math.min(lines, maxLines));
        } else {
            throw new RuntimeException("No log file found. Check logger configuration.");
        }
        return lastActivity;
    }
    
    private String readLastLines(File file, int lines) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            
            int line = 0;
            int readByte;
            long filePointer;
            for(filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                readByte = fileHandler.readByte();
                if( readByte == 0xA ) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if(readByte == 0xD ) {
                    if (filePointer < fileLength-1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append( ( char ) readByte );
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            Log.error(Geonet.GEONETWORK, e.getMessage());
            return null;
        } catch( java.io.IOException e ) {
            Log.error(Geonet.GEONETWORK, e.getMessage());
            return null;
        } finally {
            if (fileHandler != null )
            try {
                fileHandler.close();
            } catch (IOException e) {
                Log.error(Geonet.GEONETWORK, e.getMessage());
            }
        }
    }
}