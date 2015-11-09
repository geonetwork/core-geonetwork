
package org.fao.geonet.kernel.schema.editorconfig;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *       &lt;sequence>
 *         &lt;element ref="{}expanded"/>
 *         &lt;element ref="{}exclude"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "expanded",
    "exclude"
})
@XmlRootElement(name = "multilingualFields")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
public class MultilingualFields {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    protected Expanded expanded;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    protected Exclude exclude;

    /**
     * Gets the value of the expanded property.
     * 
     * @return
     *     possible object is
     *     {@link Expanded }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public Expanded getExpanded() {
        return expanded;
    }

    /**
     * Sets the value of the expanded property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expanded }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public void setExpanded(Expanded value) {
        this.expanded = value;
    }

    /**
     * Gets the value of the exclude property.
     * 
     * @return
     *     possible object is
     *     {@link Exclude }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public Exclude getExclude() {
        return exclude;
    }

    /**
     * Sets the value of the exclude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Exclude }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public void setExclude(Exclude value) {
        this.exclude = value;
    }

}
