/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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


import org.fao.geonet.entitylistener.FavouriteMetadataListListenerManager;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Access(AccessType.PROPERTY)
@Table(
    name = "FavouriteMetadataList"
)
@SequenceGenerator(name = FavouriteMetadataList.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
@EntityListeners(FavouriteMetadataListListenerManager.class)
public class FavouriteMetadataList extends GeonetEntity implements Serializable {
    public static final String CHANGE_DATE_COLUMN_NAME = "changedate";
    public static final String CREATE_DATE_COLUMN_NAME = "createdate";

    static final String ID_SEQ_NAME = "fav_md_list_id_seq";

    /**
     * id - managed by hibernate/JPA
     */
    private int id;
    /**
     * name of the list (what the user entered)
     */
    private String name;
    /**
     * If the list was created when the user is logged in, then this will be the user who created it.
     * NOTE: if this is set, sessionID is null (and visa-versa)
     */
    private User user;
    /**
     * If the list was created when the user is NOT logged in, then this will be the value of the cookie used.
     * NOTE: if this is set, user is null (and visa-versa)
     */
    private String sessionId;
    /**
     * type of the list
     */
    private ListType listType;
    /**
     * if its public, then other people can see (but not edit it).
     */
    private boolean isPublic;
    /**
     * when the list was created.
     */
    private ISODate createDate;
    /**
     * when the list was last updated.
     */
    private ISODate changeDate;
    /**
     *  list of metadata UUIDs.  Should be unique.
     */
    private List<FavouriteMetadataListItem> selections;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    public FavouriteMetadataList setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Enumerated(EnumType.STRING)
    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = CREATE_DATE_COLUMN_NAME, nullable = false, length = 30))
    public ISODate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(ISODate createDate) {
        this.createDate = createDate;
    }

    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = CHANGE_DATE_COLUMN_NAME, nullable = false, length = 30))
    public ISODate getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(ISODate changeDate) {
        this.changeDate = changeDate;
    }

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(
        name = "selection_id",
        nullable = false)
    public List<FavouriteMetadataListItem> getSelections() {
        return selections;
    }

    public void setSelections(List<FavouriteMetadataListItem> selections) {
        this.selections = selections;
    }

    public enum ListType {PreferredList, WatchList}
}
