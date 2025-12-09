/*
 *
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

package org.fao.geonet.kernel.schema.editorconfig;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}section" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}boolean" fixed="true"
 * />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{}mode"/>
 *       &lt;attribute name="toggle" type="{http://www.w3.org/2001/XMLSchema}boolean" fixed="true"
 * />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "section"
})
@XmlRootElement(name = "tab")
public class Tab {

    protected List<Section> section;
    @XmlAttribute(name = "default")
    protected Boolean _default;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "mode")
    protected String mode;
    @XmlAttribute(name = "toggle")
    protected Boolean toggle;

    /**
     * For each section a fieldset is created. Gets the value of the section property.
     *
     * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the section property.
     *
     * <p> For example, to add a new item, do as follows:
     * <pre>
     *    getSection().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list {@link Section }
     */
    public List<Section> getSection() {
        if (section == null) {
            section = new ArrayList<Section>();
        }
        return this.section;
    }

    /**
     * Gets the value of the default property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isDefault() {
        if (_default == null) {
            return true;
        } else {
            return _default;
        }
    }

    /**
     * Sets the value of the default property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the mode property.
     *
     * @return possible object is {@link String }
     */
    public String getMode() {
        if (mode == null) {
            return "flat";
        } else {
            return mode;
        }
    }

    /**
     * Sets the value of the mode property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMode(String value) {
        this.mode = value;
    }

    /**
     * Gets the value of the toggle property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isToggle() {
        if (toggle == null) {
            return true;
        } else {
            return toggle;
        }
    }

    /**
     * Sets the value of the toggle property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setToggle(Boolean value) {
        this.toggle = value;
    }

}
