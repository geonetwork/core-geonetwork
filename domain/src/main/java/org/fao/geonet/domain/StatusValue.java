package org.fao.geonet.domain;

import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

/**
 * One of the enumerated status options that a metadata can be.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="statusvalue")
public class StatusValue {
    private int _id;
    private String _name;
    private boolean _reserved;
    private Map<String, String> _labelTranslations;

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
    public boolean isReserved() {
        return _reserved;
    }
    public void setReserved(boolean reserved) {
        this._reserved = reserved;
    }
    /**
     * Get the map of langid -> label translations for groups
     */
    @ElementCollection(fetch=FetchType.LAZY, targetClass=String.class)
    @CollectionTable(joinColumns=@JoinColumn(name="iddes"),name="statusvaluesdes")
    @MapKeyColumn(name="langid")
    public Map<String, String> getLabelTranslations() {
        return _labelTranslations;
    }
    /**
     * Set new translations this should only be used for initialization.  
     * to add and remove translations use "get" and modify map.
     *
     * @param localizedTranslations the translation map
     */
    protected void setLabelTranslations(Map<String, String> localizedTranslations) {
        this._labelTranslations = localizedTranslations;
    }
}
