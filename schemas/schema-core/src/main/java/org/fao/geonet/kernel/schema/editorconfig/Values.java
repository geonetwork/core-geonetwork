
package org.fao.geonet.kernel.schema.editorconfig;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *       &lt;sequence>
 *         &lt;element ref="{}key" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="readonlyIf" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "key"
})
@XmlRootElement(name = "values")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
public class Values {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    protected List<Key> key;
    @XmlAttribute(name = "readonlyIf")
    @XmlSchemaType(name = "anySimpleType")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    protected String readonlyIf;

    /**
     * Gets the value of the key property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the key property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKey().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Key }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public List<Key> getKey() {
        if (key == null) {
            key = new ArrayList<Key>();
        }
        return this.key;
    }

    /**
     * Gets the value of the readonlyIf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public String getReadonlyIf() {
        return readonlyIf;
    }

    /**
     * Sets the value of the readonlyIf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public void setReadonlyIf(String value) {
        this.readonlyIf = value;
    }

}
