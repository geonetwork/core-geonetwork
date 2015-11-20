package org.fao.geonet.harvester.wfsfeatures.services;

import com.google.common.collect.Lists;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.harvester.wfsfeatures.HarvesterRouteBuilder;
import org.fao.geonet.harvester.wfsfeatures.event.WfsIndexingEvent;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.geonetwork.messaging.JMSMessager;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.ArrayList;
import java.util.List;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;

/**
 * Created by fgravin on 10/29/15.
 */

@Controller
public class HarvestRunner {
    @Autowired
    private JMSMessager jmsMessager;

    private static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO);


    /**
     * Index a featureType from a wfs service URL and a typename
     *
     * @param wfsUrl wfs service url
     * @param featureType feature type name
     * @param uuid  The metadata uuid (optional)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{uiLang}/wfs.harvest")
    @ResponseBody
    public JSONObject indexWfs(
            @RequestParam("url") String wfsUrl,
            @RequestParam("typename") String featureType,
            @RequestParam(value = "uuid", required = false, defaultValue = "") String uuid) throws Exception {

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("indexedFeatures", sendMessage(uuid, wfsUrl, featureType));

        return result;
    }

    /**
     * Index all featureTypes that are contained in the metadata.
     * Feature types are extracted from the online resources of the metedata.
     * The protocol should be WFS, the typename is from gmd:name element, and
     * the url from the gmd:linkage.
     *
     * @param uiLang lang
     * @param uuid uuid of the metadata
     * @param webRequest
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{uiLang}/wfs.harvest/{uuid}")
    @ResponseBody
    public JSONObject indexWfs(
            @PathVariable String uiLang,
            @PathVariable String uuid,
            NativeWebRequest webRequest) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataManager = appContext.getBean(DataManager.class);

        final String id = dataManager.getMetadataId(uuid);
        Element xml = dataManager.getMetadata(id);

        // TODO: This is ISO19139 specific
        // Use Lucene field instead ?
        final List<Element> wfsLinkages =
                Lists.newArrayList((Iterable<? extends Element>) Xml.selectNodes(xml,
                        "*//gmd:CI_OnlineResource[contains(" +
                                "gmd:protocol/gco:CharacterString, 'WFS')]",
                        NAMESPACES));

        JSONArray a = new JSONArray();
        for (Element element : wfsLinkages) {
            Element linkageElt = (Element)Xml.selectSingle(element, "gmd:linkage/gmd:URL", NAMESPACES);
            Element ftElt = (Element)Xml.selectSingle(element, "gmd:name/gco:CharacterString", NAMESPACES);

            a.add(sendMessage(uuid, linkageElt.getText(), ftElt.getText()));
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("indexedFeatures", a);

        return result;
    }

    @RequestMapping(value = "/{uiLang}/wfs.harvest.config")
    @ResponseBody
    public String getConfig(
            @RequestParam("url") String wfsUrl,
            @RequestParam("typename") String featureType,
            @RequestParam(value = "uuid", required = false, defaultValue = "") String uuid) throws Exception {

        return getApplicationProfile(uuid,wfsUrl, featureType);
    }

    private JSONObject sendMessage(final String uuid, final String wfsUrl, final String featureType) {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        WfsIndexingEvent event = new WfsIndexingEvent(appContext, uuid, wfsUrl, featureType);
        // TODO: Messages should be node specific eg. srv channel ?
        jmsMessager.sendMessage(HarvesterRouteBuilder.MESSAGE_HARVEST_WFS_FEATURES, event);

        JSONObject j = new JSONObject();
        j.put("url", wfsUrl);
        j.put("featureType", featureType);

        return j;
    }

    /**
     * Get the application profile content from the online resource.
     * The application profile contains the solr faceting configuration.
     *
     * @param uuid of the metadata
     * @param wfsUrl of the feature
     * @param featureType of the feature
     * @return applicationProfile if exists
     * @throws Exception
     */
    private String getApplicationProfile(final String uuid, final String wfsUrl,
                                         final String featureType) throws Exception {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataManager = appContext.getBean(DataManager.class);

        if (dataManager != null) {
            final String id = dataManager.getMetadataId(uuid);
            Element xml = dataManager.getMetadata(id);

            final Element applicationProfile =
                    (Element) Xml.selectSingle(xml,
                            "*//gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString = 'WFS' " +
                                    "and gmd:name/gco:CharacterString = '" + featureType + "' " +
                                    "and gmd:linkage/gmd:URL = '" + wfsUrl + "']/gmd:applicationProfile/gco:CharacterString", NAMESPACES);

            if (applicationProfile != null) {
                return applicationProfile.getText();
            }
        }
        return null;
    }

}
