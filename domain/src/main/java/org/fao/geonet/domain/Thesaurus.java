package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity indicating which thesauri are enabled.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
public class Thesaurus {
    private String _id;
    private boolean _activated;

    @Id
    public String getId() {
        return _id;
    }
    public void setId(String id) {
        this._id = id;
    }
    public boolean isActivated() {
        return _activated;
    }
    public void setActivated(boolean activated) {
        this._activated = activated;
    }
}
