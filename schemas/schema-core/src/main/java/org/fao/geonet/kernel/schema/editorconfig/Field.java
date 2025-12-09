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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element ref="{}template" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="if" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType"
 * />
 *       &lt;attribute name="isMissingLabel" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="or" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="in" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="del" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="templateModeOnly" type="{http://www.w3.org/2001/XMLSchema}boolean"
 * fixed="true" />
 *       &lt;attribute name="notDisplayedIfMissing" type="{http://www.w3.org/2001/XMLSchema}boolean"
 * fixed="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "template"
})
@XmlRootElement(name = "field")
public class Field {

    protected Template template;
    @XmlAttribute(name = "if")
    @XmlSchemaType(name = "anySimpleType")
    protected String _if;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "xpath", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String xpath;
    @XmlAttribute(name = "isMissingLabel")
    protected String isMissingLabel;
    @XmlAttribute(name = "or")
    @XmlSchemaType(name = "anySimpleType")
    protected String or;
    @XmlAttribute(name = "in")
    @XmlSchemaType(name = "anySimpleType")
    protected String in;
    @XmlAttribute(name = "del")
    protected String del;
    @XmlAttribute(name = "templateModeOnly")
    protected Boolean templateModeOnly;
    @XmlAttribute(name = "notDisplayedIfMissing")
    protected Boolean notDisplayedIfMissing;

    /**
     * Gets the value of the template property.
     *
     * @return possible object is {@link Template }
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     *
     * @param value allowed object is {@link Template }
     */
    public void setTemplate(Template value) {
        this.template = value;
    }

    /**
     * Gets the value of the if property.
     *
     * @return possible object is {@link String }
     */
    public String getIf() {
        return _if;
    }

    /**
     * Sets the value of the if property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIf(String value) {
        this._if = value;
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
     * Gets the value of the isMissingLabel property.
     *
     * @return possible object is {@link String }
     */
    public String getIsMissingLabel() {
        return isMissingLabel;
    }

    /**
     * Sets the value of the isMissingLabel property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIsMissingLabel(String value) {
        this.isMissingLabel = value;
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

    /**
     * Gets the value of the del property.
     *
     * @return possible object is {@link String }
     */
    public String getDel() {
        return del;
    }

    /**
     * Sets the value of the del property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDel(String value) {
        this.del = value;
    }

    /**
     * Gets the value of the templateModeOnly property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isTemplateModeOnly() {
        if (templateModeOnly == null) {
            return true;
        } else {
            return templateModeOnly;
        }
    }

    /**
     * Sets the value of the templateModeOnly property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setTemplateModeOnly(Boolean value) {
        this.templateModeOnly = value;
    }

    /**
     * Gets the value of the notDisplayedIfMissing property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isNotDisplayedIfMissing() {
        if (notDisplayedIfMissing == null) {
            return true;
        } else {
            return notDisplayedIfMissing;
        }
    }

    /**
     * Sets the value of the notDisplayedIfMissing property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setNotDisplayedIfMissing(Boolean value) {
        this.notDisplayedIfMissing = value;
    }

}
