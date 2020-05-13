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

package org.fao.geonet.services.resources;

import com.google.common.collect.Maps;

import org.fao.geonet.api.processing.XslProcessUtils;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.responses.IdResponse;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.metadata.XslProcessing;
import org.fao.geonet.services.resources.handlers.IResourceRemoveHandler;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;

/**
 * Delete an uploaded file from the data directory and remote its reference in the metadata record.
 */
@ReadWriteController
@Controller("resource.del.and.detach")
@Deprecated
public class RemoveAndProcess {

    @Autowired
    private DataManager dm;
    @Autowired
    private XslProcessing xslProcessing;
    @Autowired
    private ServiceManager serviceManager;

    @RequestMapping(value = {"/{portal}/{lang}/resource.del.and.detach"}, produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    IdResponse serviceSpecificExec(@PathVariable String lang,
                                   HttpServletRequest request,
                                   @RequestParam(value = Params.URL) String url,
                                   @RequestParam(defaultValue = "") String id,
                                   @RequestParam(defaultValue = "") String uuid)
        throws Exception {
        ServiceContext context = serviceManager.createServiceContext("resource.del.and.detach", lang, request);
        if (id.trim().isEmpty()) {
            id = dm.getMetadataId(uuid);
        }
        Lib.resource.checkEditPrivilege(context, id);

        // Analyze the URL to extract file name and private/public folder
        URL resourceURL = new URL(url);
        String[] parameters = resourceURL.getQuery().split("&");
        String filename = "";
        String access = "";

        for (String param : parameters) {
            if (param.startsWith("fname")) {
                String[] parts = param.split("=");
                filename = parts.length == 2 ? parts[1] : "";
            } else if (param.startsWith("access")) {
                String[] parts = param.split("=");
                access = parts.length == 2 ? parts[1] : "";
            }
        }


        // Remove the file and update the file upload/downloads tables
        IResourceRemoveHandler removeHook = context.getBean("resourceRemoveHandler",
                                                            IResourceRemoveHandler.class);
        removeHook.onDelete(context, request, Integer.parseInt(id), filename, access);

        // Set parameter and process metadata to remove reference to the uploaded file
        Map<String, String[]> allParams = Maps.newHashMap(request.getParameterMap());

        allParams.put("name", new String[]{filename});
        allParams.put("protocol", new String[]{"WWW:DOWNLOAD-1.0-http--download"});

        String process = "onlinesrc-remove";
        XsltMetadataProcessingReport report = new XsltMetadataProcessingReport(process);

        Element processedMetadata;
        try {
            final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
            processedMetadata = XslProcessUtils.process(context, id, process,
                true, true, true, report, siteURL, allParams);
            if (processedMetadata == null) {
                throw new BadParameterEx("Processing failed", "Not found:"
                    + report.getNumberOfRecordNotFound() + ", Not owner:" + report.getNumberOfRecordsNotEditable()
                    + ", No process found:" + report.getNoProcessFoundCount() + ".");
            }
        } catch (Exception e) {
            throw e;
        }

        return new IdResponse(id);
    }
}
