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

import org.fao.geonet.domain.Service;

import javax.xml.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class for the service that returns the list CSW virtual services.
 *
 * XML output:
 *
 * <service> <record> <classname>.services.main.CswDiscoveryDispatcher</classname>
 * <description>desc1</description> <id>3147</id> <name>csw-climate</name> <parameters>
 * {keyword=climate} </parameters> </record> <record> <classname>.services.main.CswDiscoveryDispatcher</classname>
 * <description>csw-water</description> <id>3315</id> <name>csw-servicenamecxz</name>
 * <parameters>{title=water, abstract=water}</parameters> </record> </service>
 *
 * @author Jose García
 */
@XmlRootElement(name = "service")
@XmlSeeAlso(Service.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class CswVirtualServiceListResponse implements Serializable {
    private static final long serialVersionUID = 624011885854733512L;

    @XmlElement(name = "record")
    private List<CswVirtualServiceForList> servicesForList;

    public CswVirtualServiceListResponse() {

    }

    public CswVirtualServiceListResponse(List<Service> services) {

        servicesForList = new ArrayList<CswVirtualServiceForList>();

        for (Service service : services) {
            servicesForList.add(new CswVirtualServiceForList(service));
        }
    }

    public List<CswVirtualServiceForList> getRecord() {
        return servicesForList;
    }

    public void setRecord(List<CswVirtualServiceForList> servicesForList) {
        this.servicesForList = servicesForList;
    }
}
