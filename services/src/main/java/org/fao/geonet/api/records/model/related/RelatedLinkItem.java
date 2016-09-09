package org.fao.geonet.api.records.model.related;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relatedLinkItem complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="relatedLinkItem">
 *   &lt;complexContent>
 *     &lt;extension base="{}relatedItem">
 *       &lt;sequence>
 *         &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
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
 *         &lt;element name="function" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *         &lt;element name="applicationProfile" type="{http://www.w3.org/2001/XMLSchema}string"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedLinkItem", propOrder = {
    "protocol",
    "description",
    "function",
    "applicationProfile"
})
public class RelatedLinkItem
    extends RelatedItem {

    protected String protocol;
    @XmlElement(required = true)
    protected RelatedLinkItem.Description description;
    protected String function;
    protected String applicationProfile;

    /**
     * Gets the value of the protocol property.
     *
     * @return possible object is {@link String }
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     *
     * @param value allowed object is {@link String }
     */
    public void setProtocol(String value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link RelatedLinkItem.Description }
     */
    public RelatedLinkItem.Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link RelatedLinkItem.Description }
     */
    public void setDescription(RelatedLinkItem.Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the function property.
     *
     * @return possible object is {@link String }
     */
    public String getFunction() {
        return function;
    }

    /**
     * Sets the value of the function property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFunction(String value) {
        this.function = value;
    }

    /**
     * Gets the value of the applicationProfile property.
     *
     * @return possible object is {@link String }
     */
    public String getApplicationProfile() {
        return applicationProfile;
    }

    /**
     * Sets the value of the applicationProfile property.
     *
     * @param value allowed object is {@link String }
     */
    public void setApplicationProfile(String value) {
        this.applicationProfile = value;
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
