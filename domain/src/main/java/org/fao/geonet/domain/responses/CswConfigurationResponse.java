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

package org.fao.geonet.domain.responses;

import org.fao.geonet.domain.Translations;

import jakarta.xml.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * This class is a helper class for the service that returns the csw service configuration.
 *
 * @author Jose García
 */
@XmlRootElement(name = "cswcapabilitiesinfofield")
@XmlAccessorType(XmlAccessType.FIELD)
public class CswConfigurationResponse implements Serializable {
    private static final long serialVersionUID = -4426385060234828544L;
    private boolean cswEnabled;
    private boolean cswMetadataPublic;
    private int capabilityRecordUuid;

    public boolean isCswEnabled() {
        return cswEnabled;
    }

    public void setCswEnabled(boolean cswEnabled) {
        this.cswEnabled = cswEnabled;
    }

    public boolean isCswMetadataPublic() {
        return cswMetadataPublic;
    }

    public void setCswMetadataPublic(boolean cswMetadataPublic) {
        this.cswMetadataPublic = cswMetadataPublic;
    }

    public int getCapabilityRecordUuid() {
        return capabilityRecordUuid;
    }

    public void setCapabilityRecordUuid(int capabilityRecordUuid) {
        this.capabilityRecordUuid = capabilityRecordUuid;
    }
}
