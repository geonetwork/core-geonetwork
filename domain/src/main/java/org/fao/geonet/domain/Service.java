package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


/**
 * One of the entities responsible for dynamic service configuration. 
 * Entity representing a {@link jeeves.interfaces.Service}.  Originally
 * they were for CSW virtual services but are generic and could in theory
 * be any arbitrary service.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
public class Service {
    private int _id;
    private String _name;
    private String _className;
    private List<ServiceParameter> _parameters = new ArrayList<ServiceParameter>();

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
    @Column(name="class")
    public String getClassName() {
        return _className;
    }
    public void setClassName(String className) {
        this._className = className;
    }
    @ManyToOne(cascade=CascadeType.ALL)
    public List<ServiceParameter> getParameters() {
        return _parameters;
    }
    public void setParameters(List<ServiceParameter> parameters) {
        this._parameters = parameters;
    }
    
    
}
