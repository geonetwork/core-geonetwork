package org.fao.geonet.api.records.model.related;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relatedMetadataItem complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="relatedMetadataItem">
 *   &lt;complexContent>
 *     &lt;extension base="{}relatedItem">
 *       &lt;sequence>
 *         &lt;element name="description">
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
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedMetadataItem", propOrder = {
    "description",
    "mdType"
})
@XmlSeeAlso({
    RelatedSiblingMetadataItem.class
})
public class RelatedMetadataItem
    extends RelatedItem {
    @XmlElement(required = true)
    protected RelatedMetadataItem.Description description;

    @XmlElement(required = true)
    protected String[] mdType;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link RelatedMetadataItem.Description }
     */
    public RelatedMetadataItem.Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link RelatedMetadataItem.Description }
     */
    public void setDescription(RelatedMetadataItem.Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the mdType property.
     *
     * @return possible object is {@link String }
     */
    public String[] getMdType() {
        return mdType;
    }

    /**
     * Sets the value of the mdType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMdType(String[] value) {
        this.mdType = value;
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
    public static class Description implements ILocalizedStringProperty {

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
