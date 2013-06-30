package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * The enumeration of all languages available in the system.
 * 
 * Note:  The difference between IsoLanguages and Languages seems to be
 * IsoLanguages lists all possible mappings between language codes and
 * Languages are the translations that can be used in the system.  For
 * example the languages the UI is translated into, the languages metadata
 * can be written in, the languages the groups, etc... should be translated
 * into.
 *
 * @author Jesse
 *
 */
@Entity
@Access(AccessType.PROPERTY)
public class Languages {
    String id;
    String name;
    boolean inspire;
    boolean _defaultLanguage;
    
    @Id
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isInspire() {
        return inspire;
    }
    public void setInspire(boolean inspire) {
        this.inspire = inspire;
    }
    @Column(name="default")
    public boolean isDefaultLanguage() {
        return _defaultLanguage;
    }
    public void setDefaultLanguage(boolean newDefault) {
        this._defaultLanguage = newDefault;
    }
    
    
}
