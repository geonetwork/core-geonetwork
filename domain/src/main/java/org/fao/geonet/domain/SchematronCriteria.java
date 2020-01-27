/*
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

package org.fao.geonet.domain;

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;

import javax.persistence.*;

import java.util.IdentityHashMap;
import java.util.List;

/**
 * An entity representing a schematron criteria. This is for the extended validation framework
 * ({@link Schematron}).
 *
 * @author delawen
 */
@Entity
@Table(name = "SchematronCriteria")
@Cacheable
@Access(AccessType.PROPERTY)
@SequenceGenerator(name = SchematronCriteria.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class SchematronCriteria extends GeonetEntity {
    static final String ID_SEQ_NAME = "schematron_criteria_id_seq";
    static final String EL_UI_TYPE = "uitype";
    static final String EL_UI_VALUE = "uivalue";

    private int id;
    private SchematronCriteriaType type;
    private String value;
    private String uiType;
    private String uiValue;
    private SchematronCriteriaGroup group;

    /**
     * Get the unique id for the schematron criteria object
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    /**
     * Set the unique id for the schematron criteria object
     */
    public SchematronCriteria setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        return "SchematronCriteria [id=" + id + ", type=" + type
            + ", value=" + value + "]";
    }

    /**
     * @return the type
     */
    @Column(nullable = false, name = "type")
    @Enumerated(EnumType.STRING)
    public SchematronCriteriaType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(SchematronCriteriaType type) {
        this.type = type;
    }

    /**
     * @return the value
     */
    @Column(nullable = false, name = "value")
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the <em>UI Type</em>.  The type of the criteria as reported in the UI.  This tends to be
     * more descriptive and useful for a user.
     *
     * @return the <em>UI Type</em>.  The type of the criteria as reported in the UI.  This tends to
     * be more descriptive and useful for a user.
     */
    public String getUiType() {
        return uiType;
    }

    /**
     * Set the <em>UI Type</em>.  The type of the criteria as reported in the UI.  This tends to be
     * more descriptive and useful for a user.
     *
     * @param uiType the <em>UI Type</em>.  The type of the criteria as reported in the UI.  This
     *               tends to be more descriptive and useful for a user.
     */
    public void setUiType(String uiType) {
        this.uiType = uiType;
    }

    /**
     *
     * @return
     */
    public String getUiValue() {
        return uiValue;
    }

    public void setUiValue(String uiValue) {
        this.uiValue = uiValue;
    }

    /**
     * Get the group this schematron criteria is part of.
     *
     * @return the containing group
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(referencedColumnName = "name"),
        @PrimaryKeyJoinColumn(referencedColumnName = "schematronId")
    })
    public SchematronCriteriaGroup getGroup() {
        return group;
    }

    /**
     * Set the group for this criteria.
     *
     * @param group the group to contain this criteria
     */
    public void setGroup(SchematronCriteriaGroup group) {
        this.group = group;
    }

    public boolean accepts(ApplicationContext applicationContext, int metadataId, Element metadata, List<Namespace> metadataNamespaces) {
        return getType().accepts(applicationContext, getValue(), metadataId, metadata, metadataNamespaces);
    }

    public boolean accepts(ApplicationContext applicationContext, Element metadata, List<Namespace> metadataNamespaces, Integer groupOwnerId) {
        return getType().accepts(applicationContext, getValue(), metadata, metadataNamespaces, groupOwnerId);
    }

    @Override
    protected Element asXml(IdentityHashMap<Object, Void> alreadyEncoded) {
        final Element element = super.asXml(alreadyEncoded);
        String valueElText = element.getChildText("value");
        if (valueElText.length() == 0) {
            element.getChild("value").setText("''");
        }
        if (element.getChild(EL_UI_TYPE) == null) {
            element.addContent(new Element(EL_UI_TYPE).setText(getType().toString()));
        }
        if (element.getChild(EL_UI_VALUE) == null) {
            element.addContent(new Element(EL_UI_VALUE).setText(getValue()));
        }
        return element;
    }

    /**
     * Create a copy of the c
     *
     * @return
     */
    public SchematronCriteria copy() {
        SchematronCriteria copy = new SchematronCriteria();
        copy.setType(getType());
        copy.setUiType(getUiType());
        copy.setValue(getValue());
        copy.setUiValue(getUiValue());
        copy.setGroup(getGroup());
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchematronCriteria that = (SchematronCriteria) o;

        if (id != that.id) return false;
        if (type != that.type) return false;
        if (uiType != null ? !uiType.equals(that.uiType) : that.uiType != null) return false;
        if (uiValue != null ? !uiValue.equals(that.uiValue) : that.uiValue != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (uiType != null ? uiType.hashCode() : 0);
        result = 31 * result + (uiValue != null ? uiValue.hashCode() : 0);
        return result;
    }
}
