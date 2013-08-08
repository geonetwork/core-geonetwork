package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * The enumeration of all languages available in the system.
 * 
 * Note: The difference between IsoLanguages and Languages seems to be IsoLanguages lists all possible mappings between language codes and
 * Languages are the translations that can be used in the system. For example the languages the UI is translated into, the languages
 * metadata can be written in, the languages the groups, etc... should be translated into.
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

    /**
     * Get the id of the language. This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     * 
     * @return the id of the language
     */
    @Id
    @GeneratedValue
    @Column(length = 5)
    public String getId() {
        return _id;
    }

    /**
     * The id of the language. This is a generated value and as such new instances should not have this set as it will simply be ignored and
     * could result in reduced performance.
     * 
     * @param id the id of the language
     */
    public void setId(String id) {
        this._id = id;
    }

    /**
     * Get the name/descriptor of the language. This is not a translated value is primarily for the admin user interface.
     * 
     * @return the language name
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name/descriptor of the language. This is not a translated value is primarily for the admin user interface.
     * 
     * @param name This is not a translated value is primarily for the admin user interface.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * For backwards compatibility we need the isinspire column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "isinspire", length = 1)
    protected char getInspire_JPAWorkaround() {
        return _inspire;
    }

    /**
     * Setter for the value actual value that will be in the database. This should not be set by end programmer.
     * 
     * @param isinspire y or n
     */
    protected void setInspire_JPAWorkaround(char isinspire) {
        _inspire = isinspire;
    }

    /**
     * Return true if this is a language required byt the inspire standards.
     * 
     * @return return true if required by inspire.
     */
    @Transient
    public boolean isInspire() {
        return _inspire == 'y';
    }

    /**
     * Set true if this is a language required byt the inspire standards.
     * 
     * @param inspire true if required by inspire
     */
    public void setInspire(boolean inspire) {
        this._inspire = inspire ? 'y' : 'n';
    }

    /**
     * For backwards compatibility we need the isdefault column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "isdefault", length = 1)
    protected char getDefaultLanguage_JPAWorkaround() {
        return _defaultLanguage;
    }

    protected void setDefaultLanguage_JPAWorkaround(char isdefault) {
        _defaultLanguage = isdefault;
    }

    /**
     * Get whether or not this language is the default system language.
     * 
     * @return true is default language.
     */
    @Transient
    public boolean isDefaultLanguage() {
        return _defaultLanguage == 'y';
    }

    /**
     * set true if this is the default language.
     * 
     * @param newDefault true if this language is the new default.
     */
    public void setDefaultLanguage(boolean newDefault) {
        this._defaultLanguage = newDefault ? 'y' : 'n';
    }
}
