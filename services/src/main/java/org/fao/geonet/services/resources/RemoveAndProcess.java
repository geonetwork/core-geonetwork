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

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.responses.IdResponse;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.metadata.XslProcessing;
import org.fao.geonet.services.metadata.XslProcessingReport;
import org.fao.geonet.services.resources.handlers.IResourceRemoveHandler;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Delete an uploaded file from the data directory and remote its
 * reference in the metadata record.
 */
@ReadWriteController
@Controller("resource.del.and.detach")
public class RemoveAndProcess  {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    @Autowired 
    private ServiceContext context;
    @Autowired
    private DataManager dm;
    
	@RequestMapping(value = "/{lang}/resource.del.and.detach", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody IdResponse serviceSpecificExec(HttpServletRequest request, @RequestParam(value=Params.URL) String url, 
    		@RequestParam(defaultValue="") String id, @RequestParam(defaultValue="") String uuid)
            throws Exception {
        if(id.trim().isEmpty()){
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
                filename = param.split("=")[1];
            } else if (param.startsWith("access")) {
                access = param.split("=")[1];
            }
        }

        if ("".equals(filename))
            throw new OperationAbortedEx("Empty filename. Unable to delete resource.");

        // Remove the file and update the file upload/downloads tables
        IResourceRemoveHandler removeHook = (IResourceRemoveHandler) context.getApplicationContext().getBean("resourceRemoveHandler");
        removeHook.onDelete(context, request, Integer.parseInt(id), filename, access);

        // Set parameter and process metadata to remove reference to the uploaded file
        request.getParameterMap().put("name", new String[]{filename});
        request.getParameterMap().put("protocol", new String[]{"WWW:DOWNLOAD-1.0-http--download"});

        String process = "onlinesrc-remove";
        XslProcessingReport report = new XslProcessingReport(process);

        Element processedMetadata;
        try {
            final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
            processedMetadata = XslProcessing.get().process(id, process,
                    true, context.getAppPath(), report, true, siteURL, request);
            if (processedMetadata == null) {
                throw new BadParameterEx("Processing failed", "Not found:"
                        + report.getNotFoundMetadataCount() + ", Not owner:" + report.getNotEditableMetadataCount()
                        + ", No process found:" + report.getNoProcessFoundCount() + ".");
            }
        } catch (Exception e) {
            throw e;
        }

        return new IdResponse(id);
    }
}