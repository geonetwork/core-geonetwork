/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
import org.fao.geonet.domain.converter.BooleanToYNConverter;
import org.fao.geonet.entitylistener.SourceEntityListenerManager;
import org.fao.geonet.repository.LanguageRepository;
import org.hibernate.annotations.Type;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a source catalogue.
 * <p>
 * A source is created for the default catalogue,
 * when a harvester is created,
 * when a MEF is imported flagging an external catalogue,
 * and when a subportal is created.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = Source.TABLE_NAME)
@EntityListeners(SourceEntityListenerManager.class)
public class Source extends Localized {
    public static final String TABLE_NAME = "Sources";
    public static final String ID_COLUMN_NAME = "uuid";
    public static final String CREATION_DATE_COLUMN_NAME = "creationDate";

    private String _uuid = UUID.randomUUID().toString();
    private String _name;
    private SourceType type = null;
    private String logo;
    private String filter;
    private String uiConfig;
    private String serviceRecord;
    private ISODate creationDate = new ISODate();
    private Integer groupOwner;
    private Boolean listableInHeaderSelector = true;

    private Boolean datahubEnabled = false;
    private String datahubConfiguration = ""; // will use the main conf if empty

    /**
     * Default constructor.  Required by framework.
     */
    public Source() {
    }

    /**
     * Convenience constructor for quickly making a Source object.
     *
     * @param uuid the uuid of the source (also the ID)
     * @param name the name.
     * @param type the type of source (harvester, subportal...).
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
    @Column(name = ID_COLUMN_NAME)
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
    @Column(name = "label", nullable = false, length = 255)
    @Nonnull
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Source source = (Source) o;
        return Objects.equals(_uuid, source._uuid) && Objects.equals(_name, source._name);
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
     *
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
     *
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
     *
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
     *
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
     * Only applies to subportal.
     *
     * @return
     */
    public Boolean getDatahubEnabled() {
        return datahubEnabled;
    }
    public Source setDatahubEnabled(Boolean datahubEnabled) {
        this.datahubEnabled = datahubEnabled;
        return this;
    }

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String getDatahubConfiguration() {
        return datahubConfiguration;
    }
    public Source setDatahubConfiguration(String datahubConfiguration) {
        this.datahubConfiguration = datahubConfiguration;
        return this;
    }


    /**
     * Get the date that the source was created.
     *
     * @return the creation date.
     */
    @AttributeOverride(
        name = "dateAndTimeUtc",
        column = @Column(
            name = CREATION_DATE_COLUMN_NAME,
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

    /**
     * Get the group id that this subportal is managed by.
     * When assigning a subportal to a group, user admin of that group
     * can manage it.
     *
     * @return the group that owns this source.
     */
    @Column(name = "groupOwner")
    public Integer getGroupOwner() {
        return groupOwner;
    }

    public Source setGroupOwner(Integer groupOwner) {
        this.groupOwner = groupOwner;
        return this;
    }


    @Column(name = "serviceRecord")
    public String getServiceRecord() {
        return serviceRecord;
    }

    public void setServiceRecord(String serviceRecord) {
        this.serviceRecord = serviceRecord;
    }

    @Column(name = "isListableInHeaderSelector", nullable = false, length = 1, columnDefinition="CHAR(1) DEFAULT 'y'")
    @Convert(converter = BooleanToYNConverter.class)
    public boolean isListableInHeaderSelector() {
        return this.listableInHeaderSelector;
    }

    public void setListableInHeaderSelector(Boolean listableInHeaderSelector) {
        this.listableInHeaderSelector = listableInHeaderSelector;
    }
}
