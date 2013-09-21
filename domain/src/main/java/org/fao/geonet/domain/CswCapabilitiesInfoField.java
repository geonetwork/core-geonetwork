package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Variable substitutions and extra information that goes in a CSW capabilities document. <br/>
 * Typically each entity represents the translated information for a single language of a single field. This is essentially a map where the
 * key is the language+field and the value is the translated label.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "cswservercapabilitiesinfo")
public class CswCapabilitiesInfoField extends GeonetEntity {
    private int _id = -1;
    private String _langId;
    private String _fieldName;
    private String _value;

    /**
     * The id of the entity. This is a generated value and not controlled by the developer.
     */
    @Id
    @GeneratedValue
    @Column(name = "idfield", length = 10)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the entity. This is typically set by the JPA entity manager and should only be set by the developer when they want to
     * merge new data with an existing entity or want to perform query by example. But even then it is not generally recommended.
     * 
     * @param id the id.
     * @return this object
     */
    public CswCapabilitiesInfoField setId(int id) {
        this._id = id;
        return this;
    }

    /**
     * Return the iso 3 letter language code representing the language that these data elements are in.
     * 
     * @return the iso 3 letter language code (eng, fre, ger, etc..)
     */
    @Column(name = "langid", nullable = false, length = 5)
    public String getLangId() {
        return _langId;
    }

    /**
     * Set the iso 3 letter language code representing the language that these data elements are in.
     * <p>
     * This is a required property.
     * </p>
     * 
     * @param langid the iso 3 language code (eng, fre, ger)
     * @return this object
     */
    public CswCapabilitiesInfoField setLangId(String langid) {
        this._langId = langid;
        return this;
    }

    /**
     * Get the field name that this info item applies to. <br/>
     * This is a required property <br/>
     * This has a set maximum length that must be respected or a database error will be thrown on save
     * 
     * @return the field name.
     */
    @Column(name="field", nullable = false, length = 32)
    public String getFieldName() {
        return _fieldName;
    }

    /**
     * Set the field name that this info item applies to. <br/>
     * This is a required property <br/>
     * This has a set maximum length that must be respected or a database error will be thrown on save. See {@link #getFieldName()} annotation
     * for maximum length.
     * 
     * @param fieldName the field name.
     * @return this object
     */
    public CswCapabilitiesInfoField setFieldName(String fieldName) {
        this._fieldName = fieldName;
        return this;
    }

    /**
     * Get the translated label for this field.
     * 
     * @return translated label
     */
    @Lob
    @Column(name = "label")
    public String getValue() {
        return _value;
    }

    /**
     * Set the translated label for this field.
     * 
     * @param newValue the translated label.
     * @return this object
     */
    public CswCapabilitiesInfoField setValue(String newValue) {
        this._value = newValue;
        return this;
    }

}
