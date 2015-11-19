package org.fao.geonet.harvester.wfsfeatures;

import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fgravin on 11/5/15.
 */
public class FeatureTypeConfig {

    // TODO: move to config
    public static final int wfsTimeOut = 60000;
    public static final int wfsMaxFeatures = 2000;
    public static final String wfsEncoding = "UTF-8";
    private String uuid;
    private String wfsUrl;
    private String featureType;

    private WFSDataStore wfsDatastore = null;
    private Map<String, String> fields = new HashMap<String, String>();

    public String getFeatureType() {
        return featureType;
    }
    public String getWfsUrl() {
        return wfsUrl;
    }
    public String getUuid() {
        return uuid;
    }
    public Map<String, String> getFields() {
        return fields;
    }
    public WFSDataStore getWfsDatastore() {
        return wfsDatastore;
    }

    FeatureTypeConfig(String uuid, String wfsUrl, String featureType) {
        this.uuid = uuid;
        this.wfsUrl = wfsUrl;
        this.featureType = featureType;
    }

    /**
     * Create a WFSDatastore for this featuretype and retrieve
     * all schema infos (attributes names and types).
     */
    public void connectToWfsService() {
        WFSDataStoreFactory factory = new WFSDataStoreFactory();
        Map m = new HashMap();

        // See http://docs.geotools.org/latest/userguide/library/referencing/order.html
        // TODO: Discuss
        System.setProperty("org.geotools.referencing.forceXY", "true");

        try {
            String getCapUrl = OwsUtils.getGetCapabilitiesUrl(wfsUrl);

            m.put(WFSDataStoreFactory.URL.key, getCapUrl);
            m.put(WFSDataStoreFactory.TIMEOUT.key, wfsTimeOut);
            m.put(WFSDataStoreFactory.TRY_GZIP, true);
            m.put(WFSDataStoreFactory.MAXFEATURES.key, wfsMaxFeatures);
            m.put(WFSDataStoreFactory.ENCODING, wfsEncoding);

            wfsDatastore = factory.createDataStore(m);

            SimpleFeatureType sft = wfsDatastore.getSchema(featureType);
            List<AttributeDescriptor> attributesDesc = sft.getAttributeDescriptors();

            for(AttributeDescriptor desc : attributesDesc) {
                fields.put(desc.getName().getLocalPart(), OwsUtils.getTypeFromFeatureType(desc));
            }
            String toto = null;
        } catch (IOException e) {
            // TODO: log errors and probably stop the process if we can't connect to the service
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
