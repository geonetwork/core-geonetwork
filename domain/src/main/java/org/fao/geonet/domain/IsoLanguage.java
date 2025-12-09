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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.fao.geonet.entitylistener.IsoLanguageEntityListenerManager;

import java.util.Map;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * An entity representing the bi-directional mapping between the different iso language codes (de ->
 * ger) and translations of the languages. (German, Deutsch, etc...)
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "IsoLanguages")
@EntityListeners(IsoLanguageEntityListenerManager.class)
@Cacheable
@SequenceGenerator(name = IsoLanguage.ID_SEQ_NAME, initialValue = 10000, allocationSize = 1)
public class IsoLanguage extends Localized {
    static final String ID_SEQ_NAME = "iso_language_id_seq";
    private int id;
    private String code;
    private String shortCode;

    /**
     * Get the id for the lang code mapping. This is a generated value and as such new instances
     * should not have this set as it will simply be ignored and could result in reduced
     * performance.
     *
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    /**
     * Set the id for the lang code mapping. This is a generated value and as such new instances
     * should not have this set as it will simply be ignored and could result in reduced
     * performance.
     *
     * @param id the id
     * @return this entity object
     */
    public IsoLanguage setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Get the 3 letter code of the mapping.
     *
     * @return the 3 letter code of the mapping.
     */
    @Column(length = 3, nullable = false)
    public String getCode() {
        return code;
    }

    /**
     * Set the 3 letter language code for this mapping
     *
     * @param code the 3 letter code.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Get the 2 letter language code for this mapping
     *
     * @return the 2 letter language code for this mapping
     */
    @Column(name = "shortcode", length = 2)
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Set the 2 letter language code for this mapping
     *
     * @param shortCode the 2 letter language code
     */
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /**
     * Get the map of langid -> label translations for groups
     */
    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "IsoLanguagesDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    @Override
    public String toString() {
        return "IsoLanguage{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", shortCode='" + shortCode + '\'' +
            '}';
    }
}
