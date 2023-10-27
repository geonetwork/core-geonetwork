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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.TransformException;

import java.util.Collections;
import java.util.Map;


public class Region {
    public static final String REGION_EL = "region";
    public static final CoordinateReferenceSystem WGS84;
    private static final String ID_ATT = "id";
    private static final String HAS_GEOM_ATT = "hasGeom";
    private static final String CATEGORY_ID_ATT = "categoryId";
    private static final String NORTH_EL = "north";
    private static final String SOUTH_EL = "south";
    private static final String EAST_EL = "east";
    private static final String WEST_EL = "west";
    private static final String LABEL_EL = "label";
    private static final String CATEGORY_EL = "categoryLabel";

    static {
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        try {
            crs = CRS.decode("EPSG:4326", true);
        } catch (Exception e) {
            Log.error(Geonet.REGION, "Unable to create latlong crs, something is wrong with Geotools dependencies", e);
        }
        WGS84 = crs;
    }

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

    public static CoordinateReferenceSystem decodeCRS(String srs) throws NoSuchAuthorityCodeException, FactoryException {
        CoordinateReferenceSystem mapCRS;
        if (srs.equals("EPSG:4326")) {
            mapCRS = WGS84;
        } else {
            mapCRS = CRS.decode(srs, false);
        }
        return mapCRS;
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

    public ReferencedEnvelope getBBox(CoordinateReferenceSystem projection) throws TransformException, FactoryException {
        return bbox.transform(projection, true);
    }

    public ReferencedEnvelope getLatLongBBox() throws TransformException, FactoryException {
        if (latlongBbox == null) {
            synchronized (bbox) {
                if (latlongBbox == null) {
                    latlongBbox = bbox.transform(WGS84, true);
                }
            }
        }
        return latlongBbox;
    }

    @Override
    public String toString() {
        String label = labels.get("eng");
        if (label == null && !labels.isEmpty()) {
            label = labels.values().iterator().next();
        }
        return categoryId + ":" + label;
    }

    public Element toElement() throws TransformException, FactoryException {
        Element regionEl = new Element(REGION_EL);

        regionEl.setAttribute(ID_ATT, getId());
        regionEl.setAttribute(CATEGORY_ID_ATT, getCategoryId());
        regionEl.setAttribute(HAS_GEOM_ATT, Boolean.toString(hasGeom()));

        regionEl.addContent(new Element(ID_ATT).setText(getId()));
        ReferencedEnvelope bbox = getLatLongBBox();
        regionEl.addContent(new Element(NORTH_EL).setText(Double.toString(bbox.getMaxY())));
        regionEl.addContent(new Element(SOUTH_EL).setText(Double.toString(bbox.getMinY())));
        regionEl.addContent(new Element(WEST_EL).setText(Double.toString(bbox.getMinX())));
        regionEl.addContent(new Element(EAST_EL).setText(Double.toString(bbox.getMaxX())));

        Element labelEl = new Element(LABEL_EL);
        regionEl.addContent(labelEl);
        for (Map.Entry<String, String> entry : getLabels().entrySet()) {
            labelEl.addContent(new Element(entry.getKey()).setText(entry.getValue()));
        }

        Element categoryEl = new Element(CATEGORY_EL);
        regionEl.addContent(categoryEl);
        for (Map.Entry<String, String> entry : getCategoryLabels().entrySet()) {
            categoryEl.addContent(new Element(entry.getKey()).setText(entry.getValue()));
        }

        return regionEl;
    }
}
