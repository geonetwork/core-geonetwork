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
 *       &lt;choice>
 *         &lt;element name="codelist" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="helper" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="context" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{}directiveAttributes" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="label" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *       &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *       &lt;attribute name="tooltip" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{}use"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "codelist",
    "helper",
    "directiveAttributes"
})
@XmlRootElement(name = "key")
public class Key {

    protected Key.Codelist codelist;
    protected Key.Helper helper;
    protected DirectiveAttributes directiveAttributes;
    @XmlAttribute(name = "label", required = true)
    protected String label;
    @XmlAttribute(name = "xpath", required = true)
    protected String xpath;
    @XmlAttribute(name = "tooltip")
    protected String tooltip;
    @XmlAttribute(name = "use")
    protected String use;

    /**
     * Gets the value of the codelist property.
     *
     * @return possible object is {@link Key.Codelist }
     */
    public Key.Codelist getCodelist() {
        return codelist;
    }

    /**
     * Sets the value of the codelist property.
     *
     * @param value allowed object is {@link Key.Codelist }
     */
    public void setCodelist(Key.Codelist value) {
        this.codelist = value;
    }

    /**
     * Gets the value of the helper property.
     *
     * @return possible object is {@link Key.Helper }
     */
    public Key.Helper getHelper() {
        return helper;
    }

    /**
     * Sets the value of the helper property.
     *
     * @param value allowed object is {@link Key.Helper }
     */
    public void setHelper(Key.Helper value) {
        this.helper = value;
    }

    /**
     * Gets the value of the directiveAttributes property.
     *
     * @return possible object is {@link DirectiveAttributes }
     */
    public DirectiveAttributes getDirectiveAttributes() {
        return directiveAttributes;
    }

    /**
     * Sets the value of the directiveAttributes property.
     *
     * @param value allowed object is {@link DirectiveAttributes }
     */
    public void setDirectiveAttributes(DirectiveAttributes value) {
        this.directiveAttributes = value;
    }

    /**
     * Gets the value of the label property.
     *
     * @return possible object is {@link String }
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLabel(String value) {
        this.label = value;
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
     * Gets the value of the tooltip property.
     *
     * @return possible object is {@link String }
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Sets the value of the tooltip property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTooltip(String value) {
        this.tooltip = value;
    }

    /**
     * Gets the value of the use property.
     *
     * @return possible object is {@link String }
     */
    public String getUse() {
        return use;
    }

    /**
     * Sets the value of the use property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUse(String value) {
        this.use = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Codelist {

        @XmlAttribute(name = "name")
        protected String name;

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

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="context" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Helper {

        @XmlAttribute(name = "name")
        protected String name;
        @XmlAttribute(name = "context")
        protected String context;

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
         * Gets the value of the context property.
         *
         * @return possible object is {@link String }
         */
        public String getContext() {
            return context;
        }

        /**
         * Sets the value of the context property.
         *
         * @param value allowed object is {@link String }
         */
        public void setContext(String value) {
            this.context = value;
        }

    }

}
