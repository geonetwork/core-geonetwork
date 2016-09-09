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

import javax.persistence.Column;
import javax.persistence.Embeddable;

import java.io.Serializable;

/**
 * Id object of a {@link org.fao.geonet.domain.SchematronCriteriaGroup} entity. Created by Jesse on
 * 2/10/14.
 */
@Embeddable
public class SchematronCriteriaGroupId implements Serializable {
    private String name;
    private int schematronId;

    public SchematronCriteriaGroupId() {
        // needed for JPA
    }

    public SchematronCriteriaGroupId(String groupName, Schematron schematron) {
        this(groupName, schematron.getId());
    }

    public SchematronCriteriaGroupId(String groupName, int schematronId) {
        this.name = groupName;
        this.schematronId = schematronId;
    }

    /**
     * Get the name/id of this group. This is only shown to the administrator
     */
    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    /**
     * Set the name/id of this group.
     *
     * @param name the group name/id
     * @return this entity
     */
    public SchematronCriteriaGroupId setName(String name) {
        this.name = name;
        return this;
    }

    @Column(name = "schematronId")
    public int getSchematronId() {
        return schematronId;
    }

    /**
     * Return the id of the associated schematron.
     *
     * @param schematronId id of the associated schematron.
     * @return this id class
     */
    public SchematronCriteriaGroupId setSchematronId(int schematronId) {
        this.schematronId = schematronId;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchematronCriteriaGroupId that = (SchematronCriteriaGroupId) o;

        if (schematronId != that.schematronId) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + schematronId;
        return result;
    }

    @Override
    public String toString() {
        return "SchematronCriteriaGroupId{" +
            "name='" + name + '\'' +
            ", schematronId=" + schematronId +
            '}';
    }
}
