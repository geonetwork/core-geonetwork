/*
 *
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.schema.editorconfig;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{}directiveAttributes" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *       &lt;attribute ref="{}use"/>
 *       &lt;attribute ref="{}addDirective"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "directiveAttributes"
})
@XmlRootElement(name = "for")
public class For {

    protected DirectiveAttributes directiveAttributes;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "use")
    protected String use;
    @XmlAttribute(name = "addDirective")
    protected String addDirective;

    /**
     * Gets the value of the directiveAttributes property.
     *
     * @return possible object is {@link DirectiveAttributes }
     */
    public DirectiveAttributes getDirectiveAttributes() {
        return directiveAttributes;
    }

    /**
     * Sets the value of the directiveAttributes property.
     *
     * @param value allowed object is {@link DirectiveAttributes }
     */
    public void setDirectiveAttributes(DirectiveAttributes value) {
        this.directiveAttributes = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the use property.
     *
     * @return possible object is {@link String }
     */
    public String getUse() {
        return use;
    }

    /**
     * Sets the value of the use property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUse(String value) {
        this.use = value;
    }

    /**
     * Gets the value of the addDirective property.
     *
     * @return possible object is {@link String }
     */
    public String getAddDirective() {
        return addDirective;
    }

    /**
     * Sets the value of the addDirective property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAddDirective(String value) {
        this.addDirective = value;
    }

}
