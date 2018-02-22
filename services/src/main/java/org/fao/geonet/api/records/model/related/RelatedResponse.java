package org.fao.geonet.api.records.model.related;


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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

    protected RelatedResponse.Children children;
    protected RelatedResponse.Parent parent;
    protected RelatedResponse.Siblings siblings;
    protected RelatedResponse.Associated associated;
    protected RelatedResponse.Service services;
    protected RelatedResponse.Dataset datasets;
    protected RelatedResponse.Fcat fcats;
    protected RelatedResponse.Hasfeaturecats hasfeaturecats;
    protected RelatedResponse.Source sources;
    protected RelatedResponse.Hassource hassources;
    protected RelatedResponse.Related related;
    protected RelatedResponse.Online onlines;
    protected RelatedResponse.Thumbnail thumbnails;

    /**
     * Gets the value of the children property.
     *
     * @return possible object is {@link RelatedResponse.Children }
     */
    public RelatedResponse.Children getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     *
     * @param value allowed object is {@link RelatedResponse.Children }
     */
    public void setChildren(RelatedResponse.Children value) {
        this.children = value;
    }

    /**
     * Gets the value of the parent property.
     *
     * @return possible object is {@link RelatedResponse.Parent }
     */
    public RelatedResponse.Parent getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     *
     * @param value allowed object is {@link RelatedResponse.Parent }
     */
    public void setParent(RelatedResponse.Parent value) {
        this.parent = value;
    }

    /**
     * Gets the value of the siblings property.
     *
     * @return possible object is {@link RelatedResponse.Siblings }
     */
    public RelatedResponse.Siblings getSiblings() {
        return siblings;
    }

    /**
     * Sets the value of the siblings property.
     *
     * @param value allowed object is {@link RelatedResponse.Siblings }
     */
    public void setSiblings(RelatedResponse.Siblings value) {
        this.siblings = value;
    }

    /**
     * Gets the value of the associated property.
     *
     * @return possible object is {@link RelatedResponse.Associated }
     */
    public RelatedResponse.Associated getAssociated() {
        return associated;
    }

    /**
     * Sets the value of the associated property.
     *
     * @param value allowed object is {@link RelatedResponse.Associated }
     */
    public void setAssociated(RelatedResponse.Associated value) {
        this.associated = value;
    }

    /**
     * Gets the value of the service property.
     *
     * @return possible object is {@link RelatedResponse.Service }
     */
    public RelatedResponse.Service getServices() {
        return services;
    }

    /**
     * Sets the value of the service property.
     *
     * @param value allowed object is {@link RelatedResponse.Service }
     */
    public void setServices(RelatedResponse.Service value) {
        this.services = value;
    }

    /**
     * Gets the value of the dataset property.
     *
     * @return possible object is {@link RelatedResponse.Dataset }
     */
    public RelatedResponse.Dataset getDatasets() {
        return datasets;
    }

    /**
     * Sets the value of the dataset property.
     *
     * @param value allowed object is {@link RelatedResponse.Dataset }
     */
    public void setDatasets(RelatedResponse.Dataset value) {
        this.datasets = value;
    }

    /**
     * Gets the value of the fcat property.
     *
     * @return possible object is {@link RelatedResponse.Fcat }
     */
    public RelatedResponse.Fcat getFcats() {
        return fcats;
    }

    /**
     * Sets the value of the fcat property.
     *
     * @param value allowed object is {@link RelatedResponse.Fcat }
     */
    public void setFcats(RelatedResponse.Fcat value) {
        this.fcats = value;
    }

    /**
     * Gets the value of the hasfeaturecats property.
     *
     * @return possible object is {@link RelatedResponse.Hasfeaturecats }
     */
    public Hasfeaturecats getHasfeaturecats() {
        return hasfeaturecats;
    }

    /**
     * Sets the value of the hasfeaturecats property.
     *
     * @param value allowed object is {@link RelatedResponse.Hasfeaturecats }
     */
    public void setHasfeaturecats(RelatedResponse.Hasfeaturecats value) {
        this.hasfeaturecats = value;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link RelatedResponse.Source }
     */
    public RelatedResponse.Source getSources() {
        return sources;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link RelatedResponse.Source }
     */
    public void setSources(RelatedResponse.Source value) {
        this.sources = value;
    }

    /**
     * Gets the value of the hassource property.
     *
     * @return possible object is {@link RelatedResponse.Hassource }
     */
    public RelatedResponse.Hassource getHassources() {
        return hassources;
    }

    /**
     * Sets the value of the hassource property.
     *
     * @param value allowed object is {@link RelatedResponse.Hassource }
     */
    public void setHassources(RelatedResponse.Hassource value) {
        this.hassources = value;
    }

    /**
     * Gets the value of the related property.
     *
     * @return possible object is {@link RelatedResponse.Related }
     */
    public RelatedResponse.Related getRelated() {
        return related;
    }

    /**
     * Sets the value of the related property.
     *
     * @param value allowed object is {@link RelatedResponse.Related }
     */
    public void setRelated(RelatedResponse.Related value) {
        this.related = value;
    }

    /**
     * Gets the value of the online property.
     *
     * @return possible object is {@link RelatedResponse.Online }
     */
    public RelatedResponse.Online getOnlines() {
        return onlines;
    }

    /**
     * Sets the value of the online property.
     *
     * @param value allowed object is {@link RelatedResponse.Online }
     */
    public void setOnlines(RelatedResponse.Online value) {
        this.onlines = value;
    }

    /**
     * Gets the value of the thumbnail property.
     *
     * @return possible object is {@link RelatedResponse.Thumbnail }
     */
    public RelatedResponse.Thumbnail getThumbnails() {
        return thumbnails;
    }

    /**
     * Sets the value of the thumbnail property.
     *
     * @param value allowed object is {@link RelatedResponse.Thumbnail }
     */
    public void setThumbnails(RelatedResponse.Thumbnail value) {
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<AssociatedSiblingMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<FCRelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedLinkItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedSiblingMetadataItem>();
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
    public static class Source implements IListOnlyClassToArray {

        protected List<RelatedMetadataItem> item;

        /**
         * Gets the value of the item property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedMetadataItem>();
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
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
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
                item = new ArrayList<RelatedThumbnailItem>();
            }
            return this.item;
        }

    }

}
