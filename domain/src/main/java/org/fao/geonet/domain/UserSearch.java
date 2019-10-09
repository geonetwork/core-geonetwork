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

import org.fao.geonet.domain.converter.UserSearchFeaturedTypeConverter;
import org.fao.geonet.entitylistener.UserSearchEntityListenerManager;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * A user custom search.
 */
@Entity
@Table(name = "UserSearch")
@Access(AccessType.PROPERTY)
@EntityListeners(UserSearchEntityListenerManager.class)
@SequenceGenerator(name = UserSearch.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class UserSearch extends Localized implements Serializable {
    static final String ID_SEQ_NAME = "user_search_id_seq";

    public static final long serialVersionUID = 8189762204841260782L;

    private int id;
    private String url;
    private UserSearchFeaturedType featuredType;
    private Date creationDate;
    private User creator;
    private String logo;

    private Set<Group> groups = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }


    @Column
    public String getUrl() {
        return url;
    }

    @Column
    @Convert(converter = UserSearchFeaturedTypeConverter.class)
    public UserSearchFeaturedType getFeaturedType() {
        return featuredType;
    }

    @Transient
    public boolean isFeatured() {
        return (featuredType != null);
    }

    @Column
    public Date getCreationDate() {
        return creationDate;
    }


    @ManyToOne
    @Nullable
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    public User getCreator() {
        return creator;
    }

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "usersearch_group",
        joinColumns = @JoinColumn(name = "usersearch_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id"))
    public Set<Group> getGroups() {
        return groups;
    }

    @Column
    @Nullable
    public String getLogo() {
        return logo;
    }


    public UserSearch setId(int id) {
        this.id = id;
        return this;
    }


    public UserSearch setUrl(String url) {
        this.url = url;
        return this;
    }


    public UserSearch setFeaturedType(UserSearchFeaturedType featuredType) {
        this.featuredType = featuredType;
        return this;
    }


    public UserSearch setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }


    public UserSearch setCreator(User creator) {
        this.creator = creator;
        return this;
    }


    public UserSearch setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public UserSearch setGroups(Set<Group> groups) {
        this.groups = groups;
        return this;
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "UserSearchDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSearch that = (UserSearch) o;
        return id == that.id &&
            url.equals(that.url) &&
            featuredType == that.featuredType &&
            creationDate.equals(that.creationDate) &&
            creator.equals(that.creator) &&
            Objects.equals(logo, that.logo) &&
            Objects.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, featuredType, creationDate, creator, logo, groups);
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %d", this.getClass().getName(), getId());
    }

}
