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
    private final int maxLines = 200;
    
    //--------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------
    @PostConstruct
    public void init() throws Exception {
        fileAppender = (FileAppender) Logger.getLogger(Geonet.GEONETWORK).getAppender(fileAppenderName);
        
        if (fileAppender == null) {
            fileAppender = (FileAppender) Logger.getLogger(Log.JEEVES).getAppender(fileAppenderName);
        }    
        
    }
    
    @RequestMapping(value = "/{lang}/log/file", produces = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public void getLog(HttpServletResponse response) throws IOException {
        if (fileAppender != null) {
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
        }
    }
    
    @RequestMapping(value = "/{lang}/log/activity", produces = {
        MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public String activity() {
        String lastActivity = null;
        
        if (fileAppender != null) {
            lastActivity = readLastLines(new File(fileAppender.getFile()));
        }          
        return lastActivity;
    }
    
    private String readLastLines(File file) {
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
                if (line >= maxLines) {
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