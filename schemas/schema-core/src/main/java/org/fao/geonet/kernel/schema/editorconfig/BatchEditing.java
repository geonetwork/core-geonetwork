
package org.fao.geonet.kernel.schema.editorconfig;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element name="section" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="field" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="use" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="removable" type="{http://www.w3.org/2001/XMLSchema}boolean" fixed="true" />
 *                           &lt;attribute name="codelist" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "section"
})
@XmlRootElement(name = "batchEditing")
public class BatchEditing {

    @XmlElement(required = true)
    protected List<BatchEditing.Section> section;

    /**
     * Gets the value of the section property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the section property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BatchEditing.Section }
     * 
     * 
     */
    public List<BatchEditing.Section> getSection() {
        if (section == null) {
            section = new ArrayList<BatchEditing.Section>();
        }
        return this.section;
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
     *         &lt;element name="field" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="use" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="removable" type="{http://www.w3.org/2001/XMLSchema}boolean" fixed="true" />
     *                 &lt;attribute name="codelist" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "field"
    })
    public static class Section {

        @XmlElement(required = true)
        protected List<BatchEditing.Section.Field> field;
        @XmlAttribute(name = "name", required = true)
        protected String name;

        /**
         * Gets the value of the field property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the field property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getField().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BatchEditing.Section.Field }
         * 
         * 
         */
        public List<BatchEditing.Section.Field> getField() {
            if (field == null) {
                field = new ArrayList<BatchEditing.Section.Field>();
            }
            return this.field;
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
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="xpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="use" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="removable" type="{http://www.w3.org/2001/XMLSchema}boolean" fixed="true" />
         *       &lt;attribute name="codelist" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Field {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "xpath", required = true)
            protected String xpath;
            @XmlAttribute(name = "use")
            protected String use;
            @XmlAttribute(name = "removable")
            protected Boolean removable;
            @XmlAttribute(name = "codelist")
            protected String codelist;

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
             * Gets the value of the use property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUse() {
                return use;
            }

            /**
             * Sets the value of the use property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUse(String value) {
                this.use = value;
            }

            /**
             * Gets the value of the removable property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public boolean isRemovable() {
                if (removable == null) {
                    return false; // FIXME
                } else {
                    return removable;
                }
            }

            /**
             * Sets the value of the removable property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setRemovable(Boolean value) {
                this.removable = value;
            }

            /**
             * Gets the value of the codelist property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCodelist() {
                return codelist;
            }

            /**
             * Sets the value of the codelist property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCodelist(String value) {
                this.codelist = value;
            }

        }

    }

}
