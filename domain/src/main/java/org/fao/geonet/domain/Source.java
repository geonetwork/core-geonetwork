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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.entitylistener.SourceEntityListenerManager;
import org.fao.geonet.repository.LanguageRepository;

import java.util.Map;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

/**
 * Entity representing a source catalogue.
 *
 * A source is created for the default catalogue,
 * when a harvester is created,
 * when a MEF is imported flagging an external catalogue,
 * and when a subportal is created.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Sources")
@EntityListeners(SourceEntityListenerManager.class)
public class Source extends Localized {
    private String _uuid = UUID.randomUUID().toString();
    private String _name;
    private SourceType type = null;
    private String logo;
    private String filter;
    private String uiConfig;
    private ISODate creationDate = new ISODate();

    /**
     * Default constructor.  Required by framework.
     */
    public Source() {
    }

    /**
     * Convenience constructor for quickly making a Source object.
     *  @param uuid  the uuid of the source (also the ID)
     * @param name  the name
     * @param type
     */
    public Source(String uuid, String name, Map<String, String> translations, SourceType type) {
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
        this.type = type;
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

        if (_name != null ? !_name.equals(source._name) : source._name != null) return false;
        if (_uuid != null ? !_uuid.equals(source._uuid) : source._uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _uuid != null ? _uuid.hashCode() : 0;
        result = 31 * result + (_name != null ? _name.hashCode() : 0);
        return result;
    }

    /**
     * Property indicating if the source is the local catalogue,
     * an external one, a harvester source or a sub portal.
     * @return
     */
    @Column(nullable = true, name = "type")
    @Enumerated(EnumType.STRING)
    public SourceType getType() {
        return type;
    }

    public Source setType(SourceType type) {
        this.type = type;
        return this;
    }

    /**
     * Only applies to subportal.
     * @return
     */
    public String getLogo() {
        return logo;
    }

    public Source setLogo(String logo) {
        this.logo = logo;
        return this;
    }


    /**
     * Only applies to subportal.
     * @return
     */
    public String getFilter() {
        return filter;
    }

    public Source setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Only applies to subportal.
     * @return
     */
    public String getUiConfig() {
        return uiConfig;
    }

    public Source setUiConfig(String uiConfig) {
        this.uiConfig = uiConfig;
        return this;
    }


    /**
     * Get the date that the source was created.
     *
     * @return the creation date.
     */
    @AttributeOverride(
        name = "dateAndTime",
        column = @Column(
            name = "creationDate",
            nullable = true,
            length = 30))
    public ISODate getCreationDate() {
        return creationDate;
    }

    /**
     * Set the date that the source was created.
     *
     * @param creationDate the creation date.
     * @return this data info object
     */
    public Source setCreationDate(ISODate creationDate) {
        this.creationDate = creationDate;
        return this;
    }

}
