package org.fao.geonet.harvester.wfsfeatures;

import com.google.common.collect.Lists;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;

/**
 * Created by fgravin on 11/4/15.
 */

public class FeatureTypeConfig {

    // TODO : would not depend too much on GN
    private DataManager dataManager;

    private static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO);

    private JSONObject config;

    public JSONObject getConfig() {
        return config;
    }

    public FeatureTypeConfig(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Map<String, String> getFields() throws JSONException {

        Map<String, String> fields = new HashMap<String, String>();
        if(this.config != null) {
            JSONArray fieldsConfig = (JSONArray)this.config.get("fields");
            for (int i = 0; i < fieldsConfig.length(); i++) {
                JSONObject o = fieldsConfig.getJSONObject(i);
                String type;
                try {
                    type = o.getString("type");
                } catch (JSONException e) {
                    type = "string";
                }
                fields.put(o.getString("name"), type);
            }
        }
        return fields;
    }

    public void load(final String applicationProfile) throws JSONException {
        if(applicationProfile != null) {
            JSONObject config = new JSONObject(applicationProfile);
            this.config = config;
        }
    }

    public void load(final String uuid, final String linkage,
                     final String featureType) throws Exception {

        String applicationProfile = getApplicationProfile(uuid, linkage, featureType);
        load(applicationProfile);
    }

    private String getApplicationProfile(final String uuid, final String linkage,
                                         final String featureType) throws Exception {
        if (dataManager != null) {
            final String id = dataManager.getMetadataId(uuid);
            Element xml = dataManager.getMetadata(id);

            final Element applicationProfile =
                    (Element) Xml.selectSingle(xml,
                            "*//gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString = 'WFS' " +
                                    "and gmd:name/gco:CharacterString = '" + featureType + "' " +
                                    "and gmd:linkage/gmd:URL = '" + linkage + "']/gmd:applicationProfile/gco:CharacterString", NAMESPACES);

            if (applicationProfile != null) {
                return applicationProfile.getText();
            }
        }
        return null;
    }
}
