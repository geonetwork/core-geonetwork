//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.status;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geonet;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Preview the logfile
 */

@Controller("previewLog")
public class PreviewLog {

    @RequestMapping(value = "/{lang}/previewlog", produces = {
            MediaType.TEXT_PLAIN_VALUE })
    public @ResponseBody String exec() throws Exception {
        StringBuilder res = new StringBuilder();

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
            File file = new File(fileName);

            RandomAccessFile object = new RandomAccessFile(file, "r");
            try {
                long pos = file.length() - 5000;
                if (pos < 0) {
                    pos = 0;
                }
                object.seek(pos);
                String line = object.readLine();
                while ((line = object.readLine()) != null) {
                    res.append(line);
                    res.append('\n');
                }
            } catch (Exception e) {

            } finally {
                if (object != null) {
                    object.close();
                }
            }
        }
        return res.toString();
    }
}