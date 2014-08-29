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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.fao.geonet.domain.responses.StatusResponse;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.resources.Resources;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller("admin.logo.upload")
public class Add implements ApplicationContextAware {
	private volatile String logoDirectory;

	private ApplicationContext context;

	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	@RequestMapping(value = "/{lang}/admin.logo.upload@json", 
			consumes = { MediaType.ALL_VALUE }, 
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	StatusResponse execJSON(@RequestParam("fname") MultipartFile fname)
			throws Exception {
		return exec(fname);
	}

	@RequestMapping(value = "/{lang}/admin.logo.upload", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	StatusResponse exec(@RequestParam("fname") MultipartFile fname)
			throws Exception {
		BufferedOutputStream stream = null;

		try {
			if (logoDirectory == null) {
				synchronized (this) {
					if (logoDirectory == null) {
						logoDirectory = Resources
								.locateHarvesterLogosDirSMVC(context);
					}
				}
			}

			if (fname.getName().contains("..")) {
				throw new BadParameterEx(
						"Invalid character found in resource name.",
						fname.getName());
			}

			if ("".equals(fname.getName())) {
				throw new Exception("Logo name is not defined.");
			}

			File serverFile = new File(logoDirectory,
					fname.getOriginalFilename());
			if (serverFile.exists()) {
				serverFile.delete();
				serverFile = new File(logoDirectory,
						fname.getOriginalFilename());
			}

			serverFile.createNewFile();

			stream = new BufferedOutputStream(new FileOutputStream(serverFile));

			int read = 0;
			byte[] bytes = new byte[1024];

			InputStream is = fname.getInputStream();

			while ((read = is.read(bytes)) != -1) {
				stream.write(bytes, 0, read);
			}
		} catch (Exception e) {
			return new StatusResponse(e.getMessage());
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		return new StatusResponse("Logo added.");
	}
}