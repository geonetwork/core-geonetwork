package org.fao.geonet.api.records.model.related;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for relatedItem complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="relatedItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="title">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="values" type="{}localizedString" maxOccurs="unbounded"
 * minOccurs="0"/>
 *                 &lt;/sequence>
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
@XmlType(name = "relatedItem", propOrder = {
    "id",
    "idx",
    "hash",
    "url",
    "title",
    "type"
})
@XmlSeeAlso({
    RelatedThumbnailItem.class,
    RelatedMetadataItem.class,
    RelatedLinkItem.class
})
public abstract class RelatedItem {

    protected String id;
    protected String idx;
    protected String hash;
    protected MultilingualValue url;
    protected String type;

    @XmlElement(required = true)
    protected MultilingualValue title;

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
     * Gets the value of the idx property.
     *
     * @return possible object is {@link String }
     */
    public String getIdx() {
        return idx;
    }

    /**
     * Sets the value of the idx property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIdx(String value) {
        this.idx = value;
    }

    /**
     * Gets the value of the hasg property.
     *
     * @return possible object is {@link String }
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets the value of the hash property.
     *
     * @param value allowed object is {@link String }
     */
    public void setHash(String value) {
        this.hash = value;
    }

    /**
     * Gets the value of the url property.
     *
     * @return possible object is {@link MultilingualValue }
     */
    public MultilingualValue getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value allowed object is {@link MultilingualValue }
     */
    public void setUrl(MultilingualValue value) {
        this.url = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link String }
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the title property.
     *
     * @return possible object is {@link MultilingualValue }
     */
    public MultilingualValue getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     *
     * @param value allowed object is {@link MultilingualValue }
     */
    public void setTitle(MultilingualValue value) {
        this.title = value;
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
     *         &lt;element name="values" type="{}localizedString" maxOccurs="unbounded"
     * minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    @JsonSerialize(using = LocalizedStringSerializer.class)
    public static class MultilingualValue implements ILocalizedStringProperty {
        protected List<LocalizedString> value;

        /**
         * Gets the value of the values property.
         *
         * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore
         * any modification you make to the returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the values property.
         *
         * <p> For example, to add a new item, do as follows:
         * <pre>
         *    getValues().add(newItem);
         * </pre>
         *
         *
         * <p> Objects of the following type(s) are allowed in the list {@link LocalizedString }
         */
        public List<LocalizedString> getValue() {
            if (value == null) {
                value = new ArrayList<LocalizedString>();
            }
            return this.value;
        }

    }

}
