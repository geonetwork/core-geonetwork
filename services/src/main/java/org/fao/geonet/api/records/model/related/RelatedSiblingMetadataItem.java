package org.fao.geonet.api.records.model.related;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relatedSiblingMetadataItem complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="relatedSiblingMetadataItem">
 *   &lt;complexContent>
 *     &lt;extension base="{}relatedMetadataItem">
 *       &lt;sequence>
 *         &lt;element name="associationType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="initiativeType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedSiblingMetadataItem", propOrder = {
    "associationType",
    "initiativeType"
})
public class RelatedSiblingMetadataItem
    extends RelatedMetadataItem {

    @XmlElement(required = true)
    protected String associationType;
    @XmlElement(required = true)
    protected String initiativeType;

    /**
     * Gets the value of the associationType property.
     *
     * @return possible object is {@link String }
     */
    public String getAssociationType() {
        return associationType;
    }

    /**
     * Sets the value of the associationType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAssociationType(String value) {
        this.associationType = value;
    }

    /**
     * Gets the value of the initiativeType property.
     *
     * @return possible object is {@link String }
     */
    public String getInitiativeType() {
        return initiativeType;
    }

    /**
     * Sets the value of the initiativeType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setInitiativeType(String value) {
        this.initiativeType = value;
    }

}
