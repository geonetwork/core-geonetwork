package org.fao.geonet.domain;

import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * One of the enumerated status options that a metadata can be.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "statusvalues")
public class StatusValue {
    private int _id;
    private String _name;
    private char _reserved = 'n';
    private Map<String, String> _labelTranslations;

    /**
     * Get the id of the StatusValue object. This is a generated value and as such new instances should not have this set as it will simply
     * be ignored and could result in reduced performance.
     * 
     * @return the id of the StatusValue object
     */
    @Id
    @GeneratedValue
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the StatusValue object. This is a generated value and as such new instances should not have this set as it will simply
     * be ignored and could result in reduced performance.
     * 
     * @param id the id of the StatusValue object
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Get the name of the StatusValue object. This is a required property.
     * 
     * @return the name of the StatusValue object.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the StatusValue object. This is a required property.
     * 
     * @param name the name of the StatusValue object.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * For backwards compatibility we need the reserved column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "reserved", nullable = false, length = 1)
    protected char getReserved_JpaWorkaround() {
        return _reserved;
    }

    /**
     * Set the column value.
     * @param reserved 'y' for true or 'n' for false.
     * @return
     */
    protected char setReserved_JpaWorkaround(char reserved) {
        return _reserved = reserved;
    }

    /**
     * Return true if this is a reserved StatusValue.
     *
     * @return true if this is a reserved StatusValue.
     */
    @Transient
    public boolean isReserved() {
        return _reserved == 'y';
    }

    /**
     * Set true if this is a reserved StatusValue.
     * @param reserved true if this is a reserved StatusValue.
     */
    public void setReserved(boolean reserved) {
        this._reserved = reserved ? 'y' : 'n';
    }

    /**
     * Get the map of langid -> label translations for groups
     */
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "iddes"), name = "statusvaluesdes")
    @MapKeyColumn(name = "langid", length = 5)
    @Column(name = "label", nullable = false)
    public Map<String, String> getLabelTranslations() {
        return _labelTranslations;
    }

    /**
     * Set new translations this should only be used for initialization. to add and remove translations use "get" and modify map.
     * 
     * @param localizedTranslations the translation map
     */
    protected void setLabelTranslations(Map<String, String> localizedTranslations) {
        this._labelTranslations = localizedTranslations;
    }
}
