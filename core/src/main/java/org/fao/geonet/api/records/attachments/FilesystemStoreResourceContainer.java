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


import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceExternalManagementProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.UrlEscapers;

public class FilesystemStoreResourceContainer implements MetadataResourceContainer {
    private final String url;
    private final int metadataId;
    private final String metadataUuid;
    private final String containerName;
    private final MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties;
    private final boolean approved;

    public FilesystemStoreResourceContainer(String metadataUuid,
                                            int metadataId,
                                            String containerName,
                                            String baseUrl,
                                            MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties,
                                            boolean approved) {
        this.metadataUuid = metadataUuid;
        this.metadataId = metadataId;
        this.approved=approved;
        this.containerName = containerName;
        this.url = baseUrl + getId();
        this.metadataResourceExternalManagementProperties = metadataResourceExternalManagementProperties;
    }

    public FilesystemStoreResourceContainer(String metadataUuid,
                                            int metadataId,
                                            String containerName,
                                            String baseUrl,
                                            boolean approved) {
        this(metadataUuid, metadataId, containerName, baseUrl, null, approved);
    }

    @Override
    public String getId() {
        return UrlEscapers.urlFragmentEscaper().escape(metadataUuid) +
                "/attachments/";
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getContainterName() {
        return containerName;
    }

    @Override
    public boolean isApproved() {
        return approved;
    }

    @Override
    public int getMetadataId() {
        return metadataId;
    }

    @Override
    public String getMetadataUuid() {
        return metadataUuid;
    }

    @Override
    public MetadataResourceExternalManagementProperties getMetadataResourceExternalManagementProperties() {
        return metadataResourceExternalManagementProperties;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting FilesystemStoreResourceContainer to json", e);
        }
    }
}
