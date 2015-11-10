
package org.fao.geonet.kernel.schema.editorconfig;

import java.math.BigInteger;
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
 *         &lt;element name="thesaurus" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="key" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="maxtags" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="transformations" type="{http://www.w3.org/2001/XMLSchema}string" />
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
    "thesaurus"
})
@XmlRootElement(name = "thesaurusList")
public class ThesaurusList {

    @XmlElement(required = true)
    protected List<ThesaurusList.Thesaurus> thesaurus;

    /**
     * Gets the value of the thesaurus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the thesaurus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getThesaurus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ThesaurusList.Thesaurus }
     * 
     * 
     */
    public List<ThesaurusList.Thesaurus> getThesaurus() {
        if (thesaurus == null) {
            thesaurus = new ArrayList<ThesaurusList.Thesaurus>();
        }
        return this.thesaurus;
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
     *       &lt;attribute name="key" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="maxtags" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="transformations" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Thesaurus {

        @XmlAttribute(name = "key", required = true)
        protected String key;
        @XmlAttribute(name = "maxtags")
        protected BigInteger maxtags;
        @XmlAttribute(name = "transformations")
        protected String transformations;

        /**
         * Gets the value of the key property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the value of the key property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setKey(String value) {
            this.key = value;
        }

        /**
         * Gets the value of the maxtags property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getMaxtags() {
            return maxtags;
        }

        /**
         * Sets the value of the maxtags property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setMaxtags(BigInteger value) {
            this.maxtags = value;
        }

        /**
         * Gets the value of the transformations property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTransformations() {
            return transformations;
        }

        /**
         * Sets the value of the transformations property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTransformations(String value) {
            this.transformations = value;
        }

    }

}
