
package org.fao.geonet.kernel.schema.editorconfig;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;anyAttribute processContents='skip'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "directiveAttributes")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
public class DirectiveAttributes {

    @XmlAnyAttribute
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-11-09T11:51:19+01:00", comments = "JAXB RI v2.2.4-2")
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
