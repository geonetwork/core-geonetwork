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

        try {
            String getCapUrl = OwsUtils.getGetCapabilitiesUrl(wfsUrl);

            m.put(WFSDataStoreFactory.URL.key, getCapUrl);
            m.put(WFSDataStoreFactory.TIMEOUT.key, 60000);
            m.put(WFSDataStoreFactory.TRY_GZIP, true);
            m.put(WFSDataStoreFactory.MAXFEATURES.key, 2000);
            m.put(WFSDataStoreFactory.ENCODING, "UTF-8");

            wfsDatastore = factory.createDataStore(m);

            SimpleFeatureType sft = wfsDatastore.getSchema(featureType);
            List<AttributeDescriptor> attributesDesc = sft.getAttributeDescriptors();

            for(AttributeDescriptor desc : attributesDesc) {
                fields.put(desc.getName().getLocalPart(), OwsUtils.getTypeFromFeatureType(desc));
            }
            String toto = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
