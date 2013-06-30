package org.fao.geonet.domain;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

/**
 * A Metadata category.  This is separate from any category listed in
 * the metadata.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Cacheable
public class Category {
    private int _id;
    private String _name;
    private Map<String, String> _labelTranslations = new HashMap<String, String>();
    
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
    
}
