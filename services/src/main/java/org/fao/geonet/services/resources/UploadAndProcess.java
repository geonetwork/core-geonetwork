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
import org.fao.geonet.services.resources.handlers.IResourceUploadHandler;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

/**
 * Handles the file upload and attach the uploaded service to the metadata record using the
 * onlinesrc-add XSL process.
 *
 * Return a simple JSON response in case of success.
 */
@Controller("resource.upload.and.link")
@Deprecated
public class UploadAndProcess {
    @Autowired
    private DataManager dm;
    @Autowired
    private XslProcessing xslProcessing;
    @Autowired
    private ServiceManager serviceManager;

    @RequestMapping(value = {"/{portal}/{lang}/resource.upload.and.link", "/{portal}/{lang}/resource-onlinesrc-upload"}, produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public IdResponse exec(HttpServletRequest request,
                           @PathVariable String lang,
                           @RequestParam(value = "filename") MultipartFile file,
                           @RequestParam(defaultValue = "") String description,
                           @RequestParam(defaultValue = "") String id,
                           @RequestParam(defaultValue = "") String uuid,
                           @RequestParam(defaultValue = "private", value = Params.ACCESS) String access,
                           @RequestParam(value = Params.OVERWRITE, defaultValue = "no") String overwrite)
        throws Exception {
        ServiceContext context = serviceManager.createServiceContext("resource.upload.and.link", lang, request);

        if (id.trim().isEmpty()) {
            id = dm.getMetadataId(uuid);
        }

        Lib.resource.checkEditPrivilege(context, id);

        // get info to log the upload

        UserSession session = context.getUserSession();
        String username = session.getUsername();
        if (username == null)
            username = "unknown (this shouldn't happen?)";

        String fname = file.getOriginalFilename();
        String fsize = Long.toString(file.getSize());
        IResourceUploadHandler uploadHook = context.getBean("resourceUploadHandler", IResourceUploadHandler.class);
        uploadHook.onUpload(file.getInputStream(), context, access, overwrite, Integer.parseInt(id), fname, Double.parseDouble(fsize));


        context.info("UPLOADED:" + fname + "," + id + "," + context.getIpAddress() + "," + username);

        Map<String, String[]> allParams = Maps.newHashMap(request.getParameterMap());
        // Set parameter and process metadata to reference the uploaded file
        allParams.put("url", new String[]{file.getName()});
        allParams.put("name", new String[]{file.getOriginalFilename()});
        allParams.put("desc", new String[]{description});
        allParams.put("protocol", new String[]{"WWW:DOWNLOAD-1.0-http--download"});

        String process = "onlinesrc-add";
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
        // -- return the processed metadata id

        return new IdResponse(id);
    }
}
