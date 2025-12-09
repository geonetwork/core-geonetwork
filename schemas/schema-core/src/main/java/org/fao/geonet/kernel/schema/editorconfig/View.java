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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element ref="{}tab" maxOccurs="unbounded"/>
 *         &lt;element ref="{}flatModeExceptions" minOccurs="0"/>
 *         &lt;element ref="{}thesaurusList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string"
 * />
 *       &lt;attribute name="disabled" type="{http://www.w3.org/2001/XMLSchema}boolean" fixed="true"
 * />
 *       &lt;attribute name="upAndDownControlHidden" type="{http://www.w3.org/2001/XMLSchema}boolean"
 * fixed="true" />
 *       &lt;attribute name="displayIfRecord" type="{http://www.w3.org/2001/XMLSchema}anySimpleType"
 * />
 *       &lt;attribute name="displayIfServiceInfo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType"
 * />
 *       &lt;attribute name="hideTimeInCalendar" type="{http://www.w3.org/2001/XMLSchema}boolean"
 * fixed="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "tab",
    "flatModeExceptions",
    "thesaurusList"
})
@XmlRootElement(name = "view")
public class View {

    @XmlElement(required = true)
    protected List<Tab> tab;
    protected FlatModeExceptions flatModeExceptions;
    protected ThesaurusList thesaurusList;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "disabled")
    protected Boolean disabled;
    @XmlAttribute(name = "upAndDownControlHidden")
    protected Boolean upAndDownControlHidden;
    @XmlAttribute(name = "displayIfRecord")
    @XmlSchemaType(name = "anySimpleType")
    protected String displayIfRecord;
    @XmlAttribute(name = "displayIfServiceInfo")
    @XmlSchemaType(name = "anySimpleType")
    protected String displayIfServiceInfo;
    @XmlAttribute(name = "hideTimeInCalendar")
    protected Boolean hideTimeInCalendar;

    /**
     * A tab is composed of a set of elements. Gets the value of the tab property.
     *
     * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the tab property.
     *
     * <p> For example, to add a new item, do as follows:
     * <pre>
     *    getTab().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list {@link Tab }
     */
    public List<Tab> getTab() {
        if (tab == null) {
            tab = new ArrayList<Tab>();
        }
        return this.tab;
    }

    /**
     * Gets the value of the flatModeExceptions property.
     *
     * @return possible object is {@link FlatModeExceptions }
     */
    public FlatModeExceptions getFlatModeExceptions() {
        return flatModeExceptions;
    }

    /**
     * Sets the value of the flatModeExceptions property.
     *
     * @param value allowed object is {@link FlatModeExceptions }
     */
    public void setFlatModeExceptions(FlatModeExceptions value) {
        this.flatModeExceptions = value;
    }

    /**
     * Gets the value of the thesaurusList property.
     *
     * @return possible object is {@link ThesaurusList }
     */
    public ThesaurusList getThesaurusList() {
        return thesaurusList;
    }

    /**
     * Sets the value of the thesaurusList property.
     *
     * @param value allowed object is {@link ThesaurusList }
     */
    public void setThesaurusList(ThesaurusList value) {
        this.thesaurusList = value;
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
     * Gets the value of the disabled property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isDisabled() {
        if (disabled == null) {
            return true;
        } else {
            return disabled;
        }
    }

    /**
     * Sets the value of the disabled property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setDisabled(Boolean value) {
        this.disabled = value;
    }

    /**
     * Gets the value of the upAndDownControlHidden property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isUpAndDownControlHidden() {
        if (upAndDownControlHidden == null) {
            return true;
        } else {
            return upAndDownControlHidden;
        }
    }

    /**
     * Sets the value of the upAndDownControlHidden property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setUpAndDownControlHidden(Boolean value) {
        this.upAndDownControlHidden = value;
    }

    /**
     * Gets the value of the displayIfRecord property.
     *
     * @return possible object is {@link String }
     */
    public String getDisplayIfRecord() {
        return displayIfRecord;
    }

    /**
     * Sets the value of the displayIfRecord property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDisplayIfRecord(String value) {
        this.displayIfRecord = value;
    }

    /**
     * Gets the value of the displayIfServiceInfo property.
     *
     * @return possible object is {@link String }
     */
    public String getDisplayIfServiceInfo() {
        return displayIfServiceInfo;
    }

    /**
     * Sets the value of the displayIfServiceInfo property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDisplayIfServiceInfo(String value) {
        this.displayIfServiceInfo = value;
    }

    /**
     * Gets the value of the hideTimeInCalendar property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isHideTimeInCalendar() {
        if (hideTimeInCalendar == null) {
            return true;
        } else {
            return hideTimeInCalendar;
        }
    }

    /**
     * Sets the value of the hideTimeInCalendar property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setHideTimeInCalendar(Boolean value) {
        this.hideTimeInCalendar = value;
    }

}
