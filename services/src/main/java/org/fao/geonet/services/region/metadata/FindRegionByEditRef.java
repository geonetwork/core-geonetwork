package org.fao.geonet.services.region.metadata;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.region.Region;
import org.jdom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Find the element using the edit information on the elements.
 *
 * @author Jesse on 1/21/2015.
 */
public class FindRegionByEditRef extends MetadataRegionFinder {
    @Override
    public boolean accepts(String id) {
        return true;
    }

    @Override
    public void findRegion(MetadataRegionSearchRequest searchRequest, List<Region> regions, MetadataRegionSearchRequest.Id mdId, String id, Element metadata) throws Exception {
        Iterator<?> iter = metadata.getDescendants();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof Element) {
                Element el = (Element) obj;
                Element geonet = el.getChild("element", Geonet.Namespaces.GEONET);
                if (geonet != null && id.equals(geonet.getAttributeValue("ref"))) {
                    Iterator<?> extent = searchRequest.descentOrSelf(el);
                    if (extent.hasNext()) {
                        regions.add(searchRequest.parseRegion(mdId, (Element) extent.next()));
                        return;
                    } else {
                        if (searchRequest.findContainingGmdEl(regions, mdId, el)) return;
                    }
                }
            }
        }
    }

    @Override
    public boolean needsEditData() {
        return true;
    }
}
