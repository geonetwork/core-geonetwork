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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

/**
 * A Metadata category. This is separate from any category listed in the metadata xml itself and is geonetwork specific.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Cacheable
@Table(name = "categories")
public class MetadataCategory {
    private int _id;
    private String _name;
    private Map<String, String> _labelTranslations = new HashMap<String, String>();

    /**
     * The id of the category. This is a generated value and not controlled by the developer.
     * 
     * @return the id
     */
    @Id
    @GeneratedValue
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the category. This is typically set by the JPA entity manager and should only be set by the developer when they want to
     * merge new data with an existing entity or want to perform query by example. But even then it is not generally recommended.
     * 
     * @param id the id.
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * The name of the category. This is a required property.
     * 
     * @return the name.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the category. This is a required non-null property.
     * 
     * @param name the new name.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Get the map of langid -> label translations for metadata categories. langid is an iso 3 character code for the language. For example:
     * eng, ger, fra, etc...
     * 
     * @return the map of langid -> label
     */
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "iddes"), name = "categoriesdes")
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _id;
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
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
        MetadataCategory other = (MetadataCategory) obj;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        return true;
    }

}
