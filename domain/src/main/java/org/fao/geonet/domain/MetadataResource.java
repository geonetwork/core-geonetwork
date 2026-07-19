/*
 * =============================================================================
 * ===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.fao.geonet.annotations.IndexIgnore;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by francois on 31/12/15.
 */
@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder(alphabetic=true)
public interface MetadataResource {

    @IndexIgnore
    String getId();

    // Don't ignore url in the index as it is used to link the urls
    String getUrl();

    MetadataResourceVisibility getVisibility();

    long getSize();

    Date getLastModification();

    @IndexIgnore
    String getFilename();

    @IndexIgnore
    boolean isApproved();

    @IndexIgnore // Metadata id is already in the index so no need to add it again.
    int getMetadataId();

    @IndexIgnore // Metadata uuid is already in the index so no need to add it again.
    String getMetadataUuid();

    String getVersion();

    MetadataResourceExternalManagementProperties getMetadataResourceExternalManagementProperties();
}
