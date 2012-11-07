package org.fao.geonet.services.region;

import java.util.Collections;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class Region {
    private final String id;
    private final Map<String, String> labels;
    private final String categoryId;
    private final Map<String, String> categoryLabels;
    private final boolean hasGeom;
    private final ReferencedEnvelope bbox;
    private volatile ReferencedEnvelope latlongBbox;
    
    public Region(String id, Map<String, String> labels, String categoryId, Map<String, String> categoryLabels, boolean hasGeom, ReferencedEnvelope bbox) {
        super();
        this.id = id;
        this.labels = Collections.unmodifiableMap(labels);
        this.categoryId = categoryId;
        this.categoryLabels = Collections.unmodifiableMap(categoryLabels);
        this.hasGeom = hasGeom;
        this.bbox = bbox;
    }
    
    public String getId() {
        return id;
    }
    public Map<String, String> getLabels() {
        return labels;
    }
    public String getCategoryId() {
        return categoryId;
    }
    public Map<String, String> getCategoryLabels() {
        return categoryLabels;
    }
    public boolean hasGeom() {
        return hasGeom;
    }

    public ReferencedEnvelope getBBox() {
        return bbox;
    }

    public ReferencedEnvelope getLatLongBBox() throws TransformException, FactoryException {
        if(latlongBbox == null) {
            synchronized (bbox) {
                if(latlongBbox == null) {
                    latlongBbox = bbox.transform(DefaultGeographicCRS.WGS84, true);
                }
            }
        }
        return latlongBbox;
    }
    
    @Override
    public String toString() {
        String label = labels.get("eng");
        if(label == null && !labels.isEmpty()){
            label = labels.values().iterator().next();
        }
        return categoryId+":"+label;
    }
}
