package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

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
    String _id;
    String _name;
    char _inspire = 'n';
    char _defaultLanguage = 'n';
    
    @Id
    @Column(length=5)
    public String getId() {
        return _id;
    }
    public void setId(String id) {
        this._id = id;
    }
    @Column(nullable=false)
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        this._name = name;
    }
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="isinspire", length=1)
    protected char getInspire_JPAWorkaround() {
        return _inspire;
    }
    protected void setInspire_JPAWorkaround(char isinspire) {
        _inspire = isinspire;
    }
    @Transient
    public boolean isInspire() {
        return _inspire == 'y';
    }
    public void setInspire(boolean inspire) {
        this._inspire = inspire ? 'y' : 'n';
    }
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="isdefault", length=1)
    protected char getDefaultLanguage_JPAWorkaround() {
        return _defaultLanguage;
    }
    protected void setDefaultLanguage_JPAWorkaround(char isdefault) {
        _defaultLanguage = isdefault;
    }
    @Transient
    public boolean isDefaultLanguage() {
        return _defaultLanguage == 'y';
    }
    public void setDefaultLanguage(boolean newDefault) {
        this._defaultLanguage = newDefault ? 'y' : 'n';
    }
    
    
}
