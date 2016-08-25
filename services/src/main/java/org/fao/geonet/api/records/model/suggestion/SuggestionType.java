
package org.fao.geonet.api.records.model.suggestion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for suggestionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="suggestionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Keyword field contains place keywords (ie.World). Try to compute metadata extent using thesaurus."/>
 *               &lt;enumeration value="Current record does not contain resource identifier. Add the       following identifier:       http://localhost:8080/geonetwork/srv/metadata/8f089c32-d0ca-4af1-a56a-4cbf34ddcc75."/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="operational" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="params" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="process" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="category" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="target" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "suggestionType", propOrder = {
    "name",
    "operational",
    "params"
})
public class SuggestionType {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String operational;
    protected String params;
    @XmlAttribute(name = "process")
    protected String process;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "category")
    protected String category;
    @XmlAttribute(name = "target")
    protected String target;

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
     * Gets the value of the operational property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getOperational() {
        return operational;
    }

    /**
     * Sets the value of the operational property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setOperational(String value) {
        this.operational = value;
    }

    /**
     * Gets the value of the params property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getParams() {
        return params;
    }

    /**
     * Sets the value of the params property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setParams(String value) {
        this.params = value;
    }

    /**
     * Gets the value of the process property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getProcess() {
        return process;
    }

    /**
     * Sets the value of the process property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setProcess(String value) {
        this.process = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the category property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCategory(String value) {
        this.category = value;
    }

    /**
     * Gets the value of the target property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTarget(String value) {
        this.target = value;
    }

}
