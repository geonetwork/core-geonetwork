package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity indicating which thesauri are enabled.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="thesaurus")
public class ThesaurusActivation {
    private String _id;
    private char _activated = 'y';

    @Id
    public String getId() {
        return _id;
    }
    public void setId(String id) {
        this._id = id;
    }
    /**
     * For backwards compatibility we need the activated column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="activated", nullable=false, length=1)
    protected char getActivated_JpaWorkaround() {
        return _activated;
    }
    protected char setActivated_JpaWorkaround(char activated) {
        return _activated = activated;
    }
    @Transient
    public boolean isActivated() {
        return _activated == 'y';
    }
    public void setActivated(boolean activated) {
        this._activated = activated ? 'y' : 'n';
    }
}
