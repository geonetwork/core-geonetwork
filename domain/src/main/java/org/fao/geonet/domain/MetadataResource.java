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

package org.fao.geonet.domain;


import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by francois on 31/12/15.
 */
@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.FIELD)
public interface MetadataResource {

    /**
     * Get the resource management properties for the resource. If null then the resource management link will not show up.
     * The resource management link is a link to an external application that is more specialized on the management of the resource.
     * This can include things such as archiving/retention, extra metadata...
     */
    class ExternalResourceManagementProperties {
        private final String url;
        private final String windowParameters;
        private final boolean modal;

        public ExternalResourceManagementProperties(String url, String windowParameters, boolean modal) {
            this.url=url;
            this.windowParameters=windowParameters;
            this.modal=modal;
        }

        public String getUrl() {
            return url;
        }

        public String getWindowParameters() {
            return windowParameters;
        }

        public boolean isModal() {
            return modal;
        }
    }

    String getId();

    String getUrl();

    MetadataResourceVisibility getVisibility();

    long getSize();

    Date getLastModification();

    String getFilename();

    boolean isApproved();

    int getMetadataId();

    String getMetadataUuid();

    String getVersion();

    ExternalResourceManagementProperties getExternalResourceManagementProperties();
}