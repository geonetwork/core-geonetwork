package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Access(AccessType.PROPERTY)
@Table(name="customelementset")
public class CustomElementSet {
    private int _id;
    private String _xpath;

    @Id
    public int getId() {
        return _id;
    }
    public void setId(int id) {
        this._id = id;
    }
    public String getXpath() {
        return _xpath;
    }

    public void setXpath(String xpath) {
        this._xpath = xpath;
    }
    
    
}
