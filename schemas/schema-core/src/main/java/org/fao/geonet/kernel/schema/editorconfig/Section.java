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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
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
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{}field"/>
 *         &lt;element ref="{}action"/>
 *         &lt;element ref="{}text"/>
 *         &lt;element ref="{}section"/>
 *         &lt;element ref="{}fieldset"/>
 *       &lt;/choice>
 *       &lt;attribute ref="{}mode"/>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="xpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{}or"/>
 *       &lt;attribute ref="{}in"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fieldOrActionOrText"
})
@XmlRootElement(name = "section")
public class Section {

    @XmlElements({
        @XmlElement(name = "field", type = Field.class),
        @XmlElement(name = "action", type = Action.class),
        @XmlElement(name = "text", type = Text.class),
        @XmlElement(name = "section", type = Section.class),
        @XmlElement(name = "fieldset", type = Fieldset.class)
    })
    protected List<Object> fieldOrActionOrText;
    @XmlAttribute(name = "mode")
    protected String mode;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "xpath")
    protected String xpath;
    @XmlAttribute(name = "or")
    protected String or;
    @XmlAttribute(name = "in")
    protected String in;

    /**
     * Gets the value of the fieldOrActionOrText property.
     *
     * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the fieldOrActionOrText property.
     *
     * <p> For example, to add a new item, do as follows:
     * <pre>
     *    getFieldOrActionOrText().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list {@link Field } {@link Action }
     * {@link Text } {@link Section } {@link Fieldset }
     */
    public List<Object> getFieldOrActionOrText() {
        if (fieldOrActionOrText == null) {
            fieldOrActionOrText = new ArrayList<Object>();
        }
        return this.fieldOrActionOrText;
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
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the xpath property.
     *
     * @return possible object is {@link String }
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * Sets the value of the xpath property.
     *
     * @param value allowed object is {@link String }
     */
    public void setXpath(String value) {
        this.xpath = value;
    }

    /**
     * Gets the value of the or property.
     *
     * @return possible object is {@link String }
     */
    public String getOr() {
        return or;
    }

    /**
     * Sets the value of the or property.
     *
     * @param value allowed object is {@link String }
     */
    public void setOr(String value) {
        this.or = value;
    }

    /**
     * Gets the value of the in property.
     *
     * @return possible object is {@link String }
     */
    public String getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIn(String value) {
        this.in = value;
    }

}
