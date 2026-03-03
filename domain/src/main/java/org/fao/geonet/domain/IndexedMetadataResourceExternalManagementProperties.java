/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends MetadataResourceExternalManagementProperties to add additional properties for indexing purposes.
 */
@XmlRootElement(name = "metadataResourceExternalManagementProperties")
@XmlAccessorType(XmlAccessType.FIELD)
public class IndexedMetadataResourceExternalManagementProperties extends MetadataResourceExternalManagementProperties {
    /**
     * Additional properties for indexing.
     */
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Constructor for IndexedMetadataResourceExternalManagementProperties.
     *
     * @param id                The identifier of the metadata resource.
     * @param url               The URL of the metadata resource.
     * @param validationStatus  The validation status of the metadata resource.
     * @param additionalProperties Additional properties for indexing (can be null).
     */
    public IndexedMetadataResourceExternalManagementProperties(@Nonnull String id, @Nonnull String url, @Nonnull ValidationStatus validationStatus, @Nullable Map<String, Object> additionalProperties) {
        super(id, url, validationStatus);
        if (additionalProperties != null) {
            this.additionalProperties = additionalProperties;
        }
    }

    /**
     * Gets the additional properties for indexing.
     *
     * @return A map of additional properties.
     */
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}


