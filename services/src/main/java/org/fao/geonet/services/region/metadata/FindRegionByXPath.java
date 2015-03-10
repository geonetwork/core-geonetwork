package org.fao.geonet.services.region.metadata;

import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.List;

/**
 * Find a geometry by the xpath to the geometry element.
 *
 * @author Jesse on 1/21/2015.
 */
public class FindRegionByXPath extends MetadataRegionFinder {
    public static final String XPATH_ID_PREFIX = "@xpath";

    @Override
    public boolean accepts(String id) {
        return id.startsWith(XPATH_ID_PREFIX);
    }

    @Override
    public void findRegion(MetadataRegionSearchRequest searchRequest, List<Region> regions, MetadataRegionSearchRequest.Id mdId,
                           String id, Element metadata) throws Exception {
        String xpath = id.substring(XPATH_ID_PREFIX.length());
        final DataManager dataManager = searchRequest.context.getBean(DataManager.class);
        final String schemaId = dataManager.autodetectSchema(metadata);
        MetadataSchema schema = dataManager.getSchema(schemaId);
        final Element geomEl = Xml.selectElement(metadata, xpath, schema.getNamespaces());
        if (geomEl != null) {
            searchRequest.findContainingGmdEl(regions, mdId, geomEl);
        }

    }

    @Override
    public boolean needsEditData() {
        return false;
    }
}
