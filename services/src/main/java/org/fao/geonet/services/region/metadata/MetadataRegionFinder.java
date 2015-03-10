package org.fao.geonet.services.region.metadata;

import org.fao.geonet.kernel.region.Region;
import org.jdom.Element;

/**
 * The strategy for locating the geometry in the metadata.
 *
 * @author Jesse on 1/21/2015.
 */
public abstract class MetadataRegionFinder {
    /**
     * Return true if this can handle the provided id
     */
    public abstract boolean accepts(String id);

    /**
     * Find the region and add it/them to the list of regions
     * @param searchRequest
     * @param regions the list to add found regions to.
     * @param mdId the id of the metadata
     * @param id the id identifying the XML element of the geometry
     * @param metadata the metadata
     */
    public abstract void findRegion(MetadataRegionSearchRequest searchRequest, java.util.List<Region> regions, MetadataRegionSearchRequest.Id mdId, String id, Element metadata) throws Exception;

    /**
     * Return true if the metadata should have the edit elements added to it in order to find the region.
     */
    public abstract boolean needsEditData();
}
