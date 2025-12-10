/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.domain;

import java.io.Serializable;

import org.fao.geonet.entitylistener.TranslationsEntityListenerManager;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
import jakarta.persistence.*;

/**
 * Variable substitutions and extra information that goes in a CSW capabilities document. <br/>
 * Typically each entity represents the translated information for a single language of a single
 * field. This is essentially a map where the key is the language+field and the value is the
 * translated label.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(
    name = "Translations",
    indexes = {
        @Index(name = "idx_translations_lang", columnList = "langid")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames={"field", "langid"})
    })
@EntityListeners(TranslationsEntityListenerManager.class)
@SequenceGenerator(name = Translations.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class Translations extends GeonetEntity implements Serializable {

    static final String ID_SEQ_NAME = "csw_server_capabilities_info_id_seq";
    private static final long serialVersionUID = -2893765878557173596L;
    private static final int ID_COLUMN_LENGTH = 10;
    private static final int LANG_ID_COLUMN_LENGTH = 5;
    private static final int FIELD_NAME_COLUMN_LENGTH = 255;
    private int _id = 0;
    private String _langId;
    private String _fieldName;
    private String _value;

    /**
     * The id of the entity. This is a generated value and not controlled by the developer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = "idfield", length = ID_COLUMN_LENGTH)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the entity. This is typically set by the JPA entity manager and should only be
     * set by the developer when they want to merge new data with an existing entity or want to
     * perform query by example. But even then it is not generally recommended.
     *
     * @param id the id.
     * @return this object
     */
    public Translations setId(final int id) {
        this._id = id;
        return this;
    }

    /**
     * Return the iso 3 letter language code representing the language that these data elements are
     * in.
     *
     * @return the iso 3 letter language code (eng, fre, ger, etc..)
     */
    @Column(name = "langid", nullable = false, length = LANG_ID_COLUMN_LENGTH)
    public String getLangId() {
        return _langId;
    }

    /**
     * Set the iso 3 letter language code representing the language that these data elements are in.
     * <p> This is a required property. </p>
     *
     * @param langid the iso 3 language code (eng, fre, ger)
     * @return this object
     */
    public Translations setLangId(final String langid) {
        this._langId = langid;
        return this;
    }

    /**
     * Get the field name that this info item applies to. <br/> This is a required property <br/>
     * This has a set maximum length that must be respected or a database error will be thrown on
     * save
     *
     * @return the field name.
     */
    @Column(name = "field", nullable = false, length = FIELD_NAME_COLUMN_LENGTH)
    public String getFieldName() {
        return _fieldName;
    }

    /**
     * Set the field name that this info item applies to. <br/> This is a required property <br/>
     * This has a set maximum length that must be respected or a database error will be thrown on
     * save. See {@link #getFieldName()} annotation for maximum length.
     *
     * @param fieldName the field name.
     * @return this object
     */
    public Translations setFieldName(final String fieldName) {
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
    @JdbcTypeCode(Types.LONGVARCHAR)
    // this is a work around for postgres so postgres can correctly load clobs
    public String getValue() {
        return _value;
    }

    /**
     * Set the translated label for this field.
     *
     * @param newValue the translated label.
     * @return this object
     */
    public Translations setValue(final String newValue) {
        this._value = newValue;
        return this;
    }

}
