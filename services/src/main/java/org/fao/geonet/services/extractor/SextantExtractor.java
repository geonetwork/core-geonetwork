package org.fao.geonet.services.extractor;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

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

	// TODO: to be customized
	private String path = "/tmp";

	@RequestMapping(value = "/{lang}/extractor.doExtract", method = RequestMethod.POST)
	public HttpEntity<byte[]> exec(@RequestBody LayerSpec[] jsonLayers, HttpServletRequest request) throws Exception {
		JSONObject status = new JSONObject();
		XmlMapper xmlMapper = new XmlMapper();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		out.write("<extract xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"extracteur.xsd\">".getBytes());
		final ServiceContext serviceContext = ServiceContext.get();

		out.write(String.format("<user lastname=\"%s\" firstname=\"%s\" mail=\"%s\" is_ifremer=\"%s\""
				+ " uidNumber=\"%s\" login=\"%s\" />",
				serviceContext.getUserSession().getName(),
				serviceContext.getUserSession().getSurname(),
				serviceContext.getUserSession().getEmailAddr(),
				"true", // TODO ...
				serviceContext.getUserSession().getUserId(),
				serviceContext.getUserSession().getUsername()).getBytes());
		out.write("<layers>".getBytes());

		for (LayerSpec l : jsonLayers) {
			xmlMapper.writeValue(out, l);
		}
		out.write("</layers></extract>".getBytes());

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		StreamSource source = new StreamSource(new StringReader(out.toString()));
		transformer.transform(source, result);
		String xmlString = result.getWriter().toString();
		System.out.println(xmlString);

		status.put("success", true);
		return new HttpEntity<byte[]>(status.toString(4).getBytes());
	}

}

// =============================================================================
