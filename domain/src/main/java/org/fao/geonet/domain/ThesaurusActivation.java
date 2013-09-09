package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
@Table(name = "thesaurus")
public class ThesaurusActivation {
    private String _id;
    private char _activated = Constants.YN_TRUE;

    /**
     * Get the id of the ThesaurusActivation.   This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     * 
     * @return the id of the ThesaurusActivation
     */
    @Id
    @GeneratedValue
    public String getId() {
        return _id;
    }

    /**
     * Set the id of the ThesaurusActivation.   This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     * 
     * @param id the id of the ThesaurusActivation
     */
    public void setId(String id) {
        this._id = id;
    }

    /**
     * For backwards compatibility we need the activated column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "activated", nullable = false, length = 1)
    protected char getActivated_JpaWorkaround() {
        return _activated;
    }

    /**
     * Set the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED for false.
     * 
     * @param activated the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED for false.
     * @return
     */
    protected void setActivated_JpaWorkaround(char activated) {
        _activated = activated;
    }

    /**
     * Return true if the thesaurus is active.
     * 
     * @return true if the thesaurus is active.
     */
    @Transient
    public boolean isActivated() {
        return Constants.toBoolean_fromYNChar(getActivated_JpaWorkaround());
    }

    /**
     * Set true if the thesaurus is active.
     * @param activated true if the thesaurus is active.
     */
    public void setActivated(boolean activated) {
        setActivated_JpaWorkaround(Constants.toYN_EnabledChar(activated));
    }
}
