package org.fao.geonet.services.region.metadata;

import com.google.common.collect.Lists;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.List;

/**
 * Find a geometry by the gml id attribute on the element.
 *
 * @author Jesse on 1/21/2015.
 */
public class FindRegionByGmlId extends MetadataRegionFinder {
    public static final String GML_ID_PREFIX = "@gml";

    @Override
    public boolean accepts(String id) {
        return id.startsWith(GML_ID_PREFIX);
    }

    @Override
    public void findRegion(MetadataRegionSearchRequest searchRequest, List<Region> regions, MetadataRegionSearchRequest.Id mdId, String id, Element metadata) throws Exception {
        String gmlId = id.substring(GML_ID_PREFIX.length());
        final Element geomEl = Xml.selectElement(metadata, "*//*[@gml:id = '" + gmlId + "']", Lists.newArrayList(Geonet.Namespaces.GML));
        if (geomEl != null) {
            searchRequest.findContainingGmdEl(regions, mdId, geomEl);
        }

    }

    @Override
    public boolean needsEditData() {
        return false;
    }
}
