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

package org.fao.geonet.kernel.harvest.harvester;

import org.fao.geonet.Logger;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by francois on 3/7/14.
 */
public class HarvesterUtil {
    public static Pair<String, Map<String, Object>> parseXSLFilter(String filter,
                                                                   Logger log) {
        String processName = filter;
        Map<String, Object> processParams = new HashMap<String, Object>();

        // Parse complex xslfilter process_name?process_param1=value&process_param2=value...
        if (filter.contains("?")) {
            String[] filterInfo = filter.split("\\?");
            processName = filterInfo[0];
            if (log.isDebugEnabled()) log.debug("      - XSL Filter name:" + processName);
            if (filterInfo[1] != null) {
                String[] filterKVP = filterInfo[1].split("&");
                for (String kvp : filterKVP) {
                    String[] param = kvp.split("=");
                    if (param.length == 2) {
                        if (log.isDebugEnabled()) {
                            log.debug("        with param:" + param[0] + " = " + param[1]);
                        }
                        processParams.put(param[0], param[1]);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("        no value for param: "
                                + param[0]);
                        }
                    }
                }
            }
        }
        return Pair.read(processName, processParams);
    }


    /**
     * Filter the metadata if process parameter is set and corresponding XSL transformation exists.
     */
    public static Element processMetadata(MetadataSchema metadataSchema,
                                          Element md,
                                          String processName,
                                          Map<String, Object> processParams,
                                          Logger log) {

        Path filePath = metadataSchema.getSchemaDir().resolve("process").resolve(processName + ".xsl");
        if (!Files.exists(filePath)) {
            log.info("     processing instruction not found for " + metadataSchema.getName() + " schema. metadata not filtered.");
        } else {
            Element processedMetadata;
            try {
                processedMetadata = Xml.transform(md, filePath, processParams);
                if (log.isDebugEnabled()) log.debug("     metadata filtered.");
                md = processedMetadata;
            } catch (Exception e) {
                log.warning("     processing error (" + processName + "): "
                    + e.getMessage());
            }
        }
        return md;
    }
}
