package org.fao.geonet.domain;

import java.io.Serializable;

import org.fao.geonet.entitylistener.CswCapabilitiesInfoFieldEntityListenerManager;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * Variable substitutions and extra information that goes in a CSW capabilities document. <br/>
 * Typically each entity represents the translated information for a single language of a single field. This is essentially a map where
 * the
 * key is the language+field and the value is the translated label.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "CswServerCapabilitiesInfo")
@EntityListeners(CswCapabilitiesInfoFieldEntityListenerManager.class)
@SequenceGenerator(name=CswCapabilitiesInfoField.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class CswCapabilitiesInfoField extends GeonetEntity implements Serializable {

	private static final long serialVersionUID = -2893765878557173596L;

	static final String ID_SEQ_NAME = "csw_server_capabilities_info_id_seq";

    private static final int ID_COLUMN_LENGTH = 10;
    private static final int LANG_ID_COLUMN_LENGTH = 5;
    private static final int FIELD_NAME_COLUMN_LENGTH = 32;
    private int _id = 0;
    private String _langId;
    private String _fieldName;
    private String _value;

    /**
     * The id of the entity. This is a generated value and not controlled by the developer.
     */
    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = "idfield", length = ID_COLUMN_LENGTH)
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
    public CswCapabilitiesInfoField setId(final int id) {
        this._id = id;
        return this;
    }

    /**
     * Return the iso 3 letter language code representing the language that these data elements are in.
     *
     * @return the iso 3 letter language code (eng, fre, ger, etc..)
     */
    @Column(name = "langid", nullable = false, length = LANG_ID_COLUMN_LENGTH)
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
    public CswCapabilitiesInfoField setLangId(final String langid) {
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
    @Column(name = "field", nullable = false, length = FIELD_NAME_COLUMN_LENGTH)
    public String getFieldName() {
        return _fieldName;
    }

    /**
     * Set the field name that this info item applies to. <br/>
     * This is a required property <br/>
     * This has a set maximum length that must be respected or a database error will be thrown on save. See {@link #getFieldName()}
     * annotation
     * for maximum length.
     *
     * @param fieldName the field name.
     * @return this object
     */
    public CswCapabilitiesInfoField setFieldName(final String fieldName) {
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
    @Type(type="org.hibernate.type.StringClobType") // this is a work around for postgres so postgres can correctly load clobs
    public String getValue() {
        return _value;
    }

    /**
     * Set the translated label for this field.
     *
     * @param newValue the translated label.
     * @return this object
     */
    public CswCapabilitiesInfoField setValue(final String newValue) {
        this._value = newValue;
        return this;
    }

}
