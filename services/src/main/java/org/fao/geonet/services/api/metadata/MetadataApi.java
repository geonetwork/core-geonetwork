package org.fao.geonet.services.api.metadata;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.api.API;
import org.fao.geonet.services.api.exception.ResourceNotFoundException;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;

/**
 * Created by francois on 29/01/16.
 */
@Service
@RequestMapping(value = {
        "/api/metadata/{metadataUuid}",
        "/api/" + API.VERSION_0_1 +
                "/metadata/{metadataUuid}"
})
@Api(value = "metadata",
        tags= "metadata",
        description = "Metadata operations")
public class MetadataApi {
    private static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO);

    private static final String LOG_MODULE = "MetadataApi";

    // TODO: Api is query xpath
    @RequestMapping(value = "/query/{savedQuery}",
                    method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public String getConfig(
            @PathVariable String metadataUuid,
            @PathVariable String savedQuery,
            @RequestParam Map<String,String> allRequestParams,
            HttpServletResponse response) throws Exception {

        // TODO: Move that to schema
        Map<String, SavedQuery> savedQueryRepository = new HashMap<>();
        savedQueryRepository.put("onlinesrc-appprofile",
                new SavedQuery("onlinesrc-appprofile",
                        "*//gmd:CI_OnlineResource[" +
                                "contains(gmd:protocol/gco:CharacterString, {{protocol}}) and " +
                                "gmd:name/gco:CharacterString = '{{name}}' and " +
                                "gmd:linkage/gmd:URL = '{{url}}'" +
                                "]/gmd:applicationProfile/gco:CharacterString",
                        Lists.newArrayList(GMD, GCO)));

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataManager = appContext.getBean(DataManager.class);

        final String id = dataManager.getMetadataId(metadataUuid);
        if (id == null) {
            throw new ResourceNotFoundException(String.format(
                    "Metadata '%s' not found.",
                    metadataUuid));
        }

        SavedQuery query = savedQueryRepository.get(savedQuery);
        if (query == null) {
            throw new ResourceNotFoundException(String.format(
                    "Saved query '%s' not found for metadata '%s' in schema.",
                    savedQuery, metadataUuid));
        }

        // TODO: Check savedquery parameters
        String xpath = query.getXpath();
        if (Log.isDebugEnabled(LOG_MODULE)) {
            Log.debug(LOG_MODULE, String.format(
                    "Saved query XPath: %s", xpath));
        }
        Iterator<String> parameters = allRequestParams.keySet().iterator();
        while(parameters.hasNext()) {
            String parameter = parameters.next();
            xpath = xpath.replaceAll("\\{\\{" + parameter + "\\}\\}", allRequestParams.get(parameter));
        }
        if (Log.isDebugEnabled(LOG_MODULE)) {
            Log.debug(LOG_MODULE, String.format(
                    "Saved query XPath after URL parameter substitution %s", xpath));
        }


        Element xml = dataManager.getMetadata(id);
        // TODO: Could return any kind of object
        // TODO: Could select multiple nodes
        final Element applicationProfile =
                (Element) Xml.selectSingle(xml,
                        xpath, query.getNamespaces());

        if (applicationProfile != null) {
            return applicationProfile.getText();
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND, "No results found for query.");
        return null;
    }
}
