package org.fao.geonet.domain;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

/**
 * @author Jesse
 *
 */
@Entity
@Table(name = "groups")
@Cacheable
@Access(AccessType.PROPERTY)
public class Group {

    private int _id;
    private String _name;
    private String _description;
    private String _email;
    private int _referrer;
    private Map<String, String> _labelTranslations = new HashMap<String, String>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * When creating a new Group DO NOT!!! Set this spring data will do this for you.
     * <p>
     * If you want to update an existing Group then you should set this id to the group you want to update and set the other values to the
     * desired values
     * </p>
     * 
     * @param id
     * @return
     */
    public Group setId(int id) {
        this._id = id;
        return this;
    }

    @Column(nullable = false, length = 32)
    public String getName() {
        return _name;
    }

    public Group setName(String name) {
        this._name = name;
        return this;
    }

    @Column(length = 255)
    public String getDescription() {
        return _description;
    }

    public Group setDescription(String description) {
        this._description = description;
        return this;
    }

    @Column(length = 32)
    public String getEmail() {
        return _email;
    }

    public Group setEmail(String email) {
        this._email = email;
        return this;
    }

    public int getReferrer() {
        return _referrer;
    }
    
    public void setReferrer(int referrer) {
        this._referrer = referrer;
    }

    /**
     * Get the map of langid -> label translations for groups
     */
    @ElementCollection(fetch=FetchType.LAZY, targetClass=String.class)
    @CollectionTable(joinColumns=@JoinColumn(name="iddes"),name="groupsdes")
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

    @Override
    public String toString() {
        return "Group [_id=" + _id + ", _name=" + _name + ", _email=" + _email + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Group other = (Group) obj;
        if (_id != other._id)
            return false;
        return true;
    }
}
