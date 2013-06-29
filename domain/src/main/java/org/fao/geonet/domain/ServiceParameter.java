package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * One of the entities responsible for dynamic service configuration. 
 * The services represent the virtual services and these are the parameters
 * for the services. 
 *
 * @author Jesse
 */

@Entity
@Access(AccessType.PROPERTY)
@Table(name = "serviceparameter")
public class ServiceParameter {
    private int _id;
    private String _name;
    private String _value;

    @Id
    public int getId() {
        return _id;
    }
    public void setId(int id) {
        this._id = id;
    }
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        this._name = name;
    }
    public String getValue() {
        return _value;
    }
    public void setValue(String value) {
        this._value = value;
    }
}
