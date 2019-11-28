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
package org.fao.geonet.kernel;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by francois on 22/10/15.
 */
@XmlRootElement(name = "edit")
public class BatchEditParameter implements Serializable {
    private String xpath;
    private String value;
    private String condition;

    public BatchEditParameter() {
    }

    public BatchEditParameter(String xpath, String value, String condition) {
        if (StringUtils.isEmpty(xpath)) {
            throw new IllegalArgumentException(
                "Parameter xpath is not set. It should be not empty and define the XPath of the element to update.");
        }
        this.xpath = xpath;
        this.value = value;
        this.condition = condition;
    }
    public BatchEditParameter(String xpath, String value) {
        this(xpath, value, null);
    }

    @XmlElement(required = true)
    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    @XmlElement(required = true)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement(required = false)
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Editing xpath ");
        sb.append(this.xpath);
        if (StringUtils.isNotEmpty(this.value)) {
            sb.append(", searching for ");
            sb.append(this.value);
        }
        sb.append(".");
        sb.append(" Check expression: ");
        sb.append(this.condition);
        return sb.toString();
    }
}
