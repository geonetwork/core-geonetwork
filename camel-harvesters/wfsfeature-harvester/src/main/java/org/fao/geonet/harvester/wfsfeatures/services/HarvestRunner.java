package org.fao.geonet.harvester.wfsfeatures.services;

import com.google.common.collect.Lists;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.utils.URIBuilder;
import org.fao.geonet.harvester.wfsfeatures.event.WfsIndexingEvent;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.geotools.gml3.v3_2.gco.GCO;
import org.geotools.gml3.v3_2.gmd.GMD;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import java.net.URI;
import java.net.URL;
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
    private ApplicationContext appContext;

    @Autowired
    private DataManager dataManager;

    private static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO);

    @RequestMapping(value = "/{uiLang}/wfs.harvest/{uuid}")
    @ResponseBody
    public JSONObject localServiceDescribe(
            @PathVariable String uiLang,
            @PathVariable String uuid,
            NativeWebRequest webRequest) throws Exception {

        final String id = dataManager.getMetadataId(uuid);
        Element xml = dataManager.getMetadata(id);

        final List<Element> wfsLinkages =
                Lists.newArrayList((Iterable<? extends Element>) Xml.selectNodes(xml,
                        "*//gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString = 'WFS']", NAMESPACES));

        JSONArray a = new JSONArray();
        for (Element element : wfsLinkages) {

            Element linkageElt = (Element)Xml.selectSingle(element, "gmd:linkage/gmd:URL", NAMESPACES);
            Element ftElt = (Element)Xml.selectSingle(element, "gmd:name/gco:CharacterString", NAMESPACES);
            Element configElt = (Element)Xml.selectSingle(element, "gmd:applicationProfile/gco:CharacterString", NAMESPACES);

            String linkage = linkageElt.getText();
            String featureType = ftElt.getText();

            URIBuilder builder = new URIBuilder(linkage);
            builder.addParameter("request", "GetFeature");
            builder.addParameter("service", "WFS");
            builder.addParameter("maxFeatures", "100");
            builder.addParameter("version", "1.0.0");
            builder.addParameter("TYPENAME", featureType);

            String url = builder.build().toURL().toString();
            WfsIndexingEvent event = new WfsIndexingEvent(appContext, uuid, linkage, url);
            appContext.publishEvent(event);

            JSONObject j = new JSONObject();
            j.put("url", url);
            j.put("featureType", featureType);
            a.add(j);
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("indexedFeatures", a);

        return result;
    }
}
