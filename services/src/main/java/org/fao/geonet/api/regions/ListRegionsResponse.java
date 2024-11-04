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

package org.fao.geonet.api.regions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.kernel.region.Region;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.operation.TransformException;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.Map;

/**
 * @author Jesse on 4/9/2015.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListRegionsResponse {
    @XmlAttribute
    @JsonProperty("count")
    private int count;

    @XmlElement
    @JsonProperty
    private final Collection<RegionResponse> region = Lists.newArrayList();

    @XmlElement
    @JsonProperty
    private final Map<String, CategoryResponse> categories = Maps.newHashMap();

    public ListRegionsResponse() {
    }

    public ListRegionsResponse(Collection<Region> regions) throws TransformException, FactoryException {
        this.count = regions.size();
        for (Region region : regions) {
            this.region.add(new RegionResponse(region));
            if (!categories.containsKey(region.getCategoryId())) {
                categories.put(region.getCategoryId(), new CategoryResponse(region));
            }
        }

    }

    public int size() {
        return region.size();
    }

    public Collection<RegionResponse> getRegions() {
        return region;
    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RegionResponse {
        @XmlAttribute
        @JsonProperty("hasGeom")
        private boolean hasGeom;
        @XmlAttribute
        @JsonProperty("categoryId")
        private String categoryId;
        @XmlAttribute
        @JsonProperty("id")
        private String id;
        @XmlElement
        @JsonProperty
        private double north, east, south, west;
        @XmlElement
        @JsonProperty
        private Map<String, String> label = Maps.newHashMap();

        public RegionResponse() {
        }

        public RegionResponse(Region input) throws TransformException, FactoryException {
            id = input.getId();
            north = input.getLatLongBBox().getMaxY();
            south = input.getLatLongBBox().getMinY();
            east = input.getLatLongBBox().getMaxX();
            west = input.getLatLongBBox().getMinX();
            label = input.getLabels();
            this.categoryId = input.getCategoryId();
            this.hasGeom = input.hasGeom();
        }
        public String getId() {
            return id;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CategoryResponse {
        @XmlElement
        @JsonProperty
        private Map<String, String> label = Maps.newHashMap();

        public CategoryResponse() {
        }

        public CategoryResponse(Region region) {
            this.label = region.getCategoryLabels();
        }
    }
}
