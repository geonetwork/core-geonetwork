/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.region;

import com.google.common.base.Optional;

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
     *
     * @return this
     */
    public abstract Request label(String labelParam);

    /**
     * Add categoryId search predicate to the search request.  If this method is called multiple
     * times the predicates are ORed together.  IE it will find regions that are either in label1 OR
     * label2
     *
     * @return this
     */
    public abstract Request categoryId(String categoryIdParam);

    /**
     * Set the max number of results that will be loaded.  A value < 0 will load all results
     *
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
     * Add an region id search predicate to the search request.  If this method is called multiple
     * times the predicates are ORed together.  IE it will find regions that have EITHER  id1 OR
     * id2
     *
     * @return this
     */
    public abstract Request id(String regionId);

    /**
     * Executes query and returns the found region or null.  IllegalStateException is thrown if
     * there is > 1 results.
     *
     * @return the region or null
     * @throws IllegalStateException if there was more than one region found
     */
    public Region get() throws Exception {
        Collection<Region> regions = execute();
        if (regions.size() > 1) {
            throw new IllegalStateException("there is more than one region found");
        }
        if (regions.isEmpty()) {
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


    /**
     * Given the request information attempt to determine the last modified value.
     *
     * This should be an efficient operation if the operation takes too long to determine (for
     * example loading many regions and determining the last modified of all of them) then {@link
     * Long#MAX_VALUE} should be returned.  This will mean that the lastModified check will be
     * skipped and the response will be returned to the client each time.
     *
     * {@link Long#MAX_VALUE} can also be returned if it is too difficult (implementation wise or
     * otherwise)
     *
     * If the id or category id does not apply then Optional.absent() should be returned since this
     * should not affect the last modified score.  This can be used as a kind of "applicable"
     *
     * Examples: <ul> <li> If the request is a category or a specific region ID it can be easily
     * calculated what the last modified date was. </li> <li> The last modified date is known for
     * the entire data set then it can be easily calculated for any request </li> <li> If each
     * category could have a different last modified then perhaps only id and category requests can
     * have a last modified or the most recent last modified of all categories could be the taken
     * </li> </ul>
     *
     * @return Optional.absent() if the search parameters are not applicable to this Region
     * implementation, {@link Long#MAX_VALUE} if the value cannot be accurately calculated for the
     * parameters set on the search object otherwise the last modified value of the last modified
     * region that would be in the response if request is executed.
     */
    public abstract Optional<Long> getLastModified() throws Exception;
}
