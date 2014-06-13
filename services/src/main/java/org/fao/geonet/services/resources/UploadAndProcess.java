//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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

import javax.servlet.http.HttpServletRequest;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.responses.IdResponse;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.metadata.XslProcessing;
import org.fao.geonet.services.metadata.XslProcessingReport;
import org.fao.geonet.services.resources.handlers.IResourceUploadHandler;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles the file upload and attach the uploaded service to the metadata record
 * using the onlinesrc-add XSL process.
 * 
 * Return a simple JSON response in case of success.
 */
@Controller("resource.upload.and.link")
public class UploadAndProcess {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }
    
    @Autowired
    private ServiceContext context;
    @Autowired
    private DataManager dm;

	@RequestMapping(value = {"/{lang}/resource.upload.and.link", "/{lang}/resource-onlinesrc-upload"}, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody IdResponse exec(HttpServletRequest request, 
    		@RequestParam("file") MultipartFile file,
    		@RequestParam(value=Params.TITLE,defaultValue="") String description,
    		@RequestParam(defaultValue="") String id, @RequestParam(defaultValue="") String uuid,
    		@RequestParam(defaultValue="private", value=Params.ACCESS) String access, 
    		@RequestParam(value=Params.OVERWRITE, defaultValue="no") String overwrite)
            throws Exception {
		
		  if(id.trim().isEmpty()){
				id = dm.getMetadataId(uuid);
	        }

        Lib.resource.checkEditPrivilege(context, id);

        // get info to log the upload

        UserSession session = context.getUserSession();
        String username = session.getUsername();
        if (username == null)
            username = "unknown (this shouldn't happen?)";

        String fname = file.getName();
        String fsize = Long.toString(file.getSize());
        IResourceUploadHandler uploadHook = (IResourceUploadHandler) context.getApplicationContext().getBean("resourceUploadHandler");
        uploadHook.onUpload(context, access, overwrite, Integer.parseInt(id), fname, new Double(fsize).doubleValue());


        context.info("UPLOADED:" + fname + "," + id + ","
                + context.getIpAddress() + "," + username);

        // Set parameter and process metadata to reference the uploaded file
        request.getParameterMap().put("url", new String[]{file.getName()});
        request.getParameterMap().put("name", new String[]{file.getName()});
        request.getParameterMap().put("desc", new String[]{description});
        request.getParameterMap().put("protocol", new String[]{"WWW:DOWNLOAD-1.0-http--download"});

        String process = "onlinesrc-add";
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
        // -- return the processed metadata id

        return new IdResponse(id);
    }
}