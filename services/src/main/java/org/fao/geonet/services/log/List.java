package org.fao.geonet.services.log;

import jeeves.server.ServiceConfig;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.services.log.ListLogFilesResponse.LogFileResponse;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieves all log4j file in folder
 */
@Controller("admin.log.list")
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
    
    @RequestMapping(value = "/{lang}/admin.logfile.list", produces = {
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
