package org.fao.geonet.domain;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Access(AccessType.PROPERTY)
@Table(name="isolanguages")
public class IsoLanguages {
    private int id;
    private String code;
    private String shortCode;
    private Map<String, String> _labelTranslations = new HashMap<String, String>();
    
    @Id
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    @Column(name="shortcode")
    public String getShortCode() {
        return shortCode;
    }
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /**
     * Get the map of langid -> label translations for groups
     */
    @ElementCollection(fetch=FetchType.EAGER, targetClass=String.class)
    @CollectionTable(joinColumns=@JoinColumn(name="iddes"),name="isolanguagesdes")
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
