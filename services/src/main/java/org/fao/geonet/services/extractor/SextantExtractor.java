package org.fao.geonet.services.extractor;

import javax.servlet.http.HttpServletRequest;

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

	@RequestMapping(value = "/{lang}/extractor.doExtract", method = RequestMethod.POST)
	public HttpEntity<byte[]> exec(@RequestBody LayerSpec[] jsonLayers, HttpServletRequest request) throws Exception {
		JSONObject status = new JSONObject();
		for (LayerSpec l : jsonLayers) {
			System.out.println(l);
		}
		jsonLayers[0].getInput();
		status.put("success", true);
		return new HttpEntity<byte[]>(status.toString(4).getBytes());
	}

}

// =============================================================================
