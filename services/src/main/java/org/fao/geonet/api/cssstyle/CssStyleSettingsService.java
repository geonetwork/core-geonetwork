package org.fao.geonet.api.cssstyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.persistence.criteria.Root;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.cssstyle.service.ICssStyleSettingService;
import org.fao.geonet.domain.CssStyleSettings;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.PathSpec;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.config.springutil.ServerBeanPropertyUpdater;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
		"/api/cssstyle",
		"/api/" + API.VERSION_0_1 +
		"/cssstyle"
})
@Api(value = "stylesheet",
tags = "stylesheet",
description = "Dynamic Stylesheet operations")
@Controller("stylesheet")
public class CssStyleSettingsService {


	@ApiOperation(
			value = "Get Stylesheet",
			notes = "This returns the generated custom CSS.",
			nickname = "getRecords")
	@RequestMapping(value = "/{fileName}",
	method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public void getStylesheet(
			@ApiIgnore
			HttpServletRequest request,
			@ApiIgnore
			HttpServletResponse response,
			@PathVariable
			String fileName
			) throws Exception {


		try {
			if(!fileName.startsWith("custom")) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return;
			} else {
				response.setStatus(HttpStatus.OK.value());
			}

			ICssStyleSettingService cssStyleSettingService = (ICssStyleSettingService)  ApplicationContextHolder.get().getBean("cssStyleSettingService");

			String content = cssStyleSettingService.getCustomCssSetting();

			// Setting up the headers:
			// The content type
			response.setContentType("text/css; charset=utf-8");
			// The file size
			response.addHeader("Content-Length", Long.toString(content.length()));
			// binary encoding
			response.setDateHeader("Last-Modified", System.currentTimeMillis());
			// Filename
			response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName);

			ServletOutputStream out = response.getOutputStream();
			out.println(content);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

	}


	@ApiOperation(value = "Saves a CssStyleSettings.", notes = "Saves a CssStyleSettings.", nickname = "saveCssStyle")
	@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public ResponseEntity saveCssStyle(
			@ApiIgnore
			HttpServletRequest request,
			@ApiIgnore
			HttpServletResponse response,
			@ApiParam(name = "gnCssStyle") @RequestBody
			CssStyleSettings cssStyleSetting
			) throws Exception {

		try {
			ICssStyleSettingService cssStyleSettingService = (ICssStyleSettingService)  ApplicationContextHolder.get().getBean("cssStyleSettingService");

			cssStyleSettingService.saveSettings(cssStyleSetting);
			
			final ServiceContext context = ApiUtils.createServiceContext(request);
			GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
//			Path lessPath = dataDirectory.getWebappDir().resolve("../../../target/geonetwork/catalog/lib/style/bootstrap/less/variables.less");
			Path lessPath = dataDirectory.getWebappDir().resolve("../../../../web-ui/src/main/resources/catalog/lib/style/bootstrap/less/variables.less");
			Charset charset = StandardCharsets.UTF_8;

			String content = new String(Files.readAllBytes(lessPath), charset);
			content = content.replaceAll("@body-bg: [\"']?(.*)[\"']?", "@body-bg: " + cssStyleSetting.getBackgroudColor() + ";");
			content = content.replaceAll("@text-color: [\"']?(.*)[\"']?", "@text-color: " + cssStyleSetting.getFontColor() + ";");
			Files.write(lessPath, content.getBytes(charset));
			
//			if(!less.exists()) {
//				throw new Exception("variables.less not found");
//			} 
//			
//			String fileContent = "";
//			
//			if(cssStyleSetting.getBackgroudColor()!=null) {
//				fileContent += "@gn-style-backgroundColor: " + cssStyleSetting.getBackgroudColor();
//			}
//			
//			try(  PrintWriter out = new PrintWriter(less)  ){
//			    out.println( fileContent );
//			}
			

			return new ResponseEntity(HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@ApiOperation(
			value = "Get CssStyleSettings",
			notes = "This returns the CssStyleSettings value.",
			nickname = "getCssStyle")
	@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE,
	method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CssStyleSettings getCssStyle(
			@ApiIgnore
			HttpServletRequest request,
			@ApiIgnore
			HttpServletResponse response) throws Exception {

		ICssStyleSettingService cssStyleSettingService = (ICssStyleSettingService)  ApplicationContextHolder.get().getBean("cssStyleSettingService");

		CssStyleSettings currentCssStyleSettings = cssStyleSettingService.getCssStyleSettings();

		if(currentCssStyleSettings==null) return new CssStyleSettings();
		else return currentCssStyleSettings;	
	}

}
