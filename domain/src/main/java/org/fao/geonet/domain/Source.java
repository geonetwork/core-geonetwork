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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.entitylistener.SourceEntityListenerManager;
import org.fao.geonet.repository.LanguageRepository;

import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity representing a metadata source.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Sources")
@EntityListeners(SourceEntityListenerManager.class)
public class Source extends Localized {
    private String _uuid;
    private String _name;
    private char _local = Constants.YN_TRUE;

    /**
     * Default constructor.  Required by framework.
     */
    public Source() {
    }

    /**
     * Convenience constructor for quickly making a Source object.
     *
     * @param uuid  the uuid of the source (also the ID)
     * @param name  the name
     * @param local if the source is the local system
     */
    public Source(String uuid, String name, Map<String, String> translations, boolean local) {
        this._uuid = uuid;
        setName(name);
        if (translations != null && translations.size() != 0) {
            setLabelTranslations(translations);
        } else {
            LanguageRepository langRepository =
                ApplicationContextHolder.get().getBean(LanguageRepository.class);
            java.util.List<Language> allLanguages = langRepository.findAll();
            Map<String, String> labelTranslations = getLabelTranslations();
            for (Language l : allLanguages) {
                String label = labelTranslations.get(l.getId());
                if (label == null) {
                    getLabelTranslations().put(l.getId(), this.getName());
                }
            }
        }
        this._local = Constants.toYN_EnabledChar(local);
    }

    /**
     * Get the uuid of the source.
     *
     * @return the uuid of the source.
     */
    @Id
    public String getUuid() {
        return _uuid;
    }

    /**
     * Set the uuid of the source.
     *
     * @param uuid the uuid of the source.
     * @return this entity
     */
    public Source setUuid(String uuid) {
        this._uuid = uuid;
        return this;
    }

    /**
     * Get the name of the source.
     *
     * @return the name of the source.
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the source.
     *
     * @param name the name of the source.
     * @return this entity
     */
    public Source setName(String name) {
        this._name = name;
        return this;
    }

    /**
     * For backwards compatibility we need the islocal column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @Column(name = "isLocal", nullable = false, length = 1)
    @JsonIgnore
    protected char getIsLocal_JpaWorkaround() {
        return _local;
    }

    /**
     * Set the column values.
     *
     * @param local Constants.YN_ENABLED or Constants.YN_DISABLED
     */
    protected void setIsLocal_JpaWorkaround(char local) {
        _local = local;
    }

    /**
     * Return true is the source refers to the local geonetwork.
     *
     * @return true is the source refers to the local geonetwork.
     */
    @Transient
    @JsonIgnore
    public boolean isLocal() {
        return Constants.toBoolean_fromYNChar(getIsLocal_JpaWorkaround());
    }

    /**
     * Set true is the source refers to the local geonetwork.
     *
     * @param local true is the source refers to the local geonetwork.
     * @return this entity
     */
    public Source setLocal(boolean local) {
        setIsLocal_JpaWorkaround(Constants.toYN_EnabledChar(local));
        return this;
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "SourcesDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false, length = 96)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        if (_local != source._local) return false;
        if (_name != null ? !_name.equals(source._name) : source._name != null) return false;
        if (_uuid != null ? !_uuid.equals(source._uuid) : source._uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _uuid != null ? _uuid.hashCode() : 0;
        result = 31 * result + (_name != null ? _name.hashCode() : 0);
        result = 31 * result + (int) _local;
        return result;
    }
}
