package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Csw custom element set.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="customelementset")
public class CustomElementSet {
    private String _xpath;

    @Id
    @Column(length=1000)
    public String getXpath() {
        return _xpath;
    }

    public void setXpath(String xpath) {
        this._xpath = xpath;
    }
    
    
}
