package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;

/**
 * One of the entities responsible for dynamic service configuration. The services represent the virtual services and these are the
 * parameters for the services.
 * 
 * @author Jesse
 */

@Embeddable
@Access(AccessType.PROPERTY)
public class ServiceParameter implements Serializable {
    private static final long serialVersionUID = -4773637291731084291L;
    private int _id;
    private String _name;
    private String _value;

    /**
     * Get the id of the parameter entity. This is a generated value and as such new instances should not have this set as it will simply be
     * ignored and could result in reduced performance.
     * 
     * @return the id of the parameter entity.
     */
    @GeneratedValue
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the parameter entity. This is a generated value and as such new instances should not have this set as it will simply be
     * ignored and could result in reduced performance.
     *
     * @param id the id of the parameter entity.
     */
    public ServiceParameter setId(int id) {
        this._id = id;
        return this;
    }

    /**
     * Get the parameter name. This is a required property.
     * 
     * @return the parameter name.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the parameter name. This is a required property.
     *
     * @param name the parameter name.
     */
    public ServiceParameter setName(String name) {
        this._name = name;
        return this;
    }

    /**
     * Get the parameter value. This is a required property.
     * 
     * @return the parameter value.
     */
    @Column(length = 1024, nullable = false)
    public String getValue() {
        return _value;
    }

    /**
     * Set the parameter value. This is a required property.
     *
     * @param value the parameter value.
     */
    public ServiceParameter setValue(String value) {
        this._value = value;
        return this;
    }
}
