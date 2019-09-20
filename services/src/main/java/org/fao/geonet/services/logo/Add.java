//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.services.logo;

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.responses.StatusResponse;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.servlet.http.HttpServletRequest;

@Controller("admin.logo.upload")
@Deprecated
public class Add implements ApplicationContextAware {
    private volatile Path logoDirectory;

    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @RequestMapping(value = "/{portal}/{lang}/admin.logo.upload",
        consumes = {MediaType.ALL_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    StatusResponse execJSON(@RequestParam("fname") MultipartFile fname, HttpServletRequest request)
        throws Exception {
        return exec(fname, request);
    }

    @RequestMapping(value = "/{portal}/{lang}/admin.logo.upload", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    StatusResponse exec(@RequestParam("fname") MultipartFile fname, HttpServletRequest request)
        throws Exception {

        try {
            Path logoDir;
            final ServiceContext serviceContext = ApiUtils.createServiceContext(request);
            final Resources resources = context.getBean(Resources.class);
            synchronized (this) {
                if (this.logoDirectory == null) {
                    this.logoDirectory = resources.locateHarvesterLogosDirSMVC(context);
                }
                logoDir = this.logoDirectory;
            }

            FilePathChecker.verify(fname.getName());

			if (StringUtils.isEmpty(fname.getName())) {
				throw new Exception("Logo name is not defined.");
			}

			try (Resources.ResourceHolder serverFile = resources.getWritableImage(serviceContext, fname.getOriginalFilename(), logoDir);
                 InputStream in = fname.getInputStream()) {
                Files.copy(in, serverFile.getPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            return new StatusResponse(e.getMessage());
        }

        return new StatusResponse("Logo added.");
    }
}
