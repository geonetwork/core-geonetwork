package org.fao.geonet.services.region;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.kernel.region.Region;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.util.Collection;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jesse on 4/9/2015.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListRegionsResponse {
    @XmlAttribute
    @JsonProperty("@count")
    private int count;

    @XmlElement
    @JsonProperty
    private Collection<RegionResponse> region = Lists.newArrayList();

    @XmlElement
    @JsonProperty
    private Map<String, CategoryResponse> categories = Maps.newHashMap();

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

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RegionResponse {
        @XmlAttribute
        @JsonProperty("@hasGeom")
        private boolean hasGeom;
        @XmlAttribute
        @JsonProperty("@categoryId")
        private String categoryId;
        @XmlAttribute
        @JsonProperty("@id")
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

        @XmlElement(name = "id")
        @JsonProperty("id")
        public String getIdElement() {
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
