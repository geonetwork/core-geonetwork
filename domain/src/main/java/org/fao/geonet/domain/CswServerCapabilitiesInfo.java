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
public class CswServerCapabilitiesInfo extends GeonetEntity {
    private int _id;
    private String _langId;
    private String _field;
    private String _label;

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
    public CswServerCapabilitiesInfo setId(int id) {
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
    public CswServerCapabilitiesInfo setLangId(String langid) {
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
    @Column(nullable = false, length = 32)
    public String getField() {
        return _field;
    }

    /**
     * Set the field name that this info item applies to. <br/>
     * This is a required property <br/>
     * This has a set maximum length that must be respected or a database error will be thrown on save. See {@link #getField()} annotation
     * for maximum length.
     * 
     * @param field the field name.
     * @return this object
     */
    public CswServerCapabilitiesInfo setField(String field) {
        this._field = field;
        return this;
    }

    /**
     * Get the translated label for this field.
     * 
     * @return translated label
     */
    @Lob
    public String getLabel() {
        return _label;
    }

    /**
     * Set the translated label for this field.
     * 
     * @param label the translated label.
     * @return this object
     */
    public CswServerCapabilitiesInfo setLabel(String label) {
        this._label = label;
        return this;
    }

}
