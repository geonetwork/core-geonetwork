package org.fao.geonet.services.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geonet;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Return the logfile
 */

@Controller("getLog")
public class GetLog {

    @RequestMapping(value = "/{lang}/getlog", produces = {
            MediaType.TEXT_PLAIN_VALUE })
    public void exec(HttpServletResponse response) throws Exception {
        Logger log = Logger.getLogger(Geonet.GEONETWORK);
        String fileName = null;

        @SuppressWarnings("unchecked")
        Enumeration<Appender> en = log.getAllAppenders();
        while (en.hasMoreElements()) {
            Appender a = en.nextElement();
            if (a instanceof FileAppender) {
                fileName = ((FileAppender) a).getFile();
            }
        }
        if (fileName != null) {
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"geonetwork.log\"");

            File file = new File(fileName);
            InputStream is = new FileInputStream(file);
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        }
    }
}
