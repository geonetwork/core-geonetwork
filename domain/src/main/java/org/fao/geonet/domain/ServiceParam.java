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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * One of the entities responsible for dynamic service configuration.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "ServiceParameters")
@SequenceGenerator(name = ServiceParam.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class ServiceParam extends GeonetEntity {
    static final String ID_SEQ_NAME = "serviceparameters_id_seq";
    private static final List<Character> LEGALVALUES = Lists.newArrayList('+', '-', ' ', null);
    private int id;
    private Service service;
    private String name;
    private String value;
    private Character occur = '+';

    public ServiceParam() {
        // for JPA
    }

    public ServiceParam(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the name of the service.
     *
     * @return the name of the service.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of the service.
     *
     * @param name the name of the service.
     */
    public ServiceParam setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the parameter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the parameter value
     */
    public ServiceParam setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the request associated with this entity.
     *
     * @return the request associated with this entity.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "id", name = "service")
    public Service getService() {
        return this.service;
    }

    /**
     * Set the request associated with this entity.
     *
     * @param service the request associated with this entity.
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * Get the "occur" property which determines how this clause affects the filtering. The allowed
     * options are '+', '-', and ' '.  Where <ul> <li>'+' means that the clause is required for a
     * result to be included</li> <li>' ' means that the clause is should true, this has the effect
     * of increasing relevancy of a particular result in the overall result set</li> <li>'-' means
     * that the clause is must be false for a result to be included</li> </ul>
     */
    public Character getOccur() {
        if (this.occur == null) {
            return '+';
        }
        return occur;
    }

    /**
     * Set occur property.
     *
     * @param occur the new value.  Legal values: '+', ' ', '-'
     */
    public void setOccur(Character occur) {
        if (!LEGALVALUES.contains(occur)) {
            throw new IllegalArgumentException(occur + " is not a legal occur value.  Legal values are: " + LEGALVALUES);
        }
        this.occur = occur;
    }
}

