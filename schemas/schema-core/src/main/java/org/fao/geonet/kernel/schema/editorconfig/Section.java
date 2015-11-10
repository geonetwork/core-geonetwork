
package org.fao.geonet.kernel.schema.editorconfig;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{}field"/>
 *         &lt;element ref="{}action"/>
 *         &lt;element ref="{}text"/>
 *         &lt;element ref="{}section"/>
 *         &lt;element ref="{}fieldset"/>
 *       &lt;/choice>
 *       &lt;attribute ref="{}mode"/>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="xpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{}or"/>
 *       &lt;attribute ref="{}in"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fieldOrActionOrText"
})
@XmlRootElement(name = "section")
public class Section {

    @XmlElements({
        @XmlElement(name = "field", type = Field.class),
        @XmlElement(name = "action", type = Action.class),
        @XmlElement(name = "text", type = Text.class),
        @XmlElement(name = "section", type = Section.class),
        @XmlElement(name = "fieldset", type = Fieldset.class)
    })
    protected List<Object> fieldOrActionOrText;
    @XmlAttribute(name = "mode")
    protected String mode;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "xpath")
    protected String xpath;
    @XmlAttribute(name = "or")
    protected String or;
    @XmlAttribute(name = "in")
    protected String in;

    /**
     * Gets the value of the fieldOrActionOrText property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fieldOrActionOrText property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFieldOrActionOrText().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     * {@link Action }
     * {@link Text }
     * {@link Section }
     * {@link Fieldset }
     * 
     * 
     */
    public List<Object> getFieldOrActionOrText() {
        if (fieldOrActionOrText == null) {
            fieldOrActionOrText = new ArrayList<Object>();
        }
        return this.fieldOrActionOrText;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMode() {
        if (mode == null) {
            return "flat";
        } else {
            return mode;
        }
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMode(String value) {
        this.mode = value;
    }

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
     * Gets the value of the xpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * Sets the value of the xpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXpath(String value) {
        this.xpath = value;
    }

    /**
     * Gets the value of the or property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOr() {
        return or;
    }

    /**
     * Sets the value of the or property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOr(String value) {
        this.or = value;
    }

    /**
     * Gets the value of the in property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIn(String value) {
        this.in = value;
    }

}
