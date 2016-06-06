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

import org.fao.geonet.domain.ServiceParam;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Class to model CSW Virtual service parameter used for the CSW virtual get service response (@see
 * org.fao.geonet.domain.responses.CswVirtualServiceResponse).
 *
 * @author Jose García
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceParameter implements Serializable {
    private static final long serialVersionUID = -7682021379005431348L;
    @XmlAttribute
    protected String name;
    @XmlValue
    protected String value;
    @XmlValue
    protected Character occur;

    public ServiceParameter() {

    }

    public ServiceParameter(ServiceParam param) {
        this.name = param.getName();
        this.value = param.getValue();
        this.occur = param.getOccur();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Character getOccur() {
        return occur;
    }

    public void setOccur(Character occur) {
        this.occur = occur;
    }
}
