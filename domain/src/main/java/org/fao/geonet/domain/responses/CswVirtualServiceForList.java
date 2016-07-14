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
import org.fao.geonet.domain.ServiceParam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper for CSW virtual services (@see org.fao.geonet.domain.Service) used for the CSW virtual
 * list service response.
 *
 * @author Jose García
 */
@XmlRootElement(name = "record")
@XmlAccessorType(XmlAccessType.FIELD)
public class CswVirtualServiceForList implements Serializable {
    private static final long serialVersionUID = 5342900799858099051L;

    private int id;
    private String name;
    @XmlElement(name = "classname")
    private String className;
    private String description;
    @XmlElementWrapper(name = "parameter")
    private List<ServiceParameter> parameters = new ArrayList<>();

    public CswVirtualServiceForList() {

    }

    public CswVirtualServiceForList(Service service) {
        id = service.getId();
        name = service.getName();
        className = service.getClassName();
        description = service.getDescription();
        for (ServiceParam serviceParam : service.getParameters()) {
            this.parameters.add(new ServiceParameter(serviceParam));
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ServiceParameter> getParameters() {
        return parameters;
    }

    public String getClassName() {
        return className;
    }

}

