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

package org.fao.geonet.api.records.model.related;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="featureType">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="attributes">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="element" maxOccurs="unbounded">
 *                               <complexType>
 *                                 <complexContent>
 *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     <sequence>
 *                                       <element name="attributeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                       <element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                       <element name="definition" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                     </sequence>
 *                                   </restriction>
 *                                 </complexContent>
 *                               </complexType>
 *                             </element>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "featureType"
})
public class FCRelatedMetadataItem extends RelatedMetadataItem {

    @XmlElement(required = true)
    protected FCRelatedMetadataItem.FeatureType featureType;



    /**
     * Gets the LocalizedString of the featureType property.
     * 
     * @return
     *     possible object is
     *     {@link FCRelatedMetadataItem.FeatureType }
     *     
     */
    public FCRelatedMetadataItem.FeatureType getFeatureType() {
        return featureType;
    }

    /**
     * Sets the LocalizedString of the featureType property.
     * 
     * @param LocalizedString
     *     allowed object is
     *     {@link FCRelatedMetadataItem.FeatureType }
     *     
     */
    public void setFeatureType(FCRelatedMetadataItem.FeatureType LocalizedString) {
        this.featureType = LocalizedString;
    }




    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element name="attributeTable" maxOccurs="unbounded" minOccurs="0">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="name" maxOccurs="unbounded" minOccurs="0">
     *                     <complexType>
     *                       <simpleContent>
     *                         <extension base="<http://www.w3.org/2001/XMLSchema>string">
     *                         </extension>
     *                       </simpleContent>
     *                     </complexType>
     *                   </element>
     *                   <element ref="{}definition" maxOccurs="unbounded" minOccurs="0"/>
     *                   <element ref="{}code" maxOccurs="unbounded" minOccurs="0"/>
     *                   <element name="link" maxOccurs="unbounded" minOccurs="0">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                   <element name="type" maxOccurs="unbounded" minOccurs="0">
     *                     <complexType>
     *                       <simpleContent>
     *                         <extension base="<http://www.w3.org/2001/XMLSchema>string">
     *                         </extension>
     *                       </simpleContent>
     *                     </complexType>
     *                   </element>
     *                   <element name="LocalizedStrings" maxOccurs="unbounded" minOccurs="0">
     *                     <complexType>
     *                       <complexContent>
     *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           <sequence>
     *                             <element name="label" maxOccurs="unbounded" minOccurs="0">
     *                               <complexType>
     *                                 <simpleContent>
     *                                   <extension base="<http://www.w3.org/2001/XMLSchema>string">
     *                                   </extension>
     *                                 </simpleContent>
     *                               </complexType>
     *                             </element>
     *                             <element ref="{}code" maxOccurs="unbounded" minOccurs="0"/>
     *                             <element ref="{}definition" maxOccurs="unbounded" minOccurs="0"/>
     *                           </sequence>
     *                         </restriction>
     *                       </complexContent>
     *                     </complexType>
     *                   </element>
     *                 </sequence>
     *               </restriction>
     *             </complexContent>
     *           </complexType>
     *         </element>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "attributeTable"
    })
    public static class FeatureType {

        protected AttributeTable attributeTable;

        /**
         * Gets the value of the attributeTable property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the attributeTable property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAttributeTable().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AttributeTable }
         * 
         * 
         */
        public AttributeTable getAttributeTable() {
            
            return this.attributeTable;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * <complexType>
         *   <complexContent>
         *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       <sequence>
         *         <element name="element" maxOccurs="unbounded" minOccurs="0">
         *           <complexType>
         *             <complexContent>
         *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 <sequence>
         *                   <element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   <element name="definition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   <element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   <element name="link" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   <element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   <element name="values" maxOccurs="unbounded" minOccurs="0">
         *                     <complexType>
         *                       <complexContent>
         *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           <sequence>
         *                             <element name="label" maxOccurs="unbounded" minOccurs="0">
         *                               <complexType>
         *                                 <simpleContent>
         *                                   <extension base="<http://www.w3.org/2001/XMLSchema>string">
         *                                   </extension>
         *                                 </simpleContent>
         *                               </complexType>
         *                             </element>
         *                             <element name="code" maxOccurs="unbounded" minOccurs="0">
         *                               <complexType>
         *                                 <simpleContent>
         *                                   <extension base="<http://www.w3.org/2001/XMLSchema>string">
         *                                   </extension>
         *                                 </simpleContent>
         *                               </complexType>
         *                             </element>
         *                             <element name="definition" maxOccurs="unbounded" minOccurs="0">
         *                               <complexType>
         *                                 <complexContent>
         *                                   <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                                   </restriction>
         *                                 </complexContent>
         *                               </complexType>
         *                             </element>
         *                           </sequence>
         *                         </restriction>
         *                       </complexContent>
         *                     </complexType>
         *                   </element>
         *                 </sequence>
         *               </restriction>
         *             </complexContent>
         *           </complexType>
         *         </element>
         *       </sequence>
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "element"
        })
        public static class AttributeTable {

            protected List<AttributeTable.Element> element;

            /**
             * Gets the value of the element property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the element property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getElement().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link AttributeTable.Element }
             * 
             * 
             */
            public List<AttributeTable.Element> getElement() {
                if (element == null) {
                    element = new ArrayList<AttributeTable.Element>();
                }
                return this.element;
            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * <complexType>
             *   <complexContent>
             *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       <sequence>
             *         <element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         <element name="definition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         <element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         <element name="link" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         <element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         <element name="values" maxOccurs="unbounded" minOccurs="0">
             *           <complexType>
             *             <complexContent>
             *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 <sequence>
             *                   <element name="label" maxOccurs="unbounded" minOccurs="0">
             *                     <complexType>
             *                       <simpleContent>
             *                         <extension base="<http://www.w3.org/2001/XMLSchema>string">
             *                         </extension>
             *                       </simpleContent>
             *                     </complexType>
             *                   </element>
             *                   <element name="code" maxOccurs="unbounded" minOccurs="0">
             *                     <complexType>
             *                       <simpleContent>
             *                         <extension base="<http://www.w3.org/2001/XMLSchema>string">
             *                         </extension>
             *                       </simpleContent>
             *                     </complexType>
             *                   </element>
             *                   <element name="definition" maxOccurs="unbounded" minOccurs="0">
             *                     <complexType>
             *                       <complexContent>
             *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                         </restriction>
             *                       </complexContent>
             *                     </complexType>
             *                   </element>
             *                 </sequence>
             *               </restriction>
             *             </complexContent>
             *           </complexType>
             *         </element>
             *       </sequence>
             *     </restriction>
             *   </complexContent>
             * </complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "name",
                    "definition",
                    "code",
                    "link",
                    "type",
                    "values"
            })
            public static class Element {

                protected String name;
                @XmlElement(nillable = true)
                protected String definition;
                @XmlElement(nillable = true)
                protected String code;
                @XmlElement(nillable = true)
                protected String link;
                @XmlElement(nillable = true)
                protected String type;
                @XmlElement(nillable = true)
                protected List<AttributeTable.Element.Values> values;

                /**
                 * Gets the value of the name property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getName() {
                    return name;
                }

                /**
                 * Sets the value of the name property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setName(String value) {
                    this.name = value;
                }

                /**
                 * Gets the value of the definition property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getDefinition() {
                    return definition;
                }

                /**
                 * Sets the value of the definition property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setDefinition(String value) {
                    this.definition = value;
                }

                /**
                 * Gets the value of the code property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getCode() {
                    return code;
                }

                /**
                 * Sets the value of the code property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setCode(String value) {
                    this.code = value;
                }

                /**
                 * Gets the value of the link property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getLink() {
                    return link;
                }

                /**
                 * Sets the value of the link property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setLink(String value) {
                    this.link = value;
                }

                /**
                 * Gets the value of the type property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getType() {
                    return type;
                }

                /**
                 * Sets the value of the type property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setType(String value) {
                    this.type = value;
                }

                /**
                 * Gets the value of the values property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the values property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getValues().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link AttributeTable.Element.Values }
                 * 
                 * 
                 */
                public List<AttributeTable.Element.Values> getValues() {
                    if (values == null || values.size()==0) {
                        values = null;
                    }
                    return this.values;
                }


                /**
                 * <p>Java class for anonymous complex type.
                 * 
                 * <p>The following schema fragment specifies the expected content contained within this class.
                 * 
                 * <pre>
                 * <complexType>
                 *   <complexContent>
                 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       <sequence>
                 *         <element name="label" maxOccurs="unbounded" minOccurs="0">
                 *           <complexType>
                 *             <simpleContent>
                 *               <extension base="<http://www.w3.org/2001/XMLSchema>string">
                 *               </extension>
                 *             </simpleContent>
                 *           </complexType>
                 *         </element>
                 *         <element name="code" maxOccurs="unbounded" minOccurs="0">
                 *           <complexType>
                 *             <simpleContent>
                 *               <extension base="<http://www.w3.org/2001/XMLSchema>string">
                 *               </extension>
                 *             </simpleContent>
                 *           </complexType>
                 *         </element>
                 *         <element name="definition" maxOccurs="unbounded" minOccurs="0">
                 *           <complexType>
                 *             <complexContent>
                 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *               </restriction>
                 *             </complexContent>
                 *           </complexType>
                 *         </element>
                 *       </sequence>
                 *     </restriction>
                 *   </complexContent>
                 * </complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                        "label",
                        "code",
                        "definition"
                })
                public static class Values {

                    @XmlElement(nillable = true)
                    protected String label;
                    @XmlElement(nillable = true)
                    protected String code;
                    @XmlElement(nillable = true)
                    protected String definition;

                    /**
                     * Gets the value of the label property.
                     * 
                     * <p>
                     * This accessor method returns a reference to the live list,
                     * not a snapshot. Therefore any modification you make to the
                     * returned list will be present inside the JAXB object.
                     * This is why there is not a <CODE>set</CODE> method for the label property.
                     * 
                     * <p>
                     * For example, to add a new item, do as follows:
                     * <pre>
                     *    getLabel().add(newItem);
                     * </pre>
                     * 
                     * 
                     * <p>
                     * Objects of the following type(s) are allowed in the list
                     * {@link AttributeTable.Element.Values.Label }
                     * 
                     * 
                     */
                    public String getLabel() {
                        return this.label;
                    }

                    /**
                     * Gets the value of the code property.
                     * 
                     * <p>
                     * This accessor method returns a reference to the live list,
                     * not a snapshot. Therefore any modification you make to the
                     * returned list will be present inside the JAXB object.
                     * This is why there is not a <CODE>set</CODE> method for the code property.
                     * 
                     * <p>
                     * For example, to add a new item, do as follows:
                     * <pre>
                     *    getCode().add(newItem);
                     * </pre>
                     * 
                     * 
                     * <p>
                     * Objects of the following type(s) are allowed in the list
                     * {@link AttributeTable.Element.Values.Code }
                     * 
                     * 
                     */
                    public String getCode() {
                        return this.code;
                    }

                    /**
                     * Gets the value of the definition property.
                     * 
                     * <p>
                     * This accessor method returns a reference to the live list,
                     * not a snapshot. Therefore any modification you make to the
                     * returned list will be present inside the JAXB object.
                     * This is why there is not a <CODE>set</CODE> method for the definition property.
                     * 
                     * <p>
                     * For example, to add a new item, do as follows:
                     * <pre>
                     *    getDefinition().add(newItem);
                     * </pre>
                     * 
                     * 
                     * <p>
                     * Objects of the following type(s) are allowed in the list
                     * {@link AttributeTable.Element.Values.Definition }
                     * 
                     * 
                     */
                    public String getDefinition() {
                       return this.definition;
                    }


                    /**
                     * <p>Java class for anonymous complex type.
                     * 
                     * <p>The following schema fragment specifies the expected content contained within this class.
                     * 
                     * <pre>
                     * <complexType>
                     *   <simpleContent>
                     *     <extension base="<http://www.w3.org/2001/XMLSchema>string">
                     *     </extension>
                     *   </simpleContent>
                     * </complexType>
                     * </pre>
                     * 
                     * 
                     */
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "", propOrder = {
                            "value"
                    })
                    public static class Code {

                        @XmlValue
                        protected String value;

                        /**
                         * Gets the value of the value property.
                         * 
                         * @return
                         *     possible object is
                         *     {@link String }
                         *     
                         */
                        public String getValue() {
                            return value;
                        }

                        /**
                         * Sets the value of the value property.
                         * 
                         * @param value
                         *     allowed object is
                         *     {@link String }
                         *     
                         */
                        public void setValue(String value) {
                            this.value = value;
                        }

                    }


                    /**
                     * <p>Java class for anonymous complex type.
                     * 
                     * <p>The following schema fragment specifies the expected content contained within this class.
                     * 
                     * <pre>
                     * <complexType>
                     *   <complexContent>
                     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                     *     </restriction>
                     *   </complexContent>
                     * </complexType>
                     * </pre>
                     * 
                     * 
                     */
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "")
                    public static class Definition {


                    }


                    /**
                     * <p>Java class for anonymous complex type.
                     * 
                     * <p>The following schema fragment specifies the expected content contained within this class.
                     * 
                     * <pre>
                     * <complexType>
                     *   <simpleContent>
                     *     <extension base="<http://www.w3.org/2001/XMLSchema>string">
                     *     </extension>
                     *   </simpleContent>
                     * </complexType>
                     * </pre>
                     * 
                     * 
                     */
                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "", propOrder = {
                            "value"
                    })
                    public static class Label {

                        @XmlValue
                        protected String value;

                        /**
                         * Gets the value of the value property.
                         * 
                         * @return
                         *     possible object is
                         *     {@link String }
                         *     
                         */
                        public String getValue() {
                            return value;
                        }

                        /**
                         * Sets the value of the value property.
                         * 
                         * @param value
                         *     allowed object is
                         *     {@link String }
                         *     
                         */
                        public void setValue(String value) {
                            this.value = value;
                        }

                    }

                }

            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element ref="{}value" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "value"
    })
    public static class Title {

        @XmlElement(nillable = true)
        protected List<LocalizedString> value;

        /**
         * Gets the value of the value property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the value property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getValue().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Value }
         * 
         * 
         */
        public List<LocalizedString> getValue() {
            if (value == null) {
                value = new ArrayList<LocalizedString>();
            }
            return this.value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element ref="{}value" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "value"
    })
    public static class Url {

        @XmlElement(nillable = true)
        protected List<LocalizedString> value;

        /**
         * Gets the value of the value property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the value property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getValue().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Value }
         * 
         * 
         */
        public List<LocalizedString> getValue() {
            if (value == null) {
                value = new ArrayList<LocalizedString>();
            }
            return this.value;
        }

    }

}
