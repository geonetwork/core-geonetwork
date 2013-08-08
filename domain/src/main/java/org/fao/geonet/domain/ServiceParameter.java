package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

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

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @Column(length = 1024, nullable = false)
    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        this._value = value;
    }
}
