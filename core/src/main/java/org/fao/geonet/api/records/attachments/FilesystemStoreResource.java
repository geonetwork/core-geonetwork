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


import com.google.common.net.UrlEscapers;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;

import java.util.Date;

/**
 * Metadata resource stored in the file system.
 *
 * Created by francois on 31/12/15.
 */
public class FilesystemStoreResource implements MetadataResource {
    private final String url;
    private final MetadataResourceVisibility metadataResourceVisibility;
    private final long size;
    private final Date lastModification;
    private final String metadataUuid;
    private final String filename;

    public FilesystemStoreResource(String metadataUuid,
                                   String filename,
                                   String baseUrl,
                                   MetadataResourceVisibility metadataResourceVisibility,
                                   long size,
                                   Date lastModification) {
        this.metadataUuid = metadataUuid;
        this.filename = filename;
        this.url = baseUrl + getId();
        this.metadataResourceVisibility = metadataResourceVisibility;
        this.size = size;
        this.lastModification = lastModification;
    }

    @Override
    public String getId() {
        return UrlEscapers.urlFragmentEscaper().escape(metadataUuid) +
                "/attachments/" +
                UrlEscapers.urlFragmentEscaper().escape(filename);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public MetadataResourceVisibility getVisibility() {
        return metadataResourceVisibility;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public Date getLastModification() {
        return lastModification;
    }

    @Override public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
        sb.append("\n");
        sb.append("Metadata: ").append(metadataUuid).append("\n");
        sb.append("Filename: ").append(filename).append("\n");
        sb.append("URL: ").append(url).append("\n");
        sb.append("Type: ").append(metadataResourceVisibility).append("\n");
        sb.append("Size: ").append(size).append("\n");
        sb.append("Last modification: ").append(lastModification).append("\n");
        return sb.toString();
    }
}
