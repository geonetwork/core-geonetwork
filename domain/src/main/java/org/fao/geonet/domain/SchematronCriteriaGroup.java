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

import javax.annotation.Nonnull;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesse on 2/6/14.
 */
@Entity
@Table(name = "SchematronCriteriaGroup")
@Cacheable
@Access(AccessType.PROPERTY)
public class SchematronCriteriaGroup extends GeonetEntity {
    private SchematronCriteriaGroupId id;
    private List<SchematronCriteria> criteria = new ArrayList<SchematronCriteria>();
    private SchematronRequirement requirement;
    private Schematron schematron;


    /**
     * Id object.
     *
     * @return id object
     */
    @EmbeddedId
    public SchematronCriteriaGroupId getId() {
        return id;
    }

    /**
     * Set the id object.
     *
     * @param id the id.
     */
    public SchematronCriteriaGroup setId(SchematronCriteriaGroupId id) {
        this.id = id;
        return this;
    }

    /**
     * Get the schematron criteria that of this group.
     *
     * @return the schematron criteria that of this group.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "group", orphanRemoval = true)
    public List<SchematronCriteria> getCriteria() {
        return criteria;
    }

    /**
     * Set the schematron criteria that of this group.
     * <p/>
     * Use {@link #addCriteria(SchematronCriteria)} for adding criteria to this group rather than
     * adding the criteria to this list.
     *
     * @param criteria the schematron criteria that of this group.
     */
    public void setCriteria(List<SchematronCriteria> criteria) {
        this.criteria = criteria;
    }

    /**
     * Get the requirement value if this criteria group is applicable for the metadata.
     *
     * @return the requirement.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public SchematronRequirement getRequirement() {
        return requirement;
    }

    /**
     * Set the level requirement for this group.
     *
     * @return this entity
     */
    public SchematronCriteriaGroup setRequirement(SchematronRequirement requirement) {
        this.requirement = requirement;
        return this;
    }

    /**
     * Get the schematron this group applies to.
     *
     * @return the schematron
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "schematronId", nullable = false, updatable = false, insertable = false)
    public Schematron getSchematron() {
        return schematron;
    }

    /**
     * Set the schematron this group applies to.
     *
     * @param schematron the schematron to set
     * @return this entity
     */
    public SchematronCriteriaGroup setSchematron(Schematron schematron) {
        this.schematron = schematron;
        if (schematron != null) {
            SchematronCriteriaGroupId id = getId();
            if (id == null) {
                id = new SchematronCriteriaGroupId();
                setId(id);
            }
            id.setSchematronId(schematron.getId());
        }
        return this;
    }

    /**
     * Set the group on the criteria object and add to the list of criteria.
     *
     * @param schematronCriteria the criteria to add to this group.
     * @return this entity
     */
    public SchematronCriteriaGroup addCriteria(SchematronCriteria schematronCriteria) {
        schematronCriteria.setGroup(this);
        getCriteria().add(schematronCriteria);
        return this;
    }
}
