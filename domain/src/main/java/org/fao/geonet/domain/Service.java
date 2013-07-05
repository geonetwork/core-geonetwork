package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * One of the entities responsible for dynamic service configuration. Entity representing a {@link jeeves.interfaces.Service}. Originally
 * they were for CSW virtual services but are generic and could in theory be any arbitrary service.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "services")
public class Service {
    private int _id;
    private String _name;
    private String _className;
    private String description;
    private List<ServiceParameter> _parameters = new ArrayList<ServiceParameter>();

    @Id
    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }
    @Column(nullable=false)
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @Column(name = "class", length = 1024, nullable = false)
    public String getClassName() {
        return _className;
    }

    public void setClassName(String className) {
        this._className = className;
    }

    @Column(length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ElementCollection(fetch = FetchType.LAZY, targetClass = ServiceParameter.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "service"), name = "serviceparameters")
    public List<ServiceParameter> getParameters() {
        return _parameters;
    }

    public void setParameters(List<ServiceParameter> parameters) {
        this._parameters = parameters;
    }

}
