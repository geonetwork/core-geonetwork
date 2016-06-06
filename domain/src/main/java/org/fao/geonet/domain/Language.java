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

import org.fao.geonet.entitylistener.LanguageEntityListenerManager;

import javax.annotation.Nonnull;
import javax.persistence.*;

/**
 * The enumeration of all languages available in the system.
 * <p/>
 * Note: The difference between IsoLanguages and Languages seems to be IsoLanguages lists all
 * possible mappings between language codes and Languages are the translations that can be used in
 * the system. For example the languages the UI is translated into, the languages metadata can be
 * written in, the languages the groups, etc... should be translated into.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Languages")
@Cacheable
@EntityListeners(LanguageEntityListenerManager.class)
public class Language extends GeonetEntity {
    String _id;
    String _name;
    char _inspire = Constants.YN_FALSE;
    char _defaultLanguage = Constants.YN_FALSE;

    /**
     * Get the id of the language. This is a generated value and as such new instances should not
     * have this set as it will simply be ignored and could result in reduced performance.
     *
     * @return the id of the language
     */
    @Id
    @Column(length = 5)
    @Nonnull
    public String getId() {
        return _id;
    }

    /**
     * The id of the language. This is a generated value and as such new instances should not have
     * this set as it will simply be ignored and could result in reduced performance.
     *
     * @param id the id of the language
     */
    public void setId(@Nonnull final String id) {
        this._id = id;
    }

    /**
     * Get the name/descriptor of the language. This is not a translated value is primarily for the
     * admin user interface.
     *
     * @return the language name
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name/descriptor of the language. This is not a translated value is primarily for the
     * admin user interface.
     *
     * @param name This is not a translated value is primarily for the admin user interface.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * For backwards compatibility we need the isinspire column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @Column(name = "isInspire", length = 1)
    protected char getInspire_JPAWorkaround() {
        return _inspire;
    }

    /**
     * Setter for the value actual value that will be in the database. This should not be set by end
     * programmer.
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
        return Constants.toBoolean_fromYNChar(getInspire_JPAWorkaround());
    }

    /**
     * Set true if this is a language required byt the inspire standards.
     *
     * @param inspire true if required by inspire
     */
    public void setInspire(boolean inspire) {
        setInspire_JPAWorkaround(Constants.toYN_EnabledChar(inspire));
    }

    /**
     * For backwards compatibility we need the isdefault column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @Column(name = "isDefault", length = 1)
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
        return Constants.toBoolean_fromYNChar(getDefaultLanguage_JPAWorkaround());
    }

    /**
     * set true if this is the default language.
     *
     * @param newDefault true if this language is the new default.
     */
    public void setDefaultLanguage(boolean newDefault) {
        setDefaultLanguage_JPAWorkaround(Constants.toYN_EnabledChar(newDefault));
    }
}
