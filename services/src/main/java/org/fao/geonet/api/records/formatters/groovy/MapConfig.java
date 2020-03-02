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

package org.fao.geonet.api.records.formatters.groovy;

import org.fao.geonet.api.records.extent.MetadataExtentApi;

/**
 * Encapsulates the map parameters used when making {@link org.fao.geonet.services.region.MetadataExtentApi}
 * requests.
 * <p>
 * The parameters are read from the setting (background, width and mapproj) and are to be used when
 * displaying geometries and extents.
 *
 * @author Jesse on 12/19/2014.
 */
public class MapConfig {
    private String background, mapproj;
    private int width, thumbnailWidth;

    public MapConfig(String background, String mapproj, int width, int thumbnailWidth) {
        this.background = background;
        this.mapproj = mapproj;
        this.width = width;
        this.thumbnailWidth = thumbnailWidth;
    }

    /**
     * The value to pass as the <em>background</em> parameter when making a request to {@link
     * org.fao.geonet.services.region.MetadataExtentApi} when rendering extents and geometries.
     */
    public String getBackground() {
        return background.toLowerCase().startsWith("http://") ? MetadataExtentApi.SETTING_BACKGROUND : background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * The projection of the map.
     */
    public String getMapproj() {
        return mapproj;
    }

    public void setMapproj(String mapproj) {
        this.mapproj = mapproj;
    }

    /**
     * The width in pixels of the map image.
     */
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * The width in pixels of the map if the map is to be a small thumbnail.
     */
    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }
}
