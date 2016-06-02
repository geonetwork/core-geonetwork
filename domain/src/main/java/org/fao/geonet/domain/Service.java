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

import com.google.common.collect.Lists;

import net.sf.json.JSONArray;

import org.fao.geonet.entitylistener.ServiceEntityListenerManager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * One of the entities responsible for dynamic service configuration. Entity representing a {@link
 * jeeves.interfaces.Service}. Originally they were for CSW virtual services but are generic and
 * could in theory be any arbitrary service.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Services")
@EntityListeners(ServiceEntityListenerManager.class)
@SequenceGenerator(name = Service.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class Service extends GeonetEntity {
    static final String ID_SEQ_NAME = "service_id_seq";
    private int _id;
    private String _name;
    private String _className;
    private String description;
    private String explicitQuery = "";
    private List<ServiceParam> _parameters = new ArrayList<>();

    /**
     * Get the id of the service entity. This is a generated value and as such new instances should
     * not have this set as it will simply be ignored and could result in reduced performance.
     *
     * @return the id of the service.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the service entity. This is a generated value and as such new instances should
     * not have this set as it will simply be ignored and could result in reduced performance.
     *
     * @param id the id of the service entity.
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Get the name of the service.
     *
     * @return the name of the service.
     */
    @Column(nullable = false, unique = true)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the service.
     *
     * @param name the name of the service.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Get the service class name.
     *
     * @return the service class name.
     */
    @Column(name = "class", length = 1024, nullable = false)
    public String getClassName() {
        return _className;
    }

    /**
     * Set the service class name.
     *
     * @param className the service class name.
     */
    public void setClassName(String className) {
        this._className = className;
    }

    /**
     * Get the description of the service. Maximum length is 1024 characters.
     *
     * @return the description of the service. Maximum length is 1024 characters.
     */
    @Column(length = 1024)
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the service. Maximum length is 1024 characters.
     *
     * @param description the description of the service. Maximum length is 1024 characters.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the init parameters to pass to the service.  The Key is the parameter name the value is
     * the parameter value.
     *
     * @return the init parameters to pass to the service.  The Key is the parameter name the value
     * is the parameter value.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "service", orphanRemoval = true)
    public List<ServiceParam> getParameters() {
        return _parameters;
    }

    /**
     * Set the init parameters to pass to the service. The Key is the parameter name the value is
     * the parameter value.
     *
     * @param parameters the init parameters to pass to the service.  The Key is the parameter name
     *                   the value is the parameter value.
     */
    public void setParameters(final List<ServiceParam> parameters) {
        this._parameters = parameters;
    }

    /**
     * Add a parameter to the set of parameters.  This method sets "this" on the parameter so that
     * when this service is saved the parameter will also be saved.
     *
     * @param param the parameter to add.
     */
    public Service addParameter(ServiceParam param) {
        if (_parameters == null) {
            _parameters = Lists.newArrayList();
        }
        param.setService(this);
        _parameters.add(param);
        return this;
    }

    /**
     * Remove a parameter from the service and set null on the parameter's service property.
     *
     * @param param the parameter to add.
     */
    public boolean removeParameter(ServiceParam param) {
        if (_parameters == null) {
            return false;
        }
        param.setService(null);
        return _parameters.remove(param);
    }

    /**
     * Remove all parameters and set null on all parameter's service properties.
     */
    public void clearParameters() {
        for (ServiceParam parameter : _parameters) {
            parameter.setService(null);
        }
        _parameters.clear();
    }

    /**
     * Get an arbitrary query string that will be added to query created from the {@link
     * ServiceParam}.  This allows complex queries to be constructed, which are impossible to
     * construct only using the normal {@link ServiceParam}.  It is recommended to only use this as
     * a last resort as it is less portable than service parameters.
     */
    @Nonnull
    public String getExplicitQuery() {
        return explicitQuery == null ? "" : explicitQuery;
    }

    /**
     * Set an arbitrary query string that will be 'ANDed' to the the {@link ServiceParam}.  This
     * allows complex queries to be constructed, which are impossible to construct only using the
     * normal {@link ServiceParam}.  It is recommended to only use this as a last resort as it is
     * less portable than service parameters.
     *
     * @param explicitQuery the lucene query
     */
    public void setExplicitQuery(String explicitQuery) {
        this.explicitQuery = explicitQuery;
    }
}
