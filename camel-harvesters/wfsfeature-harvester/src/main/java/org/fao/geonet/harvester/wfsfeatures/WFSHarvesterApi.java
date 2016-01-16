/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.harvester.wfsfeatures;

import com.google.common.collect.Lists;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder;
import org.fao.geonet.harvester.wfsfeatures.event.WFSHarvesterEvent;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.geonetwork.messaging.JMSMessager;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;

/**
 * Created by fgravin on 10/29/15.
 */

@Controller
public class WFSHarvesterApi {
    @Autowired
    private JMSMessager jmsMessager;

    private static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO);


    /**
     * Index a featureType from a wfs service URL and a typename
     *
     */
    @RequestMapping(value = "/{uiLang}/wfs.harvest",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.PUT)
    @ResponseBody
    public JSONObject indexWfs(
            @RequestBody WFSHarvesterParameter config) throws Exception {

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("indexedFeatures",
                sendMessage(config));

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
    @Deprecated
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

//            a.add(sendMessage(uuid, linkageElt.getText(), ftElt.getText()));
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

        return getApplicationProfile(uuid, wfsUrl, featureType);
    }

    private JSONObject sendMessage(WFSHarvesterParameter parameters) {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        WFSHarvesterEvent event = new WFSHarvesterEvent(appContext, parameters);
        // TODO: Messages should be node specific eg. srv channel ?
        jmsMessager.sendMessage(WFSHarvesterRouteBuilder.MESSAGE_HARVEST_WFS_FEATURES, event);

        JSONObject j = new JSONObject();
        j.put("url", parameters.getUrl());
        j.put("featureType", parameters.getTypeName());

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

        if (!StringUtils.isEmpty(uuid) && dataManager != null) {
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

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            Exception.class})
    public Object exceptionHandler(final Exception exception) {
            exception.printStackTrace();
            return  new HashMap() {{
                    put("result", "failed");
                    put("type", "file_not_found");
                    put("message", exception.getClass() + " " + exception.getMessage());
                }};
        }
}
