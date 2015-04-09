package org.fao.geonet.services.region;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.fao.geonet.kernel.region.Region;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Jesse on 4/9/2015.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListRegionsResponse {
    @XmlAttribute
    private int count;

    @XmlElement(name = "region")
    private Collection<RegionResponse> region;

    public ListRegionsResponse() {

    }

    public ListRegionsResponse(Collection<Region> region) {
        this.count = region.size();
        this.region = Collections2.transform(region, new Function<Region, RegionResponse>() {
            @Nullable
            @Override
            public RegionResponse apply(Region input) {
                try {
                    return new RegionResponse(input);
                } catch (TransformException | FactoryException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RegionResponse {
        @XmlAttribute
        private boolean hasGeom;
        @XmlAttribute
        private String categoryId;
        @XmlAttribute
        private String id;
        @XmlElement
        private double north, east, south, west;
        @XmlJavaTypeAdapter(MapAdapter.class)
        @XmlAnyElement
        private Map<String, String> label = Maps.newHashMap();
        @XmlElement
        @XmlJavaTypeAdapter(MapAdapter.class)
        private Map<String, String> categoryLabel = Maps.newHashMap();

        public RegionResponse() {
        }

        public RegionResponse(Region input) throws TransformException, FactoryException {
            id = input.getId();
            north = input.getLatLongBBox().getMaxY();
            south = input.getLatLongBBox().getMinY();
            east = input.getLatLongBBox().getMaxX();
            west = input.getLatLongBBox().getMinX();
            label = input.getLabels();
            categoryLabel = input.getCategoryLabels();
            this.categoryId = input.getCategoryId();
            this.hasGeom = input.hasGeom();
        }

        @XmlElement(name = "id")
        public String getIdElement() {
            return id;
        }
    }

    private static class MapElements {
        @XmlElement
        public String key;
        @XmlElement
        public String value;

        private MapElements() {
        } //Required by JAXB

        public MapElements(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class MapAdapter extends XmlAdapter<MapElements[], Map<String, String>> {
        public MapElements[] marshal(Map<String, String> arg0) throws Exception {
            MapElements[] mapElements = new MapElements[arg0.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : arg0.entrySet())
                mapElements[i++] = new MapElements(entry.getKey(), entry.getValue());

            return mapElements;
        }

        public Map<String, String> unmarshal(MapElements[] arg0) throws Exception {
            Map<String, String> r = new HashMap<String, String>();
            for (MapElements mapelement : arg0)
                r.put(mapelement.key, mapelement.value);
            return r;
        }
    }

}
