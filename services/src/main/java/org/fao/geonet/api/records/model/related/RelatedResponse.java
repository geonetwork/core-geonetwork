/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>Java class for relatedResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <complexType name="relatedResponse">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="children" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="parent" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="siblings" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedSiblingMetadataItem"
 * maxOccurs="unbounded" minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="associated" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="service" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="dataset" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="fcat" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="source" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="hassource" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="related" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="online" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedLinkItem" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="thumbnail" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="item" type="{}relatedThumbnailItem" maxOccurs="unbounded"
 * minOccurs="0"/>
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
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedResponse")
@XmlRootElement(name = "related")
public class RelatedResponse {

    protected Children children;
    protected Parent parent;
    protected Siblings siblings;
    protected Associated associated;
    protected Service services;
    protected Dataset datasets;
    protected Fcat fcats;
    protected Hasfeaturecats hasfeaturecats;
    protected Source sources;
    protected Hassource hassources;
    protected Related related;
    protected Online onlines;
    protected Thumbnail thumbnails;

    /**
     * Gets the value of the children property.
     *
     * @return possible object is {@link Children }
     */
    public Children getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     *
     * @param value allowed object is {@link Children }
     */
    public void setChildren(Children value) {
        this.children = value;
    }

    /**
     * Gets the value of the parent property.
     *
     * @return possible object is {@link Parent }
     */
    public Parent getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     *
     * @param value allowed object is {@link Parent }
     */
    public void setParent(Parent value) {
        this.parent = value;
    }

    /**
     * Gets the value of the siblings property.
     *
     * @return possible object is {@link Siblings }
     */
    public Siblings getSiblings() {
        return siblings;
    }

    /**
     * Sets the value of the siblings property.
     *
     * @param value allowed object is {@link Siblings }
     */
    public void setSiblings(Siblings value) {
        this.siblings = value;
    }

    /**
     * Gets the value of the associated property.
     *
     * @return possible object is {@link Associated }
     */
    public Associated getAssociated() {
        return associated;
    }

    /**
     * Sets the value of the associated property.
     *
     * @param value allowed object is {@link Associated }
     */
    public void setAssociated(Associated value) {
        this.associated = value;
    }

    /**
     * Gets the value of the service property.
     *
     * @return possible object is {@link Service }
     */
    public Service getServices() {
        return services;
    }

    /**
     * Sets the value of the service property.
     *
     * @param value allowed object is {@link Service }
     */
    public void setServices(Service value) {
        this.services = value;
    }

    /**
     * Gets the value of the dataset property.
     *
     * @return possible object is {@link Dataset }
     */
    public Dataset getDatasets() {
        return datasets;
    }

    /**
     * Sets the value of the dataset property.
     *
     * @param value allowed object is {@link Dataset }
     */
    public void setDatasets(Dataset value) {
        this.datasets = value;
    }

    /**
     * Gets the value of the fcat property.
     *
     * @return possible object is {@link Fcat }
     */
    public Fcat getFcats() {
        return fcats;
    }

    /**
     * Sets the value of the fcat property.
     *
     * @param value allowed object is {@link Fcat }
     */
    public void setFcats(Fcat value) {
        this.fcats = value;
    }

    /**
     * Gets the value of the hasfeaturecats property.
     *
     * @return possible object is {@link Hasfeaturecats }
     */
    public Hasfeaturecats getHasfeaturecats() {
        return hasfeaturecats;
    }

    /**
     * Sets the value of the hasfeaturecats property.
     *
     * @param value allowed object is {@link Hasfeaturecats }
     */
    public void setHasfeaturecats(Hasfeaturecats value) {
        this.hasfeaturecats = value;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link Source }
     */
    public Source getSources() {
        return sources;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link Source }
     */
    public void setSources(Source value) {
        this.sources = value;
    }

    /**
     * Gets the value of the hassource property.
     *
     * @return possible object is {@link Hassource }
     */
    public Hassource getHassources() {
        return hassources;
    }

    /**
     * Sets the value of the hassource property.
     *
     * @param value allowed object is {@link Hassource }
     */
    public void setHassources(Hassource value) {
        this.hassources = value;
    }

    /**
     * Gets the value of the related property.
     *
     * @return possible object is {@link Related }
     */
    public Related getRelated() {
        return related;
    }

    /**
     * Sets the value of the related property.
     *
     * @param value allowed object is {@link Related }
     */
    public void setRelated(Related value) {
        this.related = value;
    }

    /**
     * Gets the value of the online property.
     *
     * @return possible object is {@link Online }
     */
    public Online getOnlines() {
        return onlines;
    }

    /**
     * Sets the value of the online property.
     *
     * @param value allowed object is {@link Online }
     */
    public void setOnlines(Online value) {
        this.onlines = value;
    }

    /**
     * Gets the value of the thumbnail property.
     *
     * @return possible object is {@link Thumbnail }
     */
    public Thumbnail getThumbnails() {
        return thumbnails;
    }

    /**
     * Sets the value of the thumbnail property.
     *
     * @param value allowed object is {@link Thumbnail }
     */
    public void setThumbnails(Thumbnail value) {
        this.thumbnails = value;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Associated implements IListOnlyClassToArray {

        protected List<AssociatedSiblingMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link AssociatedSiblingMetadataItem
         * }
         */
        public List<AssociatedSiblingMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Children implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Dataset implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Fcat implements IListOnlyClassToArray {

        protected List<FCRelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<FCRelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Hasfeaturecats implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Hassource implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedLinkItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Online implements IListOnlyClassToArray {

        protected List<RelatedLinkItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedLinkItem }
         */
        public List<RelatedLinkItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Parent implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Related implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Service implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedSiblingMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Siblings implements IListOnlyClassToArray {

        protected List<RelatedSiblingMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link
         * RelatedSiblingMetadataItem }
         */
        public List<RelatedSiblingMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedMetadataItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    // Use different schema name for source to resolve conflict with org.fao.geonet.domain.Source
    @Schema(name = "RelatedSource")
    public static class Source implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedMetadataItem
         * }
         */
        public List<RelatedMetadataItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
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
     *         <element name="item" type="{}relatedThumbnailItem" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "item"
    })
    public static class Thumbnail implements IListOnlyClassToArray {

        protected List<RelatedThumbnailItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefor
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link RelatedThumbnailItem
         * }
         */
        public List<RelatedThumbnailItem> getItem() {
            if (item == null) {
                item = new ArrayList<>();
            }
            return this.item;
        }

    }

}
