package org.fao.geonet.services.extractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.extractor.mapping.LayerSpec;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.xml.XmlMapper;

//=============================================================================

/**
 * This controller generates the extraction requests which will be parsed by the
 * extractor (python script launched by a CronJob), see
 * https://github.com/camptocamp/sextant
 * 
 * @author pmauduit
 * 
 */

@Controller
public class SextantExtractor {

	@Autowired
	private DataManager dataManager;
	@Autowired
	private ServiceManager serviceManager;
	@Autowired
	private String extractorPath;

	private File intranetPath, extranetPath, anonymousPath;

	@PostConstruct
	public void init() throws Exception {
		// ensures directories are created
		File exPathF  = new File(extractorPath + File.separator + "folders" + File.separator + "aTraiter");
		intranetPath  = new File(exPathF, "intranet");
		extranetPath  = new File(exPathF, "extranet");
		anonymousPath = new File(exPathF, "anonymous");
		FileUtils.forceMkdir(intranetPath);
		FileUtils.forceMkdir(extranetPath);
		FileUtils.forceMkdir(anonymousPath);
	}

	@RequestMapping(value = "/{lang}/extractor.doExtract", method = RequestMethod.POST)
	public HttpEntity<byte[]> exec(@RequestBody LayerSpec[] jsonLayers, HttpServletRequest request) throws Exception {
		JSONObject status = new JSONObject();
		try {
			XmlMapper xmlMapper = new XmlMapper();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			out.write("<extract xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"extracteur.xsd\">"
					.getBytes());
			final ServiceContext serviceContext = ServiceContext.get();

			UserSession us = serviceContext.getUserSession();
			boolean isAuthenticated = us.isAuthenticated();

			if (isAuthenticated) {
				out.write(String.format(
						"<user lastname=\"%s\" firstname=\"%s\" mail=\"%s\" is_ifremer=\"%s\""
								+ " uidNumber=\"%s\" login=\"%s\" />", us.getName(), us.getSurname(),
						us.getEmailAddr(), us.getPrincipal().getOrganisation().equals("ifremer"), us.getUserId(),
						us.getUsername()).getBytes());
			}
			out.write("<layers>".getBytes());
			for (LayerSpec l : jsonLayers) {
				xmlMapper.writeValue(out, l);
			}
			out.write("</layers>".getBytes());
			out.write("</extract>".getBytes());
			// Indent using the available XML lib
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			StreamResult result = new StreamResult(new StringWriter());
			StreamSource source = new StreamSource(new StringReader(out.toString()));
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();

			if (isAuthenticated) {
				// TODO: intranet vs extranet ?
				FileUtils.writeStringToFile(new File(anonymousPath, us.getEmailAddr() + "_" + UUID.randomUUID()
						+ ".xml"), xmlString);
			} else {
				FileUtils.writeStringToFile(new File(anonymousPath, "anonymous_" + UUID.randomUUID() + ".xml"),
						xmlString);
			}
			status.put("success", true);
		} catch (Exception e) {
			status.put("success", false);
			status.put("reason", e.getMessage());
		}
		return new HttpEntity<byte[]>(status.toString(4).getBytes());
	}

}

// =============================================================================
