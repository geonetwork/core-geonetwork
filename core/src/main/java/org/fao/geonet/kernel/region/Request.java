package org.fao.geonet.kernel.region;

import org.jdom.Element;

import java.util.Collection;

/**
 * Represents a search request for regions.  All predicates will be ORed together.
 * 
 * @author jeichar
 */
public abstract class Request {
    public static final String REGIONS_EL = "regions";
    private static final String COUNT_ATT = "count";

    /**
     * Add label search predicate to the search request.  If this method is called multiple times
     * the predicates are ORed together.  IE it will find regions that contains label1 OR label2
     * @return this
     */
    public abstract Request label(String labelParam);

    /**
     * Add categoryId search predicate to the search request.  If this method is called multiple times
     * the predicates are ORed together.  IE it will find regions that are either in label1 OR label2
     * @return this
     */
    public abstract Request categoryId(String categoryIdParam);
    /**
     * Set the max number of results that will be loaded.  A value < 0 will load all results
     * @return this
     */
    public abstract Request maxRecords(int maxRecordsParam);

    /**
     * Execute the request and get the matching regions
     * 
     * @return the collection of Regions found that match the predicates
     */
    public abstract Collection<Region> execute() throws Exception;

    /**
     * Add an region id search predicate to the search request.  If this method is called multiple times
     * the predicates are ORed together.  IE it will find regions that have EITHER  id1 OR id2
     * @return this
     */
    public abstract Request id(String regionId);

    /**
     * Executes query and returns the found region or null.  IllegalStateException is thrown if there is > 1 results.
     * 
     * @return the region or null
     * @throws IllegalStateException if there was more than one region found
     */
    public Region get() throws Exception {
        Collection<Region> regions = execute();
        if(regions.size() > 1) {
            throw new IllegalStateException("there is more than one region found");
        }
        if(regions.isEmpty()) {
            return null;
        } else {
            return regions.iterator().next();
        }
    }

    /**
     * Formats all the regions found as xml
     */
    public Element xmlResult() throws Exception {
        Collection<Region> regions = execute();
        Element result = new Element(REGIONS_EL).setAttribute("class", "array");
        result.setAttribute(COUNT_ATT, Integer.toString(regions.size()));
        for (Region region : regions) {
            result.addContent(region.toElement());
        }
        return result;
    }


}
