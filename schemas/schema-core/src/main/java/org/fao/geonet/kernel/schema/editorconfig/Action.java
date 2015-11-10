
package org.fao.geonet.kernel.schema.editorconfig;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}template" minOccurs="0"/>
 *         &lt;element ref="{}directiveAttributes" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="batch"/>
 *             &lt;enumeration value="add"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="process" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="forceLabel" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute ref="{}or"/>
 *       &lt;attribute ref="{}in"/>
 *       &lt;attribute ref="{}addDirective"/>
 *       &lt;attribute name="if" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "template",
    "directiveAttributes"
})
@XmlRootElement(name = "action")
public class Action {

    protected Template template;
    protected DirectiveAttributes directiveAttributes;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "process")
    protected String process;
    @XmlAttribute(name = "forceLabel")
    protected Boolean forceLabel;
    @XmlAttribute(name = "or")
    protected String or;
    @XmlAttribute(name = "in")
    protected String in;
    @XmlAttribute(name = "addDirective")
    protected String addDirective;
    @XmlAttribute(name = "if")
    protected String _if;

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link Template }
     *     
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link Template }
     *     
     */
    public void setTemplate(Template value) {
        this.template = value;
    }

    /**
     * Gets the value of the directiveAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link DirectiveAttributes }
     *     
     */
    public DirectiveAttributes getDirectiveAttributes() {
        return directiveAttributes;
    }

    /**
     * Sets the value of the directiveAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectiveAttributes }
     *     
     */
    public void setDirectiveAttributes(DirectiveAttributes value) {
        this.directiveAttributes = value;
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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
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
     * Gets the value of the forceLabel property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForceLabel() {
        return forceLabel;
    }

    /**
     * Sets the value of the forceLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForceLabel(Boolean value) {
        this.forceLabel = value;
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

    /**
     * Gets the value of the addDirective property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddDirective() {
        return addDirective;
    }

    /**
     * Sets the value of the addDirective property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddDirective(String value) {
        this.addDirective = value;
    }

    /**
     * Gets the value of the if property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIf() {
        return _if;
    }

    /**
     * Sets the value of the if property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIf(String value) {
        this._if = value;
    }

}
