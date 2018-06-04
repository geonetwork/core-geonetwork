/*
 * =============================================================================
 * ===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.api.records.attachments;


import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;

/**
 * Metadata resource stored in the file system.
 *
 * Created by francois on 31/12/15.
 */
public class FilesystemStoreResource implements MetadataResource {
    private final String filename;
    private final String url;
    private final MetadataResourceVisibility metadataResourceVisibility;
    private double size = -1;

    public FilesystemStoreResource(String id,
                                   String baseUrl,
                                   MetadataResourceVisibility metadataResourceVisibility,
                                   double size) {
        this.filename = id;
        this.url = baseUrl + id;
        this.metadataResourceVisibility = metadataResourceVisibility;
        this.size = Double.isNaN(size) ? -1 : size;
    }

    @Override
    public String getId() {
        return filename;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getType() {
        return metadataResourceVisibility.toString();
    }

    @Override
    public double getSize() {
        return size;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
        sb.append("\n");
        sb.append("Id: ").append(filename).append("\n");
        sb.append("URL: ").append(url).append("\n");
        sb.append("Type: ").append(metadataResourceVisibility).append("\n");
        sb.append("Size: ").append(size).append("\n");
        return sb.toString();
    }
}
