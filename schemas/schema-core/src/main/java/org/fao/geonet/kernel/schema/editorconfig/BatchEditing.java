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
 *         &lt;element name="section" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="field" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="template" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="name" use="required"
 * type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="xpath" use="required"
 * type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="indexField" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *                           &lt;attribute name="use" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *                           &lt;attribute name="removable" type="{http://www.w3.org/2001/XMLSchema}boolean"
 * fixed="true" />
 *                           &lt;attribute name="insertMode">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;enumeration value="gn_add"/>
 *                                 &lt;enumeration value="gn_replace"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                           &lt;attribute name="codelist" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "section"
})
@XmlRootElement(name = "batchEditing")
public class BatchEditing {

    @XmlElement(required = true)
    protected List<BatchEditing.Section> section;

    /**
     * Gets the value of the section property.
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
     * <p> Objects of the following type(s) are allowed in the list {@link BatchEditing.Section }
     */
    public List<BatchEditing.Section> getSection() {
        if (section == null) {
            section = new ArrayList<BatchEditing.Section>();
        }
        return this.section;
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
     *       &lt;sequence>
     *         &lt;element name="field" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="template" type="{http://www.w3.org/2001/XMLSchema}string"
     * minOccurs="0"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
     * />
     *                 &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
     * />
     *                 &lt;attribute name="indexField" type="{http://www.w3.org/2001/XMLSchema}string"
     * />
     *                 &lt;attribute name="use" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="removable" type="{http://www.w3.org/2001/XMLSchema}boolean"
     * fixed="true" />
     *                 &lt;attribute name="insertMode">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;enumeration value="gn_add"/>
     *                       &lt;enumeration value="gn_replace"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *                 &lt;attribute name="codelist" type="{http://www.w3.org/2001/XMLSchema}string"
     * />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
     * />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "field"
    })
    public static class Section {

        @XmlElement(required = true)
        protected List<BatchEditing.Section.Field> field;
        @XmlAttribute(name = "name", required = true)
        protected String name;

        /**
         * Gets the value of the field property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the field property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getField().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link
         * BatchEditing.Section.Field }
         */
        public List<BatchEditing.Section.Field> getField() {
            if (field == null) {
                field = new ArrayList<BatchEditing.Section.Field>();
            }
            return this.field;
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
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this
         * class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="template" type="{http://www.w3.org/2001/XMLSchema}string"
         * minOccurs="0"/>
         *       &lt;/sequence>
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
         * />
         *       &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
         * />
         *       &lt;attribute name="indexField" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="use" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="removable" type="{http://www.w3.org/2001/XMLSchema}boolean"
         * fixed="true" />
         *       &lt;attribute name="insertMode">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;enumeration value="gn_add"/>
         *             &lt;enumeration value="gn_replace"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *       &lt;attribute name="codelist" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "template"
        })
        public static class Field {

            protected String template;
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "xpath", required = true)
            protected String xpath;
            @XmlAttribute(name = "indexField")
            protected String indexField;
            @XmlAttribute(name = "use")
            protected String use;
            @XmlAttribute(name = "removable")
            protected Boolean removable;
            @XmlAttribute(name = "insertMode")
            protected String insertMode;
            @XmlAttribute(name = "codelist")
            protected String codelist;

            /**
             * Gets the value of the template property.
             *
             * @return possible object is {@link String }
             */
            public String getTemplate() {
                return template;
            }

            /**
             * Sets the value of the template property.
             *
             * @param value allowed object is {@link String }
             */
            public void setTemplate(String value) {
                this.template = value;
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
             * Gets the value of the indexField property.
             *
             * @return possible object is {@link String }
             */
            public String getIndexField() {
                return indexField;
            }

            /**
             * Sets the value of the indexField property.
             *
             * @param value allowed object is {@link String }
             */
            public void setIndexField(String value) {
                this.indexField = value;
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
             * Gets the value of the removable property.
             *
             * @return possible object is {@link Boolean }
             */
            public boolean isRemovable() {
                if (removable == null) {
                    return false;
                } else {
                    return removable;
                }
            }

            /**
             * Sets the value of the removable property.
             *
             * @param value allowed object is {@link Boolean }
             */
            public void setRemovable(Boolean value) {
                this.removable = value;
            }

            /**
             * Gets the value of the insertMode property.
             *
             * @return possible object is {@link String }
             */
            public String getInsertMode() {
                return insertMode;
            }

            /**
             * Sets the value of the insertMode property.
             *
             * @param value allowed object is {@link String }
             */
            public void setInsertMode(String value) {
                this.insertMode = value;
            }

            /**
             * Gets the value of the codelist property.
             *
             * @return possible object is {@link String }
             */
            public String getCodelist() {
                return codelist;
            }

            /**
             * Sets the value of the codelist property.
             *
             * @param value allowed object is {@link String }
             */
            public void setCodelist(String value) {
                this.codelist = value;
            }

        }

    }

}
